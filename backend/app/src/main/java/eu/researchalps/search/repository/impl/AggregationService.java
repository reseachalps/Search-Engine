package eu.researchalps.search.repository.impl;

import eu.researchalps.search.model.FullStructureIndex;
import eu.researchalps.search.model.response.SearchResultHistograms;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.range.RangeBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AggregationService {
    public static final String PUBLIC_ENTITY = "pe";
    public static final String URBAN_UNIT = "uu";
    //public static final String TAGS = "tg";
    public static final String NATURE = "n";
    //public static final String GEO_GRID = "gg";
    private static final String PROJECT_CALL = "pc";
    private static final String COUNTRIES = "countries";
    private static final String NUTS_HIST = "nuts2";
    private static final String SOURCES_HIST = "sources";
    private static final String PUBLICATIONS_HIST = "pubhist";
    private static final String PROJECTS_HIST = "projecthist";
    private static final String LEADERS_HIST = "leadershist";
    private static final String CONNECTIONS_HIST = "connhist";
    //   private static final String WORD_CLOUD = "wc";

    // TODO: we should add missing aggregations too

    // For the following aggregations we return all terms (size=0 & minDocCount=0)
    public static final TermsBuilder NUTS_AGGREGATION = AggregationBuilders.terms(NUTS_HIST).field(FullStructureIndex.FIELDS.ADDRESS.NUTS2).size(0).minDocCount(0);
    public static final TermsBuilder PUBLIC_AGGREGATION = AggregationBuilders.terms(PUBLIC_ENTITY).field(FullStructureIndex.FIELDS.PUBLIC_ENTITY).size(0).minDocCount(0);
    public static final TermsBuilder COUNTRY_AGGREGATION = AggregationBuilders.terms(COUNTRIES).field(FullStructureIndex.FIELDS.ADDRESS.COUNTRY).size(0).minDocCount(0);
    public static final TermsBuilder SOURCES_AGGREGATION = AggregationBuilders.terms(SOURCES_HIST).field(FullStructureIndex.FIELDS.SOURCES).size(0).minDocCount(0);

    public static final RangeBuilder PUBLICATIONS_HIST_AGGREGATION = AggregationBuilders.range(PUBLICATIONS_HIST).field(FullStructureIndex.FIELDS.PUBLICATIONS.COUNT)
            .addRange("0", 0, 1).addRange("1-5", 1, 5).addRange("5-10", 5, 10).addRange("10-25", 10, 25).addRange("25-50", 25, 50).addRange("50-100", 50, 100).addRange("100-250", 100, 250)
            .addRange("250+", 250, Integer.MAX_VALUE);
    public static final RangeBuilder PROJECTS_HIST_AGGREGATION = AggregationBuilders.range(PROJECTS_HIST).field(FullStructureIndex.FIELDS.PROJECTS.COUNT)
            .addRange("0", 0, 1).addRange("1-5", 1, 5).addRange("5-10", 5, 10).addRange("10-25", 10, 25).addRange("25-50", 25, 50).addRange("50-100", 50, 100).addRange("100-250", 100, 250)
            .addRange("250+", 250, Integer.MAX_VALUE);
    public static final RangeBuilder LEADERS_HIST_AGGREGATION = AggregationBuilders.range(LEADERS_HIST).field(FullStructureIndex.FIELDS.PEOPLE_COUNT)
            .addRange("0", 0, 1).addRange("1-5", 1, 5).addRange("5-10", 5, 10).addRange("10-25", 10, 25).addRange("25-50", 25, 50).addRange("50-100", 50, 100).addRange("100-250", 100, 250)
            .addRange("250+", 250, Integer.MAX_VALUE);
    public static final RangeBuilder CONNECTIONS_HIST_AGGREGATION = AggregationBuilders.range(CONNECTIONS_HIST).field(FullStructureIndex.FIELDS.GRAPH_COUNT)
            .addRange("0", 0, 1).addRange("1-5", 1, 5).addRange("5-10", 5, 10).addRange("10-25", 10, 25).addRange("25-50", 25, 50).addRange("50-100", 50, 100).addRange("100-250", 100, 250)
            .addRange("250+", 250, Integer.MAX_VALUE);


    public SearchResultHistograms addAggregations(SearchResponse response, List<Locale> locales) {
        SearchResultHistograms histograms = new SearchResultHistograms();

        response.getAggregations().forEach(aggregation -> {
            switch (aggregation.getName()) {
                case PUBLIC_ENTITY:
                    fillValuesHistogram(aggregation, histograms.getPublicEntity());
                    break;
                case COUNTRIES:
                    fillValuesHistogram(aggregation, histograms.getCountries());
                    histograms.getCountries().getBins().forEach(bin -> bin.label = Character.toUpperCase(bin.label.charAt(0)) + bin.label.substring(1));
                    break;
                case PUBLICATIONS_HIST:
                    fillValuesHistogram(aggregation, histograms.getPublications());
                    break;
                case PROJECTS_HIST:
                    fillValuesHistogram(aggregation, histograms.getProjects());
                    break;
                case LEADERS_HIST:
                    fillValuesHistogram(aggregation, histograms.getLeaders());
                    break;
                case CONNECTIONS_HIST:
                    fillValuesHistogram(aggregation, histograms.getConnections());
                    break;
                case NUTS_HIST:
                    fillValuesHistogram(aggregation, histograms.getNuts());
                    break;
                case SOURCES_HIST:
                    fillValuesHistogram(aggregation, histograms.getSources());
                    break;
            }
        });

        return histograms;
    }


    private void fillValuesHistogram(Aggregation aggregation, SearchResultHistograms.SearchResultHistogram histo, Map<String, String> mapping) {
        ((MultiBucketsAggregation) aggregation).getBuckets().forEach(bucket -> {
            String key = bucket.getKeyAsString();
            histo.setBin(key, mapping.getOrDefault(key, key), bucket.getDocCount());
        });
    }


    private void fillValuesHistogram(Aggregation aggregation, SearchResultHistograms.SearchResultHistogram histo) {
        ((MultiBucketsAggregation) aggregation).getBuckets().forEach(bucket -> {
            histo.setBin(bucket.getKeyAsString(), bucket.getDocCount());
        });
    }


    private void fillLanguageValuesHistogram(Aggregation aggregation, SearchResultHistograms.SearchResultHistogram histo, List<Locale> locales) {
        ((MultiBucketsAggregation) aggregation).getBuckets().forEach(bucket -> {
            String lg = bucket.getKeyAsString();
            histo.setBin(lg, languageInUserLocale(lg, locales), bucket.getDocCount());
        });
    }

    private String languageInUserLocale(String lg, List<Locale> locales) {
        if (locales == null)
            locales = Collections.singletonList(Locale.getDefault());
        for (Locale l : locales) {
            String languageInUserLocale = new Locale(lg).getDisplayLanguage(l);
            if (StringUtils.isNotEmpty(languageInUserLocale)) return languageInUserLocale;
        }
        return lg;
    }

    private void fillBooleanBuckets(Aggregation aggregation, SearchResultHistograms.SearchResultHistogram histo) {
        ((MultiBucketsAggregation) aggregation).getBuckets().forEach(bucket -> {
                    if (bucket.getKey().equals("T"))
                        histo.setBin(aggregation.getName(), bucket.getDocCount());
                }
        );
    }
/*
    private void fillMapHistogram(Aggregation aggregation, SearchResultHistograms.SearchResultHistogram histo, List<Locale> locales, final int level) {
        final Collection<? extends MultiBucketsAggregation.Bucket> buckets = ((MultiBucketsAggregation) aggregation).getBuckets();
        Map<String, Long> count = new HashMap<>();
        final List<MapRequest> map = buckets.stream().map(it -> {
            final String firstLevel = it.getKey();
            count.put(it.getKey(), it.getDocCount());
            return new MapRequest(Address.countryFromPlaceId(firstLevel), Address.itemIdFromPlaceId(firstLevel), level);
        }).collect(Collectors.toList());

        // Get the map
        final List<MapItem> items = mapService.getMap(250, map);
        for (MapItem item : items) {
            final Country c = item.getEntry().getCountry();
            final String key = item.getItemId() == null ? "border-" + c.name() + "-" + item.getEntry().getMapName() : c.name() + "-" + level + "-" + item.getItemId();
            histo.addBin(key, new SearchResultHistograms.MapBin(
                    key,
                    geoLocalization.getLocalizedLabel(c, item.getLabels(), locales),
                    item.getSvg(),
                    item.getCenterX(),
                    item.getCenterY()
            ));
            if (item.getItemId() != null)
                histo.setBin(key, count.getOrDefault(Address.placeId(c, item.getItemId()), 0L));
        }
    }*/
}
