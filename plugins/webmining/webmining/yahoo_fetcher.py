
__author__ = "Guillaume Lebourgeois"
__email__ = "guillaume.lebourgeois@data-publica.com"
__status__ = "dev"

import urllib.parse

# Used to enable launch as a main
import os.path, sys 
import logging
sys.path.insert(0, os.path.abspath('.'))

from webmining.fetcher      import Fetcher
from webmining.html5wrapper import HTML5Wrapper

class YahooResult:
  """
  Structured bing search result
  """

  def __init__(self, html, wrapper):
    self.title = None
    self.url = None
    self.snippet = None
    self.wrapper = wrapper

    self._extract_result(html)
  
  def _extract_result(self, html ):
    # Searching title and page_url
    m = html("h3 a[data-bk]")
    if m.length != 0:
      self.title = m.text().strip()
      url = m.attr.href
      index = url.find("/**")
      self.url = url[index + 3:]
      self.url = urllib.parse.unquote(self.url)

    # Searching snippet
    m = html(".abstr, .sm-abs")
    if m.length != 0:
      self.snippet = self.wrapper.text(m)

  def __str__(self):
    s = "[Title : " + str(self.title) + "] "
    s += "[URL : " + str(self.url) + "] "
    s += "[Snippet : " + str(self.snippet) + "] "

    return s

class YahooFetcher:
  """
  Fetches Yahoo results for a given query
  """
  def __init__(self, tld="fr", proxy=None):
    self.fetcher = Fetcher(proxy=proxy)
    # No tld based differences in bing, the tld will be ignored
    # http://fr.search.yahoo.com/search?p=LE+PAC+DECOUPE+PORTET+SUR+GARONNE&toggle=1&cop=mss&ei=UTF-8&fr=yfp-t-703
    self.base_url = "http://fr.search.yahoo.com/search?toggle=1&cop=mss&ei=UTF-8&fr=yfp-t-703&"

    # Logging initialization
    self.logger = logging.getLogger("webmining:yahoo_fetcher")
    self.logger.setLevel(logging.INFO)
    self.wrapper = HTML5Wrapper()

  def parse(self, webpage, bresults, limit):
    webpage = self.wrapper.pq( webpage )
    if webpage is not None:
      for r in webpage(".res").items():
        gr = YahooResult(r, self.wrapper)
        bresults.append(gr)

        if len(bresults) >= limit:
          break
    return webpage

  def fetch(self, q, limit=10, start=0):
    """
    Fetches Yahoo with the query q and sends back 
    a list of results.
    param: q: a query, as a string
    param: limit: the amount of results needed (1 to 10)
    param: start: the starting offset
    return: a list of YahooResult
    """
    bresults = []
    # NB: the parameter to augment the amount of results is 'count'
    query = urllib.parse.urlencode( { "p": q} )
    url = self.base_url + query
    fr = self.fetcher.fetch(url, debug=True)
   
    self.logger.debug("Fetched url [%s]" % url)
    if fr is None or fr.webpage is None:
      self.logger.warn("Got nothing from [%s]" % url)
      return bresults
    
    self.logger.debug("Returned result - " + str(fr.fetched_url))
    f = open("index.html", "w")
    f.write(fr.webpage)
    f.close()
    fr.webpage = self.parse(fr.webpage, bresults, limit)
    
    self.logger.info("Fetched [%s] with %d results" % (url, len(bresults)))
    return bresults

