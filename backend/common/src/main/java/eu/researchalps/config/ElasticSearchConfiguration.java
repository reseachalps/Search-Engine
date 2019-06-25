package eu.researchalps.config;

import com.datapublica.companies.util.HostList;
import eu.researchalps.search.model.FullStructureIndex;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "eu.researchalps.search.repository")
public class ElasticSearchConfiguration {

    @Value("${elasticsearch.cluster:elasticsearch}")
    private String elasticSearchClusterName;
    @Value("${elasticsearch.hosts:localhost}")
    private String hosts;
    @Value("${elasticsearch.create:true}")
    private boolean create;

    public Map<String, String> getSettings() {
        return ImmutableMap.of(
                FullStructureIndex.INDEX, "scanr_settings.json"
        );
    }

    public List<Class> getIndexedTypes() {
        return Lists.newArrayList(FullStructureIndex.class);
    }

    @Bean
    @Profile("!test")
    public Client elasticSearchClient() {
        // We need a cluster name in dev otherwise, the system connects automatically to another ES with same name
        Settings settings = Settings.settingsBuilder().put("cluster.name", elasticSearchClusterName).build();
        final TransportClient client = TransportClient.builder().settings(settings).build();

        HostList.parse(hosts, (host, port) -> {
            InetSocketTransportAddress i = new InetSocketTransportAddress(new InetSocketAddress(host, port == null ? 9300 : port));
            client.addTransportAddress(i);
            return null;
        });
        return client;
    }

    @Bean
    public ElasticsearchTemplate elasticsearchTemplate(Client elasticSearchClient) {
        return new ElasticsearchTemplate(elasticSearchClient);
    }


    public boolean mustCreate() {
        return create;
    }
}
