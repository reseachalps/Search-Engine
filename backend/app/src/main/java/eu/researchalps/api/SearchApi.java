package eu.researchalps.api;

import eu.researchalps.api.util.ApiUtil;
import eu.researchalps.api.util.UserLocale;
import eu.researchalps.db.model.Project;
import eu.researchalps.db.model.SearchEvent;
import eu.researchalps.db.model.Structure;
import eu.researchalps.db.model.StructureKind;
import eu.researchalps.db.model.full.Keyword;
import eu.researchalps.db.repository.ProjectRepository;
import eu.researchalps.db.repository.SearchEventRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.search.model.AddressIndex;
import eu.researchalps.search.model.request.GeoBoundingBox;
import eu.researchalps.search.model.request.GeoDistance;
import eu.researchalps.search.model.request.SearchRequest;
import eu.researchalps.search.model.response.FullStructureResult;
import eu.researchalps.search.model.response.GeoBoxResult;
import eu.researchalps.search.model.response.GeoResult;
import eu.researchalps.search.model.response.SearchResponse;
import eu.researchalps.search.repository.FullStructureSearchRepository;
import eu.researchalps.util.ExcelExport;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.researchalps.api.util.ApiConstants;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.common.geo.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/")
public class SearchApi {
    private static final Logger log = LoggerFactory.getLogger(SearchApi.class);
    private static final String BASE_URL = "http://researchalps.data-publica.com";
    public static final int EXPORT_LIMIT = 1000;

    @Autowired
    private FullStructureSearchRepository fullStructureIndexRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private SearchEventRepository searchEventRepository;


    private final ObjectMapper objectMapper = new ObjectMapper();


    @ResponseBody
    @RequestMapping(value = "/structures/search", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public SearchResponse searchExtended(@RequestBody SearchRequest searchRequest) throws IOException {
        // set the locales inside the searchRequest to handle future i18n
        searchRequest.setLocales(new LinkedList<>());

        final SearchResponse companySearchResponse = fullStructureIndexRepository.searchFullStructureWithQuery(searchRequest);
        // log search in DB
        this.searchEventRepository.save(new SearchEvent(searchRequest.getQuery(), searchRequest.getPage(), searchRequest));
        return companySearchResponse;
    }


    /**
     * Excel export of the search
     *
     * @param searchRequestAsString
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/structures/search/export", method = RequestMethod.GET, produces = "application/vnd.ms-excel")
    public void exportSearch(@RequestParam(value = "r", required = false) String searchRequestAsString,
                             @RequestParam(value = "rp", required = false) String searchURLAsString,
                             HttpServletResponse response) throws IOException {
        log.info("Exporting request " + decode(searchRequestAsString));

        Date today = new Date();
        final String searchURL = BASE_URL
                + (StringUtils.hasLength(searchURLAsString) ? decode(searchURLAsString) : "");

        // deserialize the front end request
        SearchRequest searchRequest = StringUtils.hasLength(searchRequestAsString) ? resolveFrontString(searchRequestAsString, SearchRequest.class) : new SearchRequest();

        // set header
        response.setHeader("Content-Disposition", "attachment; filename=researchalps_" + DateFormat.getDateInstance(DateFormat.SHORT).format(new Date()) + ".xls");

        ExcelExport workbook = new ExcelExport().sheet("ResearchAlps");
        // Headers
        String[] headers = {
                "Identifier",
                "Name",
                "Acronym",
                "Alias",
                "Type",
                "Type Label",
                "Main Website",
                "Postcode",
                "City",
                "Urban Unit",
                "Latitude",
                "Longitude",
                "Projects",
                "Publications",
                "Children",
                "Connections",
                "Staff",
                "Sources",
                "ResearchAlps Link",
                "Export Date",
                "Search Query",
        };
        workbook.headers(headers);

        List<FullStructureResult> structures = fullStructureIndexRepository.getFirstFullStructureWithQuery(searchRequest, EXPORT_LIMIT);
        structures.forEach(structure -> {
            AddressIndex address = structure.getAddress();
            String stucture_link = BASE_URL + "/structure/" + structure.getId();
            boolean isCompany = structure.getKind() == StructureKind.COMPANY;

            String type = isCompany ? (structure.getCompanyType() != null ? structure.getCompanyType().getCode() : "") : (structure.getType() != null ? structure.getType().getCode() : "");
            // strangely, for companies, we use label of type (and not company type)
            String typeLabel = structure.getType() != null ? structure.getType().getLabel() : "";

            workbook.row()
                    .cell(structure.getId())
                    .cell(structure.getLabel())
                    .cell((structure.getAcronym() == null || structure.getAcronym().isEmpty() ? "" : structure.getAcronym().get(0)))
                    .cell(CollectionUtils.isEmpty(structure.getAlias()) ? "" : structure.getAlias().get(0))
                    .cell(type)
                    .cell(typeLabel)
                    .cell(structure.getMainWebsite())
                    .cell(address == null ? "" : address.getPostcode())
                    .cell(address == null ? "" : address.getCity())
                    .cell(address == null ? "" : address.getUrbanUnit())
                    .cell((address == null || address.getGps() == null) ? "" : "" + address.getGps().getLat())
                    .cell((address == null || address.getGps() == null) ? "" : "" + address.getGps().getLon())
                    .cell(structure.getProjectsCount())
                    .cell(structure.getPublicationsCount())
                    .cell(structure.getChildrenCount())
                    .cell(structure.getGraphCount())
                    .cell(structure.getPeopleCount())
                    .cell(structure.getSources() != null ? String.join(",", structure.getSources()) : "")
                    .linkedCell(stucture_link)
                    .cell(today)
                    .linkedCell(searchURL)
            ;
        });
        workbook.autoResize();
        workbook.write(response.getOutputStream());
    }


    @ResponseBody
    @ApiOperation(value = "Find structures near another structure (default to 20 nearest structure, max 100 structures)", notes = "Distance in km")
    @RequestMapping(value = "/structures/near/{id}", method = RequestMethod.GET, produces = ApiConstants.PRODUCES_JSON)
    public List<Structure> searchStructureClosedTo(@PathVariable String id, @RequestParam double distance, @RequestParam(defaultValue = "20") int nb) throws IOException {
        Structure structure = ApiUtil.fetchOrThrow(structureRepository, "structure", id);
        nb = Math.min(nb, 100);
        if (structure.getAddress() != null && structure.getAddress().getGps() != null) {
            List<Structure> byAddressGpsNear = structureRepository.findByAddressGpsNear(structure.getAddress().getGps(), new Distance(distance, Metrics.KILOMETERS), new PageRequest(0, nb));
            // filter self
            return byAddressGpsNear.stream().filter(s -> !(id.equals(s.getId()))).collect(Collectors.toList());
        } else return null;

    }

    /**
     * Return all results for the current search (no paging) with geo coordinates (for the map)
     *
     * @param searchRequest
     * @return
     * @throws IOException
     */
    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "structures/search/georesults", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public SearchResponse geoSearch(@RequestBody SearchRequest searchRequest) throws IOException {
        // set the locales inside the searchRequest to handle future i18n
        searchRequest.setLocales(new LinkedList<>());

        // force the nb of reponse to 0
        return fullStructureIndexRepository.geoElementsWithQuery(searchRequest);
    }

    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "structures/search/geo/bbox", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public List<GeoBoxResult> geoSearch(@RequestBody GeoBoundingBox request) throws IOException {
        if (request.bottomRight == null) {
            throw new IllegalArgumentException("bottomRight is not set");
        }
        if (request.topLeft == null) {
            throw new IllegalArgumentException("topLeft is not set");
        }
        return fullStructureIndexRepository.geoBoxResults(request.topLeft, request.bottomRight, request.searchRequest);
    }

    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "structures/search/geo/distance", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public List<GeoResult> geoSearch(@RequestBody GeoDistance request) throws IOException {
        if (request.center == null) {
            throw new IllegalArgumentException("center is not set");
        }
        if (request.distance == null) {
            throw new IllegalArgumentException("distance is not set");
        }
        return fullStructureIndexRepository.geoResults(request.center.lat(), request.center.lon(), request.distance, request.searchRequest);
    }

    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "structures/search/geo/polygon", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public List<GeoResult> geoSearch(@RequestBody PolygonRequest polygonRequest) throws IOException {
        if (polygonRequest.polygon.size() <= 2) {
            throw new IllegalArgumentException("Polygon has an empty area");
        }
        return fullStructureIndexRepository.geoResults(polygonRequest.polygon, polygonRequest.searchRequest);
    }

    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "structures/search/geo/label", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public List<GeoResult> geoSearch(@RequestParam String query, @RequestParam(required = false, defaultValue = "10") int size) {
        if (query.isEmpty()) {
            return Collections.emptyList();
        }
        return fullStructureIndexRepository.fastSearch(query, size);
    }

    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "structures/search/geo/id/{id}", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public GeoResult findById(@PathVariable String id) {
        return fullStructureIndexRepository.findOneGeo(id);
    }

    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "/project/search", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public List<Project> searchProject(@UserLocale List<Locale> locales, @RequestBody String query) throws IOException {
        if (StringUtils.hasLength(query))
            return projectRepository.findByAcronymLike("^" + query, 20);
        else return new ArrayList<>();
    }


    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET, produces = ApiConstants.PRODUCES_JSON)
    public Project getProject(@UserLocale List<Locale> locales, @PathVariable String id) throws IOException {
        return ApiUtil.fetchOrThrow(projectRepository, "project", id);

    }

    @ResponseBody
    @RequestMapping(value = "/structures/search/tagcloud", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public List<Keyword> tagCloud(@RequestBody SearchRequest searchRequest) throws IOException {
        return fullStructureIndexRepository.computeWordCloud(searchRequest);
    }


    /**
     * @param query
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws JsonParseException
     * @throws JsonMappingException
     */
    private <T> T resolveFrontString(String query, Class<T> clazz) throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
        return objectMapper.readValue(decode(query), clazz);
    }

    private String decode(String query) throws UnsupportedEncodingException {
        if (!StringUtils.hasLength(query)) return "";
        String queryUtf8 = new String(Base64.getDecoder().decode(query), Charset.forName("UTF-8"));
        return URLDecoder.decode(queryUtf8, "UTF-8");
    }

    public static class PolygonRequest {
        public List<GeoPoint> polygon;
        public SearchRequest searchRequest;
    }

}
