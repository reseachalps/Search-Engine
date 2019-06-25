package eu.researchalps.workflow.search;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.crawl.CrawlStoreService;
import eu.researchalps.db.model.*;
import eu.researchalps.db.model.*;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.repository.PublicationRepository;
import eu.researchalps.search.model.ActivityIndex;
import eu.researchalps.search.model.AddressIndex;
import eu.researchalps.search.model.CompanyTypeIndex;
import eu.researchalps.search.model.FullStructureIndex;
import eu.researchalps.search.model.InstitutionIndex;
import eu.researchalps.search.model.PersonIndex;
import eu.researchalps.search.model.ProjectIndex;
import eu.researchalps.search.model.PublicationIndex;
import eu.researchalps.search.model.StructureTypeIndex;
import eu.researchalps.search.model.WebPageIndex;
import eu.researchalps.search.model.WebsiteIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FullStructureMapper {
    private static final int RAW_MINIMUM_SIZE = 4000;
    private static final Logger log = LoggerFactory.getLogger(FullStructureMapper.class);
    public static final String UNDEFINED = "undefined";

    @Autowired
    private CrawlStoreService crawlStore;

    @Autowired
    private PublicationRepository publicationRepository;


    public FullStructureIndex toFullStructure(FullStructure fullStructure) {
        FullStructureIndex fullStructureIndex = new FullStructureIndex(fullStructure.getId());

        fullStructureIndex.setLabel(fullStructure.getStructure().getLabel());
        fullStructureIndex.setKind(fullStructure.getStructure().getKind());

        fullStructureIndex.setPublicEntity(fullStructure.getStructure().getType() == null || fullStructure.getStructure().getType().getPublicEntity() == null ? UNDEFINED : String.valueOf(fullStructure.getStructure().getType().getPublicEntity()));

        fullStructureIndex.setAlias(new LinkedList<>());
        if (fullStructure.getStructure().getAlias() != null) {
            fullStructure.getStructure().getAlias().stream().filter(Objects::nonNull).forEach(fullStructureIndex.getAlias()::add);
        }

        if (fullStructure.getStructure().getAlternativeNames() != null) {
            fullStructure.getStructure().getAlternativeNames().stream().map(Name::getLabel).filter(Objects::nonNull).forEach(fullStructureIndex.getAlias()::add);
        }

        // Addresse
        if (fullStructure.getStructure().getAddress() != null) {
            fullStructureIndex.setAddress(new AddressIndex(fullStructure.getStructure().getAddress()));
        }

        // Logo if none, try to use twitter logo
        if (fullStructure.getStructure().getLogo() != null)
            fullStructureIndex.setLogo(fullStructure.getStructure().getLogo());
        else if (fullStructure.getWebsites() != null && fullStructure.getWebsites().size() > 0) {
            Website website = fullStructure.getWebsites().get(0);
            if (website.getTwitter() != null && website.getTwitter().size() > 0) {
                SocialAccount socialAccount = website.getTwitter().iterator().next();
                if (socialAccount != null)
                    fullStructureIndex.setLogo(socialAccount.getProfilePictureUrl());
            }
        }

        fullStructureIndex.setAcronym(fullStructure.getStructure().getAcronyms());
        if (fullStructure.getStructure().getType() != null)
            fullStructureIndex.setType(new StructureTypeIndex(fullStructure.getStructure().getType()));
        if (fullStructure.getStructure().getCompanyType() != null)
            fullStructureIndex.setCompanyType(new CompanyTypeIndex(fullStructure.getStructure().getCompanyType()));
        fullStructureIndex.setNature(fullStructure.getStructure().getNature());
        fullStructureIndex.setLevel(fullStructure.getStructure().getLevel());
        fullStructureIndex.setCreationYear(fullStructure.getStructure().getCreationYear());

        // Badges
        if (fullStructure.getStructure().getBadges() != null) {
            fullStructureIndex.setBadges(fullStructure.getStructure().getBadges().stream().filter(Objects::nonNull).map(Badge::getCode).collect(Collectors.toList()));
        }

        // Leaders
        if (fullStructure.getStructure().getLeaders() != null) {
            fullStructureIndex.setLeaders(
                    fullStructure.getStructure().getLeaders().stream()
                            .filter(Objects::nonNull)
                            .map(PersonIndex::new).collect(Collectors.toList()));
            fullStructureIndex.setPeopleCount(fullStructureIndex.getLeadersCount()+fullStructure.getStructure().getStaff().size());
        }

        // Institutions
        if (fullStructure.getStructure().getInstitutions() != null && fullStructure.getStructure().getInstitutions().size() > 0) {
            fullStructureIndex.setInstitutions(
                    fullStructure.getStructure().getInstitutions().stream()
                            .filter(Objects::nonNull)
                            .map(InstitutionIndex::new).collect(Collectors.toList()));
        }

        // Projects
        if (fullStructure.getProjects() != null && fullStructure.getProjects().size() > 0) {
            fullStructureIndex.setProjects(
                    fullStructure.getProjects().stream()
                            .filter(Objects::nonNull)
                            .map(ProjectIndex::new).collect(Collectors.toList()));
        }

        if (fullStructure.getChildren() != null) {
            fullStructureIndex.setChildrenCount(fullStructure.getChildren().size());
        }

        if (fullStructure.getGraph() != null) {
            fullStructureIndex.setGraphCount(fullStructure.getGraph().size());
        }

        // Publications
        if (fullStructure.getPublications() != null && fullStructure.getPublications().size() > 0) {
            fullStructureIndex.setPublications(
                    publicationRepository.findByStructures(fullStructure.getId()).stream()
                            .map(PublicationIndex::new)
                            .collect(Collectors.toList())
            );
        }


        List<Activity> activities = fullStructure.getStructure().getActivities();
        if (activities != null) {
            // Activities: split by type
            Map<ActivityTypeEnum, List<ActivityIndex>> activitiesByType = activities.stream()
                    .filter(Objects::nonNull)
                    .filter(activity -> activity.getActivityType() != null)
                    .map(ActivityIndex::new).collect(Collectors.groupingBy(ActivityIndex::getType));
            for (ActivityTypeEnum type : activitiesByType.keySet()) {
                switch (type) {
                    case DOMAINE:
                        fullStructureIndex.setDomaine(activitiesByType.get(type));
                        break;
                    case ERC:
                        fullStructureIndex.setErc(activitiesByType.get(type));
                        break;
                    case NAF:
                        fullStructureIndex.setNaf(activitiesByType.get(type));
                        break;
                }
            }
            // Activities: index Activity Label
            fullStructureIndex.setActivityLabels(activities.stream().map(Activity::getLabel).filter(Objects::nonNull).collect(Collectors.toList()));
        }


        // main website
        if (fullStructure.getStructure().getLinks() != null) {
            fullStructure.getStructure().getLinks().stream()
                    .min(Comparator.comparing(Link::getType, Comparator.nullsLast(Comparator.naturalOrder())))
                    .ifPresent(link -> fullStructureIndex.setMainWebsite(link.getUrl()));
        }

        // Website and pages
        if (fullStructure.getWebsites() != null && fullStructure.getWebsites().size() > 0) {
            fullStructureIndex.setWebsiteContents(fullStructure.getWebsites().stream().map(website -> {
                // Get crawl id
                List<WebPageIndex> pages = null;
                if (website.getId() == null) {
                    log.error("No crawlID for structure [" + fullStructure.getId() + "] with url [" + website.getId() + "]");
                    return null;
                }
                pages = this.crawlStore.getCrawlTexts(website.getId()).stream().map(page -> new WebPageIndex(page.getTitle(), page.getContent())).collect(Collectors.toList());
                log.trace("Got crawl " + website.getId() + " from crawl store with " + pages.size() + " pages.");
                return new WebsiteIndex(website.getBaseURL(), website.getCrawlMode(), pages);
            }).filter(Objects::nonNull).collect(Collectors.toList()));
        }

        // Build a raw String for word cloud
        StringBuilder raw = new StringBuilder();

        // Add free form activities
        if (activities != null) {
            activities.stream().filter(a -> a.getActivityType() == null).forEach(a -> raw.append(a.getLabel()).append(" "));
        }

        if (fullStructureIndex.getProjects() != null)
            fullStructureIndex.getProjects().forEach(project -> {
                raw.append(project.getLabel()).append(" ");
                raw.append(project.getDescription()).append(" ");
            });
        if (fullStructureIndex.getPublications() != null)
            fullStructureIndex.getPublications().forEach(publication -> {
                raw.append(publication.getTitle()).append(" ");
                raw.append(publication.getSubtitle()).append(" ");
                raw.append(publication.getSummary()).append(" ");
            });

        if (raw.length() < RAW_MINIMUM_SIZE && fullStructureIndex.getWebsiteContents() != null)
            fullStructureIndex.getWebsiteContents().stream().flatMap(c -> c.getWebPages().stream()).forEach(webPage -> raw.append(webPage.getContent()).append(" "));
        fullStructureIndex.setRaw(raw.toString());

        fullStructureIndex.setSources(fullStructure.getStructure().getSources().stream().map(Source::getLabel).collect(Collectors.toList()));


        return fullStructureIndex;
    }

}
