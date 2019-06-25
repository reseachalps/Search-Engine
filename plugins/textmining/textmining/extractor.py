
import re
from textmining import LIB_PATH

class Extractor:
  """
  Parses plain text and returns structured data
  """

  def __init__(self):
    pass


  def extract_prices(self, txt):
    """
    Extracts prices as in
    57 593,98 EUR
    """
    prices = []
    if type(txt) != str:
      return prices

    #pattern = "((\d.?)*) EUR"
    # JOUE / BOAMP Pattern
    pattern = "(.{100})((\d.?)*) EUR"
    matches = re.findall(pattern, txt)

    for m in matches:
      # JOUE
      """
      if "valeur totale finale" not in m[0].lower() and \
         "valeur estimée" not in m[0].lower() and \
         "dotation financière" not in m[0].lower() and \
         "estimation initiale du montant" not in m[0].lower() and \
         "estimation financière" not in m[0].lower() and \
         "attribué pour un montant de" not in m[0].lower() and \
         "estimation annuelle" not in m[0].lower() and \
         "montant annuel indicatif" not in m[0].lower() and \
         "montant annuel minimum" not in m[0].lower() and \
         "montant minimum annuel" not in m[0].lower() and \
         "montant maximal annuel" not in m[0].lower() and \
         "montant maximum" not in m[0].lower() and \
         "montant minimum" not in m[0].lower() and \
         "maximum annuel" not in m[0].lower() and \
         "minimum annuel" not in m[0].lower() and \
         "part du marché susceptible d’être sous-traitée" not in m[0].lower() and \
         "lot" not in m[0].lower() and \
         "quadriennal" not in m[0].lower() and \
         "montant estimatif" not in m[0].lower() and \
         "fourchette" not in m[0].lower():
        continue
      """
      
      # BOAMP
      if "montant final du march&eacute; ou du lot attribu&eacute;" not in m[0].lower() and \
         "lot" not in m[0].lower() and \
         "chiffre d'affaires minimal" not in m[0].lower() and \
         "montant estim&eacute;" not in m[0].lower() and \
         "sont comprises entre" not in m[0].lower() and \
         "seuil maximum" not in m[0].lower() and \
         "seuil minimum" not in m[0].lower() and \
         "montant annuel maximum" not in m[0].lower() and \
         "montant annuel minimum" not in m[0].lower() and \
         "montant annuel" not in m[0].lower() and \
         "montant maximum" not in m[0].lower() and \
         "montant maximal" not in m[0].lower() and \
         "montant minimum" not in m[0].lower() and \
         "montant maximum annuel" not in m[0].lower() and \
         "montant minimum annuel" not in m[0].lower():
        continue
      
      if len( m[1] ) < 1:
        continue

      p = m[1].replace(' ', '')
      p = p.replace(',', '.')
      try:
        p = float( p )
        prices.append( p )
      except:
        pass

    return prices

  def extract_contact(self, txt, remove=None, dep=None):
    t = {}
    t["localisation"] = {}
    t["telephone"] = self.extract_phone(txt)
    t["fax"] = self.extract_fax(txt)
    t["email"] = self.extract_email(txt)
    t["nom"] = self.extract_name(txt)
    t["localisation"]["code_postal"] = self.extract_zipcode(txt, dep)
    t["site"] = self.extract_site(txt)

    # If we have nothing, too bad quality to be used
    if t["telephone"] is None and \
       t["fax"]       is None and \
       t["nom"]       is None and \
       t["email"]     is None and \
       t["localisation"]["code_postal"] is None:
      return {}

    # Getting missing information (probably adress)
    loc = txt
    loc = re.sub("tél.?.? \d\d.\d\d.\d\d.\d\d.\d\d", "", loc, flags=re.I)
    loc = re.sub("tél.?.? \+33.\d.\d\d.\d\d.\d\d.\d\d", "", loc, flags=re.I)
    loc = re.sub("tel.?.? \+33.\d.\d\d.\d\d.\d\d.\d\d", "", loc, flags=re.I)
    loc = re.sub("tél.?.? \+33.\d\d.\d\d.\d\d.\d\d.\d\d", "", loc, flags=re.I)
    loc = re.sub("fax.?.? \d\d.\d\d.\d\d.\d\d.\d\d", "", loc, flags=re.I)
    loc = re.sub("fax.?.? \+33.\d.\d\d.\d\d.\d\d.\d\d", "", loc, flags=re.I)
    loc = re.sub("fax.?.? \+33.\d\d.\d\d.\d\d.\d\d.\d\d", "", loc, flags=re.I)
    loc = re.sub("telecopie.?.? \d\d.\d\d.\d\d.\d\d.\d\d", "", loc, flags=re.I)
    loc = re.sub("\s\S@\S\s", "", loc)
    loc = re.sub("\s(\S+?@\S+?\.\S+) ?", "", loc, re.S|re.M)

    if remove is not None:
      loc = re.sub(remove, "", loc, flags=re.S)

    if t["email"] is not None:
      loc = re.sub("adresse mail", "", loc, flags=re.I)
      loc = re.sub("courriel électronique", "", loc, flags=re.I)
      loc = re.sub("e-mail", "", loc, flags=re.I)
      loc = re.sub("email", "", loc, flags=re.I)
      loc = re.sub("mail", "", loc, flags=re.I)
      loc = re.sub("courriel", "", loc, flags=re.I)
      loc = re.sub("mèl", "", loc, flags=re.I)
    
    if t["site"] is not None:
      loc = loc.replace(t["site"], "")
      loc = re.sub("adresse internet du profil acheteur", "", loc, flags=re.I)

    # Cleaning text from successive junk chars
    loc = re.sub("[,:\.;\s]{2,}", ", ", loc, flags=re.U|re.M)

    # Cleaning the end of the text from its junk chars
    j = 0
    size = len(loc) - 1
    junk = set([',', ':', '.', ';', ' '])
    for i in range( 0, size ):
      if loc[size - i] in junk:
        j += 1
      else:
        break

    loc = loc[0:size-j + 1]
    
    if len( loc )  > 1:
      t["localisation"]["lieu"] = loc

    return t
  
  # Correspondant : Mme Grelaud ou M. Martin,
  # contact : Mme Pele,
  def extract_name(self, txt):
    m = re.search("correspondant : (.*?),", txt, flags=re.S|re.M|re.I)
    if m is not None:
      return m.group(1)
    else:
      m = re.search("contact : (.*?),", txt, flags=re.S|re.M|re.I)
      if m is not None:
        return m.group(1)
      else:
        return None

  # tél. 02 96 94 12 41
  def extract_phone(self, txt):
    m = re.search("tél.?.? (\d\d.\d\d.\d\d.\d\d.\d\d)", txt, flags=re.S|re.M|re.I)
    if m is not None:
      return m.group(1)
    
    else:
      m = re.search("tel.?.? (\d\d.\d\d.\d\d.\d\d.\d\d)", txt, flags=re.S|re.M|re.I)
      if m is not None:
        return m.group(1)
      
      else:
        m = re.search("tél.?.? (\+33.\d.\d\d.\d\d.\d\d.\d\d)", txt, flags=re.S|re.M|re.I)
        if m is not None:
          return m.group(1)

        # Case where number is not in a good format
        # tél, +33 02 51 49 79 73
        # fax +33 02.51.55.05.64
        else:
          m = re.search("tél.?.? \+33.(\d\d.\d\d.\d\d.\d\d.\d\d)", txt, flags=re.S|re.M|re.I)
          if m is not None:
            return m.group(1)
          else:
            return None

  # fax 02.96.78.25.91
  # Fax, 01.60.34.77.68
  def extract_fax(self, txt):
    m = re.search("fax.?.? (\d\d.\d\d.\d\d.\d\d.\d\d)", txt, flags=re.S|re.M|re.I)
    if m is not None:
      return m.group(1)
    
    else:
      m = re.search("telecopie.?.? (\d\d.\d\d.\d\d.\d\d.\d\d)", txt, flags=re.S|re.M|re.I)
      if m is not None:
        return m.group(1)
      
      else:
        m = re.search("fax.?.? (\+33.\d.\d\d.\d\d.\d\d.\d\d)", txt, flags=re.S|re.M|re.I)
        if m is not None:
          return m.group(1)
        
        # Case where number is not in a good format
        # fax +33 02.51.55.05.64
        else:
          m = re.search("fax.?.? \+33.(\d\d.\d\d.\d\d.\d\d.\d\d)", txt, flags=re.S|re.M|re.I)
          if m is not None:
            return m.group(1)
          else:
            return None
  
  def extract_email(self, txt):
    #m = re.search("\s(\S*?@\w\.\w)\s*?", txt, re.S|re.M)
    m = re.search("\s(\S+?@\S+?\.\S+) ?", txt, flags=re.S|re.M)
    if m is not None:
      return m.group(1)
    else:
      return None
  
  def extract_site(self, txt):
    """
    Tries to find an url in text.
    """
    m = re.search("(http://.*?\.(com|fr|org|eu))[/\.\s,;:]", txt, flags=re.S|re.M)
    if m is not None:
      return m.group(1)
    
    else:
      return None

  def extract_zipcode(self, txt, dep=None):
    """
    Finds a zipcode in fluzzy unstructured text.
    Ex. : 44600
    Ex. : 75 382

    Matches which contain CS or CP are eliminated
    """
    
    # Prevents matching with boites postales
    # CS, C.S., BP, B.P.
    cleantxt = re.sub("(C\.?S\.?|B\.?P\.?)\s\d{5}", "", txt)

    # Prevent matching with prices
    cleantxt = re.sub("\d{2}\s?\d{3}\s?(euros|€)", "", cleantxt)

    if dep is None:
      patterns = [".?.?\s([\d]{5})\s", "\(([\d]{5})\)", ".?.?\s(\d\d \d\d\d)\s"]
      for p in patterns:
        m = re.search(p, cleantxt, flags=re.S|re.M)
        if m is not None:
          return m.group(1).replace(' ', '')
    
    else:
      patterns = ["\s(%d[\d]{3})\s" % dep, "\((%d[\d]{3})\)" % dep, "-(%d[\d]{3})\s" % dep, "\s(%d \d\d\d)\s" % dep]
      for p in patterns:
        m = re.search(p, cleantxt, flags=re.S|re.M)
        if m is not None:
          return m.group(1).replace(' ', '')

    return None

  def extract_capital(self, txt):
    """
    Tries to extract 'capital social' amount from txt 
    with an amount in euros
    """
    if txt is None:
      return None
    target = txt.lower()
    pattern = "capital( social)?( est)?( de)?( :)? {0,4}([\d\., ]*?) ?(euros|€)"
    
    m = re.search(pattern, target)
    if m is not None:
      capital = re.sub("[^\d]", "", m.group(5))
      return int(capital)
    else:
      return None

if __name__ == "__main__":
  # Testing capital extraction
  f = open(LIB_PATH + "resources/testsocial.txt")
  txt = f.read()
  txts = txt.split("---")

  capitals = [7600, 263000, 6200, 105000, 12195, 10000, 10000, 470798]
  e = Extractor()
  count = 0
  for t in txts:
    cap = e.extract_capital(t)
    if cap != capitals[count]:
      print("ERROR - %s extracted instead of %s" % (str(cap), str(capitals[count])))
    else:
      print("OK - %s" % str(cap))

    count += 1

