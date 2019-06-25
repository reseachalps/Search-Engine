/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
package eu.researchalps.crawl.impl;

import eu.researchalps.config.CassandraConfiguration;
import eu.researchalps.crawl.CrawlStoreService;
import eu.researchalps.crawl.CrawlText;
import com.datastax.driver.core.*;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Main service for indexation. This service is manually registred in order to set it enabled (for prod) or not (for
 * tests) Not declared as a service to desactivate for tests
 */
public class CrawlStoreServiceCassandraImpl implements CrawlStoreService {

    private static final Logger log = LoggerFactory.getLogger(CrawlStoreServiceCassandraImpl.class);
    public static final String CRAWL_STORE_TYPE_INFO = "crawl_info";
    public static final String CRAWL_STORE_TYPE_DATA = "crawl_data";

    @Autowired
    private CassandraConfiguration cassandraConfiguration;
    @Autowired
    private Cluster cassandraCluster;

    private Session cassandraSession;
    private PreparedStatement selectTextFromData;

    @PostConstruct
    public void init() throws IOException {
        if (cassandraCluster != null) {
            ensureTablesCreated(false);
        }
    }

    /**
     * Insure that the crawl index is correctly created.
     *
     * @param delete force recreation of the index if true
     * @throws IOException
     */
    public void ensureTablesCreated(boolean delete) throws IOException {
        ensureKeyspaceCreated(cassandraConfiguration.getKeyspace(), delete);
        // put the mappings anyway
        createTable(CRAWL_STORE_TYPE_DATA);
        createTable(CRAWL_STORE_TYPE_INFO);
        prepareQueries();
    }

    /**
     * Insure that the index is correctly created.
     *
     * @param index  index name
     * @param delete force recreation of the index if true
     * @throws IOException
     */
    private void ensureKeyspaceCreated(String index, boolean delete) throws IOException {
        // Check if  cassandraCluster.getMetadata().getKeyspace(index)  is not enought to get this information
        boolean keyspaceExists = cassandraCluster.getMetadata().getKeyspaces().stream()
                .map(KeyspaceMetadata::getName)
                .anyMatch(indexName -> index.compareTo(indexName) == 0);

        Session localCassandraSession = cassandraCluster.connect();

        if (keyspaceExists && delete) {
            localCassandraSession.execute("DROP KEYSPACE " + index + ";");
            keyspaceExists = false;
        }

        if (!keyspaceExists) {
            String create = IOUtils.toString(
                    CrawlStoreServiceCassandraImpl.class.getResourceAsStream("/eu/researchalps/crawl_store/create_keyspace.cql"),
                    Charsets.UTF_8);
            localCassandraSession.execute(create.replace("%s", index));
        }

        cassandraSession = cassandraCluster.connect(index);
    }

    private void createTable(String table) throws IOException {
        log.info("Create table for keyspace " + cassandraConfiguration.getKeyspace() + " table:" + table);

        String source = IOUtils.toString(
                CrawlStoreServiceCassandraImpl.class.getResourceAsStream("/com/datapublica/scanr/crawl_store/create_table_" + table + ".cql"),
                Charsets.UTF_8);

        cassandraSession.execute(source);
    }

    private void prepareQueries() {
        selectTextFromData = this.cassandraSession.prepare("SELECT title, relevant_txt, lang FROM " + CRAWL_STORE_TYPE_DATA + " WHERE website_id = ?");
        selectTextFromData.setConsistencyLevel(ConsistencyLevel.ONE);
    }

    protected static CrawlText buildCrawlTextFromRow(Row row) {
        return new CrawlText(row.getString("title"), row.getString("relevant_txt"), row.getString("lang"));
    }

    @Override
    public List<CrawlText> getCrawlTexts(String websiteId) {
        return getCrawlData(websiteId, selectTextFromData, CrawlStoreServiceCassandraImpl::buildCrawlTextFromRow);
    }

    @Override
    public Map<String, List<CrawlText>> getCrawlTexts(List<String> websiteIds) {
        return websiteIds.stream().distinct().collect(Collectors.toMap(it -> it, this::getCrawlTexts));
    }

    private <T> List<T> getCrawlData(String websiteId, PreparedStatement query, Function<Row, T> mapping) {
        ResultSet results = this.cassandraSession.execute(query.bind(websiteId));
        return results.all().stream().map(mapping).collect(Collectors.toList());
    }

}
