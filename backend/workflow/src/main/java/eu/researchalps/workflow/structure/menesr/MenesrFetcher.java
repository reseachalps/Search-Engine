package eu.researchalps.workflow.structure.menesr;

import com.datapublica.common.http.DPHttpClient;
import com.datapublica.common.http.DPHttpResponse;
import eu.researchalps.db.model.*;
import eu.researchalps.db.model.*;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.util.TolerantDateDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Service
public class MenesrFetcher {
    private static final Logger log = LoggerFactory.getLogger(MenesrFetcher.class);

    @Autowired
    private DPHttpClient client;

    @Autowired
    private MenesrConfiguration config;

    private final ObjectMapper om;

    public MenesrFetcher() {
        om = new ObjectMapper();
        om.findAndRegisterModules();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        om.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        om.enable(JsonParser.Feature.ALLOW_COMMENTS);

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Date.class, new TolerantDateDeserializer());
        om.registerModule(simpleModule);
    }

    public List<Structure> fetchPublicStructures() {
        log.info("Fetching public structures");
        List<Structure> fetch = fetch(config.getRNSRAction(), Structure.class);
        fetch.forEach(s -> checkAndCleanStructure(s, StructureKind.RNSR));
        return fetch;
    }

    public List<Structure> fetchCompanies() {
        log.info("Fetching private structures");
        List<Structure> fetch = fetch(config.getCompaniesAction(), Structure.class);
        fetch.forEach(s -> checkAndCleanStructure(s, StructureKind.COMPANY));
        return fetch;
    }

    public List<Project> fetchProjects() {
        log.info("Fetching projects from MENESR");
        List<Project> projects = fetch(config.getProjectsAction(), Project.class);
        for (Project project : projects) {
            // remove null thems
            if (project.getThemes() != null) {
                project.setThemes(project.getThemes().stream().filter(Objects::nonNull).collect(Collectors.toList()));
            }
        }

        return projects;
    }


    public List<Publication> fetchPublications() {
        log.info("Fetching publications from MENESR");
        List<Publication> publications = fetch(config.getPublicationAction(), Publication.class);

        // filter invalid publications
        publications = publications.stream().filter(this::checkAndCleanPublication).collect(Collectors.toList());

        return publications;
    }

    public long fetchPublications(Consumer<Publication> action) {
        log.info("Fetching publications");
        try(InputStream is = new FileInputStream(config.getPublicationAction())) {
            return fetchStream(is, Publication.class).filter(this::checkAndCleanPublication).peek(action).count();
        } catch (FileNotFoundException e) {
            log.info("Skipping publications (file not found {})", config.getPublicationAction().getAbsoluteFile().toString());
            return 0;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected <E> List<E> fetch(HttpGet action, Class<E> clazz) {
        try {
            DPHttpResponse response = client.execute(action);
            List<E> extracted = parseJsonList(clazz, response.text());
            log.debug("... fetch success");
            return extracted;
        } catch (IOException e) {
            // Should not happen if the application is correctly configured, crash the task
            throw new IllegalStateException(e);
        }
    }

    protected <E> List<E> parseJsonList(Class<E> clazz, String text) throws IOException {
        String json = text.trim();
        if (json.charAt(0) != '[') {
            // not a json array, hack everything
            json = "["+json.replace('\n', ',')+"]";
        }
        return om.readValue(json, om.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    protected <E> List<E> fetch(File path, Class<E> clazz) {
        try {
            if (!path.exists()) {
                return new LinkedList<>();
            }
            String json = IOUtils.toString(new FileInputStream(path), "UTF-8").trim();
            List<E> extracted = parseJsonList(clazz, json);
            log.debug("... read file success");
            return extracted;
        } catch (IOException e) {
            // Should not happen if the application is correctly configured, crash the task
            throw new IllegalStateException(e);
        }
    }

    protected <E> Stream<E> fetchStream(InputStream path, Class<E> clazz) throws IOException {
        final CollectionType valueType = om.getTypeFactory().constructCollectionType(List.class, clazz);
        BufferedReader reader = new BufferedReader(new InputStreamReader(path, "UTF-8"));
        return reader.lines().flatMap(txt -> {
            try {
                if (txt.charAt(0) == '[') {
                    return om.readValue(txt, valueType);
                } else {
                    return Stream.of(om.readValue(txt, clazz));
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Check consistency of the structure and clean it up
     */
    private boolean checkAndCleanPublication(Publication publication) {
        try {
            final String id = publication.computeId();
            if (id == null) {
                return false;
            }
            publication.setId(id);
            // Publication may have structures already defined during load. We store them in "validatedStructure". They will be merged with computed structures
            Set<String> structures = publication.getStructures();
            if (structures != null) {
                publication.setValidatedStructures(structures.stream().filter(Objects::nonNull).map(it -> {
                    // In case of SIRET, extract the siren
                    return it.length() == 14 ? it.substring(0, 9) : it;
                }).collect(Collectors.toSet()));
            }
        } catch (IllegalStateException e) {
            log.error("Skip Publication " + publication.getIdentifiers(), e);
            return false;
        }
        return true;
    }

    /**
     * Check consistency of the structure and clean it up
     *
     * @param structure
     * @throws IllegalArgumentException
     */
    private void checkAndCleanStructure(Structure structure, StructureKind defaultKind) throws IllegalArgumentException {
        // if kind is not given, set it to the default value
        if (structure.getKind() == null)
            structure.setKind(defaultKind);

        // Make sure that links actually have an id
        if (structure.getLinks() != null) {
            structure.getLinks().forEach(Link::computeId);
            // for main structure of type Private, get url to top page
//            if (StructureKind.COMPANY.equals(defaultKind))
//                structure.getLinks().stream().filter(l -> CrawlMode.FULL_DOMAIN.equals(l.getMode())).forEach(l -> l.setUrl(NormalizeURL.urlToTop(l.getUrl())));
        }
        // Make sure that links actually have an id
        if (structure.getInstitutions() != null) {
            for (Institution institution : structure.getInstitutions()) {
                Institution.AssociationCode code = institution.getCode();
                if (code == null) {
                    continue;
                }
                // No type = code is useless
                if (code.getType() == null) {
                    // Normalize null codes
                    institution.setCode(null);
                    continue;
                }
                code.normalize();
            }
        }

        // check activity
        if (structure.getActivities() != null)
            structure.getActivities().stream().filter(a -> a.getLabel() == null && a.getCode() == null)
                    .forEach(activity -> log.trace("Invalid activity for stucture " + structure.getId()));

        // Check value of the level
        if (structure.getLevel() != null && !LevelEnum.isValidValue(structure.getLevel()))
            throw new IllegalArgumentException("Invalid level " + structure.getLevel() + " for structure " + structure.getId());

        // cleanup address gps (if loaded as 0,0)
        if (structure.getAddress() != null && structure.getAddress().getGps() != null && structure.getAddress().getGps().getX() == 0 && structure.getAddress().getGps().getY() == 0) {
            structure.getAddress().setGps(null);
        }

        // cleanup null tags
        if (structure.getBadges() != null) {
            structure.setBadges(structure.getBadges().stream().filter(t -> t != null && t.getCode() != null).collect(Collectors.toList()));
        }
    }
}
