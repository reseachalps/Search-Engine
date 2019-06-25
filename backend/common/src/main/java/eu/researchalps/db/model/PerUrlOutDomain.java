package eu.researchalps.db.model;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Outlinks domains
 */
public class PerUrlOutDomain {
    private String url;
    private List<OutDomain> outDomains = Lists.newArrayList();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<OutDomain> getOutDomains() {
        return outDomains;
    }

    public void setOutDomains(List<OutDomain> outDomains) {
        this.outDomains = outDomains;
    }
}
