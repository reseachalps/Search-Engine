package eu.researchalps.workflow.website.extractor.dto;

import eu.researchalps.db.model.Address;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
*
*/
public class CoreExtractorOut {
    public String id;
    public String jobId;
    public String country;

    public Set<String> dailymotion;
    public Set<String> youtube;
    public Set<String> twitter;
    public Set<String> vimeo;
    public Set<String> facebook;
    public Set<String> linkedin;
    public Set<String> instagram;
    public Set<String> pinterest;
    public Set<String> viadeo;
    public Set<String> googleplus;

    public List<Contact> contact;

    public Set<String> rss;
    public Set<String> catalog;
    public List<Email> email;
    public List<String> phone;
    public List<String> fax;
    public List<String> contactform;

    public boolean legal;
    public boolean useterms;
    public List<Double> capital;
    public List<String> localId;
    public Set<String> certifications;

    public String description;
    public List<List<Object>> summary;

    public boolean mobile;

    public Map<String, Integer> outlinks;
    public List<Address> addresses;

    public ECommerceMeta ecommerce_meta;
    public List<ExtractedHour> hours;
    public ExtractedApps mobile_apps;
}
