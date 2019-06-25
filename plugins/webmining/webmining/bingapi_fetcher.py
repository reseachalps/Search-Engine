
__author__ = "Guillaume Lebourgeois"
__email__ = "guillaume.lebourgeois@data-publica.com"
__status__ = "dev"

import urllib.parse
import base64
import json

# Used to enable launch as a main
import os.path
import sys
import logging
sys.path.insert(0, os.path.abspath('.'))

from webmining.fetcher import Fetcher
from webmining.html5wrapper import HTML5Wrapper


class EmptyBingAPIAccount(Exception):
    def __init__(self, message):
        super(EmptyBingAPIAccount, self).__init__(message)
        self.message = message


class BingAPIResult:
    """
    Structured bing search result
    """

    def __init__(self, json):
        self.title = json["Title"]
        self.url = json["Url"]
        self.snippet = json["Description"]

    def __str__(self):
        s = "[Title : " + str(self.title) + "] "
        s += "[URL : " + str(self.url) + "] "
        s += "[Snippet : " + str(self.snippet) + "] "

        return s


class BingAPIFetcher:
    """
    Fetches Bing results for a given query
    """
    def __init__(self, key, proxy=None):

        self.base_url = "https://api.datamarket.azure.com/Bing/SearchWeb/v1/Web?$format=json&"
        # Building authentification from key
        s = '%s:%s' % (key, key)
        credentials = base64.b64encode(s.encode('utf-8'))
        self.auth = 'Basic %s' % credentials.decode('utf-8')

        # Markets for localized and more accurate search
        self.markets = {"FR": "fr-FR", "BE": "fr-BE", "GB": "en-GB", "US": "en-US", "DE": "de-DE", "UK": "en-GB"}

        # Fetcher initialization
        self.fetcher = Fetcher(proxy=proxy)
        self.fetcher.headers["Authorization"] = self.auth

        # Logging initialization
        self.logger = logging.getLogger("webmining:bingapi_fetcher")
        self.logger.setLevel(logging.INFO)
        self.wrapper = HTML5Wrapper()

    def parse(self, webpage, bresults):

        # We check out API account is not empty. This is tricky, as in this case the Bing API
        # speaks in plain-text and no more in json.
        if webpage.strip() == "Insufficient balance for the subscribed offer in user's account":
            raise EmptyBingAPIAccount("Insufficient balance for the subscribed offer in user's account")
        json_result = json.loads(webpage, encoding="utf-8")
        result_list = json_result['d']['results']

        if webpage is not None:
            for r in result_list:
                br = BingAPIResult(r)
                bresults.append(br)

        return webpage

    def fetch(self, q, start=0, country="FR"):
        """
        Fetches Bing with the query q and sends back
        a list of results.
        param: q: a query, as a string
        param: start: the starting offset (first 50 results are start=0, next 50 start=1, ...)
        param: country of the searched company
        return: a list of BingAPIResult
        """
        bresults = []
        # Simple quote parasite bing query parser
        q = q.replace("'", "")
        query = "'%s" % q
        query += "'"
        query = urllib.parse.urlencode({
            "Query": query,
            "$top": "50",
            '$skip': "%i" % (start * 50),
            'Market': "'%s'" % self.markets[country],
            'Options': "'DisableLocationDetection'",
        })
        url = self.base_url + query
        fr = self.fetcher.fetch(url, debug=False, force_encoding="utf-8")

        self.logger.debug("Fetched url [%s] start=%s" % (url, start))
        if fr is None or fr.webpage is None:
            self.logger.warn("Got nothing from [%s]" % url)
            return bresults

        self.logger.debug("Returned result - " + str(fr.fetched_url))
        self.parse(fr.webpage, bresults)

        self.logger.info("Fetched [%s] with %d results" % (url, len(bresults)))

        return bresults
