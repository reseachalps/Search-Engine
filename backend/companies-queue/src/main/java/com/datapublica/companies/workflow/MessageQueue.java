package com.datapublica.companies.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Declaration of persistent queues
 */
public class MessageQueue<DTO> {
    /*

        FOCUS_CRAWLER(UrlDTO.class),
        FOCUS_CRAWLER_OUT(CrawlerPlugin.Out.class),
        CORE_EXTRACTOR(CoreExtractorPlugin.In.class),
        CORE_EXTRACTOR_OUT(CoreExtractorOut.class),
        CATEGORIZER(CategorizerPlugin.In.class),
        CATEGORIZER_OUT(CategorizerPlugin.Out.class),
        META_SCORER(MetaScorerPlugin.In.class),
        META_SCORER_OUT(MetaScorerPlugin.Out.class),
        WEBSITE_DETECTER(WebsiteDetecterPlugin.In.class),
        WEBSITE_DETECTER_OUT(WebsiteDetecterPlugin.Out.class),
        WEBSITE_CHECKER(WebsiteCheckerPlugin.In.class),
        WEBSITE_CHECKER_OUT(WebsiteCheckerPlugin.Out.class),
        SIGNALS(Signal.class),
        SCORER_FROM_COMPANY(ExchangeNode.COMPANY_UPDATED),
        SCORER_FROM_WEBSITE(ExchangeNode.WEBSITE_UPDATED),
        INDEX_COMPANY(ExchangeNode.FULLCOMPANY_UPDATED),
        SIGNAL_SOURCE_BUILDER(ExchangeNode.FULLCOMPANY_UPDATED),
        SIGNAL_RSS_HANDLER(RssOut.class),
        SIGNAL_FACEBOOK_FETCHER(SignalSocialFetchMessage.class),
        SIGNAL_TWITTER_FETCHER(SignalSocialFetchMessage.class),
        SIGNAL_SOCIAL_HANDLER(SignalsOut.class),
        SIGNAL_CATEGORIZER(SignalCategorizerPlugin.In.class),
        SIGNAL_CATEGORIZER_OUT(SignalTagsDTO.class),
        SIGNAL_INDEXER(SignalIndexerPlugin.SignalIndexerCommand.class),
        SCREENSHOT_UPDATE(ExchangeNode.WEBSITE_UPDATED),
        INDEX_SIMPLE_COMPANY(ExchangeNode.FULLCOMPANY_UPDATED),
        SCREENSHOT(UrlDTO.class),
        SCREENSHOT_OUT(ScreenshotPlugin.Out.class),
        COMPANIES_CLUSTERIZER(CompanyClusterizerPlugin.In.class),
        COMPANIES_CLUSTERIZER_OUT(CompanyClusterizerPlugin.Out.class),
        BING_CONTACTS(BingContactsPlugin.In.class),
        BING_CONTACTS_OUT(ContactsDTO.class),
        CONTACTS_UPDATE(ExchangeNode.FULLCOMPANY_UPDATED),
        RSS_FETCHER(RssInput.class),
        MEDIA_RSS_OUT(RssOut.class),
        GENERIC_MEDIA_IN(NewsMediaTreatment.In.class),
        FUNDRAISING_MEDIA_IN(FundraisingMediaTreatment.In.class),
        NAMED_ENTITY_DETECTER(NamedEntityQuery.class),
        FUNDRAISE_DETECTER(FundraisingMediaItem.class),
        FUNDRAISE_DETECTER_OUT(FundraiseDetecterPlugin.Out.class),
        SCHEDULER(ScheduledJobResponse.class, ScheduledJobMessage.class),
        PLUGIN_ERROR(PluginError.class),
        FCU_COMPANY(ExchangeNode.COMPANY_UPDATED),
        FCU_WEBSITE(ExchangeNode.WEBSITE_UPDATED),
        FCU_FILINGS(String.class),
        FC_REFRESH(FullCompanyRefreshDTO.class),
        BRAND_IMPORT(EmptyImportOrder.class),
        PATENT_IMPORT(EmptyImportOrder.class),
        BEL_IMPORT(EmptyImportOrder.class),
        BODACC_IMPORT(EmptyImportOrder.class),
        TARGETING_UPDATE(ExchangeNode.FULLCOMPANY_UPDATED),
        PARTITIONNER_UPDATE(ExchangeNode.FULLCOMPANY_UPDATED),
        COMPANIES_IMPORT(CompanyImportDTO.class),
        WEBSITE_RECRAWL_ALL(RecrawlProcess.RecrawlOrder.class),
        PHONE_FILTER_ALL(PhoneFilteringProcess.PhoneFilteringOrder.class),
        OVH_UPLOAD(OvhUploadDTO.class),
        SCREENSHOT_UPLOAD_OUT(OvhUploadDTO.Out.class),
        LOGOS_UPLOAD_OUT(OvhUploadDTO.Out.class);
     */

    private Class<? extends DTO> clazz;
    private final String name;
    private final Class<DTO> checkClazz;
    private ExchangeNode exchange;

    private MessageQueue(String name, Class<? extends DTO> clazz, Class<DTO> checkClazz, ExchangeNode<DTO> exchange) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Message queue name cannot be empty");
        }
        this.name = name;
        this.clazz = clazz;
        this.checkClazz = checkClazz;
        this.exchange = exchange;
    }

    public Class<? extends DTO> getMessageClass() {
        return clazz;
    }

    public ExchangeNode getExchangeNode() {
        return exchange;
    }

    public Class<DTO> getCheckClass() {
        return checkClazz;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageQueue<?> that = (MessageQueue<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    private static Map<String, MessageQueue<?>> instances = new HashMap<>();

    public static <DTO> MessageQueue<DTO> get(String name, ExchangeNode<DTO> node) {
        return get(name, node.getMessageClass(), node.getMessageClass(), node);
    }

    public static <DTO> MessageQueue<DTO> get(String name, Class<DTO> dtoClass) {
        return get(name, dtoClass, dtoClass, null);
    }

    public static <DTO> MessageQueue<DTO> get(String name, Class<? extends DTO> dtoClass, Class<DTO> checkClazz) {
        return get(name, dtoClass, checkClazz, null);
    }

    private static <DTO> MessageQueue<DTO> get(String name, Class<? extends DTO> dtoClass, Class<DTO> checkClazz, ExchangeNode<DTO> exchangeNode) {
        name = name.toUpperCase();
        MessageQueue<?> queue = instances.get(name);
        if (queue == null) {
            MessageQueue<DTO> queueDeclaration = new MessageQueue<>(name, dtoClass, checkClazz, exchangeNode);
            instances.put(name, queueDeclaration);
            return queueDeclaration;
        }
        if (!queue.checkClazz.equals(checkClazz) || ! Objects.equals(queue.exchange, exchangeNode)) {
            throw new IllegalArgumentException("Declared queue node " + name + " has conflicting declaration");
        }
        //noinspection unchecked
        return (MessageQueue<DTO>) queue;
    }

    public static Collection<MessageQueue<?>> all() {
        return instances.values();
    }

    @JsonCreator
    public static MessageQueue getByName(String name) {
        MessageQueue result = null;
        if (StringUtils.isNotEmpty(name)) {
            result = instances.get(name.toUpperCase());
        }
        if (result == null) {
            throw new IllegalArgumentException("Unable to get queue by name for :'" + name + "'");
        }
        return result;
    }
}
