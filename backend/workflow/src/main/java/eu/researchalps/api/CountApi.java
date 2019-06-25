package eu.researchalps.api;

import eu.researchalps.db.repository.FullStructureRepository;
import eu.researchalps.db.repository.ProjectRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.db.repository.WebsiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@Controller
@RequestMapping("/services/counts")
public class CountApi {
    @Autowired
    private StructureRepository structure;
    @Autowired
    private WebsiteRepository website;
    @Autowired
    private FullStructureRepository fullStructure;
    @Autowired
    private ProjectRepository project;

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.GET)
    public CountReport get() {
        return new CountReport(structure.count(), website.count(), project.count(), fullStructure.count());
    }

    public static class CountReport {
        public long structures;
        public long websites;
        public long projects;
        public long fullStructures;

        public CountReport(long structures, long websites, long projects, long fullStructures) {
            this.structures = structures;
            this.websites = websites;
            this.projects = projects;
            this.fullStructures = fullStructures;
        }
    }
}
