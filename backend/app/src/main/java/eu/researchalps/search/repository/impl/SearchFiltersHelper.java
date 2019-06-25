package eu.researchalps.search.repository.impl;

import eu.researchalps.search.model.FullStructureIndex;
import eu.researchalps.search.model.request.GeoGridFilter;
import eu.researchalps.search.model.request.MultiValueSearchFilter;
import eu.researchalps.search.model.request.RangeFilter;
import eu.researchalps.search.model.request.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SearchFiltersHelper {
    private static final Logger log = LoggerFactory.getLogger(SearchFiltersHelper.class);

    /**
     * Combine filters with an AND clause
     */
    public static QueryBuilder andFilters(List<QueryBuilder> filters) {
        // combine the filters
        if (filters.size() > 1) {
            BoolQueryBuilder mainFilter = new BoolQueryBuilder();
            for (QueryBuilder f : filters) {
                mainFilter = mainFilter.must(f);
            }
            return mainFilter;
        } else if (filters.size() == 1) {
            return filters.iterator().next();
        } else
            return null;
    }

    public static List<QueryBuilder> buildFilters(SearchRequest request) {
        List<QueryBuilder> filters = new ArrayList<>();

        addTermFilter(filters, FullStructureIndex.FIELDS.KIND, request.getKind());
        addTermFilter(filters, FullStructureIndex.FIELDS.PUBLIC_ENTITY, request.getPublicEntity());
        addTermFilter(filters, FullStructureIndex.FIELDS.TYPE.LABEL, request.getType());
        addTermFilter(filters, FullStructureIndex.FIELDS.ADDRESS.URBAN_UNIT, request.getUrbanUnit());
        addTermFilter(filters, FullStructureIndex.FIELDS.ADDRESS.DEPARTEMENT, request.getDepartements());
        addTermFilter(filters, FullStructureIndex.FIELDS.DOMAINE.CODE, request.getDomaine());
        addTermFilter(filters, FullStructureIndex.FIELDS.NAF.CODE, request.getNaf());
        addTermFilter(filters, FullStructureIndex.FIELDS.ERC.CODE, request.getErc());
        addTermFilter(filters, FullStructureIndex.FIELDS.INSTITUTIONS.ID, request.getInstitutions());
        addTermFilter(filters, FullStructureIndex.FIELDS.PROJECTS.ID, request.getProjects());
        addTermFilter(filters, FullStructureIndex.FIELDS.BADGES, request.getBadges());
        addTermFilter(filters, FullStructureIndex.FIELDS.PROJECTS.CALL_LABEL, request.getCalls());
        addTermFilter(filters, FullStructureIndex.FIELDS.ADDRESS.COUNTRY, request.getCountries());
        addTermFilter(filters, FullStructureIndex.FIELDS.ADDRESS.NUTS2, request.getNuts());
        addTermFilter(filters, FullStructureIndex.FIELDS.SOURCES, request.getSources());
        addTermFilter(filters, FullStructureIndex.FIELDS.ID, request.getIds());

        addGeoBoundingBox(filters, FullStructureIndex.FIELDS.ADDRESS.GPS, request.getGeoGrid());

        return filters;
    }

    private static QueryBuilder andFilter(MultiValueSearchFilter filter, Function<Object, QueryBuilder> termHandler) {
        if (filter.getValues().size() > 0) {
            BoolQueryBuilder andFilter = QueryBuilders.boolQuery();
            filter.getValues().forEach(el -> andFilter.must(termHandler.apply(el)));
            return andFilter;
        } else {
            throw new InvalidSearchFilterException(filter, "Empty AND filter");
        }
    }

    private static QueryBuilder orFilter(MultiValueSearchFilter filter, Function<Object, QueryBuilder> termHandler) {
        if (filter.getValues().size() > 0) {
            BoolQueryBuilder orFilter = QueryBuilders.boolQuery();
            orFilter.minimumNumberShouldMatch(1);
            filter.getValues().forEach(el -> orFilter.should(termHandler.apply(el)));
            return orFilter;
        } else {
            throw new InvalidSearchFilterException(filter, "Empty OR filter");
        }
    }

    private static QueryBuilder processFilterNode(MultiValueSearchFilter node,
                                                  Function<Object, QueryBuilder> termHandler,
                                                  Supplier<QueryBuilder> existsHandler) {
        switch (node.getOp()) {
            case all:
                return andFilter(node, termHandler);

            case any:
                return orFilter(node, termHandler);

            case none:
                return QueryBuilders.boolQuery().mustNot(orFilter(node, termHandler));

            case not_all:
                return QueryBuilders.boolQuery().mustNot(andFilter(node, termHandler));

            case exists:
                if (existsHandler != null) {
                    return existsHandler.get();
                } else {
                    throw new InvalidSearchFilterException(node, "Exists filter builder not provided (should the filter use exists?) for " + node.toString());
                }

        }
        throw new InvalidSearchFilterException(node, "Unrecognized filter " + node.getOp());
    }

    private static void addTermFilter(List<QueryBuilder> filters, String field, MultiValueSearchFilter multiValueSearchFilter) {
        if (multiValueSearchFilter != null)
            filters.add(processFilterNode(multiValueSearchFilter,
                    termValue -> QueryBuilders.termQuery(field, termValue),
                    () -> QueryBuilders.existsQuery(field)));
    }

    private static void addTermFilters(List<QueryBuilder> filters, String field, List<MultiValueSearchFilter> multiValueSearchFilters) {
        if (!CollectionUtils.isEmpty(multiValueSearchFilters)) {
            for (MultiValueSearchFilter f : multiValueSearchFilters) {
                try {
                    addTermFilter(filters, field, f);
                } catch (InvalidSearchFilterException e) {
                    // rethrow with more context
                    throw new InvalidSearchFilterException(f, field, e.getBody().reason);
                }
            }
        }
    }


    private static void addGeoBoundingBox(List<QueryBuilder> filters, String field, GeoGridFilter geoGrid) {
        if (geoGrid != null) {
            filters.add(QueryBuilders.geoBoundingBoxQuery(field).topLeft(geoGrid.topLeft).bottomRight(geoGrid.bottomRight));
        }
    }


    private static void addRangeFilter(List<QueryBuilder> filters, String field, RangeFilter range) {
        if (range != null) {
            if (range.min != null || range.max != null) {
                final RangeQueryBuilder rangeFilter = QueryBuilders.rangeQuery(field);
                if (range.min != null)
                    rangeFilter.gte(range.min);
                if (range.max != null)
                    rangeFilter.lt(range.max);

                // if range.missing we authorized missing values
                BoolQueryBuilder orQuery = QueryBuilders.boolQuery().should(rangeFilter).should(QueryBuilders.missingQuery(field)).minimumNumberShouldMatch(1);

                filters.add(range.missing ?
                        orQuery : rangeFilter);
            } else if (!range.missing) {
                filters.add(QueryBuilders.existsQuery(field));
            }
        }
    }

}
