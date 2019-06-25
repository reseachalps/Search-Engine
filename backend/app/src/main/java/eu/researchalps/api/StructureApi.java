package eu.researchalps.api;

import eu.researchalps.api.exception.NotFoundException;
import eu.researchalps.api.util.ApiUtil;
import eu.researchalps.db.model.Link;
import eu.researchalps.db.model.LinkType;
import eu.researchalps.db.model.SocialAccount;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.model.full.Keyword;
import eu.researchalps.db.repository.FullStructureRepository;
import eu.researchalps.db.repository.WordStemMappingRepository;
import eu.researchalps.search.repository.FullStructureSearchRepository;
import eu.researchalps.service.ScreenshotStorageService;
import eu.researchalps.api.util.ApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


@Controller
@RequestMapping("/structures/")
public class StructureApi {

    public static final Comparator<Link> LINK_COMPARATOR = Comparator.comparing(Link::getType, Comparator.nullsLast(Comparator.naturalOrder()));
    @Autowired
    private WordStemMappingRepository wordStemMappingRepository;
    @Autowired
    private FullStructureRepository fullStructureRepository;
    @Autowired
    private FullStructureSearchRepository fullStructureSearchRepository;
    @Autowired
    private ScreenshotStorageService screenshotStorageService;

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = ApiConstants.PRODUCES_JSON)
    public FullStructure get(@PathVariable String id) throws IOException {
        FullStructure structure = ApiUtil.fetchOrThrow(fullStructureRepository, "structure", id);

        // if the associated structures have no logo we try to get the twitter url
        List<String> twitterLogoToFetchStructures = new ArrayList<>();
        if (structure.getParents() != null) {
            structure.getParents().forEach(lightStructure -> {
                if (lightStructure.getLogo() == null) twitterLogoToFetchStructures.add(lightStructure.getId());
            });
        }
        structure.getChildren().forEach(lightStructure -> {
            if (lightStructure.getLogo() == null) twitterLogoToFetchStructures.add(lightStructure.getId());
        });
        // fetch the logos in db
        if (twitterLogoToFetchStructures.size() > 0) {
            Map<String, String> logoByStructureId = new HashMap<>();
            fullStructureRepository.findByIdsLightWithTwitterLogo(twitterLogoToFetchStructures).forEach(fullStructure -> {
                if (fullStructure.getWebsites() != null
                        && fullStructure.getWebsites().size() > 0
                        && fullStructure.getWebsites().get(0).getTwitter() != null
                        && fullStructure.getWebsites().get(0).getTwitter().size() > 0
                        ) {
                    SocialAccount next = fullStructure.getWebsites().get(0).getTwitter().iterator().next();
                    if (next.getProfilePictureUrl() != null)
                        logoByStructureId.put(fullStructure.getId(), next.getProfilePictureUrl());
                }
            });
            // enrich the corresponding light structures
            if (structure.getParents() != null) {
                structure.getParents().forEach(lightStructure -> {
                    if (lightStructure.getLogo() == null)
                        lightStructure.setLogo(logoByStructureId.get(lightStructure.getId()));
                });
            }
            structure.getChildren().forEach(lightStructure -> {
                if (lightStructure.getLogo() == null)
                    lightStructure.setLogo(logoByStructureId.get(lightStructure.getId()));
            });
        }

        return structure;
    }
    @ResponseBody
    @RequestMapping(value = "{id}/keywords", method = RequestMethod.GET, produces = ApiConstants.PRODUCES_JSON)
    public List<Keyword> keywords(@PathVariable String id, HttpServletResponse response) throws IOException {
        //TODO: should be computed as part of the workflow
        return fullStructureSearchRepository.computeWordCloud(id, wordStemMappingRepository.findOne(id));
    }

    @ApiIgnore
    @ResponseBody
    @RequestMapping(value = "{id}/screenshot", method = RequestMethod.GET, produces = "image/png")
    public void screenshot(@PathVariable String id, HttpServletResponse response) throws IOException {
        FullStructure fs = fullStructureRepository.findOne(id, FullStructureField.STRUCTURE);
        List<Link> links = fs.getStructure().getLinks();
        if (links == null || links.isEmpty()) {
            redirectToDefaultThumbnail(response);
            return;
        }
        Optional<Link> link = links.stream()
                .filter(it -> it.getId() != null)
                .filter(
                        it -> it.getType() != LinkType.hceres &&
                                it.getType() != LinkType.wikipedia &&
                                it.getType() != LinkType.rnsr
                )
                .sorted(LINK_COMPARATOR)
                .filter(it -> screenshotStorageService.exists(it.getId()))
                .findFirst();

        if (!link.isPresent()) {
            redirectToDefaultThumbnail(response);
            return;
        }
        try {
            byte[] bytes = screenshotStorageService.get(link.get().getId());
            response.setStatus(200);
            response.getOutputStream().write(bytes);
        } catch (NotFoundException nfe) {
            redirectToDefaultThumbnail(response);
        }
    }

    public static void redirectToDefaultThumbnail(HttpServletResponse response) {
        response.setStatus(302);
        response.setHeader("Location", "/app/assets/img/ReSearchAlpsLogoGrey.png");
    }
}
