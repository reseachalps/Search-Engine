package eu.researchalps.workflow.website.extractor;


import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import eu.researchalps.db.model.*;
import eu.researchalps.db.model.*;
import eu.researchalps.db.repository.OutlinkRepository;
import eu.researchalps.db.repository.WebsiteRepository;
import eu.researchalps.util.RepositoryLock;
import eu.researchalps.workflow.website.entity.EntityExtractorPlugin;
import eu.researchalps.workflow.website.extractor.dto.CoreExtractorOut;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
@Component
public class CoreExtractorPlugin extends QueueComponent implements QueueListener<CoreExtractorOut> {

    public static final MessageQueue<In> QUEUE_IN = MessageQueue.get("CORE_EXTRACTOR", In.class);
    public static final MessageQueue<CoreExtractorOut> QUEUE_OUT = MessageQueue.get("CORE_EXTRACTOR_OUT", CoreExtractorOut.class);

    private RepositoryLock<Website, String, WebsiteRepository> repository;

    @Autowired
    private EntityExtractorPlugin entityExtractorPlugin;

    @Autowired
    public void setRepository(WebsiteRepository repository) {
        this.repository = RepositoryLock.get(repository);
    }

    @Autowired
    private OutlinkRepository outlinkRepository;

    public void execute(Website w) {
        queueService.push(new In(w), QUEUE_IN, QUEUE_OUT);
    }

    @Override
    public void receive(CoreExtractorOut out) {
        Website saved = repository.update(out.id, tx -> {
            Website w = tx.getNotNull();
            merge(w, out);
            tx.saveDeferred();
        }).getSaved();
        entityExtractorPlugin.execute(saved);
    }

    private Website merge(Website w, CoreExtractorOut out) {
        setSocial(w, out);
        setCommunication(w, out);
        setSummary(w, out);
        setTech(w, out);
        setECommerce(w, out);
        mergeOutlinks(w.getId(), w.getBaseURL(), out.outlinks);
        computeScore(w);
        return w;
    }

    private void computeScore(Website w) {
        /**
         *
         */
        double score = 0;

        if (w.getMonitoring() != null && !w.getMonitoring().isEmpty()) {
            score += 0.2;
        }
        if (w.getCanonical() != null && w.getCanonical()) {
            score += 0.2;
        }
        if ((w.getTwitter() != null && !w.getTwitter().isEmpty()) ||
                (w.getFacebook() != null && !w.getFacebook().isEmpty()) ||
                (w.getLinkedIn() != null && !w.getLinkedIn().isEmpty()) ||
                (w.getViadeo() != null && !w.getViadeo().isEmpty()) ||
                (w.getGooglePlus() != null && !w.getGooglePlus().isEmpty())) {
            score += 0.3;
        }
        if ((w.getYoutube() != null && !w.getYoutube().isEmpty()) ||
                (w.getDailymotion() != null && !w.getDailymotion().isEmpty()) ||
                (w.getVimeo() != null && !w.getVimeo().isEmpty())) {
            score += 0.1;
        }
        if (w.getContactForms() != null && !w.getContactForms().isEmpty()) {
            score += 0.1;
        }
        if (w.getPlatforms() != null && !w.getPlatforms().isEmpty()) {
            score += 0.1;
        }
        w.setQuality(score);
    }

    private void setECommerce(Website w, CoreExtractorOut out) {
        w.setEcommerce(out.ecommerce_meta.pages_with_basket >= 3 && out.ecommerce_meta.pages_with_prices >= 1 && out.ecommerce_meta.avg_prices_per_page > 1);
    }

    private Set<SocialAccount> toSocialAccount(Collection<String> result) {
        return result.stream().map(it -> new SocialAccount(it, 1.0, null)).collect(Collectors.toSet());
    }

    private void setSocial(Website w, CoreExtractorOut out) {
        w.setDailymotion(toSocialAccount(out.dailymotion));
        w.setYoutube(toSocialAccount(out.youtube));
        w.setTwitter(toSocialAccount(out.twitter));
        w.setVimeo(toSocialAccount(out.vimeo));
        w.setFacebook(toSocialAccount(out.facebook));
        w.setLinkedIn(toSocialAccount(out.linkedin));
        w.setInstagram(toSocialAccount(out.instagram));
        w.setViadeo(toSocialAccount(out.viadeo));
        w.setGooglePlus(toSocialAccount(out.googleplus));
    }

    private void setCommunication(Website w, CoreExtractorOut out) {
        w.setRss(out.rss.stream().map(it -> new RssFeed(it, 1.0f)).collect(Collectors.toList()));
        w.setGenericEmails(out.email.stream().filter(it -> it.generic).map(it -> it.email).collect(Collectors.toList()));
        w.setEmails(out.email.stream().filter(it -> !it.generic).map(it -> it.email).collect(Collectors.toList()));
        w.setPhones(out.phone);
        w.setFaxes(out.fax);
        w.setContactForms(out.contactform);
        w.setAddresses(out.addresses);
    }

    private void setSummary(Website w, CoreExtractorOut out) {
        w.setDescription(out.description);
        w.setMetaDescription(out.description);
    }


    private void setTech(Website w, CoreExtractorOut out) {
        w.setMobile(out.mobile);
    }

    private void mergeOutlinks(String domain, String url, Map<String, Integer> outlinks) {
        Outlink outlink = outlinkRepository.findOne(domain);
        if (outlink == null) {
            outlink = new Outlink();
            outlink.setDomain(domain);
        }
        PerUrlOutDomain perUrl = new PerUrlOutDomain();
        perUrl.setUrl(url);
        List<OutDomain> outDomains = perUrl.getOutDomains();
        domainMapToOutDomains(outDomains, outlinks);

        Map<String, Integer> total = Maps.newHashMap(outlinks);
        List<PerUrlOutDomain> finalPerUrl = Lists.newArrayList(perUrl);
        // now perUrl is built we have to merge the data
        for (PerUrlOutDomain referer : outlink.getReferers()) {
            if (url.equals(referer.getUrl())) {
                // we already have this one
                continue;
            }
            // put old entry and add
            finalPerUrl.add(referer);
            for (OutDomain refOutDomain : referer.getOutDomains()) {
                total.merge(refOutDomain.getDomain(), refOutDomain.getCount(), (a, b) -> a + b);
            }
        }
        outlink.setReferers(finalPerUrl);
        outlink.getOutDomains().clear();
        domainMapToOutDomains(outlink.getOutDomains(), total);
        outlinkRepository.save(outlink);
    }

    private void domainMapToOutDomains(List<OutDomain> outDomains, Map<String, Integer> outlinks) {
        for (Map.Entry<String, Integer> entry : outlinks.entrySet()) {
            OutDomain o = new OutDomain();
            o.setDomain(entry.getKey());
            o.setCount(entry.getValue());
            outDomains.add(o);
        }
    }

    @Override
    public MessageQueue<CoreExtractorOut> getQueue() {
        return QUEUE_OUT;
    }


    public static class In {
        public String id;
        public String jobId;
        public String domain;
        public String country;

        public In() {
        }

        public In(Website w) {
            this.id = w.getId();
            this.country = "FR";
            this.jobId = w.getId();
            this.domain = w.getId();
        }
    }

    private static String extractDomain(Website w) {
        try {
            return new URL(w.getBaseURL()).getHost();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Can not have a malformed URL exception at that point " + w.getBaseURL());
        }
    }
}
