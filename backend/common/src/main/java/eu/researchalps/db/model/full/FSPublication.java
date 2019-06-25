package eu.researchalps.db.model.full;

import eu.researchalps.db.model.Identifier;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.model.publication.PublicationAuthor;
import eu.researchalps.db.model.publication.PublicationSource;
import eu.researchalps.db.model.publication.PublicationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.lang.Math.min;

/**
 * Summary of a pubication to be included in a FullStructure
 * @see eu.researchalps.db.model.Publication for the description of the content
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FSPublication {
    private String id;
    /**
     * Publication Type, enum should be explicit
     */
    private PublicationType type;
    private String title;
    private List<PublicationAuthor> authors = new LinkedList<>();
    private int authorsCount;
    /**
     * Where the publication has been published (journal, event...)
     */
    private PublicationSource source;

    private Set<Identifier> identifiers;

    /**
     * SStructures directly affiliated with this publication
     */
    private Set<String> structures = new HashSet<>();
    /**
     * Last time the data was updated on sources
     */
    private Date lastSourceDate;
    /**
     * Known publication date (may be null)
     */
    private Date publicationDate;
    @LastModifiedDate
    private Date lastUpdated;
    private boolean webDetected;

    private String link;


    public FSPublication(Publication publication) {
        this.id = publication.getId();
        this.title = publication.getTitle();
        this.type = publication.getType();
        this.authorsCount = publication.getAuthors().size();
        this.authors = publication.getAuthors().subList(0, min(3, this.authorsCount));

        // remove useless affiliations
        for (PublicationAuthor author : authors) {
            author.setAffiliations(null);
        }
        this.source = publication.getSource();
        this.lastSourceDate = publication.getLastSourceDate();
        this.publicationDate = publication.getPublicationDate();
        this.structures = publication.getStructures();

        this.identifiers = publication.getIdentifiers();
        this.link = publication.getLink();
    }

    public FSPublication() {
    }

    public String getId() {
        return id;
    }

    public PublicationType getType() {
        return type;
    }

    public void setType(PublicationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public List<PublicationAuthor> getAuthors() {
        return authors;
    }

    public PublicationSource getSource() {
        return source;
    }

    public Set<String> getStructures() {
        return structures;
    }

    public Date getLastSourceDate() {
        return lastSourceDate;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setStructures(Set<String> structures) {
        this.structures = structures;
    }

    public void setWebDetected(boolean webDetected) {
        this.webDetected = webDetected;
    }

    public boolean isWebDetected() {
        return webDetected;
    }

    public int getAuthorsCount() {
        return authorsCount;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public String getLink() {
        return link;
    }
}
