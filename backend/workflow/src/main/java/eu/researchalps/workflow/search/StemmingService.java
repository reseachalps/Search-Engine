package eu.researchalps.workflow.search;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.service.FeatureService;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This service will compute the Mapping between stem and most frequent corresponding word in preperation for the word cloud.
 */
@Service
public class StemmingService {
    private static final Logger log = LoggerFactory.getLogger(FullStructureMapper.class);
    public static final String ANALYZER_TO_USE = "word_cloud";
    public static final Pattern PATTERN = Pattern.compile("(https?|ftp)://[^\\s/$.?#].[^\\s]*");

    private Analyzer analyzer;

    @Autowired
    protected FeatureService featureService;

    /**
     * From a free text form, generate the stem mapping
     *
     * This is based on the actual analyzer.
     *
     * @param text A free text
     * @return A map of stem -> most frequent form
     */
    public byte[] buildStemMapping(String text,  Map<String, String> mff) {
        text = PATTERN.matcher(text).replaceAll(" ");

        StringReader tReader = new StringReader(text);
        try (TokenStream tStream = analyzer.tokenStream("contents", tReader)) {
            CharTermAttribute term = tStream.addAttribute(CharTermAttribute.class);
            OffsetAttribute offsetAttribute = tStream.addAttribute(OffsetAttribute.class);
            tStream.reset();

            Map<String, List<String>> mapping = new HashMap<>();
            while (tStream.incrementToken()) {
                String stem = new String(term.buffer(), 0, term.length());
                if (!featureService.getIdx().containsKey(stem)) {
                    continue;
                }
                String rawTerm = text.substring(offsetAttribute.startOffset(), offsetAttribute.endOffset());
                mapping.putIfAbsent(stem, new ArrayList<>());
                mapping.get(stem).add(rawTerm);
            }
            long[] result = new long[mapping.size()];
            int i = 0;
            // compute the most frequent raw term
            for (Map.Entry<String, List<String>> entry : mapping.entrySet()) {
                String stem = entry.getKey();
                List<String> terms = entry.getValue();
                result[i++] = (featureService.getIdx().get(stem).longValue() << 32) + terms.size();
                Map<String, Long> collect = terms.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));
                Map.Entry<String, Long> mostCommonEntry = collect.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get();
                String mostCurrentTerm = mostCommonEntry.getKey();
                if (!stem.equals(mostCurrentTerm) && (stem.indexOf('.') == -1)) {
                    mff.putIfAbsent(stem, mostCurrentTerm);
                }
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(result.length * 8);
            byteBuffer.asLongBuffer().put(result);

            return byteBuffer.array();
        } catch (IOException ioe) {
            log.error("Invalid stemming mapping generation ", ioe);
            // shouldnt happen
            throw new RuntimeException(ioe);
        }
    }

    public void stemCount(Map<String, Long> vector, String text) {
        StringReader tReader = new StringReader(text);
        try (TokenStream tStream = analyzer.tokenStream("contents", tReader)) {
            CharTermAttribute term = tStream.addAttribute(CharTermAttribute.class);
            tStream.reset();
            while (tStream.incrementToken()) {
                String stem = new String(term.buffer(), 0, term.length());
                vector.compute(stem, (key, value) -> value == null ? 1 : value + 1);
            }
        } catch (IOException ioe) {
            log.error("Invalid stemming mapping generation ", ioe);
        }
    }

    public StemmingService() throws IOException {
        AnalysisService analysisService = createAnalysisService(IOUtils.toString(StemmingService.class.getResourceAsStream("/eu/researchalps/config/scanr_settings.json")));
        analyzer = analysisService.analyzer(ANALYZER_TO_USE);
        if (analyzer == null) {
            throw new IllegalStateException("Cant find analyzer "+ANALYZER_TO_USE);
        }
    }

    /**
     * Create an AnalysisService that is correctly configured for the given settings source
     *
     * Inspired mostly by mocks that can be found in the org.elasticsearch.index.analysis test source of ES
     * https://github.com/elastic/elasticsearch/blob/2.3/plugins/analysis-icu/src/test/java/org/elasticsearch/index/analysis/AnalysisTestUtils.java
     *
     * @param source The text content of the settings file (most likely a json)
     * @return the preconfigured analysis service with all the analyzers ready
     */
    private static AnalysisService createAnalysisService(String source) {
        // we dont care about the index here
        Index index = new Index("mock");
        Settings indexSettings = Settings.settingsBuilder()
                .loadFromSource(source)
                // needed to build an IndicesAnalysisService
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                // needed to build an Environment
                .put("path.home", System.getProperty("java.io.tmpdir"))
                .build();
        // dark injection magic
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(indexSettings), new EnvironmentModule(new Environment(indexSettings))).createInjector();
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, indexSettings),
                new IndexNameModule(index),
                new AnalysisModule(indexSettings, parentInjector.getInstance(IndicesAnalysisService.class)))
                .createChildInjector(parentInjector);
        // injector is ready with all of his instances
        return injector.getInstance(AnalysisService.class);
    }
/*
    public static void main(String[] args) throws IOException {
        String text = "Salut les gars, j'aime bien les études, la justice, le spatial, la justice (C-Radar) \"c-radar\" spatiale, études urbaines, justices spatiales, c'est très urbain et C-Radar c'est bien";

        StemmingService stemmingService = new StemmingService();
        stemmingService.featureService = new FeatureService();
        Map<String, String> mapping = new HashMap<>();
        stemmingService.buildStemMapping(text, mapping);
        mapping.forEach((k, v) -> System.out.println(k + "->" + v));
        System.out.println("------- Mapping size:" + mapping.size());
        System.out.println("------- Stemming");
        stemmingService.displayWordCloud(text, true);
        System.out.println("------- No stemming");
        stemmingService.displayWordCloud(text, false);
    }

*/
    public void displayWordCloud(String text, boolean stemming) throws IOException {
        List<String> terms = new ArrayList<>();
        text = PATTERN.matcher(text).replaceAll(" ");
        final Map<String, Integer> idx = featureService.getIdx();

        StringReader tReader = new StringReader(text);
        try (TokenStream tStream = analyzer.tokenStream("contents", tReader)) {
            CharTermAttribute term = tStream.addAttribute(CharTermAttribute.class);
            tStream.reset();

            while (tStream.incrementToken()) {
                final String stem = new String(term.buffer(), 0, term.length());
                if (!idx.containsKey(stem)) {
                    //System.out.println("Ignoring stem " + stem);
                    // ignoring!
                    continue;
                }
                terms.add(stem);
            }

        }
        Map<String, String> mapping = new HashMap<>();
        if (stemming) {
            buildStemMapping(text, mapping);
        }



        terms.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting())).entrySet().stream().filter(it -> featureService.getDf(idx.get(it.getKey())) < 15000).sorted(Comparator.comparing(i -> -i.getValue() * (featureService.getIdf(idx.get(i.getKey()))))).limit(20).
                forEach(
                        e -> {
                            final String stem = e.getKey();
                            System.out.println(stem + "|" + mapping.getOrDefault(stem, stem) + " " + e.getValue()+"|"+featureService.getDf(idx.get(e.getKey()))+"|"+(e.getValue() *  featureService.getIdf(idx.get(e.getKey())) ));
                        }
                );
    }

}
