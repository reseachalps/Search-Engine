import logging
import os
import re
from enum import Enum
from collections import defaultdict
import csv

import requests
from unidecode import unidecode

from scanr_entityextractor import ahocorasick as aho
from entities_extractor.entities import Entity, Entities, tokenize, identity
from textmining.blacklist import Blacklist
from scanr_entityextractor.cache import Cache
from scanr_entityextractor import LIB_PATH


def ascii_normalization(text):
    """replaces accent to their ascii equivalent and removes all non-alphanumeric characters"""
    norm = re.sub('[^0-9a-zA-Z]', "", unidecode(text))
    return norm.lower() 


def normalization(text):
    """replaces multiple spaces or every punctuation different than [.,-] by a single space."""
    return re.sub('[^0-9a-zA-Z.,-](\\s+)', " ", text)


def admit(name, bl):
    """checks if a short label is not a blacklisted common name."""
    striped = name.strip()
    # Must not be a digit
    if striped.isdigit():
        return False

    
    # Restrictive conditions: 
    if striped.isalpha():
        return not (striped.upper() == striped or striped.lower() == striped or striped.capitalize() == striped)

    # Must not be a common word, and must have at more than 3 letters (w/out spaces)    
    if striped.upper() == striped or striped.lower() == striped or striped.capitalize() == striped:
        lower_bl = [s.lower() for s in bl]
        return (striped.lower() not in lower_bl) and len(re.sub("\\s+", "", striped)) > 3   
    return striped not in bl and len(re.sub("\\s+", "", striped)) > 3
 

def admit_label(name, blacklist, min_char=20):
    return len(name) > min_char and admit(name, blacklist)


def get_blacklisted_entities():
    """adds blacklist items in csv to the set of blacklisted items."""
    blacklist = set()
    paths=["short_label_blacklist.csv"]

    for path in paths:
        abs_path = os.path.join(LIB_PATH, "resources", path)
        with open(abs_path, "r") as f:
            labels = csv.reader(f)
            blacklist = blacklist.union(set([label[0] for label in labels]))
    return blacklist

class MatchMethod(Enum):
    #exact match detection, useful for id detection. 
    EXACT = "EXACT"
    #long label detection, useful to detect long expressions (patent/publication)
    LONG_LABEL = "LONG_LABEL"
    #short label detection, useful to detect project name
    SHORT_LABEL = "SHORT_LABEL"


class CannotFetchUrlException(Exception):
    def __init__(self):
        super().__init__(
               "No data found!")


class UnkownMatchingMethod(Exception):
    def __init__(self, method):
        super().__init__(
               "Unknown matching method [%s] " % method)


class APICaller():
    def __init__(self, conf):
        self.base_url = conf["base_url"]
        self.auth = conf["username"], conf["password"]
   
    def fetch(self, params="api/entities"):
        url = self.base_url + '/' + params
        result = requests.get(url, auth=self.auth)
        return result


class EntityProvider:
    """
    Entities cache 
    """

    def __init__(self, api_conf, tokenizer=tokenize):
        self.tokenizer = tokenizer
        self.api = APICaller(api_conf) 
        self.cache = Cache()

        #logging handler:
        self.logger = logging.getLogger("entity_provider")
        self.logger.setLevel(logging.INFO)
        file_handler = logging.FileHandler(filename=LIB_PATH + "../log/log.txt")
        file_handler.setFormatter(logging.Formatter('[%(levelname)s][%(name)s][%(asctime)s]  %(message)s'))
        self.logger.addHandler(file_handler)

        #blacklisting operations
        self.bl = Blacklist().bl.union(get_blacklisted_entities())
        self.logger.info("blacklist list has %d items" %len(self.bl))        
 
    def get(self):
        """
        Give the Entities object filled with companies with the given tag.
        Impl is cached to avoid mass traffic.

        :param url: the url to get entities from
        :return: A Entities object
        """
        entities = self.cache.get("entities")
        if entities is not None:
            return entities

        entities = self._fetch()

        # Update the cache
        self.cache.put("entities", entities)
        self.logger.info("cache is ready!")

        return entities

    def _fetch(self, params="api/entities"):
        """
        Fetches entities recorded for the given url

        :param url:
        :return: The corresponding list of entities
        """
        self.logger.info("Fetching url [%s] " % self.api.base_url + "/" + params)

        #fetching... 
        response = self.api.fetch(params) 
        if not response.status_code == requests.codes.ok:
            self.logger.warning("Fetching the API doesn't give an 200 HTTP response...")
            raise CannotFetchUrlException()
         
        results = response.json()
        if len(results) == 0:
           raise CannotFetchUrlException() 

        entities_list = defaultdict()
        # Preparing entities based on matching method type
        entities_list[MatchMethod.EXACT] = Entities([], tokenizer=self.tokenizer, transform=identity)
        entities_list[MatchMethod.SHORT_LABEL] = Entities([], tokenizer=self.tokenizer, transform=normalization)
        # Preparing expression matching using Ahocorasick data structure 
        entities_list[MatchMethod.LONG_LABEL] = aho.Trie()
        
        count = 0
        blacklisted = list()
        for result in results:
            id_ = result["id"]
            type_ = result["type"]
            labels = result["labels"]
            for entity in labels:
                if entity["method"] in ["SHORT_LABEL", "EXACT"]:
                    if admit(entity["label"], self.bl):
                        entities_list[MatchMethod(entity["method"])].add_entity(Entity(entity["label"], (id_, type_)))
                        count += 1
                    else:
                        blacklisted.append(entity["label"])
                        self.logger.warning("blacklisting short-label %s" %entity["label"])
                 
                elif entity["method"] == "LONG_LABEL":
                    if admit_label(entity["label"], self.bl):
                        entities_list[MatchMethod(entity["method"])].add_word(ascii_normalization(entity["label"]), (id_, type_))
                        count += 1
                    else:
                        blacklisted.append(entity["label"])
                        self.logger.warning("blacklisting long-label %s" %entity["label"])
                
                else:
                     raise UnkownMatchingMethod("method [%s] is not implemented." % entity["method"])

        # computing automaton to prepare ahocorasick on long-label entities
        entities_list[MatchMethod("LONG_LABEL")].make_automaton()       

        self.logger.info("total items retrieved : %d" % count)
        self.logger.warning("Has blacklisted %d items."  % len(blacklisted))
        return entities_list
