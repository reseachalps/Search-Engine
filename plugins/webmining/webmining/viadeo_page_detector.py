if __name__ == "__main__":
    import sys
    sys.path.append(".")

from webmining.bingapi_fetcher import BingAPIFetcher
import re
from Levenshtein import jaro_winkler
import unicodedata
import string
from urllib.parse import urlparse


VIADEO_URL = re.compile("https?://fr.viadeo\.com/fr/company/(?P<company>[^/]*)/?.*")

COMPANY_DESIGNATIONS = [" SA ", " SAS ", " SARL "]


def strip_punctuation(s):
    translate_table = dict((ord(char), None) for char in string.punctuation)
    return s.translate(translate_table)


def strip_accents(s):
    return ''.join(c for c in unicodedata.normalize('NFD', s)
                   if unicodedata.category(c) != 'Mn')


def normalize(s):
    s = s.upper()
    for designation in COMPANY_DESIGNATIONS:
        s = " " + s + " "
        s = s.replace(designation, "").strip()

    return strip_accents(strip_punctuation(s))


class ViadeoAccount:

    def __init__(self, company, url):
        self.company = company
        self.url = url

    def __str__(self):
        return self.url


class ViadeoAccountDetector:

    def __init__(self, api_key):
        self.bing = BingAPIFetcher(api_key)

    def _fetch(self, query, company):
        results = self.bing.fetch(query)
        return self.parse_results(results, company)

    def detect(self, company_name, company_website=None):
        request = 'site:viadeo.com/fr/company "%s"' % company_name
        result = self._fetch(request, company_name)

        if result is None and company_website is not None:
            company_domain = urlparse(company_website).netloc
            if company_domain != "":
                request = 'site:viadeo.com/fr/company "%s"' % company_domain
                result = self._fetch(request, company_name)

        if result is None:
            #sys.stderr.write("no results\n")
            return None

        if not VIADEO_URL.match(result.url):
            #sys.stderr.write("Not a viadeo url: " + result.url + "\n")
            return None

        company_identifier = VIADEO_URL.search(result.url).groupdict()["company"]

        #If the identifier is the universal name and not the id, we test for similarity
        try:
            int(company_identifier)
        except ValueError:
            score = jaro_winkler(normalize(company_name), normalize(company_identifier))
            if score < 0.7:
                #sys.stderr.write("%s too distant from %s (%.2f)\n" % (normalize(company_name),
                #                                                      normalize(company_identifier),
                #                                                      score))
                return None
        return result

    def parse_results(self, results, company):
        if len(results) == 0:
            return None
        else:
            return ViadeoAccount(company, results[0].url)

if __name__ == "__main__":
    import csv
    api_key = "ERie4sUx5F4tnOOphz4IVfOj3tnR8Ba1xBxCZPkZqqo="
    li = ViadeoAccountDetector(api_key)
    with open(sys.argv[1], "r") as f:
        reader = csv.reader(f, delimiter="\t")
        for line in reader:
            #sys.stderr.write("begin detection for " + normalize(line[0]) + "\n")
            print(li.detect(*line))
