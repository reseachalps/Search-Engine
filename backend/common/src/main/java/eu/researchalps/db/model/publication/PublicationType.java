package eu.researchalps.db.model.publication;

/**
 * type of publication
 */
public enum PublicationType {
    ARTICLE("Article dans une revue"),
    COMMUNICATION("Communication dans un congrès"),
    POSTER("Poster"),
    BOOK("Ouvrage (y compris édition critique et traduction)"),
    CHAPTER("Chapitre d'ouvrage"),
    PROCEEDINGS("Direction d'ouvrage, Proceedings"),
    REPORT("Rapport de recherche"),
    PATENT("Brevet"),
    THESIS("Thèse"),
    HDR("HDR"),
    LECTURE("Cours et mémoire de cours"),
    DISSERTATION("Mémoire d'étudiant"),
    RESEARCH_DATA("Donnée de recherche"),
    OTHER_PUBLISHED("Autre publication"),
    OTHER_UNPUBLISHED("Pré-publication, Document de travail");

    private String labelFR;

    PublicationType(String labelFR) {
        this.labelFR = labelFR;
    }

    public String getLabelFR() {
        return labelFR;
    }
}
