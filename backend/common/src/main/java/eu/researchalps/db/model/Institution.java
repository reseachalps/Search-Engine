package eu.researchalps.db.model;

/**
 * Relation to institution
 *
 * The name of the relation is "code"
 *
 * Created by loic on 15/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class Institution {
    /**
     * Identifier of the institution called UAI (e.g. 0755361V)
     */
    private String id;
    /**
     * Alternative identifier of the institution (e.g. 18007003901803)
     */
    private String siret;
    /**
     * Full label (e.g. Institut national de la recherche agronomique)
     */
    private String label;
    /**
     * Acronym (e.g. INRA)
     */
    private String acronym;
    /**
     * URL to the institution base website (optional)
     */
    private String url;

    // BELLOW IS SPECIFIC OF THE ASSOCIATION BTW Structure & Institution

    /**
     * Association code between the host structure and this institution (e.g. UMR 8896)
     */
    private AssociationCode code;
    /**
     * The association type between the structure and the institution (e.g. Etablissement support)
     */
    private String type;
    /**
     * Year of the association
     */
    private Integer year;

    public Institution() {
    }

    public Institution(String id, String siret, String label, String acronym, String url, AssociationCode code, String type, Integer year) {
        this.id = id;
        this.siret = siret;
        this.label = label;
        this.acronym = acronym;
        this.url = url;
        this.code = code;
        this.type = type;
        this.year = year;
    }

    public String getId() {
        return id;
    }

    public String getSiret() {
        return siret;
    }

    public String getLabel() {
        return label;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getUrl() {
        return url;
    }

    public AssociationCode getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public Integer getYear() {
        return year;
    }

    public void setCode(AssociationCode code) {
        this.code = code;
    }

    public static class AssociationCode {
        private String type;
        private String number;
        private String normalized;

        public AssociationCode(String type, String number) {
            this.type = type.toUpperCase();
            this.number = number;
        }

        protected static String normalize(String type, String number) {
            type = type.replaceAll("_.*", "");
            return number != null ? type+" "+number.replaceAll("^0*", "") : null;
        }

        public AssociationCode(String code) {
            String[] split = code.trim().split("\\s+");
            assert split.length == 2;
            type = split[0].toUpperCase();
            number = split[1];
        }

        public AssociationCode() {
        }

        public String getType() {
            return type;
        }

        public String getNumber() {
            return number;
        }

        public String getNormalized() {
            return normalized;
        }

        public void normalize() {
            normalized = normalize(type, number);
        }

        @Override
        public String toString() {
            return type+" "+number;
        }
    }
}
