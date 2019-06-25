import logging
import re
import html.parser as html_parser
from fastmatch import Matcher

# Needed to import textmining in standalone mode
# (i.e. to generate needed files)
if __name__ == "__main__":  # pragma: nocover
    import sys
    import os.path
    sys.path.insert(0, os.path.abspath("."))

from textmining import LIB_PATH
import csv
from collections import namedtuple
import unicodedata
from entities_extractor.entities import Entities
import os


def normalize_text(text):
    text = text.strip().lower()
    text = ''.join((c for c in unicodedata.normalize('NFD', text)
                    if unicodedata.category(c) != 'Mn'))
    text = re.sub("[\W]", " ", text)
    text = re.sub("\s+", " ", text)
    return text


class Address:
    """
    Address modelisation
    """

    def __init__(self, mo):
        """
        :param mo: re.compile match object versus address pattern
        """
        self.address = mo.groupdict()["address"].strip()
        self.zipcode = mo.groupdict()["zipcode"].strip()
        self.city = mo.groupdict()["city"].strip()

    def __str__(self):
        return "[%r] [%s] [%s]" % (self.address, self.zipcode, self.city)

    def __eq__(self, other):
        return self.address == other.address and \
            self.zipcode == other.zipcode and \
            self.city == other.city


class AddressDetecter:
    """
    Tries to detect and extract an address from raw text
    Only works with french addresses for now.
    """

    # Regex pattern to remove boites postales
    rm_bp = re.compile("\s(b|bt|#|-|boîte|cs|bp|/|bus|box|boite|bte|CS|BP|BUS|BOX|BOITE|BTE)\s?\d{1,5}")

    def __init__(self, cache_results=False,check_supported=True, **kwargs):
        """
        :param cache_results (boolean) if true, the result caching mechanism is enabled
        """

        self.logger = logging.getLogger("textmining:address_detecter")
        self.logger.setLevel(logging.INFO)
        if "fvoies" in kwargs:
            raise DeprecationWarning("fvoies is deprecated.\n"
                                     "Please use detect_address(..., fvoies=X)")

        resources_dir = os.path.join(LIB_PATH, "resources/localization")
        self.check_supported = check_supported;

        if not os.path.exists(resources_dir):
            raise NotImplementedError("No resources")

        # Cache where country specific resources are cached
        self.localization_cache = {}

        # Iterating over coutry specific resources
        for path in os.listdir(resources_dir):
            # We consider that all directories in the resources_dir represent a country
            if os.path.isdir(os.path.join(resources_dir, path)):
                country_name = path
                country_path = os.path.join(resources_dir, path)
                country = namedtuple("Country", ["cities",  # Set of entities,
                                                 "zipcodes",
                                                 "voies",
                                                 "main_regex",
                                                 "secondary_regex",
                                                 "streets_matcher"])
                # For some country (like France), there’s no need for a harcoded street list
                country.streets = None
                street_path = os.path.join(country_path, "streets.txt")
                # If streets.txt exists, a hardcoded street list is needed
                if os.path.exists(street_path):
                    with open(street_path, "r") as f:
                        country.streets = set(f.read().splitlines())
                        country.streets_matcher = Matcher()
                        country.streets_matcher.set_words(country.streets)

                with open(os.path.join(country_path, "main.regex"), "r") as f:
                    regex = f.read().strip()
                    country.main_regex = re.compile(regex, re.S | re.U | re.I)

                try:
                    with open(os.path.join(country_path, "secondary.regex"), "r") as f:
                        regex = f.read().strip()
                        country.secondary_regex = re.compile(regex, re.S | re.U | re.I)
                except IOError as e:
                    country.secondary_regex = None
                    print("Unable to open file secondary.rege")  # Does not exist OR no read permissions

                country.voies = cities = country.zipcodes = set()
                try:
                    with open(os.path.join(country_path, "cities.csv"), "r") as f:
                        reader = csv.reader(f, delimiter=",")
                        for zipcode, city in reader:
                            zipcode = str(int(zipcode))  # Ensures consistency when zipcode begins with 0
                            country.zipcodes.add(zipcode)
                            city = normalize_text(city)
                            cities.add(city)
                        country.cities = Entities(cities)
                except IOError as e:
                    country.cities = None
                    print("Unable to open file cities.csv")  # Does not exist OR no read permissions

                try:
                    # Populating voies set with resource file
                    with open(os.path.join(country_path, "voies.csv"), "r") as f:
                        for row in f.readlines():
                            row = row.strip().lower()
                            row = row.split(",")
                            voies = map(normalize_text, row)
                            country.voies.update(voies)
                except IOError as e:
                    country.voies = None
                    print("Unable to open file voies.csv")  # Does not exist OR no read permissions

                self.localization_cache[country_name] = country
        self.results_cache = None
        if cache_results:
            # Caches matched strings in a set for bad results, and a dict for the one that yielded a good result
            self.empty_cache()

    def empty_cache(self):
        """
        Empties the results cache
        """

        self.results_cache = {"ok": {}, "nok": set()}

    def search_against_cache(self, match):
        """
        Checks if a match has already been seen
        if the match is new, it returns an address extracted from the match,
        if the match has already been seen, it either returns :
            - the corresponding address if the match was positive
            - None if the match was negative
        """

        if self.results_cache is None:
            raise RuntimeError("Cannot check against cache if results caching is not enabled")

        # Getting the raw match
        match_str = match.group(0)
        if match_str in self.results_cache["nok"]:
            return None
        elif match_str in self.results_cache["ok"]:
            return self.results_cache["ok"][match_str]
        else:
            return Address(match)

    def check_country(self, country):
        if country not in self.localization_cache:
            if self.check_supported:
                raise NotImplementedError("No localization data for the country %s" % country)
            else:
                self.logger.warn("Unsupported country [%s] " % country)
                return None

        return self.localization_cache[country]

    def detect(self, txt, html=False):
        raise DeprecationWarning("detect is deprecated.\n"
                                 "Please use detect_address instead")

    def detect_address(self, txt, html=False, country="FR"):
        """
        Tries to find an address in txt using the country specific
        extractions

        :param txt: text to compute
        :param html: says if text is in html format
        :param country: indicates the address format to search for
        """
        return self.detect_addresses(txt, html, country, only_one=True)

    def parse_address(self, match, country):
        """
        Using a match from a regex, either returns a correponding Address object
        if the match is correct or None
        """
        caching = self.results_cache is not None

        if caching:
            addr = self.search_against_cache(match)
        else:
            addr = Address(match)
        if addr is None:
            return None

        addr = self._validate_address(addr, country=country)
        if addr is False:
            # If result caching is enabled, we cache the match as nok
            if caching:
                self.results_cache["nok"].add(match.group(0))
            return None
        else:
            # If result caching is enabled, we cache the match as ok
            if caching and match.group(0) not in self.results_cache["ok"]:
                    self.results_cache["ok"][match.group(0)] = addr
            # print("Address match %s,  %s" % (match,addr))
            return addr

    def _clean_addr_text(self, txt, html):
        cleantxt = None

        # Cleans html junk if exists
        if html:
            if not hasattr(self, "unescaper"):
                self.unescaper = html_parser.HTMLParser()

            cleantxt = self.unescaper.unescape(txt)
            cleantxt = re.sub("<.+?>", " ", cleantxt, flags=re.S)
        else:
            cleantxt = txt

        # First remove eventual annoying tokens
        annoying_tokens = [(" • ", ""), ("•", ""), (":", ""), (".", ""), ("/", ""), ("Cedex", ""), ("cedex", ""), (" - ", " "), (" − ", " "), (" – ", " "), ("n°", " ")]
        for token, replacement in annoying_tokens:
            cleantxt = cleantxt.replace(token, replacement)
        cleantxt = self.rm_bp.sub(" ", cleantxt)
        cleantxt = re.sub("\s+", " ", cleantxt)

        return cleantxt

    def detect_addresses(self, txt, html=False, country="FR", only_one=False):
        """
        Tries to find multiple addresses in txt

        :param txt: text to compute
        :param html: says if text is in html format
        :param country: indicates the address format to search for
        :returns: this method returns a list of adresses
        """

        country_data = self.check_country(country)
        if country_data is None:
            return None
        cleantxt = self._clean_addr_text(txt, html).lower()
        addrs = []

        # Search for the country classical address format
        # print('Looking for address in %s' % cleantxt)
        matches = country_data.main_regex.finditer(cleantxt)
        for match in matches:
            addr = self.parse_address(match, country)
            if only_one:
                return addr
            if addr is not None and addr not in addrs:
                addrs.append(addr)

        # Looking also for the more specific formats
        if country_data.secondary_regex is not None:
            matches = country_data.secondary_regex.finditer(cleantxt)
            for match in matches:
                addr = self.parse_address(match, country)
                if only_one:
                    return addr
                if addr is not None and addr not in addrs:
                    addrs.append(addr)

        if only_one:
            return None
        return addrs

    def detect_voie(self, txt, country="FR"):
        """
        Detect the presence of "voies" according to the country
        in a block of text.
        """

        check_country = self.check_country(country)
        if check_country is None:
            return None

        voies = sorted(self.localization_cache[country].voies, key=lambda t: len(t), reverse=True)

        txt = normalize_text(txt)
        tokens = re.split("\s+", txt)
        for voie in voies:
            # Rules specific for dutch (things as bergstraat)
            for token in tokens:
                if token.endswith(voie):
                    return True
            idx = txt.find(voie)
            # Rules used for french voies
            if idx != -1:
                # Checking that the string begeins by the voie or
                # that identified voie is preceeded by a space
                before = (idx == 0) or txt[idx - 1].isspace()

                # Checking that the string ends by the voie or
                # that identified voie is followed by a space
                after = ((idx + len(voie) == len(txt)) or txt[idx + len(voie)].isspace())
                if before and after:
                    return voie

        return None

    def detect_city(self, txt, country="FR"):
        """
        Detect cities in a block of text according to a given country.

        :param lang: indicates wich cities are valid
        """

        country_data = self.check_country(country)
        if country_data is None:
            return [txt]
        txt = normalize_text(txt)
        entities = country_data.cities

        # If country don't provide a city list, it mean there no need to validate city.
        if entities is None:
            return [txt]

        return [m.value.entity for m in entities.match(txt)]

    def _validate_address(self, addr, country="FR"):
        """
        Indicates if an extracted address seems valid
        or not according to the given country

        :param addr: an Address object
        :param country: indicates the Address format
        :return: True if valid or False
        """

        country_data = self.check_country(country)
        if country_data is None:
            return False

        # Is there a valid city in the city part ?
        cities = self.detect_city(addr.city, country=country)

        if len(cities) < 1:
            self.logger.info("Rejected address with no city (%s)" % addr.city)
            return False
        # If only one 'candidate' we replace the matched city
        # by a surely valid one
        elif len(cities) >= 1:
            # We remove superfluous tokens in the city
            # ex: city is 'Saint Rémicourt junk toto'
            #     detected city is 'saint remicourt'
            #     cleaned city will be 'Saint Rémicourt'
            tokens = re.split("[\W]", addr.city)
            detected_tokens = cities[0].split(" ")
            for token in tokens:
                if normalize_text(token) not in detected_tokens:
                    addr.city = addr.city.replace(token, "")

        # If the streets list is present, we also check if the address is not contained in
        # it, else, we may reject valid addresses such as some streets in belgium
        # Is there a voie in the address part ?
        # Make check in 'voies' only if country provide such file.
        if country_data.voies is not None:
            if self.detect_voie(addr.address, country=country) is None:
                valid = False
                if country_data.streets:
                    # Is there any token in the streets list ? (here no fuzzy match)
                    tokens = addr.address.split(" ")
                    for token in tokens:
                        # If so, we continue address validation
                        if token in country_data.streets:
                            valid = True
                            break
                # If no voie and no token in streets, the address is rejected
                if not valid:
                    self.logger.info("Rejected address with no voie (%s)" % addr.address)
                    return False

        # Do we have to check street validity ?
        if country_data.streets is not None:
            # We first remove the street number if any
            # the accepted street number forms are 22, 22a, 22-24 and variants
            street = re.sub(",", "", addr.address)
            street = re.sub("\d{1,4}[abc]?(?:-\d{1,4}[abc]?)?", "", street)
            # We split the address in tokens and check for street validity by removing
            # the leftmost token if no match, until no token is left. Ex:
            # Data Publica Chauchastraat => no match
            # Publica Chauchastraat => no match
            # Chauchastraat => match
            tokens = street.split(" ")
            original_tokens = tokens
            is_valid = False

            while len(tokens) > 0:
                s = " ".join(tokens).strip()
                # Difflib compares string as sequences, as streets are all in lower cases, the string
                # to be compared must also be in lowercase, else Molenstraat and polenstraat have the
                # same score for example
                candidate = country_data.streets_matcher.match(normalize_text(s))
                if len(candidate) == 1:
                    candidate = candidate[0][0]
                    # We remove matched tokens that aren’t in the candidate
                    # If we matched SAS Chauchatstraat with Chauchastraat, we
                    # want to keep only Chauchastraat
                    # But we keep tokens that are in the voies because
                    # ex Route de Paris matched rte de paris.
                    for token in tokens:
                        if normalize_text(token) not in candidate and token not in country_data.voies:
                            addr.address = addr.address.replace(token, "").strip()

                    # Now, we remove the tokens that were removed to get to the match
                    useless = list(set(original_tokens) - set(tokens))
                    for token in useless:
                        addr.address = addr.address.replace(token, "").strip()
                    if len(addr.address) >= len(candidate):
                        is_valid = True
                    break
                tokens = tokens[1:]
            if not is_valid:
                self.logger.info("Rejected address with no valid street in [%s]" % addr.address)
                return False

        addr.address = addr.address.strip(" ,-–.")
        addr.city = addr.city.strip()
        addr.zipcode = addr.zipcode.replace(' ', '').strip()
        self.logger.info("Got address %s" % addr)

        return addr

if __name__ == "__main__":
    import sys

    detecter = AddressDetecter()
    with open(sys.argv[1], "r") as f:
        villes = set(list(map(str.strip, f.readlines())))
        print(len(villes))
        before = len(villes)
        bad = 0
        for rue in villes:
            if not detecter.detect_city(rue, country="BE"):
                bad += 1
                print(rue)

        print((before - bad) / before)
