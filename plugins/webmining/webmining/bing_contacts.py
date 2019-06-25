if __name__ == "__main__":
    import sys
    sys.path.append(".")

from webmining.bingapi_fetcher import BingAPIFetcher
from webmining.viadeo_fetcher import ViadeoFetcher
import re
import unicodedata
from Levenshtein import jaro_winkler
import string
import logging
#from datetime import datetime
import datetime


def strip_punctuation(s):
    translate_table = dict((ord(char), None) for char in string.punctuation)
    return s.translate(translate_table)


def strip_accents(s):
    return ''.join(c for c in unicodedata.normalize('NFD', s)
                   if unicodedata.category(c) != 'Mn')


def normalize(s):
    return strip_punctuation(strip_accents(s.lower()))


country_to_tld = {"FR": "fr", "BE": "be", "UK": "uk"}

viadeo_snippet_patterns = [
    "%d\. ([0-9]{4}\. )?(?P<job>[^;]*?)\. (?P<company>[^;]*?)\.",
]

months = [
    "Janvier ", "Février ", "Mars ",
    "Avril ", "Mai ", "Juin ",
    "Juillet ", "Août ", "Aout ", "Septembre ",
    "Octobre ", "Novembre ", "Décembre "
]

position_whitelist = [
    "manager", "responsable", "ingenieur", "engineer", "ceo", "pdg",
    "directeur", "cto", "cfo", "cmo", "fondateur", "fondatrice", "founder",
    "co-founder", "cofounder", "co-fondateur", "co-fondatrice", "cofondateur",
    "cofondatrice", "developer", "développeur", "chargé", "chargée",
    "director", "commercial", "scientist", "stage", "stagiaire", "assistante",
    "assistant", "directrice", "chef", "secretaire", "secrétaire", "comptable",
    "technicien", "charge", "chargee", "ingénieur", "gestionnaire", "adjoint",
    "adjointe", "administrateur", "administratrice", "gérant", "gérante",
    "gerant", "gerante", "conseiller", "consultant", "consultante", "conseillere"
    "conseillère", "analyst", "président", "présidente", "president", "presidente",
    "vice", "expert", "resp", "cadre", "head", "architect", "chief", "vp", "drh",
    "associate", "specialist", "spécialiste", "leader", "lead", "executive",
    "coordinator", "developpeur", "architecte", "cogerant", "co-gerant", "cogérant",
    "cogérante", "cogerante", "co-gérante", "co", "co-gérant", "svp", "intern",
    "member", "analyste", "développeuse", "developpeuse", "commerciale", "cco",
    "graphiste", "designer", "officer", "agent", "agente", "formateur", "formatrice",
    "juriste", "attaché", "attachée", "attache", "attachee", "webmaster", "senior",
    "junior", "acheteur", "mandataire", "mandatrice", "auditeur", "auditrice",
    "partner", "contrôleur", "contrôleuse", "controleur", "controleuse", "support",
    "ingénieure", "ingenieure"
]

english_position_whitelist = [
    "manager", "engineer", "ceo", "cto", "cfo", "cmo", "founder", "co-founder",
    "cofounder", "developer", "director", "scientist", "analyst", "vice", "head",
    "architect", "chief", "vp", "associate", "specialist", "leader", "lead",
    "executive", "coordinator", "svp", "intern", "member", "cco", "designer",
    "officer", "webmaster", "senior", "junior", "support", "partner", "support"
]

company_designations = [" SAS ", " SA ", " SARL "]


class Contact:

    def __init__(self, name, job):
        self.name = name
        self.job = job
        self.sources = []

    def __str__(self):
        return 'Contact "%s" (job: %s), sources: %s' % (self.name, self.job,
                                                        self.sources)

    def __repr__(self):
        return str(self)

    def tuple(self):
        return self.name, self.job


class BingContacts:
    def __init__(self, bing_key):
        self.key = bing_key

    def fetch(self, company_name, country, city=None, pages=1):
        for designation in company_designations:
            company_name = " " + company_name + " "
            company_name = company_name.replace(designation, "")
            company_name = company_name.strip()

        viadeo = BingViadeoContacts(self.key).fetch(company_name, country, city, pages)
        linkedin = BingLinkedinContacts(self.key).fetch(company_name, country,
                                                        city, pages)
        results = viadeo + linkedin
        results = [result for result in results if self.is_acceptable_job(result.job)]
        ret = self.merge(map(self.normalize, results))
        ret = filter(lambda x: self.remove_homonym(x, company_name), ret)
        return ret

    def normalize(self, contact):
        contact.job = contact.job.replace(" et ", " & ")\
                                 .replace(" and ", " & ")
        return contact

    def is_acceptable_job(self, job):
        for word in map(strip_punctuation, job.lower().split()):
            if word in position_whitelist:
                return True
        return False

    def remove_homonym(self, contact, company):
        return normalize(company) not in normalize(contact.name)

    def merge_positions(self, position1, position2):
        lposition1 = strip_punctuation(position1.lower())
        lposition2 = strip_punctuation(position2.lower())

        if lposition1 == lposition2:
            return position1

        if jaro_winkler(lposition1, lposition2) >= 0.7:
            return position1

        is_fr1 = self._is_french(lposition1)
        is_fr2 = self._is_french(lposition2)

        if is_fr1 and not is_fr2:
            return position1
        elif is_fr2 and not is_fr1:
            return position2

        return position1 if len(lposition1) > len(lposition2) else position2

    def _is_french(self, position):
        for token in position.split():
            if token in english_position_whitelist:
                return False
        return True

    def merge(self, contacts):
        contacts = list(contacts)
        if len(contacts) == 0:
            return []

        duplicates = []

        for current in range(len(contacts)):
            actual = contacts[current]
            for contact in contacts[current + 1:]:
                if contact.name.upper() == contacts[current].name.upper():
                    actual.job = self.merge_positions(contact.job, actual.job)
                    actual.sources.extend(contact.sources)
                    duplicates.append(contact)

        return [contact for contact in contacts if contact not in duplicates]


class BingViadeoContacts:

    def __init__(self, bing_key):
        self.bing = BingAPIFetcher(bing_key)
        self.viadeo = ViadeoFetcher()

        self.logger = logging.getLogger("webmining:viadeo_contact_fetcher")
        self.logger.setLevel(logging.INFO)

    def fetch(self, company_name, country, city=None, pages=1):
        # Viadeo activated for France only
        if country != "FR":
            return []

        for designation in company_designations:
            company_name = " " + company_name + " "
            company_name = company_name.replace(designation, "")
            company_name = company_name.strip()

        tld = country_to_tld[country]
        query = '(site:%s.viadeo.com/%s/profile) intitle:"%s" %d' % (tld, tld, company_name, datetime.datetime.now().year)

        if city is not None:
            query = query + '("%s")' % (company_name, city)

        results = []
        for page in range(pages):
            results += self.bing.fetch(query, start=page, country=country)

        all_contacts = []
        for res in results:
            # Sometimes, the "©{year}" in the footer is the snippet
            if "©" in res.snippet:
                continue
            if company_name.lower() not in res.title.lower():
                continue

            contact = self.extract(company_name, res.title, res.snippet)
            if contact is not None:
                contact_obj = Contact(*contact)
                contact_obj.sources.append(res.url)
                all_contacts.append(contact_obj)

        self.logger.info("%d contacts found" % len(all_contacts))

        return all_contacts

    def extract(self, company_name, title, snippet):
        name = title.split(",")[0]

        for month in months:
            snippet = snippet.replace(month, "")
            snippet.replace("  ", " ")

        for pattern in viadeo_snippet_patterns:
            match = re.search(pattern % datetime.datetime.now().year, snippet, re.I)
            if match:
                return name, match.group("job")
        return None


class BingLinkedinContacts:
    def __init__(self, bing_key):
        self.bing = BingAPIFetcher(bing_key)
        self.logger = logging.getLogger("webmining:linkedin_contact_fetcher")
        self.logger.setLevel(logging.INFO)

    def fetch(self, company_name, country, city=None, pages=1):
        tld = country_to_tld[country]
        query = '(site:%s.linkedin.com/pub/ OR site:%s.linkedin.com/in/) ' % (tld, tld)

        if city is not None:
            query = query + '("%s" "%s") ' % (company_name, city)
        else:
            query = query + '"%s"' % (company_name,)

        results = []
        for page in range(pages):
            results += self.bing.fetch(query, start=page, country=country)

        all_contacts = []
        for res in results:
            if "/pub/dir" in res.url:
                continue

            contact = self.extract(company_name, res.title, res.snippet)
            if contact is not None:
                contact_obj = Contact(*contact)
                contact_obj.sources.append(res.url)
                all_contacts.append(contact_obj)

        return all_contacts

    def extract(self, company, title, snippet):
        contact_name = re.match("^(.*) \\| LinkedIn", title)
        normalize = lambda x: x.strip().lower()

        if contact_name is None:
            return None

        contact_name = contact_name.group(1)
        job = None
        matched_company = None
        self.logger.debug("Searching data for %s in company %s" % (contact_name, company))

        # good snippets come in the form 'Contact Name. Title. Location.'
        """
        'Clément Chastagnol. R&D Engineer chez Data Publica, PhD in Computer Sciences. Lieu Région de Paris , France Secteur Études/recherche'
        """
        m = re.match("%s\. (.+?)(?: chez | at | @ )(.+?)\. " % re.escape(contact_name), snippet)
        if m is not None:
            job = m.group(1)
            matched_company = m.group(2)

            if normalize(company) in normalize(matched_company):
                return (contact_name, job)
            else:
                self.logger.warning("Company name mismatch for %s : %s VS %s" % \
                                    (contact_name, company, matched_company))

        return None


if __name__ == "__main__":
    import csv

    bing = BingContacts("ERie4sUx5F4tnOOphz4IVfOj3tnR8Ba1xBxCZPkZqqo=")

    with open(sys.argv[1], "r") as f:
        reader = csv.reader(f, delimiter="\t")
        for row in reader:
            contacts = bing.fetch(row[1])
            for contact in contacts:
                temp = row[:]
                temp.extend([contact.name, contact.job])
                temp.extend(contact.sources)
                print("\t".join(temp))
