
import time
from queue import Queue as Queue
from multiprocessing import Queue as MPQueue
from urllib.parse    import urlparse

__author__ = "Guillaume Lebourgeois"
__email__ = "guillaume.lebourgeois@data-publica.com"
__status__ = "dev"

class SeedElement:
  """
  A seed entry with its meta information.
  """

  def __init__(self, url, groupid):
    self.groupid = groupid # To which group of page comes this page
    self.url = url
    self.depth = 0       # reached depth    
    self.domain = urlparse( url ).netloc
    
    # Updated by crawler
    self.html = None
    self.relevant_txt = None
    self.title = None
    self.headers = {}
    self.charset = None
    self.content_type = None
    self.http_status = None

    # Updated by external process
    self.score = 0.0
    self.data = None
    self.metas = {}

  def __str__(self):
    s = ""
    s += "[%s]\n" % self.url 
    s += "Depth     : %d\n" % self.depth 
    s += "Domain    : %s\n" % self.domain 
    s += "HTML size : %d\n" % len(self.html)

    return s

  def __eq__(self, y):
    return self.__dict__ == y.__dict__


class Seed:
  """
  A seed used by the crawler to initiates itself.
  Once created, its queue can be publicly accessed.

  Seed can only be loaded from a file for now, db access 
  will bee added later.
  """
  def __init__(self, f=None, s=None, multiproc=True):
    """
    :param f: seed filename
    :param s: a list containing seed elements, can be used in replacement of a seed file
    """
    if multiproc:
      self.q = MPQueue()
    else:
      self.q = Queue()

    if f is not None:
      self._load( f )
      # waits to let the buffer fill the queue
      # only usefull when little amount of data
      if multiproc:
        time.sleep( 2 )
    
    elif s is not None:
      for l in s:
        id_ = l[0]
        url_ = l[1]
        url_ = url_.strip()
        se = SeedElement( url_, id_ )

        self.q.put( se, block=False )
      # waits to let the buffer fill the queue
      # only usefull when little amount of data
      if multiproc:
        time.sleep( 2 )

  def _load(self, filename):
    """
    Loads URLs from a file.
    An url per line.
    """
    f = open( filename ) 
    count = 0
    for l in f:
      l = l.strip()
      se = SeedElement(l, groupid=count)
      self.q.put( se, block=False )
      count += 1

    f.close()



