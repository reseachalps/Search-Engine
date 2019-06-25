package eu.researchalps.api;

import eu.researchalps.db.model.*;
import eu.researchalps.db.model.publication.PublicationType;
import eu.researchalps.db.repository.ProjectRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.util.GeoUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.researchalps.api.util.ApiConstants;
import eu.researchalps.db.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by loic on 09/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Controller
@RequestMapping("/nomenclatures")
public class NomenclatureApi {

    public static final String BADGE = "BADGE";
    public static final String LEVEL = "LEVEL";
    public static final String ACTIVITY = "ACTIVITY";
    public static final String RELATION = "RELATION";
    public static final String PUBLICATION_TYPE = "PUBLICATION_TYPE";
    public static final String DEPARTEMENTS = "DEPARTEMENTS";
    public static final String INSTITUTIONS = "INSTITUTIONS";
    private Map<String, Map<String, String>> cached = new HashMap<>();
    private Date cacheExpiration = new Date();

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private StructureRepository structureRepository;

    private boolean cacheUpdateInProgress = false;

    public NomenclatureApi() throws IOException {
        final File f = new File("/tmp/nomenclatures.json");
        if (f.exists()) {
            final ObjectMapper om = new ObjectMapper();
            cached = om.readValue(f, om.getTypeFactory().constructMapType(HashMap.class, om.getTypeFactory().constructType(String.class), om.getTypeFactory().constructMapType(HashMap.class, String.class, String.class)));
            cacheExpiration = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
        }
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.GET, produces = ApiConstants.PRODUCES_JSON)
    public synchronized Map<String, Map<String, String>> get(@RequestParam(defaultValue = "false", required = false) boolean resetCache) {
        if (!resetCache && !cached.isEmpty()) {
            if (cacheExpiration.after(new Date())) {
                return cached;
            } else if (cacheUpdateInProgress) {
                return cached;
            } else {
                cacheUpdateInProgress = true;
                new Thread(this::updateCache).start();
                return cached;
            }
        }

        updateCache();
        return cached;
    }

    private void updateCache() {
        Map<String, Map<String, String>> cached = new HashMap<>();
        projectRepository.findAll().stream().forEach(project -> {
            // only call labels are indexed
            if (project.getCall() != null)
                putInCache("CALLS", project.getCallLabel(), project.getCallLabel(), cached);
        });

        for (Structure structure : structureRepository.findAll()) {
            List<Institution> institutions = structure.getInstitutions();
            if (institutions != null) {
                for (Institution institution : institutions) {
                    putInCache(INSTITUTIONS, institution.getId(), institution.getAcronym() == null ? institution.getLabel() : institution.getAcronym(), cached);
                }
            }
            List<Activity> activities = structure.getActivities();
            if (activities != null) {
                for (Activity activity : activities) {
                    ActivityTypeEnum type = activity.getActivityType();
                    if (type == null) continue;
                    putInCache(type.name(), activity.getCode(), activity.getLabel(), cached);
                }
            }
            List<Badge> badges = structure.getBadges();
            if (badges != null) {
                for (Badge badge : badges) {
                    putInCache(BADGE, badge.getCode(), badge.getLabel(), cached);
                }
            }
        }

        // levels
        for (LevelEnum levelEnum : LevelEnum.values()) {
            putInCache(LEVEL, String.valueOf(levelEnum.getValue()), levelEnum.getLabel(), cached);
        }

        // activity
        for (ActivityTypeEnum activityTypeEnum : ActivityTypeEnum.values()) {
            putInCache(ACTIVITY, activityTypeEnum.name(), activityTypeEnum.getLabel(), cached);
        }

        // relations
        for (RelationTypeEnum relationTypeEnum : RelationTypeEnum.values()) {
            putInCache(RELATION, relationTypeEnum.name(), relationTypeEnum.getLabel(), cached);
        }

        // relations
        for (PublicationType publicationType : PublicationType.values()) {
            putInCache(PUBLICATION_TYPE, publicationType.name(), publicationType.getLabelFR(), cached);
        }

        // relations
        for (Map.Entry<String, String> dpt : GeoUtils.DEPARTEMENT_LABELS.entrySet()) {
            putInCache(DEPARTEMENTS, dpt.getKey(), dpt.getValue(), cached);
        }

        this.cached = cached;

        cacheExpiration = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
        cacheUpdateInProgress = false;
    }

    private Map<String, String> currentCategory;
    private String previousCategory;
    private void putInCache(String category, String id, String label, Map<String, Map<String, String>> cached) {
        if (id == null) return;
        if (!category.equals(previousCategory)) {
            currentCategory = cached.computeIfAbsent(category, k -> new HashMap<>());
        }
        currentCategory.put(id, label);
        previousCategory = category;
    }
}
