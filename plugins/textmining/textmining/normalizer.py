import phonenumbers
import re, unicodedata
from datetime import datetime, date, time

class Normalizer:
  """
  This module contains methods to normalize commons
  objects, such as phone numbers, addresses, dates, ...
  """

  def normalize_phone_number(self, number, country="FR"):
    """
    Normalizes fax / phone to international standard
    number, such as +33623576856.
    param: number: must be a string
    Returns None if normalization failed.
    """
    if number is None:
      return None

    try:
        phone = phonenumbers.parse(number, country)
    except phonenumbers.NumberParseException:
        return None

    if not phonenumbers.is_possible_number(phone) or not phonenumbers.is_valid_number(phone):
        return None

    return phonenumbers.format_number(phone, phonenumbers.PhoneNumberFormat.INTERNATIONAL)

  def normalize_text(self, text):
    """
    Normalizes a string into a lowered one,
    without accents. Ponctuation and successing whitespaces are 
    also removed.
    """
    if text is None:
      return None

    t = text.lower().strip()
    t = re.sub('[\.,;!\?]+', '', t)
    t = re.sub('[\s]+', ' ', t)
    t = ''.join((c for c in unicodedata.normalize('NFD', t) if unicodedata.category(c) != 'Mn'))

    return t

  def normalize_date_to_datetime(self, date_):
  
    if type(date_) == datetime:
      return date_
      
    date_ = self.normalize_date( date_ )
    if date_ is None:
      return None
    
    parts = date_.split('/')
    d = date( int(parts[2]), int(parts[1]), int(parts[0]) )
    t = time()
    nd = datetime.combine(d, t)
    
    return nd
  
  def normalize_date(self, date_):
    """
    Transforms heterogenous date formats into a 
    uniformed one as : day/month/year.
    This can be used to create a Date object in mongoDB.

    Managed format are :
      20110504 12:00
      09092011
      09 09 2011

    """
    if date_ is None:
      return None

    # Months mapping + other replacements
    smonths = {"janvier": "01", "février": "02", "mars": "03", "avril": "04", "mai": "05", "juin": "06",
               "juillet": "07", "août": "08", "septembre": "09", "octobre": "10", "novembre": "11", 
               "décembre": "12", "1er": "01"}

    # We assume not to compute dates earlier 
    # than 01/01/1900 and older than 31/12/2012
    days = range(1, 32)
    months = range(1, 13)
    years = range(1900, 2051)
  
    d = date_.lower()
    
    # Let's try to map months in plain text
    for m in smonths:
      d = re.sub(m, smonths[m], d)

    # Normalization by keeping only digits
    d = re.sub('[^\d]', '', d)

    if len(d) == 6:
      # date with uncomplete year : 051214
      # we add two first missing digits
      d = d[0:4] + "20" + d[4:6]

    elif len(d) >= 8:
      d = d[0:8]

    # if one digit is missing, it may be a zero in front of a one-digit day
    elif len(d) == 7:
      d = "0" + d[0:7]

    else:
      return None
    
    # Tries d-m-y format
    if int(d[0:2]) in days   and \
       int(d[2:4]) in months and \
       int(d[4:8]) in years:
      d = d[0:2] + '/' + d[2:4] + '/' + d[4:8]

    # Tries y-m-d format
    elif int(d[0:4]) in years  and \
         int(d[4:6]) in months and \
         int(d[6:8]) in days:
      d = d[6:8] + '/' + d[4:6] + '/' + d[0:4]

    else:
      return None

    return d

# For testing purpose
if __name__ == "__main__":
  # Test now in test directory of the package
  pass

