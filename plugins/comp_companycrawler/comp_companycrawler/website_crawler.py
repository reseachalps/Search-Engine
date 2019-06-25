import logging
from urllib.parse import urlparse
from webmining import crawler
from webmining.fetcher import Proxy
from textmining.stemmer import Stemmer
from cstore_api.crawl_store import CrawlData, CrawlStore
from datetime import datetime
from langdetect import detect as lang_detect
from langdetect.lang_detect_exception import LangDetectException

from comp_companycrawler.webentity import WebEntity
from comp_companycrawler import LIB_PATH

PAGE_SIZE_LIMIT = 300000
# Crawling modes:
entire = crawler.CrawlMode.entire
single = crawler.CrawlMode.single
subpath = crawler.CrawlMode.subpath


class WebsiteCrawler:
    """
    Given an entity name or URL, tries to fetch the maximum of
    information possible from the web.
    """

    def __init__(self, proxy, cs):
        if proxy is not None and "url" in proxy:
            self.proxy = Proxy(url=proxy["url"], username=proxy["username"], password=proxy["password"])
        else:
            self.proxy = None
        self.cs = CrawlStore(conf=cs)
        self.stemmer = Stemmer()

        # Logging initialization
        self.logger = logging.getLogger("company_crawler")
        self.logger.setLevel(logging.INFO)
        file_handler = logging.FileHandler(filename=LIB_PATH + "../log/log.txt")
        file_handler.setFormatter(logging.Formatter('[%(levelname)s][%(name)s][%(asctime)s] %(message)s'))

        self.logger.addHandler(file_handler)

    def _crawl_entity(self, url_, crawl_info, crawl_store, domain_depth=2, page_limit=80, metas_=None, mode=entire, 
                      page_size=PAGE_SIZE_LIMIT):

        """
        Gather information about an entity
        """
        url = None
        
        report = WebEntity()
        report["summary"] = []
        report["countpage"] = 0
        report["main_lang"] = ""
        report["languages"] = {}

        if not self._is_url(url_):
            self.logger.warn("[%s] is not an URL" % url_)
            # Turns report from a list to an empty histogram
            report.normalize(self.stemmer)
            return report
        else:
            url = url_

        dom = self._get_domain(url)
        report["url"] = url
        report["domain"] = dom

        self.logger.info("Launching crawl on url [%s] at depth %d" % (url, domain_depth))

        # ---
        # Crawling
        # ---
        vacuum = crawler.Crawler(seedlist=[(0, url)], debug=False, proxy=self.proxy, mode=mode, max_page_size=page_size)
        for p in vacuum.crawl(proc=None, domain_depth=domain_depth, crawl_depth=0, page_limit=page_limit,
                              wait_courtesy=0.5, html2txt=False, metas=None):
            lang = ""
            if p.relevant_txt is not None:
                if len(p.relevant_txt) > 0:
                    # In some cases, langdetecter has not enough features in text to
                    # detect language (Ex. : {"url":"http://nwglobalvending.be","country":"BE"})
                    try:
                        lang = lang_detect(p.relevant_txt).upper()
                    except LangDetectException:
                        self.logger.warning("Impossible to detect language in page %s" % p.url)

                # Manage fucked up languages
                if lang == "AF":
                    lang = "NL"

                # Counts lang repartition in website
                if lang in report["languages"]:
                    report["languages"][lang] += 1
                else:
                    report["languages"][lang] = 1

                report["summary"].append((lang, p.relevant_txt))

            page = CrawlData(crawl_id=crawl_info.id, url=p.url, domain=self._get_domain(p.url),
                             charset=p.charset, http_status=p.http_status, headers=p.headers,
                             depth=p.depth, content_type=p.content_type, crawl_date=datetime.now(),
                             title=p.title, content=p.html, relevant_txt=p.relevant_txt, lang=lang)

            crawl_store.push_page(page)
            report["countpage"] += 1

        # Crawl is over, let's normalize website
        # Stemmer is heavy to instanciate, that's why we pass it as a reference
        report.normalize(self.stemmer)

        return report

    def crawl(self, website, depth=2, page_max=80, mode=entire, page_size=PAGE_SIZE_LIMIT):
        """
        For now, just open a list of entities and then compute them
        """
        url = website["url"]

        crawl_info = self.cs.begin_crawl(url=url, depth=depth, max_pages=page_max)
        self.logger.info("Initiating crawl ID [%s] for url [%s]" % (crawl_info.id, website["url"]))
        report = self._crawl_entity(url, crawl_info, self.cs, domain_depth=depth, page_limit=page_max, mode=mode, 
                                    page_size=page_size)
 
        # Cutting histogram to a sustainable size
        # Arbitrary chosen 1000
        freq = list(report["summary"].items())
        freq.sort(key=lambda e: e[1], reverse=True)
        freq = dict(freq[:1000])

        self.cs.end_crawl(crawl_info, page_count=report["countpage"],
                          histogram=freq, main_lang=report["main_lang"])
        self.logger.info("Crawl [%s] ended with %d pages, and main language %s." % (crawl_info.id, report["countpage"], report["main_lang"]))

        return report

    def _get_domain(self, url):
        """
        Returns domain from URL
        """
        o = urlparse(url)

        if len(o.netloc) < 1:
            return None
        else:
            return o.netloc

    def _get_domain_url(self, url):
        """
        Builds a domain url from an url, which can be leading to a particular
        webpage.
        """
        o = urlparse(url)
        return o.scheme + "://" + o.netloc

    def _is_url(self, url):
        """
        Determines if a text element is plain text, or
        an URL.
        """
        o = urlparse(url)

        if len(o.netloc) < 1:
            return False
        else:
            return True
