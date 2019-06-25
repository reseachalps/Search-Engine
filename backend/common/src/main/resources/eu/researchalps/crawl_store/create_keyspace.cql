CREATE KEYSPACE IF NOT EXISTS %s
WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': 1
};
