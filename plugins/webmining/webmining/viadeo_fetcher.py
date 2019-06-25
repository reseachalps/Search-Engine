import re
import datetime
from html.parser import HTMLParser

from textmining import normalizer

# Used to enable launch as a main
import os.path, sys

sys.path.insert(0, os.path.abspath('.'))
from webmining.fetcher import Fetcher
from webmining.html5wrapper import HTML5Wrapper


class ViadeoPosition:
  """
  Structured career position.
  """


  def __init__(self, html, wrapper):
    self.position = None
    self.company = None
    self.start_date = None
    self.end_date = None
    self.is_current = None
    self.summary = None
    self.wrapper = wrapper

    self._extract_result(html)


  def _extract_result(self, html):

    # Position
    m = html("div.title p")
    if m.length > 0:
      self.position = self.wrapper.text(m.eq(0))

    # Company
    # <span itemprop="worksFor" itemscope="" itemtype="http://schema.org/Organization"><span itemprop="name"><a href="http://fr.viadeo.com/fr/company/data-publica" itemprop="url" class="itemName" title="Data Publica">Data Publica</a>
    m = html("a.itemName")
    if m.length > 0:
      self.company = self.wrapper.text(m)

    # Start date
    m = html(".dtstart")
    if m.length > 0:
      self.start_date = self._parse_date(m.attr.title.strip())

    # End date
    m = html(".dtend")
    if m.length > 0:
      self.is_current = html(".stillIntrue").length > 0
      if not self.is_current:
        self.end_date = self._parse_date(m.attr.title.strip())


    # Summary
    m = html(".description")
    if m.length > 0:
      self.summary = self.wrapper.text(m)


  def _parse_date(self, text):
    """
    Parsing date from a Viadeo profile.
    Extracted format is yyyy-MM-dd.
    """
    if len(text) == 4:
      return datetime.datetime.strptime(text, "%Y")
    if len(text) == 7:
      return datetime.datetime.strptime(text, "%Y-%m")
    if len(text) == 10:
      return datetime.datetime.strptime(text, "%Y-%m-%d")
      #
    print("Cannot parse date [" + text + "]")
    return None


  def __str__(self):
    _str = "Position=[%s]" % (self.position)
    _str += " Company=[%s]" % (self.company)
    _str += " Start=[%s]" % (self.start_date)
    if self.end_date is not None:
      _str += " End=[%s]" % (self.end_date)
    _str += " Current=[%s]" % (self.is_current)
    if self.summary is not None:
      _str += " Summary=[%s]" % (self.summary.replace("\n", "").strip())
    return _str


class ViadeoResult:
  """
  Structured viadeo profile extraction
  """

  def __init__(self, html, url, wrapper):
    self.firstname = None
    self.lastname = None
    self.title = None
    self.bio = None
    self.positions = []
    self.profile_url = url
    self.wrapper = wrapper

    self._extract_result(html)

  def _extract_result(self, html):

    # firstname
    m = html("span.firstname")
    if m.length > 0:
      self.firstname = m.text().strip()

    # lastname
    m = html("span.lastname")
    if m.length > 0:
      self.lastname = m.text().strip()

    # Position
    m = html(".bd h3")
    if m.length > 0:
      self.title = self.wrapper.text(m)

    # Bio
    # <p class="summary detailsProfil">...</p>
    m = html(".detailResume p")
    if m.length > 0:
      self.bio = self.wrapper.text(m)

    # Positions
    for m in html(".blockitemEmployment").items():
      self.positions.append(ViadeoPosition(m, self.wrapper))

  def __str__(self):
    if self.bio is not None:
      bio = self.bio.replace('\n', '')
    else:
      bio = self.bio
    _str = "Firstname=[%s] Lastname=[%s] Title=[%s] Bio=[%s] Url=[%s] Positions=[" % \
           (self.firstname, self.lastname, self.title, bio, self.profile_url)
    for position in self.positions:
      _str += "\n  * " + str(position)
    _str += "\n]"
    return _str


class ViadeoFetcher:
  LANG = "(com|de|en|es|fr|gb|it)"

  def __init__(self, proxy=None):
    self.fetcher = Fetcher(proxy=proxy)
    self.wrapper = HTML5Wrapper()
    self.normalizer = normalizer.Normalizer()
    self.valid_domains = re.compile("^.*.viadeo." + self.LANG + "$")
    self._rpath1 = re.compile("^\\/" + self.LANG + "\\/profile\\/([^\\/]+).*$")
    self._rpath2 = re.compile("^\\/r\\/profile\\/([^\\/]+)\\/" + self.LANG + "\\/public(\\/.*)?$")
    self._rtitle = re.compile("^([^,]+).*$")

  def validate_url(self, domain, path):
    """
    Validates if an url is a viadeo profile or not.
    param: domain: The URL domain
    param: path: The URL path
    return: true/false
    """
    # Valid domain and profile path
    return self.valid_domains.match(domain) is not None and \
           (self._rpath1.match(path) is not None or self._rpath2.match(path) is not None)

  def validate_contact(self, title, firstname, lastname):
    """
    Validates if the profile page corresponds to the specified contact.
    param: title: The page title
    param: firstname: The contact first name
    param: lastname: The contact last name
    return: True if the page corresponds to the specified contact, False otherwise
    """
    # Extract name from title
    title = title.replace("<b>", "").replace("</b>", "")
    m = self._rtitle.search(title)
    # Matching title
    if m is not None:
      return self.normalize_name(m.group(1)) == self.normalize_name(firstname + lastname)
      # Invalid
    return False

  def normalize_name(self, name):
    """
    Normalize a name for comparison
    param: name: The name to normalize
    return: The normalized name for comparison (firstname + lastname, lowercase ASCII, withtout separators)
    """
    text = re.sub('[\-0-9\s]+', '', name)
    text = self.normalizer.normalize_text(text)
    return text

  def parse(self, fr):
    html = self.wrapper.pq(fr.webpage)
    lr = ViadeoResult(html, url=fr.fetched_url, wrapper=self.wrapper)
    return lr

  def extract_profile(self, url):
    """
    Fetches profile URL and cleans html.
    """
    # Extract profile
    fr = self.fetcher.fetch(url, debug=False)
    if fr is None or fr.webpage is None:
      return None

    return self.parse(fr)

