import logging
import re

from pyquery import PyQuery as pq

from webmining import html5wrapper
from cstore_api.crawl_store import CrawlStore
from entities_extractor.entities import Entities, Entity
from scanr_entityextractor import LIB_PATH
import scanr_entityextractor.entityprovider as etp

def entity_sort(match):
    return -len(re.sub("\\s+", "", match.value.entity)), match.value.entity


class NotFoundCrawl(Exception):
    def __init__(self, url):
        super().__init__("Could not fetch crawl of url [%s]" %url)


class NamedEntityDetecter:
    def __init__(self, api_conf, cs):
        self.entityprovider = etp.EntityProvider(api_conf)
        self.cs = CrawlStore(cs)
        self.htmlwrapper = html5wrapper.HTML5Wrapper()

        # Logging initialization
        self.logger = logging.getLogger("named_entity_detecter")
        self.logger.setLevel(logging.INFO)
        file_handler = logging.FileHandler(filename=LIB_PATH + "../log/log.txt")
        file_handler.setFormatter(logging.Formatter('[%(levelname)s][%(name)s][%(asctime)s]  %(message)s'))
        self.logger.addHandler(file_handler)

    def _extract_text_from_crawl(self, url, depth=4, page_max=300):
        crawl = self.cs.get_crawl_by_url(url, depth, page_max)
        if not crawl:
           raise NotFoundCrawl(url)
        
        # extracting text and raw html
        text, html = "", "" 
        for page in crawl:
            text += self.htmlwrapper.text(self.htmlwrapper.pq(page.content))
            text += "\n"
            html += page.content
            html += "\n"
        return text, html

    def detect(self, url, text, entities, method):
        """
        Detect entities in the given url 

        :param text: The url from which we'll extract the text
        :return: A list of {label: , pos: } dictionaries. label be an entity's label, pos\
                 be a couple of text indices
        """

        matches = entities.match(text)

        # Sort the result matches to present the largest one first
        matches = sorted(matches, key=entity_sort)

        # Construct the result
        m = [{"id": m.value.data, "pos": m.pos} for m in matches]
        
        if method == "EXACT": 
            log_format = str(["(id %s, pos %s)" % (e["id"], str(e["pos"])) for e in m]) 
        else:
            log_format = str(["(id %s, pos %s) with semantic context [%s]" % (e["id"], str(e["pos"]), 
                             etp.normalization(text)[int(e["pos"][0]) - 30:int(e["pos"][1]) + 30]) for e in m]) 

        if len(m) > 0: 
            self.logger.info("Text of url [%s] matches with %s" % (url, log_format))
        else:
            self.logger.info("Text of url [%s] doesn't match any %s entity." % (url, method.lower()))
       
        return m

    def detect_labels(self, url, text, automaton):
        
        # matching using ahocorasick algorithm
        norm_text = etp.ascii_normalization(text)

        matches = [m for m in automaton.iter(norm_text)]
        m = [{"id": m[1][0], 
              "pos": m[0]} for m in matches]

        if len(m) > 0: 
            self.logger.info("Text of url [%s] matches with : %s" % (
            url,
            str(["(id %s, pos %s)" % (e["id"], str(e["pos"])) for e in m])
            ))
        else:
            self.logger.info("Text of url [%s] doesn't match with any long-label entity." % url)
       
        return m

    def detect_all(self, url):
        """
        Detect entities based on tags

        :param url: The url whose text will be matched against all entities
        :return: An aggregated list of {id: , pos: } dictionaries.
                 id be an entity id, pos be a couple of text indices
        """

        result = dict()
        self.logger.info("Extracting text for url [%s]..." % url)
        try:
            text, html = self._extract_text_from_crawl(url)
        except NotFoundCrawl as n:
            self.logger.warning("Crawl not found for url [%s]" % url)
            return []

        detect = []
        self.logger.info("Retrieving entities to match...")
        entities = self.entityprovider.get()

        self.logger.info("Starting detecting entities on url [%s]..." %url) 
        for method, ent in entities.items():

            if isinstance(ent, Entities):
                self.logger.info("Detecting %s entities..." % method.value.lower())
                detect.extend(self.detect(url, text, ent, method.value))
                if method == etp.MatchMethod("EXACT"):
                    self.logger.info("Detecting exact entities within raw html...")
                    detect.extend(self.detect(url, html, ent, method.value))
            else:
                self.logger.info("Detecting %s entities..." % method.value.lower())
                detect.extend(self.detect_labels(url, text, ent))
            
        if len(detect) > 0:
            #formating and deduplicating matched results
            self.logger.info("Detection done, now deduplicating the %d detected entities..." %len(detect))
            for d in detect:
                result[d["id"]] = d
                            
        self.logger.info("Text of url [%s] finaly matches with %s" % (
        url,
        str(["( %s %s )" % (r["id"][1], r["id"][0]) for r in result.values()])
        ))
              
        return list(result.values())
