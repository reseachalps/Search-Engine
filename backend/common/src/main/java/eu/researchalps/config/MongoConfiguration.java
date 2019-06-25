package eu.researchalps.config;

import com.datapublica.companies.config.converters.CurrencyFromStringMongoConverter;
import com.datapublica.companies.config.converters.CurrencyToStringMongoConverter;
import com.datapublica.companies.util.HostList;
import com.datapublica.companies.util.MongoTemplateExtended;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Configuration
@EnableMongoRepositories(basePackages = {"eu.researchalps.db.repository", "com.datapublica.companies.repository"}, mongoTemplateRef = "mongoTemplate")
@EnableMongoAuditing
@Profile("!test")
public class MongoConfiguration extends AbstractMongoConfiguration {

    @Value("${mongo.hosts:localhost}")
    private String hosts;
    @Value("${mongo.user:}")
    private String user;
    @Value("${mongo.pass:}")
    private String pass;
    @Value("${mongo.db:scanr}")
    private String db;
    @Value("${mongo.read.strategy:EXCLUSIVE}")
    private ReadStrategy readStrategy;

    /**
     * Should a read operation be authorized and how
     */
    public static enum ReadStrategy {
        // No fallback strategy is authorized (~ slaveOk=false)
        EXCLUSIVE,
        // Prefer primary but allow a fallback to secondary
        PRIMARY,
        // Prefer secondary but allow a fallback to primary
        SECONDARY,
        // Select a random host to answer a query
        RANDOM
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(
                Arrays.asList(
                        new CurrencyFromStringMongoConverter(),
                        new CurrencyToStringMongoConverter()
                )
        );
    }

    @Override
    public MongoTemplateExtended mongoTemplate() throws Exception {
        MongoTemplateExtended mongoTemplate = new MongoTemplateExtended(mongoDbFactory(), mappingMongoConverter());
        mongoTemplate.setWriteConcern(WriteConcern.SAFE);
        return mongoTemplate;
    }

    @Override
    protected String getDatabaseName() {
        return db;
    }

    @Override
    public MongoClient mongo() {
        List<ServerAddress> parsed = HostList.parse(hosts, (host, port) -> {
            try {
                if (port == null)
                    return new ServerAddress(host);
                else
                    return new ServerAddress(host, port);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        });

        List<MongoCredential> credentials = null;
        if (!user.isEmpty()) {
            credentials = Collections.singletonList(MongoCredential.createScramSha1Credential(user, db, pass.toCharArray()));
        }
        MongoClient mongo;
        if (parsed.size() > 1) {
            mongo = new MongoClient(parsed, credentials);
            mongo.setReadPreference(getReadPreference());
        } else {
            mongo = new MongoClient(parsed.get(0), credentials);
        }
        return mongo;
    }

    private ReadPreference getReadPreference() {
        ReadPreference preference = null;
        switch (readStrategy) {
            case EXCLUSIVE:
                preference = ReadPreference.primary();
                break;
            case PRIMARY:
                preference = ReadPreference.primaryPreferred();
                break;
            case SECONDARY:
                preference = ReadPreference.secondaryPreferred();
                break;
            case RANDOM:
                preference = ReadPreference.nearest();
                break;
        }
        return preference;
    }

    @Override
    protected String getMappingBasePackage() {
        return "com.datapublica";
    }
}
