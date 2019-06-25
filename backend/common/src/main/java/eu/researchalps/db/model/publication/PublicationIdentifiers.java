package eu.researchalps.db.model.publication;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

/**
 * Stores the different identifiers of a publication
 */
public class PublicationIdentifiers {
    @Indexed
    private String doi;
    @Indexed
    private String hal;
    @Indexed
    private String prodinra;
    @Indexed
    private String thesesfr;
    // The same publication can be published in different registrars (EP, WPO, US...)
    @Indexed
    private List<String> patent;
    @Indexed
    private List<String> oai;

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public void setHal(String hal) {
        this.hal = hal;
    }

    public void setProdinra(String prodinra) {
        this.prodinra = prodinra;
    }

    public void setPatent(List<String> patent) {
        this.patent = patent;
    }

    public String getDoi() {
        return doi;
    }

    public String getHal() {
        return hal;
    }

    public String getProdinra() {
        return prodinra;
    }

    public List<String> getPatent() {
        return patent;
    }

    public List<String> getOai() {
        return oai;
    }

    public void setOai(List<String> oai) {
        this.oai = oai;
    }

    public String getThesesfr() {
        return thesesfr;
    }

    public void setThesesfr(String thesesfr) {
        this.thesesfr = thesesfr;
    }
}
