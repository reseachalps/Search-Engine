
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

class BingResult:
  """
  Structured bing search result
  """

  def __init__(self, html, wrapper):
    self.title = None
    self.url = None
    self.snippet = None
    self.wrapper = wrapper

    self._extract_result(html)
  
  """
  <li class="sa_wr"><div class="sa_cc" u="0|5060|4857780905183668|7MTe2ZIMGJ6hEHuT80NLxDikJXRlA8ja"><div Class="sa_mc"><div class="sb_tlst"><h3><a href="http://www.data-publica.com/" h="ID=SERP,5106.1"><strong>Data</strong> <strong>Publica</strong> | Les donn&#233;es pour votre business</a></h3></div><div class="sb_meta"><cite>www.<strong>data</strong>-<strong>publica</strong>.com</cite><span class="c_tlbxTrg"><span class="c_tlbxH" H="BASE:CACHEDPAGEDEFAULT" K="SERP,5108.1"></span></span></div><p><strong>Data</strong> <strong>Publica</strong>, l'annuaire des donn&#233;es en France ... Nos produits et services Le catalogue gratuit de publications 15 800 jeux de donn&#233;es dont 4300 visualisables</p></div></div></li>
  """
  def _extract_result(self, html ):
    # Searching title and page_url
    m = html("h3 a[h]")
    if m.length != 0:
      self.title = m.text().strip()
      self.url = urllib.parse.unquote(m.attr.href)

    # Searching snippet
    m = html("p")
    if m.length != 0:
      self.snippet = self.wrapper.get_text_from_subtree(m)

  def __str__(self):
    s = "[Title : " + str(self.title) + "] "
    s += "[URL : " + str(self.url) + "] "
    s += "[Snippet : " + str(self.snippet) + "] "

    return s

class BingFetcher:
  """
  Fetches Bing results for a given query
  """
  def __init__(self, tld="fr", proxy=None):
    self.fetcher = Fetcher(proxy=proxy)
    self.cookie = {"_FS": "NU=1&mkt=fr-FR&ui=fr-FR"}
    # No tld based differences in bing, the tld will be ignored
    self.base_url = "http://www.bing.com/search?qs=n&form=QBLH&filt=all&sc=0-13&sp=-1&sk=&pq="

    # Logging initialization
    self.logger = logging.getLogger("webmining:bing_fetcher")
    self.logger.setLevel(logging.INFO)
    self.wrapper = HTML5Wrapper()

  def parse(self, webpage, bresults, limit):
    webpage = self.wrapper.pq( webpage )
    if webpage is not None:
      for r in webpage("li.sa_wr").items():
        gr = BingResult(r, self.wrapper)
        bresults.append(gr)

        if len(bresults) >= limit:
          break
    return webpage

  def fetch(self, q, limit=10, start=0):
    """
    Fetches Bing with the query q and sends back 
    a list of results.
    param: q: a query, as a string
    param: limit: the amount of results needed (1 to 10)
    param: start: the starting offset
    return: a list of BingResult
    """
    bresults = []
    # NB: the parameter to augment the amount of results is 'count'
    query = urllib.parse.urlencode( { "q": q, "first": start } )
    url = self.base_url + query
    fr = self.fetcher.fetch(url, debug=True, cookies=self.cookie)
   
    self.logger.debug("Fetched url [%s]" % url)
    if fr is None or fr.webpage is None:
      self.logger.warn("Got nothing from [%s]" % url)
      return bresults
    
    self.logger.debug("Returned result - " + str(fr.fetched_url))
    fr.webpage = self.parse(fr.webpage, bresults, limit)
    
    self.logger.info("Fetched [%s] with %d results" % (url, len(bresults)))
    return bresults

