package eu.researchalps.search.model.response;

import eu.researchalps.db.model.Identifier;
import eu.researchalps.db.model.Source;
import eu.researchalps.db.model.full.LightStructure;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.model.publication.PublicationAuthor;
import eu.researchalps.db.model.publication.PublicationSource;
import eu.researchalps.db.model.publication.PublicationThematic;
import eu.researchalps.db.model.publication.PublicationType;

import java.util.*;

/**
 * Created by loic on 25/04/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class PublicationDTO {
    public String id;
    public PublicationType type;
    public String title;
    public String subtitle;
    public List<PublicationAuthor> authors = new LinkedList<>();
    public List<PublicationAuthor> thesisDirectors = new LinkedList<>();
    public PublicationSource source = new PublicationSource();
    public String summary;
    public String alternativeSummary;
    public Set<Identifier> identifiers = new HashSet<>();
    public List<PublicationThematic> thematics = new LinkedList<>();
    public List<LightStructure> structures = new LinkedList<>();
    public String link;
    public String linkDocument;
    public Date lastSourceDate;
    public Date publicationDate;
    public List<Source> sources;

    public PublicationDTO(Publication pub, List<LightStructure> structures) {
        this.id = pub.getId();
        this.type = pub.getType();
        this.title = pub.getTitle();
        this.subtitle = pub.getSubtitle();
        this.authors = pub.getAuthors();
        this.thesisDirectors = pub.getThesisDirectors();
        this.source = pub.getSource();
        this.summary = pub.getSummary();
        this.alternativeSummary = pub.getAlternativeSummary();
        this.identifiers = pub.getIdentifiers();
        this.thematics = pub.getThematics();
        this.structures = structures;
        this.link = pub.getLink();
        this.linkDocument = pub.getLinkDocument();
        this.lastSourceDate = pub.getLastSourceDate();
        this.publicationDate = pub.getPublicationDate();
        this.sources = pub.getSources();
    }
}
