package eu.researchalps.workflow.search;

import com.datapublica.companies.util.HostList;
import eu.researchalps.service.FeatureService;
import eu.researchalps.search.model.FullStructureIndex;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.data.util.CloseableIterator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.*;
import java.util.function.Function;

/**
 * Created by loic on 2019-04-30.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class TestWordCloud {
    private static <T> CloseableIterator<T> doStream(Client client, final SearchResponse initScroll, final long scrollTimeInMillis, Function<SearchResponse, Iterator<T>> mapper) {
        return new CloseableIterator<T>() {

            /** As we couldn't retrieve single result with scroll, store current hits. */
            private volatile Iterator<T> currentHits = mapper.apply(initScroll);

            /** The scroll id. */
            private volatile String scrollId = initScroll.getScrollId();

            /** If stream is finished (ie: cluster returns no results. */
            private volatile boolean finished;

            @Override
            public void close() {
                try {
                    // Clear scroll on cluster only in case of error (cause elasticsearch auto clear scroll when it's done)
                    if (!finished && scrollId != null && currentHits != null && currentHits.hasNext()) {
                        client.prepareClearScroll().addScrollId(scrollId).execute().actionGet();
                    }
                } finally {
                    currentHits = null;
                    scrollId = null;
                }
            }

            @Override
            public boolean hasNext() {
                // Test if stream is finished
                if (finished) {
                    return false;
                }
                // Test if it remains hits
                if (currentHits == null || !currentHits.hasNext()) {
                    // Do a new request
                    SearchResponse response = client.prepareSearchScroll(scrollId)
                            .setScroll(TimeValue.timeValueMillis(scrollTimeInMillis)).execute().actionGet();
                    // Save hits and scroll id
                    currentHits = mapper.apply(response);
                    finished = !currentHits.hasNext();
                    scrollId = response.getScrollId();
                }
                return currentHits.hasNext();
            }

            @Override
            public T next() {
                if (hasNext()) {
                    return currentHits.next();
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }


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


    public static void main(String[] args) throws IOException {
        final FeatureService featureService = new FeatureService();
        Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").build();
        final TransportClient client = TransportClient.builder().settings(settings).build();

        HostList.parse("localhost:9300", (host, port) -> {
            InetSocketTransportAddress i = new InetSocketTransportAddress(new InetSocketAddress(host, port == null ? 9300 : port));
            client.addTransportAddress(i);
            return null;
        });
        SearchRequestBuilder srb = client.prepareSearch("researchalps");


        // We build a query, with a selection of field to be matched associated with a boost score.
        // Ou r boost score here is in [1, 5]
        final String query = "deep learning";
        QueryStringQueryBuilder queryString = QueryBuilders.queryStringQuery(query).defaultOperator(QueryStringQueryBuilder.Operator.AND);
        SEARCH_ALL_AND_HIGHLIGHT.forEach(queryString::field);
        queryString.useDisMax(false); // Favorizes results where the query match in many fields

        // Boost the document for which the terms are present and close
        final BoolQueryBuilder should = QueryBuilders.boolQuery();
        SEARCH_ALL_AND_HIGHLIGHT.forEach((field, boost) -> {
                    should.should(QueryBuilders.matchPhraseQuery(field, query).slop(2).boost(60));
                }
        );

        srb.setQuery(QueryBuilders.boolQuery().must(queryString).should(should)); //QueryBuilders.idsQuery("fullstructure").ids("113612")

//        srb.addAggregation(AggregationBuilders.terms("ids").field("id").order(Terms.Order.aggregation("top_hit", false)).size(50).subAggregation(AggregationBuilders.max("top_hit").script(new Script("_score"))));

        srb.setFetchSource(new String[]{"raw"}, null);
        srb.setScroll("1m");
        srb.setSize(20);


        System.out.println();
        Map<String, String> mff = new HashMap<>();
        Map<Integer, Integer> total = new HashMap<>();

        final StemmingService stemmingService = new StemmingService();
        stemmingService.featureService = featureService;

        final SearchResponse searchResponse = srb.execute().actionGet();

        try (CloseableIterator<String> result = doStream(client, searchResponse, 30000, it -> Arrays.stream(it.getHits().getHits()).map(h -> (String) h.getSource().get("raw")).iterator())) {

            int i = 0;
            while (result.hasNext()) {
                final String raw = result.next();
                final byte[] stem = stemmingService.buildStemMapping(raw, mff);

                ByteBuffer byteBuffer = ByteBuffer.wrap(stem);

                final LongBuffer longBuffer = byteBuffer.asLongBuffer();

                for (int k = 0 ; k < (stem.length / 8) ; k++) {
                    long s = longBuffer.get(k);
                    int idx = (int) (s >> 32);
                    int count = (int) s;
                    total.compute(idx, (key, v) -> v == null ? count : v + count);
                }
                i++;
                System.out.println(i);
                if (i >= 200)
                    break;
            }
        }
//        final Sampler sample = searchResponse.getAggregations().get("sample");
//        final Terms ids = searchResponse.getAggregations().get("ids");
//        for (Terms.Bucket bucket : ids.getBuckets()) {
//            System.out.println(bucket.getKeyAsString());
//        }

        total.entrySet().stream().map(it -> {
            final String feature = featureService.getFeature(it.getKey());
            return Pair.of(mff.getOrDefault(feature, feature), featureService.getIdf(it.getKey()) * it.getValue());
        }).sorted(Comparator.comparing(x -> -x.getRight())).limit(50).forEach(it -> {
            System.out.println(it.getRight().intValue() +" \t"+it.getLeft());
        });

//        stemmingService.displayWordCloud((String)searchResponse.getHits().getHits()[0].getSource().get("raw"), true);

        /*
        Map<String, Long> vector = new HashMap<>();
        try (CloseableIterator<String> result = doStream(client, searchResponse, 30000, it -> Arrays.stream(it.getHits().getHits()).map(h -> (String) h.getSource().get("raw")).iterator())) {
            int i = 0;
            while (result.hasNext()) {
                final String raw = result.next();
                Map<String, Long> local = new HashMap<>();
                stemmingService.stemCount(local, raw);
                for (String k : local.keySet()) {
                    vector.compute(k, (key, value) -> value == null ? 1 : value + 1);
                }
                System.out.println(vector.size());
                System.out.println(i++);
            }
        } finally {
            final ObjectMapper om = new ObjectMapper();
            try (FileOutputStream fos = new FileOutputStream("/tmp/test-"+System.currentTimeMillis()+".json")) {
                fos.write(om.writeValueAsBytes(vector));
            }
        }*/
/*
        final ObjectMapper om = new ObjectMapper();
        Map<String, Integer> filtered = new HashMap<>();
        {
            System.out.println("Read df file");
            Map<String, Integer> asd = om.readValue(new File("/Users/loic/data/df-researchalps-1556649268359.json"), om.getTypeFactory().constructMapType(HashMap.class, String.class, Integer.class));
            System.out.println("Done, filtering now");
            final Pattern pattern = Pattern.compile("[a-z]");
            for (Map.Entry<String, Integer> entry : asd.entrySet()) {
                if (entry.getValue() < 20) {
                    continue;
                }
                if (!pattern.matcher(entry.getKey()).find()) {
                    continue;
                }
                filtered.put(entry.getKey(), entry.getValue());
            }
            System.out.println("Filtered");
        }
        System.gc();
        System.out.println("GC");
        // sort and remove top 1000
        final List<Map.Entry<String, Integer>> result = filtered.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        System.out.println("Sorted");
            om.writeValue(new File("/tmp/df2.json"), new DFFile(result.stream().map(Map.Entry::getKey).collect(Collectors.toList()), result.stream().map(Map.Entry::getValue).collect(Collectors.toList())));
        System.out.println("All done");*/
    }
    public static class DFFile {
        public List<String> keys;
        public List<Integer> values;

        public DFFile(List<String> keys, List<Integer> values) {
            this.keys = keys;
            this.values = values;
        }
    }

}
