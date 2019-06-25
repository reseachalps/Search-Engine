package eu.researchalps.workflow.full;

import eu.researchalps.db.model.*;
import eu.researchalps.db.model.*;
import eu.researchalps.db.model.full.FSProject;
import eu.researchalps.db.model.full.FSPublication;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.repository.FullStructureCorrectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by loic on 12/05/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class FullStructureCorrecter {
    @Autowired
    private FullStructureCorrectionRepository repository;

    /**
     * Correct, if necessary, a full structure with whatever is provided by crowdsourcing data or forced data
     *
     * @param fs The full structure to correct
     */
    public boolean correctStructure(FullStructure fs) {
        if (fs == null || fs.getStructure() == null || fs.getStructure().getLinks() == null) {
            // safety first
            return false;
        }
        FullStructureCorrection correction = repository.findOne(fs.getId());
        if (correction == null) {
            // nothing to apply
            return false;
        }
        boolean doneSomething = false;
        String mainWebsiteId = fs.getStructure().getLinks().stream().filter(it -> it != null).filter(it -> it.getType() == LinkType.main).map(Link::getId).filter(it -> it != null).findFirst().orElse(null);
        Website mainWebsite = null;
        if (mainWebsiteId != null) {
            mainWebsite = fs.getWebsites().stream().filter(it -> it.getId().equals(mainWebsiteId)).findFirst().orElse(null);
        }
        if (mainWebsite != null) {
            if (correction.getActivityDescription() != null && !correction.getActivityDescription().equals(mainWebsite.getDescription())) {
                mainWebsite.setDescription(correction.getActivityDescription());
                doneSomething = true;
            }
            if (correction.getFacebookAccounts() != null) {
                Set<SocialAccount> toSet = correction.getFacebookAccounts().stream().map(it -> new SocialAccount(it, 1.0 / correction.getFacebookAccounts().size(), null)).collect(Collectors.toSet());
                if (!toSet.equals(mainWebsite.getFacebook())) {
                    mainWebsite.setFacebook(toSet);
                    doneSomething = true;
                }
            }
            if (correction.getTwitterAccounts() != null) {
                Set<SocialAccount> toSet = correction.getTwitterAccounts().stream().map(it -> new SocialAccount(it, 1.0 / correction.getTwitterAccounts().size(), null)).collect(Collectors.toSet());
                if (!toSet.equals(mainWebsite.getTwitter())) {
                    mainWebsite.setTwitter(toSet);
                    doneSomething = true;
                }
            }
        }
        doneSomething |= removeFromList(fs.getDetectedPublications(), correction.getRemovedPublications(), FSPublication::getId);
        doneSomething |= removeFromList(fs.getDetectedProjects(), correction.getRemovedProjects(), FSProject::getId);
        return doneSomething;
    }

    protected <E> boolean removeFromList(List<E> list, Set<String> toRemove, Function<E, String> idGetter) {
        if (toRemove == null || toRemove.isEmpty() || list == null || list.isEmpty()) {
            return false;
        }
        boolean doneSomething = false;
        for (Iterator<E> iterator = list.iterator(); iterator.hasNext(); ) {
            E publication = iterator.next();
            if (toRemove.contains(idGetter.apply(publication))) {
                iterator.remove();
                doneSomething = true;
            }
        }
        return doneSomething;
    }
}
