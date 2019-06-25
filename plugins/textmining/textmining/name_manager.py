
import unicodedata
from textmining import LIB_PATH

class NameManager:
  """
  Tools to manage names
  """
  def __init__(self, firstnames=LIB_PATH + "resources/firstnames.txt"):
    # List of firstnames
    self.firstnames_path = firstnames
    self.firstnames = set()

    # Loading firstnames
    with open( self.firstnames_path ) as f:
      for name in f:
        nname = unicodedata.normalize('NFKD', name).encode('ASCII', 'ignore').decode()
        nname = nname.strip().lower()
        self.firstnames.add(nname)

  def is_firstname(self, token):
    """
    Normalizes a token and then says if it seems to be 
    a firstname or not.
    param: token: a string of one token
    return: Boolean
    """
    ntoken = unicodedata.normalize('NFKD', token).encode('ASCII', 'ignore').decode()
    ntoken = ntoken.strip().lower()

    if ntoken in self.firstnames:
      return True

    return False
