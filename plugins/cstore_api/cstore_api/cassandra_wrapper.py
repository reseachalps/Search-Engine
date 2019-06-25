from cassandra.cluster import Cluster, NoHostAvailable
from cassandra.query import dict_factory
from cassandra import ConsistencyLevel, Unavailable, Timeout
import logging
import traceback
import time
import random


class Cassandra:
    class Timeout(Exception):
        pass

    class UnableToConnect(Exception):
        pass

    ONE = ConsistencyLevel.ONE
    QUORUM = ConsistencyLevel.QUORUM

    def __init__(self, conf, retry=3, sleep=60):
        self.logger = logging.getLogger("crawl_store_api")
        self.logger.setLevel(logging.INFO)

        self.conf = conf
        self.cluster = None
        self.session = None
        self.query_cache = None
        self.connect(retry, sleep)

    def connect(self, retry=3, sleep=60):
        for i in range(retry):
            try:
                if self.cluster is not None:
                    self.session = None
                    self.cluster.shutdown()
                    self.cluster = None

                hostnames = self.conf["hostnames"]
                random.shuffle(hostnames)
                self.cluster = Cluster(hostnames, protocol_version=3)
                self.session = self.cluster.connect(self.conf["keyspace"])
                self.session.row_factory = dict_factory
                self.query_cache = {}
                return

            except NoHostAvailable:
                self.logger.warn("No known Cassandra node available.")
                self.logger.warn("Sleeping %s" % sleep)
                time.sleep(sleep)
                continue

        raise Cassandra.UnableToConnect()

    def prepare_query(self, query, consistency):
        key = (query, consistency)

        prepared = self.query_cache.get(key)
        if prepared is None:
            prepared = self.session.prepare(query)
            prepared.consistency_level = consistency
            self.query_cache[key] = prepared

        return prepared

    def query(self, query, params, consistency=ConsistencyLevel.QUORUM, retry=3, timeout=60, sleep=60):
        for i in range(retry):
            try:
                prepared = self.prepare_query(query, consistency)
                return self.session.execute(prepared, params, timeout=timeout)

            except Unavailable:
                # http://datastax.github.io/python-driver/api/cassandra.html#cassandra.Unavailable
                self.logger.warn("There were not enough live replicas to satisfy the requested consistency level")
                self.logger.warn("Sleeping %s" % sleep)
                time.sleep(sleep)
                continue

            except Timeout:
                # http://datastax.github.io/python-driver/api/cassandra.html#cassandra.Timeout
                self.logger.warn("Replicas failed to respond to the coordinator node before timing out.")
                self.logger.warn("Sleeping %s" % sleep)
                time.sleep(sleep)
                continue

            except:
                self.logger.warn(traceback.format_exc())
                self.logger.warn("Sleeping %s" % sleep)
                time.sleep(sleep)
                self.connect(retry, timeout)
                continue

        raise Cassandra.Timeout()

    def query_one(self, *args, **kwargs):
        result = self.query(*args, **kwargs)
        if len(result) == 0:
            return None
        assert len(result) == 1
        return result[0]
