package eu.researchalps.db.model;

import com.google.common.collect.Lists;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Outlink identified on a crawl.
 */
@Document
public class Outlink {
    @Id
    private String domain;

    private List<PerUrlOutDomain> referers = Lists.newArrayList();

    private List<OutDomain> outDomains = Lists.newArrayList();

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<PerUrlOutDomain> getReferers() {
        return referers;
    }

    public void setReferers(List<PerUrlOutDomain> referers) {
        this.referers = referers;
    }

    public List<OutDomain> getOutDomains() {
        return outDomains;
    }

    public void setOutDomains(List<OutDomain> outDomains) {
        this.outDomains = outDomains;
    }
}
