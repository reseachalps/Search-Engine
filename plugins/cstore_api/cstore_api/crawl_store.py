import datetime
import logging
from collections import namedtuple
from .cassandra_wrapper import Cassandra
import uuid


CrawlInfo = namedtuple("CrawlInfo",
                       ["id",
                        "url", "depth", "max_pages", "status",
                        "start_date", "end_date",
                        "page_count", "histogram", "main_lang"])


CrawlData = namedtuple("CrawlData",
                       ["crawl_id", "domain", "url", "http_status", "depth", "crawl_date",
                        "headers", "content_type", "charset",
                        "title", "content", "relevant_txt", "lang"])


class CrawlStore:
    STATUS_IN_PROGRESS = "IN_PROGRESS"
    STATUS_COMPLETED = "COMPLETED"

    def __init__(self, conf, init_index=False):
        self.client = Cassandra(conf)

        # Logging initialization
        self.logger = logging.getLogger("crawl_store_api")
        self.logger.setLevel(logging.INFO)

    def get_crawl_by_url(self, url, depth=2, max_pages=80):
        """
        Returns the crawl for the given url (or None)
        """
        # Build search query
        query = "SELECT id FROM crawl_info WHERE url = ? AND depth = ? AND max_pages = ?"
        crawl_id = self.client.query_one(query, [url, depth, max_pages], Cassandra.ONE)

        if crawl_id is None:
            self.logger.warn("No crawl for this URL [%s]" % url)
            return None

        crawl_id = crawl_id["id"]
        query = "SELECT * FROM crawl_data WHERE crawl_id = ?"
        return [CrawlData(**p) for p in self.client.query(query, [crawl_id], Cassandra.ONE)]

    def get_crawl_histogram_by_url(self, url, depth=2, max_pages=80):
        """
        Returns the crawl histogram for the given url (or None)
        """
        query = "SELECT histogram FROM crawl_info WHERE url = ? AND depth = ? AND max_pages = ?"
        histogram = self.client.query_one(query, [url, depth, max_pages], Cassandra.ONE)

        if histogram is None:
            self.logger.warn("No crawl for this URL [%s]" % url)
            return None

        return histogram["histogram"]

    def get_crawl_info(self, url, depth=2, max_pages=80, get_histogram=True):
        cols = list(CrawlInfo._fields)
        if not get_histogram:
            cols.remove("histogram")
        cols = ", ".join(cols)

        query = "SELECT %s FROM crawl_info WHERE url = ? AND depth = ? AND max_pages = ?" % cols
        crawl_info = self.client.query_one(query, [url, depth, max_pages])
        if crawl_info is not None:
            if not get_histogram:
                crawl_info["histogram"] = None
            return CrawlInfo(**crawl_info)
        return None

    def begin_crawl(self, url, depth=2, max_pages=80):
        """
        Start a crawl:
            - First create the crawl_info record
            - Set status to STATUS_IP
            - Clean the crawl_data if needed
            - Return the crawl_id (to push subsequent pages)
        """
        # Retrieve crawl id
        crawl_info = None

        crawl_info = self.get_crawl_info(url, depth, max_pages, get_histogram=False)
        if crawl_info is not None:
            self.client.query("DELETE FROM crawl_data WHERE crawl_id = ?", [crawl_info.id])
        else:
            crawl_info = CrawlInfo(id=uuid.uuid4(),
                                   url=url, depth=depth, max_pages=max_pages,
                                   status=None, start_date=None, end_date=None,
                                   page_count=None, histogram=None, main_lang=None)

        crawl_info = crawl_info._replace(start_date=datetime.datetime.now(),
                                         status=CrawlStore.STATUS_IN_PROGRESS,
                                         end_date=None,
                                         page_count=None,
                                         histogram=None,
                                         main_lang=None)

        self._insert_crawl_info(crawl_info)

        return crawl_info

    def push_page(self, crawl_data):
        """
        Push a crawl page into the crawl_store
        """
        return self._insert_crawl_data(crawl_data)

    def end_crawl(self, crawl_info, page_count, histogram, main_lang):
        """
        End the crawl:
            - Update status
            - Set end date
            - Set page count
            - Insert the histogram
        """
        # Update object
        crawl_info = crawl_info._replace(status=CrawlStore.STATUS_COMPLETED,
                                         end_date=datetime.datetime.now(),
                                         page_count=page_count,
                                         histogram=histogram,
                                         main_lang=main_lang)

        self._insert_crawl_info(crawl_info)

    def _insert_crawl_info(self, crawl_info, *args, **kwargs):
        as_dict = vars(crawl_info)
        fields = ', '.join(f for f in as_dict.keys())
        markers = ', '.join('?' for f in as_dict.keys())
        self.client.query("INSERT INTO crawl_info (%s) VALUES (%s)" % (fields, markers), params=as_dict, *args, **kwargs)

    def _insert_crawl_data(self, crawl_data, *args, **kwargs):
        as_dict = vars(crawl_data)
        fields = ', '.join(f for f in as_dict.keys())
        markers = ', '.join('?' for f in as_dict.keys())
        self.client.query("INSERT INTO crawl_data (%s) VALUES (%s)" % (fields, markers), params=as_dict, *args, **kwargs)

