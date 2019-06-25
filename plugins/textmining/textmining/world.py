import json
import csv
from textmining import LIB_PATH


class Country:
  def __init__(self, row):
    # "Sort Order","Common Name","Formal Name","Type","Sub Type","Sovereignty","Capital","ISO 4217 Currency Code","ISO 4217 Currency Name","ITU-T Telephone Code","ISO 3166-1 2 Letter Code","ISO 3166-1 3 Letter Code","ISO 3166-1 Number","IANA Country Code TLD"
    self.name = row[1]
    self.fullname = row[2]
    self.type = row[3]
    self.subtype = row[4]
    self.sovereignty = row[5]
    self.capital = row[6]
    self.currency = row[7]
    self.tld = row[13]

class World:
  def __init__(self):
    self.lcountries = []
    self.dcountries = {}
    self.scountries = set()

    with open(LIB_PATH + "resources/territories.json", "r") as f:
        self.traductions = json.loads(f.read())

    reader = csv.reader( open(LIB_PATH + "resources/world.csv"), delimiter=',', quotechar='"' )

    for row in reader:
      c = Country( row )
      self.lcountries.append( c )
      self.dcountries[c.name] = c
      self.scountries.add( c.name )

  def get_countries_list(self):
    return self.lcountries

  def get_country(self, name):
    if name in self.scountries:
      return self.dcountries[name]
    else:
      return None

  def get_all_country_names(self, country):
      """
      This method uses countries cited in resources/territories.json
      and not the ones in world.csv
      """

      country = country.lower()

      if country not in self.traductions.keys():
          raise ValueError("No names for this country [%s]" % country)

      return self.traductions[country]["localeDisplayNames"]["territories"].values()
