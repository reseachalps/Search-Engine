package eu.researchalps.config;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * samuel
 * 03/12/15, 18:45
 */
@Configuration
public class ElasticSearchInitializer {
    @Autowired
    ElasticsearchTemplate template;

    @Autowired
    Client client;

    @Autowired
    ElasticSearchConfiguration configuration;

    public void setConfiguration(ElasticSearchConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setTemplate(ElasticsearchTemplate template) {
        this.template = template;
    }

    @PostConstruct
    public void init() throws IOException {
        if (!configuration.mustCreate())
            return;

        configuration.getSettings().entrySet().forEach(e -> initSettings(e.getKey(), e.getValue()));
        configuration.getIndexedTypes().forEach(this::initType);
    }

    public void initSettings(String indexName, String settingsFilename) {
        // Get configuration as string
        // Import configured analyzers
        final String settings;
        try (InputStream stream = ElasticSearchConfiguration.class.getResourceAsStream(settingsFilename)) {
            settings = IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create index if not exists
        if (!client.admin().indices().prepareExists(indexName).execute().actionGet().isExists()) {
            client.admin().indices().prepareCreate(indexName).setSettings(settings).execute().actionGet();
        }
        // Update settings otherwise
        else {
            client.admin().indices().prepareClose(indexName).execute().actionGet();
            client.admin().indices().prepareUpdateSettings(indexName).setSettings(settings).execute().actionGet();
            client.admin().indices().prepareOpen(indexName).execute().actionGet();
        }
    }

    public void initType(Class klass) {

        ElasticsearchPersistentEntity<?> entity = template.getElasticsearchConverter().getMappingContext().getPersistentEntity(klass);
        // create the index and associate the mapping
        if (!template.indexExists(entity.getIndexName()) || !template.typeExists(entity.getIndexName(), entity.getIndexType())) {
            template.createIndex(klass);
            template.refresh(klass);
            template.putMapping(klass);
        }
    }
}
