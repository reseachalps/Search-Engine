package eu.researchalps.search.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.StructureKind;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;

import java.text.SimpleDateFormat;
import java.util.List;

@Document(indexName = FullStructureIndex.INDEX, type = FullStructureIndex.TYPE)
@Mapping(mappingPath = "/eu/researchalps/config/scanr_mapping.json")
public class FullStructureIndex {
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String INDEX = "researchalps";
    public static final String TYPE = "fullstructure";

    public static final class FIELDS {
        public static final String ID = "id";
        public static final String LABEL = "label";
        public static final String ALIAS = "alias";
        public static final String KIND = "kind";
        public static final String PUBLIC_ENTITY = "publicEntity";
        public static final String LOGO = "logo";
        public static final String ACRONYM = "acronym";
        public static final String COMPANY_TYPE = "companyType";
        public static final String NATURE = "nature";
        public static final String LEVEL = "level";
        public static final String CREATION_YEAR = "creationYear";
        public static final String BADGES = "badges";
        public static final String ACTIVITY_LABELS = "activityLabels";
        public static final String RAW = "raw";
        public static final String MAIN_WEBSITE = "mainWebsite";
        public static final String CHILDREN_COUNT = "childrenCount";
        public static final String GRAPH_COUNT = "graphCount";
        public static final String SOURCES = "sources";
        public static final String PEOPLE_COUNT = "peopleCount";


        public static final class ADDRESS {
            public static final String PREFIX = "address.";
            public static final String COUNT = PREFIX.substring(0, PREFIX.length()-1) + "Count";
            public static final String POSTCODE = PREFIX + "postcode";
            public static final String URBAN_UNIT = PREFIX + "urbanUnit";
            public static final String DEPARTEMENT = PREFIX + "departement";
            public static final String CITY = PREFIX + "city";
            public static final String COUNTRY = PREFIX + "country";
            public static final String GPS = PREFIX + "gps";
            public static final String NUTS1 = PREFIX + "nuts1";
            public static final String NUTS2 = PREFIX + "nuts2";
            public static final String NUTS3 = PREFIX + "nuts3";
        }

        public static final class TYPE {
            public static final String ALL = "type";
            public static final String PREFIX = ALL + ".";
            public static final String CODE = PREFIX + "code";
            public static final String LABEL = PREFIX + "label";
        }


        public static final class LEADERS {
            public static final String PREFIX = "leaders.";
            public static final String COUNT = PREFIX.substring(0, PREFIX.length()-1) + "Count";
            public static final String COMPLETE_NAME = PREFIX + "completeName";
            public static final String TITLE = PREFIX + "title";
        }

        public static final class NAF {
            public static final String PREFIX = "naf.";
            public static final String COUNT = PREFIX.substring(0, PREFIX.length()-1) + "Count";
            public static final String CODE = PREFIX + "code";
        }

        public static final class DOMAINE {
            public static final String PREFIX = "domaine.";
            public static final String COUNT = PREFIX.substring(0, PREFIX.length()-1) + "Count";
            public static final String CODE = PREFIX + "code";
        }

        public static final class ERC {
            public static final String PREFIX = "erc.";
            public static final String COUNT = PREFIX.substring(0, PREFIX.length()-1) + "Count";
            public static final String CODE = PREFIX + "code";
        }

        public static final class INSTITUTIONS {
            public static final String PREFIX = "institutions.";
            public static final String COUNT = PREFIX.substring(0, PREFIX.length()-1) + "Count";
            public static final String ID = PREFIX + "id";
            public static final String CODE = PREFIX + "code";
            public static final String LABEL = PREFIX + "label";
            public static final String ACRONYM = PREFIX + "acronym";
        }

        public static final class PROJECTS {
            public static final String PREFIX = "projects.";
            public static final String COUNT = PREFIX.substring(0, PREFIX.length()-1) + "Count";
            public static final String ID = PREFIX + "id";
            public static final String ACRONYM = PREFIX + "acronym";
            public static final String LABEL = PREFIX + "label";
            public static final String DESCRIPTION = PREFIX + "description";
            public static final String CALL = PREFIX + "call";
            public static final String CALL_LABEL = PREFIX + "callLabel";
        }

        public static final class PUBLICATIONS {
            public static final String PREFIX = "publications.";
            public static final String COUNT = PREFIX.substring(0, PREFIX.length()-1) + "Count";
            public static final String TITLE = PREFIX + "title";
            public static final String SUBTITLE = PREFIX + "subtitle";
            public static final String AUTHORS = PREFIX + "authors";
            public static final String SUMMARY = PREFIX + "summary";
            public static final String ALTERNATIVE_SUMMARY = PREFIX + "alternativeSummary";
        }

        public static final class WEBSITE {
            public static final String PREFIX = "websiteContents.";
            public static final String BASE_URL = PREFIX + "baseURL";

            public static final class WEBPAGES {
                public static final String PREFIX = WEBSITE.PREFIX + "webPages.";
                public static final String CONTENT = PREFIX + "content";
            }
        }
    }

    @Id
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String id;

    @Field(type = FieldType.Object)
    protected List<WebsiteIndex> websiteContents;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "structure_name", searchAnalyzer = "structure_name")
    private String label;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "structure_name", searchAnalyzer = "structure_name")
    private List<String> alias;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private StructureKind kind;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String publicEntity;

    @Field(type = FieldType.Object)
    private AddressIndex address;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String logo;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "structure_name", searchAnalyzer = "structure_name")
    private List<String> acronym;

    @Field(type = FieldType.Object)
    private StructureTypeIndex type;

    @Field(type = FieldType.Object)
    private CompanyTypeIndex companyType;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String nature;

    @Field(type = FieldType.Integer)
    private Integer level;

    @Field(type = FieldType.Integer)
    private Integer creationYear;

    @Field(type = FieldType.Object)
    private List<PersonIndex> leaders;
    @Field(type = FieldType.Integer)
    private int leadersCount;
    @Field(type = FieldType.Integer)
    private int peopleCount;

    @Field(type = FieldType.Object)
    private List<ActivityIndex> naf;
    @Field(type = FieldType.Integer)
    private int nafCount;

    @Field(type = FieldType.Object)
    private List<ActivityIndex> erc;
    @Field(type = FieldType.Integer)
    private int ercCount;

    @Field(type = FieldType.Object)
    private List<ActivityIndex> domaine;
    @Field(type = FieldType.Integer)
    private int domaineCount;

    @Field(type = FieldType.Object)
    private List<InstitutionIndex> institutions;
    @Field(type = FieldType.Integer)
    private int institutionsCount;

    @Field(type = FieldType.Object)
    private List<ProjectIndex> projects;
    @Field(type = FieldType.Integer)
    private int projectsCount;

    @Field(type = FieldType.Object)
    private List<PublicationIndex> publications;
    @Field(type = FieldType.Integer)
    private int publicationsCount;

    /**
     * Badge codes
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private List<String> badges;
    @Field(type = FieldType.Integer)
    private int badgesCount;

    @Field(type = FieldType.String)
    private List<String> activityLabels;
    @Field(type = FieldType.Integer)
    private int activityLabelsCount;

    @Field(type = FieldType.String, analyzer = "word_cloud")
    private String raw;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String mainWebsite;

    @Field(type = FieldType.Integer)
    private int childrenCount = 0;

    @Field(type = FieldType.Integer)
    private int graphCount = 0;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private List<String> sources;

    public FullStructureIndex() {
    }

    public FullStructureIndex(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getAlias() {
        return alias;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public StructureKind getKind() {
        return kind;
    }

    public void setKind(StructureKind kind) {
        this.kind = kind;
    }

    public AddressIndex getAddress() {
        return address;
    }

    public void setAddress(AddressIndex address) {
        this.address = address;
    }

    public String getPublicEntity() {
        return publicEntity;
    }

    public void setPublicEntity(String publicEntity) {
        this.publicEntity = publicEntity;
    }

    public List<WebsiteIndex> getWebsiteContents() {
        return websiteContents;
    }

    public void setWebsiteContents(List<WebsiteIndex> websiteContents) {
        this.websiteContents = websiteContents;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<String> getAcronym() {
        return acronym;
    }

    public void setAcronym(List<String> acronym) {
        this.acronym = acronym;
    }

    public StructureTypeIndex getType() {
        return type;
    }

    public void setType(StructureTypeIndex type) {
        this.type = type;
    }

    public CompanyTypeIndex getCompanyType() {
        return companyType;
    }

    public void setCompanyType(CompanyTypeIndex companyType) {
        this.companyType = companyType;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getCreationYear() {
        return creationYear;
    }

    public void setCreationYear(Integer creationYear) {
        this.creationYear = creationYear;
    }

    public List<PersonIndex> getLeaders() {
        return leaders;
    }

    public void setLeaders(List<PersonIndex> leaders) {
        this.leaders = leaders;
        this.leadersCount = leaders != null ? leaders.size() : 0;
    }

    public List<ActivityIndex> getNaf() {
        return naf;
    }

    public void setNaf(List<ActivityIndex> naf) {
        this.naf = naf;
        this.nafCount = naf != null ? naf.size() : 0;
    }

    public List<ActivityIndex> getErc() {
        return erc;
    }

    public void setErc(List<ActivityIndex> erc) {
        this.erc = erc;
        this.ercCount = erc != null ? erc.size() : 0;
    }

    public List<ActivityIndex> getDomaine() {
        return domaine;
    }

    public void setDomaine(List<ActivityIndex> domaine) {
        this.domaine = domaine;
        this.domaineCount = domaine != null ? domaine.size() : 0;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
        this.badgesCount = badges != null ? badges.size() : 0;
    }

    public List<InstitutionIndex> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(List<InstitutionIndex> institutions) {
        this.institutions = institutions;
        this.institutionsCount = institutions != null ? institutions.size() : 0;
    }

    public void setActivityLabels(List<String> activityLabels) {
        this.activityLabels = activityLabels;
        this.activityLabelsCount = activityLabels != null ? activityLabels.size() : 0;
    }

    public List<String> getActivityLabels() {
        return activityLabels;
    }

    public void setProjects(List<ProjectIndex> projects) {
        this.projects = projects;
        this.projectsCount = projects != null ? projects.size() : 0;
    }

    public List<ProjectIndex> getProjects() {
        return projects;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public List<PublicationIndex> getPublications() {
        return publications;
    }

    public void setPublications(List<PublicationIndex> publications) {
        this.publications = publications;
        this.publicationsCount = publications != null ? publications.size() : 0;
    }

    public String getMainWebsite() {
        return mainWebsite;
    }

    public void setMainWebsite(String mainWebsite) {
        this.mainWebsite = mainWebsite;
    }

    public int getLeadersCount() {
        return leadersCount;
    }

    public int getNafCount() {
        return nafCount;
    }

    public int getErcCount() {
        return ercCount;
    }

    public int getDomaineCount() {
        return domaineCount;
    }

    public int getInstitutionsCount() {
        return institutionsCount;
    }

    public int getProjectsCount() {
        return projectsCount;
    }

    public int getPublicationsCount() {
        return publicationsCount;
    }

    public int getBadgesCount() {
        return badgesCount;
    }

    public int getActivityLabelsCount() {
        return activityLabelsCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setGraphCount(int graphCount) {
        this.graphCount = graphCount;
    }

    public int getGraphCount() {
        return graphCount;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
