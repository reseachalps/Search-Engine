
import re, unicodedata, html.parser
import csv

# Used to enable launch as a main
import os.path, sys 
sys.path.insert(0, os.path.abspath('.'))

from textmining.blacklist    import Blacklist
from textmining.name_manager import NameManager
from textmining              import LIB_PATH

class Contact:
  """Represents a contact extracted
  """
  def __init__(self):
    self.firstname = None
    self.lastname = None

    # position found, type of position, is it a referenced position ?
    self.position = None
    self.category = None
    self.referenced = None
    self.matched_position = None
    
    # Used when extracted in crawl to know at which depth 
    # contact was extracted.
    self.depth = 0
    self.source = ""

  def to_dict(self):
    d = {"firstname": self.firstname, "lastname": self.lastname, "position": self.position, \
         "category": self.category, "referenced": self.referenced, \
         "matched": self.matched_position, "depth": self.depth, "source": self.source}

    return d
     
  def __str__(self):
    s = str( self.firstname ) + " "
    s += str( self.lastname ) + ", "
    s += str( self.position ) 
    #s += " [%d:%s]" % (self.depth, self.source)

    return s

class ContactDetecter:
  """
  Detects structured contacts in text.
  """

  def __init__(self, firstnames=LIB_PATH + "resources/firstnames.txt",
                     positions=LIB_PATH + "resources/positions.txt",
                     ref_positions=LIB_PATH + "resources/ref_positions.csv"):
    self.unescaper = html.parser.HTMLParser()
    # List of firstnames
    self.nmanager = NameManager(firstnames=firstnames)
    
    # List of titles
    self.positions_path = positions
    self.positions = set()
    self.ref_positions = set()
    self.ref_position_cat = {} # Mapping between a reference position and its category

    # List of blacklisted tailing words
    self.blacklist = Blacklist()

    # address tokens we dont't want
    self.address_tokens = set( ["rue", "r", "avenue", "chemin", "av", "boulevard",
                                "bd", "chemin", "impasse"] ) 
    
    # Augmenting address tokens with contextual words we don't want
    self.address_tokens |= set( ["st", "saint"] )

    # Loading position tokens
    with open( self.positions_path ) as f:
      for pos in f:
        # Normalize position by lowering + removing accents
        normed_pos = pos.strip().lower()
        normed_pos = unicodedata.normalize('NFKD', normed_pos).encode('ASCII', 'ignore').decode()
        self.positions.add(normed_pos)

    # Loading position references
    with open(ref_positions) as f:
      csvreader = csv.reader(f, delimiter='\t')
      cat = None
      cats = range(1000, 16000, 1000)

      for row in csvreader:
        if int(row[0]) in cats:
          cat = row[3]
          continue

        for i in range(1, 4):
          normed_pos = row[i].strip().lower()
          normed_pos = unicodedata.normalize('NFKD', normed_pos).encode('ASCII', 'ignore').decode()
          self.ref_positions.add(normed_pos)
          self.ref_position_cat[normed_pos] = {"category": cat.strip(), "fullname": row[3].strip()}

    # Regexp combination
    re_title = "([a-zA-Z]+?\s)" # first part of contact title (optional)
    re_title2 = "([a-zA-Z]+?\s[\s&]{0,2})" # second part of contact title (optional)
    re_main_title = "[a-zA-Z]+?" # last part of contact title (obligatory)
    re_name_part = "([A-Z][-a-zA-Z]+?)" # name part

    # Handles the case where title is on the left of the name
    self.re_combined_left = "(" + re_title + "?" + re_title2 + "?" + re_main_title + ")[\s:]{1,6}" + \
                       re_name_part + "\s\s?" + re_name_part + "\s"
    self.re_contact_left = re.compile( self.re_combined_left, flags=re.U|re.S )
    
    # Handles the case where title is on the right of the name
    self.re_combined_right = re_name_part + "\s\s?" + re_name_part + "[\s:,]{1,5}" + \
                       "(" + re_title + "?" + re_title2 + "?" + re_main_title + ")\s"
    self.re_contact_right = re.compile( self.re_combined_right, flags=re.U|re.S )
  
  def detect(self, raw_txt):
    found_contacts = []
    # HTML cleaning
    norm_txt = self.unescaper.unescape( raw_txt )

    # Untel est président
    norm_txt = re.sub( " est ", " : ", norm_txt )

    # Removes accents to have regexp working properly
    norm_txt = unicodedata.normalize('NFKD', norm_txt).encode('ASCII', 'ignore').decode()
    # Add a 'stop' after line breaks in order to prevent \s matching
    norm_txt = norm_txt.replace( '\n', ' |||\n||| ' )
    # Removes social titles
    norm_txt = re.sub( "(Madame |Monsieur |Mme |Mrs |Mr |M\. |Mlle |Dr |Doctor |Docteur |Maitre )", "", norm_txt )

    # Searches for names
    candidates = self.re_contact_left.findall( norm_txt )
    for c in candidates:
      found = False
      con = Contact()
      
      if self.nmanager.is_firstname(c[3]) and self._is_position(c[0]):
        con.firstname = c[3]
        con.lastname = c[4]
        con.position = c[0]
        found = True

      elif self.nmanager.is_firstname(c[4]) and self._is_position(c[0]):
        con.firstname = c[4]
        con.lastname = c[3]
        con.position = c[0]
        found = True

      if found:
        found_contacts.append(con)
    
    candidates = self.re_contact_right.findall( norm_txt )
    for c in candidates:
      found = False
      con = Contact()
      
      if self.nmanager.is_firstname(c[0]) and self._is_position(c[2]):
        con.firstname = c[0]
        con.lastname = c[1]
        con.position = c[2]
        found = True

      elif self.nmanager.is_firstname(c[1]) and self._is_position(c[2]):
        con.firstname = c[1]
        con.lastname = c[0]
        con.position = c[2]
        found = True

      if found:
        found_contacts.append(con)

    final_contacts = []
    for con in found_contacts:
      con = self._validate_contact(con)
      if con is not None:
        final_contacts.append(con)
    
    del found_contacts
    return final_contacts

  def _validate_contact(self, contact):
    """
    Cleans out the contact name and position
    """
    contact.position = contact.position.replace( '\n', ' ' )
    matched_p = contact.position
    attrs = ["position", "firstname", "lastname"]
    
    # Let's first check out if position is a referenced one
    candidate = None
    lp = contact.position.lower()
    for p in self.ref_positions:
      # Adding spaces to avoid inclusions (ex. detect Recteur instead of Directeur)
      if ' ' + p + ' ' in ' ' + lp + ' ':
        # Found position must have a similar amount of tokens than reference
        if candidate is None or len(p.split(' ')) > len(candidate[1].split(' ')):
          candidate = (contact.position, p, self.ref_position_cat[p])
      
      # If firstname on lastname detected is in fact a position, bad contact
      if p in contact.firstname.lower() or p in contact.lastname.lower():
        return None
        
    if candidate is not None:
      #print(candidate)
      contact.position = candidate[2]["fullname"]
      contact.category = candidate[2]["category"]
      contact.referenced = True
      contact.matched_position = matched_p
      
      return contact

    # If not referenced position, validation step
    for attr in attrs:
      tokens = getattr(contact, attr).split(' ')

      for tok in tokens:
        # If position seems to be an adress we don't want it
        if tok.lower() in self.address_tokens:
          return None

        # If blacklisted token, let's remove it
        if tok.lower() in self.blacklist.bl:
          setattr( contact, attr, getattr(contact, attr).replace( ' ' + tok, ' ' ).strip() )
          setattr( contact, attr, getattr(contact, attr).replace( tok + ' ', ' ' ).strip() )

    # Checking contact still makes sense after changes
    if contact.position == "" or contact.firstname == "" or \
       contact.lastname == "":
      return None

    # Checking if we mixed up a name part with a title
    if contact.firstname.lower() in self.positions or \
       contact.lastname.lower() in self.positions:
      return None
    
    contact.referenced = False
    contact.matched_position = matched_p

    return contact

  def _is_position(self, txt):
    """
    Says if a portion of text seems to be a valid position or not.
    """
    tokens = txt.split(' ')
    for t in tokens:
      t = t.strip().lower()
      if t in self.positions:
        return True

    return False

if __name__ == "__main__":
  # Test now in test directory of the package
  c = ContactDetecter()
  pass

