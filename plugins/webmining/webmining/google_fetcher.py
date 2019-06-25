
__author__ = "Guillaume Lebourgeois"
__email__ = "guillaume.lebourgeois@data-publica.com"
__status__ = "dev"

import urllib.parse, re

# Used to enable launch as a main
import os.path, sys 
sys.path.insert(0, os.path.abspath('.'))

from webmining.fetcher      import Fetcher
from webmining.html5wrapper import HTML5Wrapper

class GoogleResult:
  """
  Structured google search result
  """

  def __init__(self, html, wrapper):
    self.title = None
    self.url = None
    self.snippet = None
    self.wrapper = wrapper

    self._extract_result( html )

  def _extract_result(self, html ):
    # Searching title and page_url
    # <a href="/url?q=http://toto.fr/&amp;sa=U&amp;ei=PzqxT_DHD6TO4QTW5_SfCQ&amp;ved=0CCQQFjAA&amp;usg=AFQjCNF-MocZl9cnbj-ZYO3oe556LnQYOA"><b>Toto</b> - Vente de tissus africain</a>
    m = html("h3 a[href]")
    if m.length != 0:
      print(m.attr.href)
      urlmatch = re.search('.*/url\?.*&?url=(.*?)&.*', m.attr.href, flags=re.M|re.S)
      url = m.attr.href
      if urlmatch is not None:
        url = urllib.parse.unquote(urlmatch.group(1))

      self.title = m.text().strip()
      self.url = url

    # Searching snippet
    # <div class="s"><b>Toto</b> est un groupe de rock américain formé dans les années 1970 en Californie <br>  par Jeff Porcaro (batterie) et David Paich (clavier), auxquels se joignent Steve <b>...</b><br/><div>
    m = html("span.st")
    if m.length != 0:
      self.snippet = self.wrapper.get_text_from_subtree(m)

  def __str__(self):
    s = "[Title : " + str(self.title) + "] "
    s += "[URL : " + str(self.url) + "] "
    s += "[Snippet : " + str(self.snippet) + "] "

    return s


class GoogleFetcher:
  """
  Fetches google results for a given query
  """

  def __init__(self, tld="fr", proxy=None):
    self.fetcher = Fetcher(proxy=proxy)
    self.wrapper = HTML5Wrapper()
    self.base_url = "http://www.google.%s/search?rls=en&ie=UTF-8&oe=UTF-8&" % tld


  def parse(self, webpage, gresults, limit):
    webpage = self.wrapper.pq(webpage)
    #html = html.decode( "utf-8" )
    if webpage is not None:
      for r in webpage(".g").items():
        gr = GoogleResult(r, self.wrapper)
        gresults.append(gr)

        if len(gresults) >= limit:
          break
    return webpage

  def fetch(self, q, limit=10, start=0):
    """
    Fetches Google with the query q and sends back 
    a list of results.
    param: q: a query, as a string
    param: limit: the amount of results needed (1 to 10)
    param: start: the starting offset
    return: a list of GoogleResult
    """
    gresults = []
    query = urllib.parse.urlencode( { "q": q, "start": start } )
    url = self.base_url + query
    fr = self.fetcher.fetch( url, debug=True )
    
    if fr is None or fr.webpage is None:
      return gresults
    
    if fr.fetched_url.startswith("http://www.google.fr/sorry/"):
      raise GoogleBlacklistingError()

    fr.webpage = self.parse(fr.webpage, gresults, limit)
    return gresults


class GoogleBlacklistingError(Exception):
  """
  Exception raised when a blacklisting from Google is detected.
  """

  def __str__(self):
    return "Google fetcher has detected a blacklisting."

