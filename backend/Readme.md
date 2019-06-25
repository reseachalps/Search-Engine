### Backend

This is the API behind all the search engine and the workflow of the Re-search alps engine.

This is a gradle project. The main modules are `app` and `workflow` which are spring boot runnables.

### Configuration

```
server.port=8080 # Http server port

elasticsearch.hosts=localhost # Elasticsearch node list
elasticsearch.cluster=elasticsearch # Cluster name
elasticsearch.create=true

mongo.hosts=localhost # Mongo server list
mongo.db=researchalps
mongo.user=ra
mongo.pass=
mongo.read.strategy=PRIMARY

rabbit.hosts=localhost
rabbit.user=guest
rabbit.pass=guest

cassandra.hosts=localhost
cassandra.keyspace=crawl_store

screenshot.storage=/tmp/screenshots
```
