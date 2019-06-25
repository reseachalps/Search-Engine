package eu.researchalps.db.model.publication;

import eu.researchalps.db.model.Identifier;
import eu.researchalps.db.model.Source;
import eu.researchalps.util.TolerantDateDeserializer;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.xuender.unidecode.Unidecode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

/**
 * Publicaiton in scnar
 */
@Document
public class Publication {
    @Id
    private String id;
    /**
     * Publication Type, enum should be explicit
     */
    @Indexed
    private PublicationType type;
    /**
     * Title of the publication
     */
    private String title;
    /**
     * Subtitle of the publication
     */
    private String subtitle;
    /**
     * authors of the publication
     */
    private List<PublicationAuthor> authors = new LinkedList<>();
    /**
     * thesis directors when the publication is a thesis
     */
    private List<PublicationAuthor> thesisDirectors = new LinkedList<>();
    /**
     * Where the publication has been published (journal, event...)
     */
    private PublicationSource source = new PublicationSource();
    /**
     * summary of the publication
     */
    private String summary;
    /**
     * alternative summary of the publication (often the english summary)
     */
    private String alternativeSummary;
    /**
     * Thematics of the publication
     */
    private List<PublicationThematic> thematics = new LinkedList<>();
    /**
     * Structures directly affiliated with this publication
     */
    @Indexed
    private Set<String> structures = new HashSet<>();
    /**
     * Structures asssociated with this publication by MENESR
     */
    private Set<String> validatedStructures = new HashSet<>();

    /**
     * Link to the portal who has published the data
     */
    private String link;
    /**
     * Link to the full text document
     */
    private String linkDocument;
    /**
     * Last time the data was updated on sources
     */
    @JsonDeserialize(using = TolerantDateDeserializer.class)
    private Date lastSourceDate;
    /**
     * Known publication date (may be null)
     */
    @JsonDeserialize(using = TolerantDateDeserializer.class)
    private Date publicationDate;
    @LastModifiedDate
    private Date lastUpdated;

    private Set<Identifier> identifiers = new HashSet<>();

    /**
     * Source information
     */
    private List<Source> sources;

    public Publication(String title, PublicationType type, PublicationAuthor firstAuthor) {
        this.title = title;
        this.type = type;
        authors.add(firstAuthor);
        this.id = computeId();
    }

    public Publication() {
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

    public String getSubtitle() {
        return subtitle;
    }

    public List<PublicationAuthor> getAuthors() {
        return authors;
    }

    public PublicationSource getSource() {
        return source;
    }

    public String getSummary() {
        return summary;
    }

    public String getAlternativeSummary() {
        return alternativeSummary;
    }

    public List<PublicationThematic> getThematics() {
        return thematics;
    }

    public Set<String> getStructures() {
        return structures;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setAlternativeSummary(String alternativeSummary) {
        this.alternativeSummary = alternativeSummary;
    }

    public String computeId() {
        // safety check
        if (title == null) return null;

        // for Patents, use the patent Id
        String normalizedTitle = Unidecode.decode(title).toLowerCase().replaceAll("[^a-z0-9]*", "");
        String result;
        if (type == PublicationType.PATENT) {
            result = "PATENT:" + normalizedTitle;
        } else {
            if (title == null || authors.isEmpty() || authors.get(0).getLastName() == null) {
                return null;
            }
            String author = Unidecode.decode(authors.get(0).getLastName()).toLowerCase().replaceAll("[^a-z0-9]*", "");
            result = author + ":" + normalizedTitle;
        }
        // Ignore long title (only few cases mostly due to wrong formatting rules on the provider side)
        if (result.length() > 1000) {
            return null;
        }
        return result;
    }

    public Date getLastSourceDate() {
        return lastSourceDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastSourceDate(Date lastSourceDate) {
        this.lastSourceDate = lastSourceDate;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthors(List<PublicationAuthor> authors) {
        this.authors = authors;
    }

    public List<PublicationAuthor> getThesisDirectors() {
        return thesisDirectors;
    }

    public void setThesisDirectors(List<PublicationAuthor> thesisDirectors) {
        this.thesisDirectors = thesisDirectors;
    }

    public void setSource(PublicationSource source) {
        this.source = source;
    }

    public void setThematics(List<PublicationThematic> thematics) {
        this.thematics = thematics;
    }

    public void setStructures(Set<String> structures) {
        this.structures = structures;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLinkDocument() {
        return linkDocument;
    }

    public void setLinkDocument(String linkDocument) {
        this.linkDocument = linkDocument;
    }

    public void setValidatedStructures(Set<String> validatedStructures) {
        this.validatedStructures = validatedStructures;
    }

    public Set<String> getValidatedStructures() {
        return validatedStructures;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonSetter("identifiers")
    public void setIdentifiersJson(JsonNode node) throws JsonProcessingException {
        this.identifiers = new HashSet<>();
        if (node.isArray()) {
            for (JsonNode ident : node) {
                identifiers.add(OBJECT_MAPPER.treeToValue(ident, Identifier.class));
            }
        } else {
            PublicationIdentifiers oldIds = OBJECT_MAPPER.treeToValue(node, PublicationIdentifiers.class);
            if (oldIds.getDoi() != null) {
                identifiers.add(new Identifier("DOI", oldIds.getDoi()));
            }
            if (oldIds.getHal() != null) {
                identifiers.add(new Identifier("HAL", oldIds.getHal()));
            }
            if (oldIds.getProdinra() != null) {
                identifiers.add(new Identifier("ProdINRA", oldIds.getProdinra()));
            }
            if (oldIds.getThesesfr() != null) {
                identifiers.add(new Identifier("theses.fr", oldIds.getThesesfr()));
            }
            if (oldIds.getPatent() != null) {
                for (String id : oldIds.getPatent()) {
                    if (id != null) {
                        identifiers.add(new Identifier("PATENT", id));
                    }
                }
            }
            if (oldIds.getOai() != null) {
                for (String id : oldIds.getOai()) {
                    if (id != null) {
                        identifiers.add(new Identifier("OAI", id));
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Publication that = (Publication) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(lastUpdated, that.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastUpdated);
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }
}
