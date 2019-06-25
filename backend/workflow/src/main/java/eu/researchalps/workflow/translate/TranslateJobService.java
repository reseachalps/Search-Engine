package eu.researchalps.workflow.translate;

import com.datapublica.companies.model.error.ErrorMessage;
import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.ErrorHandler;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import eu.researchalps.api.TranslateCallbackApi;
import eu.researchalps.config.HostConfiguration;
import eu.researchalps.crawl.CrawlStoreService;
import eu.researchalps.crawl.CrawlText;
import eu.researchalps.db.model.Translation;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.repository.WebsiteRepository;
import eu.researchalps.etranslation.AskTranslation;
import eu.researchalps.etranslation.Translate;
import eu.researchalps.etranslation.WSEndpointHandlerService;
import eu.researchalps.db.repository.TranslationRepository;
import eu.researchalps.util.RepositoryLock;
import eu.researchalps.workflow.website.WebsiteAnalysisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static eu.researchalps.db.repository.TranslationRepository.ALL_LANGS;

@Component
public class TranslateJobService extends QueueComponent implements QueueListener<TranslateJobService.TranslationRequest> {
    private static final Logger log = LoggerFactory.getLogger(TranslateJobService.class);

    public static final MessageQueue<TranslationRequest> QUEUE = MessageQueue.get("TRANSLATE", TranslationRequest.class);

    private static final String clientApplicationName = "INEA_ResearchAlps_20170807";
    private static final String username = "research-alps-user";

    private static final XmlMapper objectMapper = new XmlMapper();
    private static final int MAX_RUNNING_JOBS = 4;
    private static final int MAX_REQUEST_DURATION_IN_MINUTES = 30;

    static {
        objectMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        objectMapper.registerModule(new JaxbAnnotationModule());
    }

    private AskTranslation translationService = new WSEndpointHandlerService().getAskTranslationPort();


    protected final Map<Long, RequestEntry> runningJobs = new HashMap<>();

    @Autowired
    private HostConfiguration hostConfiguration;

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private CrawlStoreService crawlStoreService;

    private RepositoryLock<Website, String, WebsiteRepository> websiteRepository;

    @Autowired
    private WebsiteAnalysisService websiteAnalysisService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    public void setWebsiteRepository(WebsiteRepository repository) {
        this.websiteRepository = RepositoryLock.get(repository);
    }

    @PostConstruct
    private void init() {

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());
        executor.scheduleAtFixedRate(this::cleanExpiredRequests, 0, 1, TimeUnit.MINUTES);
    }

    public Long translate(String lang, Map<String, String> batch, String websiteId) {
        final Translate.Arg0 arg = new Translate.Arg0();
        final Translate.Arg0.CallerInformation caller = new Translate.Arg0.CallerInformation();
        caller.setApplication(clientApplicationName);
        caller.setUsername(username);
        arg.setCallerInformation(caller);
        arg.setSourceLanguage(lang.toUpperCase());
        final Translate.Arg0.TargetLanguages languages = new Translate.Arg0.TargetLanguages();
        for (String targetLang : ALL_LANGS) {
            if (!lang.equals(targetLang)) {
                languages.getTargetLanguage().add(targetLang.toUpperCase());
            }
        }
        arg.setTargetLanguages(languages);
        arg.setExternalReference(websiteId);

        final String host = hostConfiguration.getBaseURL() + "/api" + TranslateCallbackApi.API_PREFIX;
        arg.setErrorCallback(host + TranslateCallbackApi.ERROR_ROUTE);
        arg.setRequesterCallback(host + TranslateCallbackApi.SUCCESS_ROUTE);
        final Translate.Arg0.Destinations destinations = new Translate.Arg0.Destinations();
        arg.setDestinations(destinations);
        destinations.getHttpDestination().add(host + TranslateCallbackApi.RECEIVE_ROUTE);
        final Translate.Arg0.DocumentToTranslateBase64 doc = new Translate.Arg0.DocumentToTranslateBase64();
        arg.setDocumentToTranslateBase64(doc);
        try {
            final Root content = new Root(
                    batch.entrySet().stream()
                            .map(it -> new TextContent(it.getKey(), it.getValue()))
                            .collect(Collectors.toList())
            );
            doc.setContent(objectMapper.writeValueAsBytes(content));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        doc.setFormat("xml");
        log.info("Submitting translation job for website "+websiteId+" in lang "+lang+" callback is "+host);
        final Long jobId;
        synchronized (runningJobs) {
            try {
                while (runningJobs.size() >= MAX_RUNNING_JOBS) {
                    log.info("Waiting for jobs {} to complete", runningJobs);
                    runningJobs.wait();
                }
                jobId = translationService.translate(arg);
                if (jobId < 0) {
                    throw new RuntimeException("Couldnt submit translation job: got code "+jobId);
                }
                log.info("Job request is {}", jobId);
                runningJobs.put(jobId, new RequestEntry(websiteId, Instant.now()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return jobId;
    }

    @Override
    public void receive(TranslationRequest message) {
        final List<CrawlText> texts = crawlStoreService.getCrawlTexts(message.websiteId);
        Map<String, Map<String, String>> toTranslate = new HashMap<>();
        Set<String> titles = new HashSet<>();
        Set<String> pages = new HashSet<>();

        for (CrawlText text : texts) {
            final String lang = text.getLang().toLowerCase();
            if (!ALL_LANGS.contains(lang)) {
                // skip unknown langs
                continue;
            }
            final Map<String, String> batch = toTranslate.computeIfAbsent(lang, it -> new HashMap<>());
            addTranslationEntry(lang, titles, batch, text.getTitle());
            addTranslationEntry(lang, pages, batch, text.getContent());
        }

        websiteRepository.update(message.websiteId, tx -> {
            final Website website = tx.getNotNull();
            website.setTitles(titles);
            website.setPages(pages);
            tx.saveDeferred(website);
        });

        for (Map.Entry<String, Map<String, String>> entry : toTranslate.entrySet()) {
            translate(entry.getKey(), entry.getValue(), message.websiteId);
        }
    }

    private void addTranslationEntry(String lang, Set<String> allIds, Map<String, String> batch, String str) {
        final String strId = DigestUtils.md5Hex(str);
        if (allIds.contains(strId)) {
            // useless we already know this one
            return;
        }
        allIds.add(strId);
        synchronized (this) {
            Translation translation = translationRepository.findOne(strId);
            if (translation == null) {
                translation = new Translation();
                translation.setId(strId);
            } else {
                if (translation.getTranslations().size() == ALL_LANGS.size()) {
                    return;
                }
            }
            batch.put(strId, str);
            // compression?
            translation.getTranslations().put(lang, str);
            translationRepository.save(translation);
        }
    }

    public void callback(String targetLanguage, String externalId, byte[] content, long requestId) {
        handleRequest(requestId);
        targetLanguage = targetLanguage.toLowerCase();
        final Root root;
        try {
            root = objectMapper.readValue(content, Root.class);
        } catch (IOException e) {
            log.warn("Couldnt read response from the translation job", e);
            return;
        }
        log.info("Got a response for website {} it contains {} entries translated to {}", externalId, root.text.size(), targetLanguage);
        for (TextContent text : root.text) {
            synchronized (this) {
                translationRepository.updateEntry(text.id, targetLanguage, text.content);
            }
        }

        final Website website = websiteRepository.read(externalId);
        if (!translationRepository.areTranslationsComplete(website.getTitles())) {
            return;
        }
        if (!translationRepository.areTranslationsComplete(website.getPages())) {
            return;
        }
        // all translations are complete
        log.info("All translations are ready for website {} go to the next step", externalId);
        websiteAnalysisService.analysisEnd(externalId);
    }

    public void enqueue(String websiteId) {
        final TranslationRequest request = new TranslationRequest();
        request.websiteId = websiteId;
        queueService.push(request, QUEUE);
    }

    public void handleError(long requestId, byte[] content) {
        final RequestEntry entry;
        synchronized (runningJobs) {
            // remove all other requests for this website id, we have to retry everything
            entry = handleRequest(requestId);
            if (entry == null) {
                // already removed request so we can ignore it
                return;
            }
            final List<Long> requestsToRemove = runningJobs.entrySet()
                    .stream()
                    .filter(it -> it.getValue().websiteId.equals(entry.websiteId))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            requestsToRemove.forEach(runningJobs::remove);
            runningJobs.notifyAll();
        }

        // record the error in the workflow error handler
        final String message;
        try {
            message = new ObjectMapper().writeValueAsString(new TranslationRequest(entry.websiteId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        final String errorMessage = content == null ? "" : new String(content, StandardCharsets.UTF_8);
        errorHandler.handle(new ErrorMessage(QUEUE.getName(), message, null, "Error sent by the translation service: "+ errorMessage, true, 1));
    }

    private RequestEntry handleRequest(long key) {
        synchronized (runningJobs) {
            final RequestEntry remove = runningJobs.remove(key);
            runningJobs.notifyAll();
            return remove;
        }
    }

    private void cleanExpiredRequests() {
        synchronized (runningJobs) {
            final List<Long> requestsToRemove = runningJobs.entrySet()
                    .stream()
                    .filter(it -> Duration.between(it.getValue().creationDate, Instant.now()).toMinutes() >= MAX_REQUEST_DURATION_IN_MINUTES)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            for (Long requestId : requestsToRemove) {
                handleError(requestId, "Timeout of the request, never got an answer".getBytes(StandardCharsets.UTF_8));
            }
        }

    }

    @XmlRootElement(name = "root")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Root {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<TextContent> text;

        public Root(TextContent... texts) {
            text = Arrays.asList(texts);
        }

        public Root() {
        }

        public Root(List<TextContent> text) {
            this.text = text;
        }
    }

    public static class TextContent {
        @XmlAttribute(name = "id")
        @XmlID
        public String id;

        @XmlValue
        public String content;

        public TextContent(String id, String content) {
            this.id = id;
            this.content = content;
        }

        public TextContent() {
        }
    }

    @Override
    public MessageQueue<TranslationRequest> getQueue() {
        return QUEUE;
    }

    public static class TranslationRequest {
        public String websiteId;

        public TranslationRequest(String websiteId) {
            this.websiteId = websiteId;
        }

        public TranslationRequest() {
        }
    }

    public static class RequestEntry extends TranslationRequest {
        public Instant creationDate;

        public RequestEntry(String websiteId, Instant creationDate) {
            super(websiteId);
            this.creationDate = creationDate;
        }

        @Override
        public String toString() {
            return "{" +
                    "websiteId='" + websiteId + '\'' +
                    ", creationDate=" + creationDate +
                    '}';
        }
    }
/*

    public static void main(String[] args) throws IOException {
        final TranslateJobService translateJob = new TranslateJobService();
//        final Long translate = translateJob.translate("asd",
//                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><texts><text id=\"5be7266da751566e09b28fac012e09a7\">This page describes how to submit a translation request to the CEF Automated Translation internal service (\"eTranslation\") via a Web Service call. This is a typical B2B integration scenario, where a client application needs to send an automated translation request and receive the result. The interaction with the CEF Web Service is asynchronous. This means that the client sends a translation request and is notified back at a later point in time when the text snippet or document is translated. Thus, calling the web service doesn't block the client. However, the client needs to expose a callback URL, where it receives a notification that a certain translation job is completed. After translation is completed, eTranslation sends the translated text or document to the destination URL specified by the Client.</text>" +
//                "<text id=\"4bac0d45f14988ea98298405c80b75bf\">The client sends a translation request to the eTranslation web service ; eTranslation web service replies synchronously with the eTranslation request ID (positive number) or an error code (negative number) ;" +
//                        "</text></texts>");
//        System.out.println(translate);
        Long translate = translateJob.translate("EN", ImmutableMap.of(
                "5be7266da751566e09b28fac012e09a7", "This page describes how to submit a translation request to the CEF Automated Translation internal service (\"eTranslation\") via a Web Service call. This is a typical B2B integration scenario, where a client application needs to send an automated translation request and receive the result. The interaction with the CEF Web Service is asynchronous. This means that the client sends a translation request and is notified back at a later point in time when the text snippet or document is translated. Thus, calling the web service doesn't block the client. However, the client needs to expose a callback URL, where it receives a notification that a certain translation job is completed. After translation is completed, eTranslation sends the translated text or document to the destination URL specified by the Client.",
                "4bac0d45f14988ea98298405c80b75bf", "The client sends a translation request to the eTranslation web service ; eTranslation web service replies synchronously with the eTranslation request ID (positive number) or an error code (negative number) ;"
        ), "youpi.com");
        System.out.println(translate);
    }
*/
}
