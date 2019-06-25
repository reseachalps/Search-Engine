package eu.researchalps.db.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.util.NormalizeURL;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Document
public class Website {
    /**
     * Id of the website
     * Simplified url to avoid duplicates (www. for instance, or http/https)
     */
    @Id
    private String id;

    /**
     * Crawl entry point
     */
    private String baseURL;

    /**
     * Crawl mode
     */
    private CrawlMode crawlMode;

    /**
     * Monitoring platforms identified
     */
    private List<String> monitoring;

    /**
     * Monitoring platforms identified
     */
    private List<Address> addresses;

    /**
     * True if website is identified as canonical
     */
    private Boolean canonical;

    /**
     * True if website is identified as responsive
     */
    private Boolean responsive;
    /**
     * True if website is identified as mobile friendly
     */
    private Boolean mobile;

    /**
     * True if website is identified as an eCommerce website
     */
    private Boolean ecommerce;

    /**
     * CMS platforms identified
     */
    private List<String> platforms;
    /**
     * RSS feeds
     */
    private List<RssFeed> rss;

    /**
     * Ids of projects detected using named entity detection
     */
    private List<String> extractedProjects = new LinkedList<>();

    /**
     * Ids of publications (e.g. mosty patents) using named entity detection
     */
    private List<String> extractedPublications = new LinkedList<>();

    /**
     * Resolved publications from the publication extractor
     */
    private List<String> resolvedPublications = new LinkedList<>();

    /**
     * scocial account extracted
     */
    private Set<SocialAccount> facebook = new HashSet<>();
    /**
     * scocial account extracted
     */
    private Set<SocialAccount> linkedIn = new HashSet<>();
    /**
     * scocial account extracted
     */
    private Set<SocialAccount> viadeo = new HashSet<>();
    /**
     * scocial account extracted
     */
    private Set<SocialAccount> youtube = new HashSet<>();
    /**
     * scocial account extracted
     */
    private Set<SocialAccount> twitter = new HashSet<>();
    /**
     * scocial account extracted
     */
    private Set<SocialAccount> googlePlus = new HashSet<>();
    /**
     * scocial account extracted
     */
    private Set<SocialAccount> dailymotion = new HashSet<>();
    /**
     * scocial account extracted
     */
    private Set<SocialAccount> vimeo = new HashSet<>();
    /**
     * scocial account extracted
     */
    private Set<SocialAccount> instagram = new HashSet<>();

    /**
     * List of contact form urls
     */
    private List<String> contactForms;

    /**
     * List of generic emails found on the website
     */
    private List<String> genericEmails;

    /**
     * List of emails found on the website
     */
    private List<String> emails;

    /**
     * List of phone numbers found on the website
     */
    private List<String> phones;

    /**
     * List of fax numbers found on the website
     */
    private List<String> faxes;

    /**
     * description extracted from website or social networks
     */
    private String description;
    /**
     * html meta description
     */
    private String metaDescription;

    private Double quality;

    @CreatedDate
    private Date createdDate;

    @LastModifiedDate
    private Date lastUpdated;

    /**
     * last completion of the crawl
     */
    private Date lastCompletion;

    /**
     * Number of pages crawled
     */
    private int pageCount;

    private Set<String> titles = new HashSet<>();
    private Set<String> pages = new HashSet<>();

    public Website() {
    }

    public Website(String id, String baseURL, CrawlMode crawlMode) {
        this.id = id;
        this.baseURL = baseURL;
        this.crawlMode = crawlMode;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public CrawlMode getCrawlMode() {
        return crawlMode;
    }

    public void setCrawlMode(CrawlMode crawlMode) {
        this.crawlMode = crawlMode;
    }

    public String getId() {
        return id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Date getLastCompletion() {
        return lastCompletion;
    }

    public void setLastCompletion(Date lastCompletion) {
        this.lastCompletion = lastCompletion;
    }

    public static String idFromUrl(String baseURL) {
        return NormalizeURL.normalizeForIdentification(baseURL);
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public List<String> getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(List<String> monitoring) {
        this.monitoring = monitoring;
    }

    public Boolean getCanonical() {
        return canonical;
    }

    public void setCanonical(Boolean canonical) {
        this.canonical = canonical;
    }

    public Boolean getResponsive() {
        return responsive;
    }

    public void setResponsive(Boolean responsive) {
        this.responsive = responsive;
    }

    public Boolean getMobile() {
        return mobile;
    }

    public void setMobile(Boolean mobile) {
        this.mobile = mobile;
    }

    public Boolean getEcommerce() {
        return ecommerce;
    }

    public void setEcommerce(Boolean ecommerce) {
        this.ecommerce = ecommerce;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public List<RssFeed> getRss() {
        return rss;
    }

    public void setRss(List<RssFeed> rss) {
        this.rss = rss;
    }

    public Set<SocialAccount> getFacebook() {
        return facebook;
    }

    public void setFacebook(Set<SocialAccount> facebook) {
        this.facebook = facebook;
    }

    public Set<SocialAccount> getLinkedIn() {
        return linkedIn;
    }

    public void setLinkedIn(Set<SocialAccount> linkedIn) {
        this.linkedIn = linkedIn;
    }

    public Set<SocialAccount> getViadeo() {
        return viadeo;
    }

    public void setViadeo(Set<SocialAccount> viadeo) {
        this.viadeo = viadeo;
    }

    public Set<SocialAccount> getYoutube() {
        return youtube;
    }

    public void setYoutube(Set<SocialAccount> youtube) {
        this.youtube = youtube;
    }

    public Set<SocialAccount> getTwitter() {
        return twitter;
    }

    public void setTwitter(Set<SocialAccount> twitter) {
        this.twitter = twitter;
    }

    public Set<SocialAccount> getGooglePlus() {
        return googlePlus;
    }

    public void setGooglePlus(Set<SocialAccount> googlePlus) {
        this.googlePlus = googlePlus;
    }

    public Set<SocialAccount> getDailymotion() {
        return dailymotion;
    }

    public void setDailymotion(Set<SocialAccount> dailymotion) {
        this.dailymotion = dailymotion;
    }

    public Set<SocialAccount> getVimeo() {
        return vimeo;
    }

    public void setVimeo(Set<SocialAccount> vimeo) {
        this.vimeo = vimeo;
    }

    public Set<SocialAccount> getInstagram() {
        return instagram;
    }

    public void setInstagram(Set<SocialAccount> instagram) {
        this.instagram = instagram;
    }


    public List<String> getContactForms() {
        return contactForms;
    }

    public void setContactForms(List<String> contactForms) {
        this.contactForms = contactForms;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public Double getQuality() {
        return quality;
    }

    public void setQuality(Double quality) {
        this.quality = quality;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getPageCount() {
        return pageCount;
    }

    public List<String> getExtractedPublications() {
        return extractedPublications;
    }

    public void setExtractedPublications(List<String> extractedPublications) {
        this.extractedPublications = extractedPublications;
    }

    public List<String> getExtractedProjects() {
        return extractedProjects;
    }

    public void setExtractedProjects(List<String> extractedProjects) {
        this.extractedProjects = extractedProjects;
    }

    public List<String> getResolvedPublications() {
        return resolvedPublications;
    }

    public void setResolvedPublications(List<String> resolvedPublications) {
        this.resolvedPublications = resolvedPublications;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public List<String> getFaxes() {
        return faxes;
    }

    public void setFaxes(List<String> faxes) {
        this.faxes = faxes;
    }

    public List<String> getGenericEmails() {
        return genericEmails;
    }

    public void setGenericEmails(List<String> genericEmails) {
        this.genericEmails = genericEmails;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public Set<String> getTitles() {
        return titles;
    }

    public void setTitles(Set<String> titles) {
        this.titles = titles;
    }

    public Set<String> getPages() {
        return pages;
    }

    public void setPages(Set<String> pages) {
        this.pages = pages;
    }
}
