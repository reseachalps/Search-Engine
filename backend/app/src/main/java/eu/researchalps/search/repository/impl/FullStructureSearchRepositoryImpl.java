package eu.researchalps.search.repository.impl;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.config.ElasticSearchConfiguration;
import eu.researchalps.db.model.WordStemMapping;
import eu.researchalps.db.model.full.Keyword;
import eu.researchalps.db.repository.WordStemMappingRepository;
import eu.researchalps.search.model.FullStructureIndex;
import eu.researchalps.search.model.request.SearchRequest;
import eu.researchalps.search.model.response.FullStructureResult;
import eu.researchalps.search.model.response.GeoBoxResult;
import eu.researchalps.search.model.response.GeoResult;
import eu.researchalps.search.model.response.SearchResponse;
import eu.researchalps.search.repository.FullStructureSearchRepositoryCustom;
import eu.researchalps.service.FeatureService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BoostAttribute;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsRequestBuilder;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FullStructureSearchRepositoryImpl implements FullStructureSearchRepositoryCustom {
    private static final Logger log = LoggerFactory.getLogger(FullStructureSearchRepositoryImpl.class);

    public static final String[] FETCH = new String[]{
            FullStructureIndex.FIELDS.ID,
            FullStructureIndex.FIELDS.LABEL,
            FullStructureIndex.FIELDS.KIND,
            FullStructureIndex.FIELDS.ALIAS,
            FullStructureIndex.FIELDS.PUBLIC_ENTITY,
            FullStructureIndex.FIELDS.TYPE.ALL,
            FullStructureIndex.FIELDS.ACRONYM,
            FullStructureIndex.FIELDS.LEVEL,
            FullStructureIndex.FIELDS.LOGO,
            FullStructureIndex.FIELDS.ADDRESS.DEPARTEMENT,
            FullStructureIndex.FIELDS.ADDRESS.POSTCODE,
            FullStructureIndex.FIELDS.ADDRESS.CITY,
            FullStructureIndex.FIELDS.ADDRESS.COUNTRY,
            FullStructureIndex.FIELDS.MAIN_WEBSITE,
            FullStructureIndex.FIELDS.INSTITUTIONS.PREFIX + "*"
    };

    public static final String[] FETCH_GEO = new String[]{
            FullStructureIndex.FIELDS.ID,
            FullStructureIndex.FIELDS.LABEL,
            FullStructureIndex.FIELDS.ACRONYM,
            FullStructureIndex.FIELDS.ADDRESS.COUNTRY,
            FullStructureIndex.FIELDS.ADDRESS.GPS
    };

    public static final String[] FETCH_GEO_BOX = new String[]{
            FullStructureIndex.FIELDS.ID,
            FullStructureIndex.FIELDS.ADDRESS.GPS
    };

    public static final String[] FETCH_GEO_BOX_DETAILED = new String[]{
            FullStructureIndex.FIELDS.ID,
            FullStructureIndex.FIELDS.LABEL,
            FullStructureIndex.FIELDS.ADDRESS.GPS,
            FullStructureIndex.FIELDS.PROJECTS.COUNT,
            FullStructureIndex.FIELDS.PUBLICATIONS.COUNT,
            FullStructureIndex.FIELDS.CHILDREN_COUNT,
            FullStructureIndex.FIELDS.GRAPH_COUNT,
    };

    public static final String[] FETCH_EXPORT = {
            FullStructureIndex.FIELDS.ID,
            FullStructureIndex.FIELDS.LABEL,
            FullStructureIndex.FIELDS.ACRONYM,
            FullStructureIndex.FIELDS.KIND,
            FullStructureIndex.FIELDS.ALIAS,
            FullStructureIndex.FIELDS.PUBLIC_ENTITY,
            FullStructureIndex.FIELDS.TYPE.ALL,
            FullStructureIndex.FIELDS.COMPANY_TYPE,
            FullStructureIndex.FIELDS.MAIN_WEBSITE,
            FullStructureIndex.FIELDS.ADDRESS.POSTCODE,
            FullStructureIndex.FIELDS.ADDRESS.CITY,
            FullStructureIndex.FIELDS.ADDRESS.URBAN_UNIT,
            FullStructureIndex.FIELDS.ADDRESS.GPS,
            FullStructureIndex.FIELDS.ADDRESS.COUNTRY,
            FullStructureIndex.FIELDS.PEOPLE_COUNT,
            FullStructureIndex.FIELDS.PROJECTS.COUNT,
            FullStructureIndex.FIELDS.PUBLICATIONS.COUNT,
            FullStructureIndex.FIELDS.CHILDREN_COUNT,
            FullStructureIndex.FIELDS.GRAPH_COUNT,
            FullStructureIndex.FIELDS.SOURCES
    };


    public static final Map<String, Integer> SEARCH_ALL_AND_HIGHLIGHT = new HashMap<>();
    public static final String HIGHLIGHTER_TYPE = "fvh";
    public static final int SCROLL_TIMEOUT = 120000;

    static {
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.ID, 5);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.LABEL, 5);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.ACRONYM, 10);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.INSTITUTIONS.CODE, 10);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.ALIAS, 2);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.LEADERS.COMPLETE_NAME, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.ACTIVITY_LABELS, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.PUBLICATIONS.TITLE, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.PUBLICATIONS.SUBTITLE, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.PUBLICATIONS.AUTHORS, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.PUBLICATIONS.SUMMARY, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.PUBLICATIONS.ALTERNATIVE_SUMMARY, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.PROJECTS.ACRONYM, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.PROJECTS.LABEL, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.PROJECTS.DESCRIPTION, 1);
        SEARCH_ALL_AND_HIGHLIGHT.put(FullStructureIndex.FIELDS.WEBSITE.WEBPAGES.CONTENT, 1);
    }

    public static final String HIGHLIGHT_PRE = "<strong>";
    public static final String HIGHLIGHT_POST = "</strong>";
    public static final int HIGHLIGHT_FRAGMENT_SIZE = 100;


    private static final int MAX_FOR_SCROLL = 5000;

    @Autowired
    private Client elasticSearchClient;
    @Autowired
    private AggregationService aggregationService;
    @Autowired
    private ElasticSearchConfiguration elasticSearchConfiguration;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private WordStemMappingRepository stemMappingRepository;
    @Autowired
    private FeatureService featureService;

    private ObjectMapper jsonMapper;

    @PostConstruct
    private void init() {
        jsonMapper = new ObjectMapper();
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Override
    public SearchResponse geoElementsWithQuery(SearchRequest request) throws IOException {
        // return all results with geo info
        SearchRequestBuilder searchRequestBuilder = buildSearchQuery(request, false);
        searchRequestBuilder.setFetchSource(FETCH_GEO, null);

        List<FullStructureResult> results = getAllFullStructure(searchRequestBuilder);
        return new SearchResponse(request, results.size(), results);
    }

    @Override
    public List<GeoBoxResult> geoBoxResults(GeoPoint topLeft, GeoPoint bottomRight, SearchRequest searchRequest) throws IOException {
        final QueryBuilder query = buildQuery(searchRequest);
        return genericFetchAll(
                FETCH_GEO_BOX,
                srb -> srb.setQuery(QueryBuilders.boolQuery().must(query).must(QueryBuilders.geoBoundingBoxQuery(FullStructureIndex.FIELDS.ADDRESS.GPS).topLeft(topLeft).bottomRight(bottomRight))),
                GeoBoxResult::new
        );
    }

    @Override
    public List<GeoResult> geoResults(double lat, double lon, String radius, SearchRequest searchRequest) throws IOException {
        final QueryBuilder query = buildQuery(searchRequest);
        return genericFetchAll(
                FETCH_GEO_BOX_DETAILED,
                srb -> srb.setQuery(QueryBuilders.boolQuery().must(query).must(QueryBuilders.geoDistanceQuery(FullStructureIndex.FIELDS.ADDRESS.GPS).distance(radius).lat(lat).lon(lon))),
                GeoResult::new
        );
    }

    @Override
    public List<GeoResult> geoResults(List<GeoPoint> polygon, SearchRequest searchRequest) throws IOException {
        final QueryBuilder query = buildQuery(searchRequest);
        return genericFetchAll(
                FETCH_GEO_BOX_DETAILED,
                srb -> {
                    final GeoPolygonQueryBuilder geo = QueryBuilders.geoPolygonQuery(FullStructureIndex.FIELDS.ADDRESS.GPS);
                    polygon.forEach(geo::addPoint);
                    srb.setQuery(QueryBuilders.boolQuery().must(query).must(geo));
                },
                GeoResult::new
        );
    }

    @Override
    public GeoResult findOneGeo(String id) {
        final List<GeoResult> findAll = genericFetchAll(
                FETCH_GEO_BOX_DETAILED,
                srb -> srb.setQuery(QueryBuilders.termQuery(FullStructureIndex.FIELDS.ID, id)),
                GeoResult::new
        );
        return findAll.isEmpty() ? null : findAll.get(0);
    }

    @Override
    public List<GeoResult> fastSearch(String query, int size) {
        SearchRequestBuilder searchRequestBuilder = elasticSearchClient.prepareSearch(FullStructureIndex.INDEX).setTypes(FullStructureIndex.TYPE);
        searchRequestBuilder.setFetchSource(FETCH_GEO_BOX_DETAILED, null);
        searchRequestBuilder.setQuery(QueryBuilders.matchPhrasePrefixQuery(FullStructureIndex.FIELDS.LABEL, query));
        searchRequestBuilder.setFrom(0).setSize(size);

        org.elasticsearch.action.search.SearchResponse response = searchRequestBuilder.execute().actionGet();
        List<FullStructureResult> results = new ArrayList<>();
        response.getHits().forEach(hit -> {
            try {
                results.add(jsonMapper.readValue(hit.getSourceAsString(), FullStructureResult.class));
            } catch (IOException e) {
            }
        });

        return results.stream().map(GeoResult::new).collect(Collectors.toList());
    }



    private <E> List<E> genericFetchAll(String[] fetch, Consumer<SearchRequestBuilder> build, Function<FullStructureResult, E> map) {
        SearchRequestBuilder searchRequestBuilder = elasticSearchClient.prepareSearch(FullStructureIndex.INDEX).setTypes(FullStructureIndex.TYPE);
        searchRequestBuilder.setFetchSource(fetch, null);
        build.accept(searchRequestBuilder);
        return getAllFullStructure(searchRequestBuilder).stream().map(map).collect(Collectors.toList());
    }


    @Override
    public SearchResponse searchFullStructureWithQuery(SearchRequest request) throws IOException {
        log.trace("Launching query... < " + request.getQuery());
        long t = System.currentTimeMillis();

        log.trace("Querying...");
        SearchRequestBuilder searchRequestBuilder = buildSearchQuery(request, true);
        searchRequestBuilder.setFetchSource(FETCH, null);

        final org.elasticsearch.action.search.SearchResponse response = searchRequestBuilder.execute().actionGet();

        log.trace("Building results");
        long totalHits = response.getHits().totalHits();
        List<FullStructureResult> results = new ArrayList<>();

        for (SearchHit hit : response.getHits()) {
            if (hit != null) {
                // Map hit to indexed Company
                FullStructureResult searchResult = jsonMapper.readValue(hit.getSourceAsString(), FullStructureResult.class);
                // Add highlights
                final Map<String, FullStructureResult.HighlightItem> highlights = new HashMap<>();
                // Add highlight
                hit.getHighlightFields().values().stream().filter(field -> field.getName() != null).forEach(field -> {
                    int dotIndex = field.getName().indexOf('.');
                    String fieldName = dotIndex < 0 ? field.getName() : field.getName().substring(0, dotIndex);
                    if (field.getFragments() != null && field.getFragments().length > 0 && !highlights.containsKey(fieldName))
                        highlights.put(fieldName, new FullStructureResult.HighlightItem(fieldName, field.getFragments()[0].toString()));
                });
                searchResult.setHighlights(new ArrayList<>(highlights.values()));
                results.add(searchResult);
            }
        }

        // Build the response
        SearchResponse result = new SearchResponse(request, totalHits, results);

        // extract histograms if necessary
        result.setHistograms(aggregationService.addAggregations(response, request.getLocales()));


        log.debug("Search < " + request.getQuery() + " done in " + (System.currentTimeMillis() - t) + " ms");
        return result;
    }

    @Override
    public List<FullStructureResult> getFirstFullStructureWithQuery(SearchRequest request, int size) throws IOException {
        SearchRequestBuilder searchRequestBuilder = buildSearchQuery(request, true);
        // Fetch all fields but raw text
        searchRequestBuilder.setFetchSource(FETCH_EXPORT, null);
        return getFirstFullStructure(searchRequestBuilder, size);
    }

    @Override
    public List<FullStructureResult> getAllFullStructureWithQuery(SearchRequest request) throws IOException {
        SearchRequestBuilder searchRequestBuilder = buildSearchQuery(request, true);
        // Fetch all fields but raw text
        searchRequestBuilder.setFetchSource(FETCH_EXPORT, null);
        return getAllFullStructure(searchRequestBuilder);
    }

    /**
     * @param searchRequest
     * @param addAggregationsAndHighlights
     * @return
     * @throws IOException
     */

    private SearchRequestBuilder buildSearchQuery(SearchRequest searchRequest, boolean addAggregationsAndHighlights) throws IOException {


        // Create the query
        SearchRequestBuilder searchRequestBuilder = elasticSearchClient.prepareSearch(FullStructureIndex.INDEX).setTypes(FullStructureIndex.TYPE);

        // Add pagination
        searchRequestBuilder.setFrom(searchRequest.getFrom()).setSize(searchRequest.getPageSize());


        // Setup highlighting
        if (addAggregationsAndHighlights) {
            switch (searchRequest.getSearchField()) {
                case ALL:
                    SEARCH_ALL_AND_HIGHLIGHT.keySet().forEach(searchRequestBuilder::addHighlightedField);
                case ID:
                    break;
            }
            // To use the fvh highlighter, fields  must have term_vector set to with_positions_offsets
            // see https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-highlighting.html#fast-vector-highlighter
            searchRequestBuilder
                    .setHighlighterPreTags(HIGHLIGHT_PRE)
                    .setHighlighterPostTags(HIGHLIGHT_POST)
                    .setHighlighterFragmentSize(HIGHLIGHT_FRAGMENT_SIZE)
                    .setHighlighterType(HIGHLIGHTER_TYPE)
                    .setHighlighterNumOfFragments(1);
        }

        // Create the search terms and filters
        QueryBuilder query = buildQuery(searchRequest);

        // Add sort order.
        addSort(searchRequest, query, searchRequestBuilder);

        // add aggregations (facets)
        if (addAggregationsAndHighlights)
            searchRequestBuilder
                    .addAggregation(AggregationService.PUBLIC_AGGREGATION)
                    .addAggregation(AggregationService.NUTS_AGGREGATION)
                    .addAggregation(AggregationService.COUNTRY_AGGREGATION)
                    .addAggregation(AggregationService.SOURCES_AGGREGATION)
                    .addAggregation(AggregationService.PROJECTS_HIST_AGGREGATION)
                    .addAggregation(AggregationService.PUBLICATIONS_HIST_AGGREGATION)
                    .addAggregation(AggregationService.CONNECTIONS_HIST_AGGREGATION)
                    .addAggregation(AggregationService.LEADERS_HIST_AGGREGATION);

        return searchRequestBuilder;
    }

    private void addSort(SearchRequest searchRequest, QueryBuilder query, SearchRequestBuilder requestBuilder) {
        // TODO: no sort for the moment
        requestBuilder.setQuery(query);
    }

    private String normalize(String query) {
        if (query == null)
            return null;
        return query.trim();
    }

    private QueryBuilder buildQuery(SearchRequest searchRequest) throws IOException {
        if (searchRequest == null) {
            return QueryBuilders.matchAllQuery();
        }
        List<QueryBuilder> filterValues = SearchFiltersHelper.buildFilters(searchRequest);

        final String query = normalize(QuerySyntaxHelper.fixQuery(searchRequest.getQuery()));

        // Search Query and search fields
        QueryBuilder textQuery;

        if (Strings.isNullOrEmpty(query) || query.equals("*")) {
            textQuery = QueryBuilders.matchAllQuery();
        } else {
            assert QuerySyntaxHelper.checkValid(query);
            switch (searchRequest.getSearchField()) {
                case ALL:
                    // We build a query, with a selection of field to be matched associated with a boost score.
                    // Ou r boost score here is in [1, 5]
                    QueryStringQueryBuilder queryString = QueryBuilders.queryStringQuery(query).defaultOperator(QueryStringQueryBuilder.Operator.AND);
                    SEARCH_ALL_AND_HIGHLIGHT.forEach(queryString::field);
                    queryString.useDisMax(false); // Favorizes results where the query match in many fields

                    // Boost the document for which the terms are present and close
                    final BoolQueryBuilder should = QueryBuilders.boolQuery();
                    SEARCH_ALL_AND_HIGHLIGHT.forEach((field, boost) -> {
                                should.should(QueryBuilders.matchPhraseQuery(field, query).slop(2).boost(60));
                            }
                    );

                    // Combine the query string and the "close words" boost
                    textQuery = QueryBuilders.boolQuery().must(queryString).should(should);

                    break;
                case ID:
                    textQuery = QueryBuilders.queryStringQuery(query).defaultOperator(QueryStringQueryBuilder.Operator.AND).field(FullStructureIndex.FIELDS.ID);
                    break;
                default:
                    textQuery = QueryBuilders.matchAllQuery();
                    break;
            }
        }
        // Combine all the filters
        if (filterValues.size() > 0) {
            final QueryBuilder allFilters = SearchFiltersHelper.andFilters(filterValues);

            // Assemble textQuery and filters
            return QueryBuilders.boolQuery().must(textQuery).filter(allFilters);
        } else
            return textQuery;
    }

    @Override
    public List<Keyword> computeWordCloud(String id, WordStemMapping mapping) throws IOException {
        TermVectorsRequestBuilder termVectorsRequest = elasticSearchClient.prepareTermVectors()
                .setIndex(FullStructureIndex.INDEX)
                .setType(FullStructureIndex.TYPE)
                .setId(id)
                .setSelectedFields(FullStructureIndex.FIELDS.RAW)
                .setFilterSettings(new TermVectorsRequest.FilterSettings(50, 2, null, 1, null, 3, null))
                .setTermStatistics(true)
                .setFieldStatistics(false)
                .setPositions(false)
                .setOffsets(false);
        TermVectorsResponse termVectorsResponse = elasticSearchClient.termVectors(termVectorsRequest.request()).actionGet();


        List<Keyword> result = new ArrayList<>();
        if (termVectorsResponse.getFields() != null && termVectorsResponse.getFields().terms(FullStructureIndex.FIELDS.RAW) != null) {
            TermsEnum iterator = termVectorsResponse.getFields().terms(FullStructureIndex.FIELDS.RAW).iterator();
            while (iterator.next() != null) {
                String keyword = (mapping != null) ? mapping.mapStem(iterator.term().utf8ToString()) : iterator.term().utf8ToString();
                result.add(new Keyword(keyword, iterator.attributes().getAttribute(BoostAttribute.class).getBoost(), (iterator.totalTermFreq() / iterator.docFreq())));
                iterator.next();
            }
            // sort the results
            result.sort((k1, k2) -> -Float.compare(k1.score, k2.score));
        }
        return result;
    }

    @Override
    public List<Keyword> computeWordCloud(SearchRequest request) throws IOException {
        // Create the query
        SearchRequestBuilder searchRequestBuilder = elasticSearchClient.prepareSearch(FullStructureIndex.INDEX).setTypes(FullStructureIndex.TYPE);

        // Add pagination
        searchRequestBuilder.setFrom(0).setSize(200);
        searchRequestBuilder.setFetchSource(false);
        QueryBuilder query = buildQuery(request);
        addSort(request, query, searchRequestBuilder);

        log.info("Launching request for tagcloud...");
        // fetch the top 200 matches
        final Set<String> ids = Arrays.stream(searchRequestBuilder.execute().actionGet().getHits().getHits()).map(SearchHit::getId).collect(Collectors.toSet());

        log.info("Got {} ids on the request", ids.size());

        Map<String, String> mff = new HashMap<>();
        Map<Integer, Integer> total = new HashMap<>();

        // decode the stem mapping data
        stemMappingRepository.findAll(ids).forEach(mapping -> {
            mff.putAll(mapping.getStemToWord());
            final byte[] stem = mapping.getVector();

            ByteBuffer byteBuffer = ByteBuffer.wrap(stem);

            final LongBuffer longBuffer = byteBuffer.asLongBuffer();

            for (int k = 0 ; k < (stem.length / 8) ; k++) {
                long s = longBuffer.get(k);
                int idx = (int) (s >> 32);
                int count = (int) s;
                total.compute(idx, (key, v) -> v == null ? count : v + count);
            }
        });

        log.info("Fetched the mapping data, got {} features and {} mff", total.size(), mff.size());

        final List<Keyword> result = total.entrySet().stream().map(it -> {
            final String feature = featureService.getFeature(it.getKey());
            final String form = mff.getOrDefault(feature, feature);
            return new Keyword(form, (float) featureService.getIdf(it.getKey()) * it.getValue(), it.getValue());
        }).sorted(Comparator.comparing(x -> -x.score)).limit(50).collect(Collectors.toList());

        log.info("Computed the top 50 keyword");

        return result;
    }


    /**
     * Return the first fullStructures (size should be inferior to the scan limit which is around 10K)
     *
     * @param searchRequestBuilder the serach request
     * @param size                 number of elements to retrieve
     * @return
     */
    private List<FullStructureResult> getFirstFullStructure(SearchRequestBuilder searchRequestBuilder, int size) {
        searchRequestBuilder.setFrom(0).setSize(size);

        List<FullStructureResult> results = new ArrayList<>();

        searchRequestBuilder.execute().actionGet().getHits().forEach(hit -> {
            try {
                results.add(jsonMapper.readValue(hit.getSourceAsString(), FullStructureResult.class));
            } catch (IOException e) {
            }
        });

        return results;
    }

    /**
     * Return all the fullStructure using scan and scroll
     *
     * @param searchRequestBuilder
     * @return
     */
    private List<FullStructureResult> getAllFullStructure(SearchRequestBuilder searchRequestBuilder) {
        searchRequestBuilder.setFrom(0).setSize(MAX_FOR_SCROLL);
        searchRequestBuilder.setScroll(new TimeValue(SCROLL_TIMEOUT));

        org.elasticsearch.action.search.SearchResponse response = searchRequestBuilder.execute().actionGet();
        List<FullStructureResult> results = new ArrayList<>();

        while (true) {
            response.getHits().forEach(hit -> {
                try {
                    results.add(jsonMapper.readValue(hit.getSourceAsString(), FullStructureResult.class));
                } catch (IOException e) {
                }
            });
            response = elasticSearchClient.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();

            log.trace("Fetched" + response.getHits().getHits().length + " elements");

            //Break condition: No hits are returned
            if (response.getHits().getHits().length == 0) {
                elasticSearchClient.prepareClearScroll().addScrollId(response.getScrollId()).execute().actionGet();
                break;
            }

        }
        return results;
    }

}
