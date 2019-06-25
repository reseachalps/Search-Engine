from urllib.parse import urlparse, urljoin, parse_qs
import re
import sys
import time
import feedparser
from collections import namedtuple
import lxml.etree
import json
from fuzzywuzzy import fuzz
import logging

# Meta extractor needs DP textmining library
from textmining.contact_detecter import ContactDetecter
from textmining.phone_detecter import PhoneDetecter

from textmining.extractor import Extractor
from textmining.wf_histogram import WFHistogram
from textmining import address_detecter

# Used to enable launch as a main
import os.path
import traceback

sys.path.insert(0, os.path.abspath('.'))
from webmining.fetcher import Fetcher
from webmining.html5wrapper import HTML5Wrapper
from webmining.html5wrapper import pq
from webmining import LIB_PATH
from webmining.contactform_extractor import ContactFormExtractor
from webmining import fb_api
from webmining.ecommerce_extractor import ECommerceExtractor as ECE


class UnknownMetaException(Exception):

    def __init__(self, msg):
        pass


class MetaExtractionException(Exception):

    def __init__(self, msg):
        pass


class MetaExtractor:
    """
    Extracts wanted meta information from a WebPage HTML source
    """

    def __init__(self, proxy, tld_file=LIB_PATH + "resources/tlds.txt", check_supported=True):
        self.wrapper = HTML5Wrapper()
        # Known metas
        # self.metas = ["twitter", "facebook", "linkedin", "siren", "email", "viadeo", "phone", "contact"]
        self.metasgroups = {"Social": SocialExtractor(),
                            "Communication": CommunicationExtractor(tld_file=tld_file, proxy=proxy,
                                                                    wrapper=self.wrapper,
                                                                    check_supported=check_supported),
                            "Monitoring": MonitoringExtractor(),
                            "CMS": CMSExtractor(proxy=proxy),
                            "Shopping": ShoppingExtractor(proxy=proxy),
                            "Structure": StructureExtractor(self.wrapper),
                            "eCommerce": ECommerceExtractor(check_supported=check_supported)}
        self.proxy = proxy

    def extract(self, metas, html, relevant_txt, url, firstpage, country="FR", lang="FR"):
        """
        Extract asked metas from html
        param: metas: meta-data to extract
        param: html: html version of the webpage, can be a parsed pq element
        param: relevant_txt: text version of the webpage
        param: url: url webpage
        param: firstpage: a boolean saying if this page is the first to compute for a website.
               It is used to avoid massive fetching of pages for the extraction of some metas.
            param: country: indicates wich extractions to use (upper case ISO format)
        """

        dom = None
        results = {}

        if not isinstance(html, pq):
            try:
                dom = self.wrapper.pq(html)
                # cleandom = self.wrapper.pq(html, remove_script=True)
            except:
                return {}
        else:
            dom = html
            # cleandom = html.remove("script")

        raw_txt = dom.text()  # Dumb and fast way to get whole text (perfs are similar as re.sub(<.*>))
        # clean_txt = cleandom.text()  # text without <script>, cdata in scripts, ...

        # Check asked metas exist
        for meta in metas:
            if meta not in self.metasgroups.keys():
                raise UnknownMetaException(meta)
            else:
                try:
                    ext = self.metasgroups[meta].extract(dom, raw_txt, relevant_txt,
                                                         url, firstpage,
                                                         country=country, lang=lang)
                    results.update(ext)
                except Exception as e:
                    tb = sys.exc_info()[2]
                    traceback.print_tb(tb)
                    raise MetaExtractionException("Meta Extraction exception").with_traceback(tb)

        return results


class SocialExtractor:
    """
    Extracts social information on websites :
      Company Profile (url) for : twitter, facebook, linkedin, viadeo, googleplus
      Presence of videos (boolean) for : youtube, dailymotion, vimeo
    """

    def __init__(self):
        # Social: Facebook, Twitter, Google Plus, Linkedin, Viadeo, Instagram, YouTube, dailymotion, vimeo
        self.metas = ["twitter", "facebook", "linkedin", "viadeo", "googleplus", "instagram"]
        self.metas.extend(["youtube", "dailymotion", "vimeo"])

    def extract(self, dom, raw_txt, relevant_txt, url, firstpage, country="FR", lang="FR"):
        results = {}

        # For each link in dom
        for node in dom("a[href]").items():
            href = node.attr.href

            # For each social network where no video found yet
            for m in self.metas:
                if m not in results.keys() or results[m] is None:
                    results[m] = self._extract_social_link(link=href, meta=m)

        # Some links can only be found in iframes
        for node in dom("iframe[src]").items():
            src = node.attr.src
            # For each social network where no video found yet
            for m in ["dailymotion", "vimeo", "youtube", "facebook"]:
                if m not in results.keys() or results[m] is None:
                    results[m] = self._extract_social_link(link=src, meta=m)

        return results

    def _extract_social_link(self, link, meta):
        """
        Switch toward specialized link extractors
        """
        if meta == "twitter":
            return self.extract_twitter(href=link)
        if meta == "facebook":
            return self.extract_facebook(href=link)
        if meta == "linkedin":
            return self.extract_linkedin(href=link)
        if meta == "viadeo":
            return self.extract_viadeo(href=link)
        if meta == "googleplus":
            return self.extract_googleplus(href=link)
        if meta == "instagram":
            return self.extract_instagram(href=link)
        if meta == "youtube":
            return self.extract_youtube(href=link)
        if meta == "dailymotion":
            return self.extract_dailymotion(src=link)
        if meta == "vimeo":
            return self.extract_vimeo(src=link)

    def extract_instagram(self, href):
        """
        Tries to extract Instagram account URL
        """
        m = re.search("(https?://instagram.com/.+)", href)
        if m is not None:
            return m.group(1)

        return None

    def extract_vimeo(self, src):
        """
        Tries to extract vimeo URL to a video or channel
        """
        # <iframe src="http://player.vimeo.com/video/56553983?title=0&byline=0&portrait=0"></iframe>
        m = re.search("(https?://player.vimeo.com/.+)", src)
        if m is not None:
            return m.group(1)

        return None

    def extract_dailymotion(self, src):
        """
        Tries to extract dailymotion URL to a video or channel
        """
        # <iframe frameborder="0" width="270" height="152" src="http://www.dailymotion.com/embed/video/xq6zpm?logo=0&hideInfos=1"></iframe>
        m = re.search("(https?://www.dailymotion.com/.+)", src)
        if m is not None:
            return m.group(1)

        return None

    def extract_youtube(self, href):
        """
        Tries to extract youtube URL to a video or channel
        """
        # href="http://www.youtube.com/embed/JW7jK3UXHQo?autoplay=1&rel=0"
        m = re.search("(https?://www.youtube.com/.+)", href)
        if m is not None:
            return m.group(1)

        return None

    def extract_twitter(self, href):
        """
        Tries to extract twitter account as @account_name
        """
        BLACKLIST = ["share", "home", "intent", "account", "search", "signup"]
        BLACKLIST += ["privacy", "timeline", "twitterapi", "articles"]
        BLACKLIST += ["twitter", "statuses"]

        # <a href="http://www.twitter.com/datapublica" target="_blank"><img src="/static/img/twitter_16.png" alt="twitter_16" width="16" height="16"> Twitter</a>
        m = re.search("twitter.com/"  # root
                      # optional fail from webmaster
                      "(?:https?://www.twitter.com/)?"
                      "(?:#!/)?"  # optionnal hashbang escape
                      "@?"  # optionnal @ before account
                      "(\w+)"  # account (\w → [a-zA-Z0-9_])
                      "(?:$|[/#?\"'])"  # must be terminated by end of string, or /#?, or "' if this was some javascript
                      "", href)

        if m is not None:
            account = m.group(1)

            # We ignore share buttons
            for blacklisted in BLACKLIST:
                if account.startswith(blacklisted):
                    return None
            return "@" + account.lower()

        return None

    def extract_facebook(self, href):
        """
        Tries to extract facebook account URL
        """

        # like box matching
        # <iframe src="http://www.facebook.com/plugins/likebox.php?href=https%3A%2F%2Fwww.facebook.com%2Fcompapharma&amp;width=250&amp;height=100&amp;show_faces=false&amp;colorscheme=light&amp;stream=false&amp;show_border=false&amp;header=false" scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:250px; height: 100px;" allowTransparency="true"></iframe>
        if "//www.facebook.com/plugins/likebox.php" in href:
            params = parse_qs(urlparse(href).query)
            if "href" in params:
                href = params["href"][0]

        # <a href="http://www.facebook.com/datapublica" target="_blank"><img src="/static/img/facebook_16.png" width="16" height="16"> Facebook</a>
        facebook = fb_api.get_facebook_account(href)
        if facebook is not None:
            return "http://www.facebook.com/" + facebook

        return None

    def extract_viadeo(self, href):
        """
        Tries to extract viadeo account URL
        """
        # http://www.viadeo.com/fr/profile/sas.neoppidum
        # country code is not always indicated
        m = re.search("(https?://www.viadeo.com/\w*/?profile/.+)", href)
        if m is not None:
            return m.group(1).lower()

        return None

    def extract_linkedin(self, href):
        """
        Tries to extract linkedin account URL
        """
        href = href.lower()
        # http://www.linkedin.com/company/data-publica
        m = re.search("https?://(?:www\.)?linkedin.com/(company/[^/]+)", href)
        if m is None:
            return None

        account = m.group(1)
        account = re.sub("\?.*$", "", account)
        account = re.sub("#.*$", "", account)
        account = "http://www.linkedin.com/%s" % account

        return account

    def extract_googleplus(self, href):
        """
        Tries to extract google+ account URL
        """
        # href="https://plus.google.com/110323587230527980117?prsrc=3"
        # href="https://plus.google.com/u/0/101330197763441062911/posts"
        m = re.search("(//plus.google.com/.+)", href)
        if m is not None and "share?" not in m.group(1):
            return "https:" + m.group(1).lower()

        return None


class CommunicationExtractor:

    def __init__(self, tld_file, proxy, wrapper, check_supported=True):
        # Logging initialization
        self.logger = logging.getLogger("webmining:communication_extractor")
        self.logger.setLevel(logging.INFO)

        self.check_supported = check_supported
        # Communication: SIREN, phone, contact form, emails, RSS, RSS/week, legal mention,
        #                Mobile site, responsive site
        self.metas = ["localId", "phone", "email", "contact", "contactform", "legal",
                      "useterms", "rss", "mobile", "responsive", "capital", "description", "addresses"]

        self.check_supported = check_supported

        # Loading all the localized resources
        resources_dir = os.path.join(LIB_PATH, "resources/localization")

        if not os.path.exists(resources_dir):
            raise NotImplementedError("No resources")

        # Cache where country specific resources are cached
        self.localization_cache = {}

        # Cache containing the current domain’s fetched rss links
        self.rss_cache = set()

        # Cache containing information for email filtering
        self.email_filtering_data = None
        with open(os.path.join(LIB_PATH, "resources", "email_filtering.json"), "r") as f:
            self.email_filtering_data = json.load(f)

        # Iterating over country specific resources
        for path in os.listdir(resources_dir):
            # We consider that all directories in the resources_dir represent a
            # country
            if os.path.isdir(os.path.join(resources_dir, path)):
                country_name = path
                country_path = os.path.join(resources_dir, path)
                country = namedtuple(
                    "country", ["legals", "useterms", "identification", "generic_emails"])

                with open(os.path.join(country_path, "generic_emails.txt"), "r") as f:
                    country.generic_emails = set(map(str.strip, f.readlines()))

                with open(os.path.join(country_path, "legals.txt"), "r") as f:
                    country.legals = set(map(str.strip, f.readlines()))

                with open(os.path.join(country_path, "useterms.txt"), "r") as f:
                    country.useterms = set(map(str.strip, f.readlines()))

                with open(os.path.join(country_path, "identification.txt"), "r") as f:
                    country.identification = set(
                        map(lambda x: re.compile(x.strip()), f.readlines()))

                self.localization_cache[country_name] = country

        self.contacter = ContactDetecter()
        self.extor = Extractor()
        self.ad = address_detecter.AddressDetecter(cache_results=True, check_supported=check_supported)
        self.tlds = set()
        self.tel = PhoneDetecter()
        self.fetcher = Fetcher(proxy=proxy)
        self.iosfetcher = Fetcher(proxy=proxy,
                                  user_agent="Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3")

        self.wrapper = wrapper
        # Used to tell when to empty the address detecter’s results cache
        # i.e. when we change website
        self.previous_domain = None

        # Allow countries to specify an other country code for phone detection
        self.phone_country = {'UK': 'GB'}

        # TLDin this file are from IANA organisation
        with open(tld_file) as f:
            for tld in f:
                self.tlds.add(tld.strip())

    def get_country(self, country):
        if country not in self.localization_cache:
            if self.check_supported:
                raise NotImplementedError("No resource for country %s" % country)
            else:
                self.logger.warn("Unsupported country %s" % country)
                return None
        return self.localization_cache[country]

    def extract(self, dom, raw_txt, relevant_txt, url, firstpage, country="FR", lang="FR"):
        results = {}

        domain = urlparse(url).hostname
        if self.previous_domain is None or domain != self.previous_domain:
            self.previous_domain = domain
            self.ad.empty_cache()

        if firstpage:
            self.rss_cache = set()

        results["localId"] = self.extract_id(raw_txt, country=country)
        results["phone"], results["fax"] = self.extract_phone(
            raw_txt, country=country)
        results["email"] = self.extract_email(dom, raw_txt, domain, country=country)
        results["contact"] = self.extract_contacts(raw_txt)
        results["legal"] = self.extract_legal(raw_txt)
        results["useterms"] = self.extract_useterms(raw_txt)
        results["rss"] = self.extract_rss(dom, url)
        results["responsive"] = self.extract_responsive(dom)
        results["description"] = self.extract_description(dom)
        results["capital"] = self.extor.extract_capital(raw_txt)
        results["addresses"] = self.ad.detect_addresses(
            raw_txt, html=False, country=country)

        # This extraction does an xtra fetch, we only do it for the first page
        if firstpage:
            results["mobile"] = self.extract_mobile(url)

        if self.extract_contactform(dom):
            results["contactform"] = url
        else:
            results["contactform"] = None

        return results

    def extract_mobile(self, url):
        """
        http://www.cabinetnardi.com/mobile/
        http://le-choix-funeraire.mobi/
        http://iphone.revision-et-finance-cogefor.fr
        http://m.agencecomtesse.com
        """
        up = urlparse(url)
        domain = up.scheme + "://" + up.netloc
        fr = self.iosfetcher.fetch(domain)
        if fr is not None and fr.fetched_url != domain:
            if "mobile" in fr.fetched_url or \
                    ".mobi" in fr.fetched_url or \
                    "iphone" in fr.fetched_url or \
                    "//m." in fr.fetched_url:
                return True

        return None

    def extract_responsive(self, dom):
        return len(dom("meta[name='viewport']")) > 0

    def extract_description(self, dom):
        """
        Extracts content from meta description in headers

        param: dom: the dom where to apply extraction
        """
        description = None
        desc = dom("meta[name='description']")

        # TODO: manage og
        # desc = dom("meta[name='og:description']")

        if desc.length > 0:
            description = ""
            for d in desc.items():
                if d is not None and d.attr is not None and \
                   d.attr.content is not None:
                    description += d.attr.content + ' '

        if description is None or not self._validate_description(description):
            return None

        # Remove HTML tags if present, but keep newline tags as newlines
        for tag in HTML5Wrapper.newline_tags:
            regex = "</?%s.*?>" % tag
            description = re.sub(regex, "\n", description, flags=re.I | re.M)

        # Remove remaining tags
        description = re.sub("<.+?>", " ", description, flags=re.M)
        # Remove supernumerary newlines and spaces
        description = re.sub(r"\n{2,}", "\n", description)
        description = re.sub(" {2,}", " ", description)

        return description.strip()

    def _validate_description(self, desc):
        """
        Determines if an extracted description seems to be a quality one.
        """
        badstart = ("site", "bienvenue", "joomla", "wordpress")
        badend = ("...")
        normed = desc.lower().strip()

        if normed.startswith(badstart):
            return False

        if normed.endswith(badend):
            return False

        wf = WFHistogram(normed)
        if len(wf.freq) < 5:
            return False

        return True

    def _find_rss(self, dom, url):
        domain = urlparse(url).netloc
        rsslink = None
        # First looking into head links
        # supports "rss+xml"
        for link in dom("head link[type*='application/rss'][href]").items():
            rsslink = urljoin(url, link.attr.href)
            break

        if rsslink is None:
            for node in dom("a[href]").items():
                href = node.attr.href
                # If this link could be a rss one
                if "rss" in href.lower():
                    rsslink = ""
                    if href.startswith("http"):
                        if domain in url:
                            rsslink = href
                        else:
                            continue
                    # Build absolute link from relative link
                    else:
                        rsslink = urljoin(url, href)

                    break
        # replace feed:// with http://
        if rsslink is not None and rsslink.startswith("feed:"):
            rsslink = rsslink[5:]
            if rsslink.startswith("//"):
                rsslink = "http:" + rsslink
                # supports feed:https:// as well!

        # If the rss feed is unknown, we return it
        if rsslink not in self.rss_cache:
            self.rss_cache.add(rsslink)
            return rsslink
        else:
            return None

    def extract_rss(self, dom, url):
        rsslink = self._find_rss(dom, url)
        # no rss found
        if rsslink is None:
            return (None, None)

        # One a potential RSS link has been found, let's check it out
        return self._compute_rss_stats(rsslink, self.fetcher.fetch(rsslink, debug=True))

    def _compute_rss_stats(self, rsslink, fr):
        if fr is not None and not ("application/xml" in fr.headers["content-type"] or
                                   "text/xml" in fr.headers["content-type"] or
                                   "application/rss+xml" in fr.headers["content-type"]):
            return (None, None)

        try:
            rss = self.wrapper.pq(fr.webpage)
        except (lxml.etree.XMLSyntaxError, lxml.etree.ParserError):
            return (rsslink, 0)

        # Now let's get more recent and oldest item dates in stream
        first = last = None
        count = 0
        for entry in rss("item").items():
            count += 1
            date = feedparser._parse_date(entry("pubDate").text())
            if date is not None:
                publication = time.mktime(date)
                if first is None or first < publication:
                    first = publication
                if last is None or last > publication:
                    last = publication

        # Compute ratio items per week
        if first is not None and last is not None:
            timedelta = first - last
            if timedelta > 0:
                weekratio = count / (timedelta / (7 * 24 * 60 * 60))

                return (rsslink, weekratio)

        return (rsslink, 0)

    def extract_legal(self, raw_txt, country="FR"):
        country = self.get_country(country)
        if country is None:
            return None
        low = raw_txt.lower()
        for i in country.legals:
            if i in low:
                return True
        return None

    def extract_useterms(self, raw_txt, country="FR"):
        country = self.get_country(country)
        if country is None:
            return None
        low = raw_txt.lower()
        for i in country.useterms:
            if i in low:
                return True
        return None

    def extract_contactform(self, dom):
        """
        Searches a contact form in page by looking input names in forms.
        """
        """
    Searches a contact form in page. Uses a linear classifier.
    """
        c = ContactFormExtractor(dom)

        if c.predict():
            return True
        else:
            return None

    def extract_id(self, txt, country="FR"):
        """
        Tries to extract ID (siren, siret, TVA, KBO, etc…) from page text
        """

        re_country = self.get_country(country)
        if re_country is None:
            return None

        lower_txt = txt.lower()
        for regex in re_country.identification:
            m = re.search(regex, lower_txt)
            if m is not None:
                ide = re.sub('[^\d]', '', m.group(1))

                # Checking extraction quality
                if country == "BE":
                    if len(ide) < 10:
                        ide = "0" + ide

                    if len(ide) != 10:
                        return None

                elif country == "FR":
                    if len(ide) != 9:
                        return None

                elif country == 'UK':
                    if len(ide) == 7:
                        ide = "0" + ide

                return ide

        return None

    def extract_contacts(self, raw_txt):
        return self.contacter.detect(raw_txt)

    def extract_phone(self, raw_txt, country="FR"):
        """
        Returns a tuple containing :
            - a list of detected phones
            - a list of detected faxes
        """

        phone_country_ = country
        if country in self.phone_country:
            phone_country_ = self.phone_country[country]
        results = self.tel.detect(raw_txt, country=phone_country_)
        phones = [r[1] for r in results if r[0] == "phone"]
        faxes = [r[1] for r in results if r[0] == "fax"]

        return (phones, faxes)

    def _validate_email(self, email, domain, country="FR"):
        """
        Checks out that the email is valid and usable.
        Sorts emails between generic ones and direct contacts.
        param: email: a str believed to be an email
        param: domain: the domain of the analyzed website; used to determine if an email address
                       is really related to the website
        return: a tuple (email, is_contact) where is_contact in [True, False]
                False is for generic contact emails such as jobs@foo.com
        """
        if self.check_supported:
            country = self.get_country(country)

        email = email.strip().lower()

        # We accept at maximum 3 sub-domains of mail
        m = re.search("([\w\.\-]+@[\w\-]+(\.[\w\-]+){1,3})", email)

        if m is not None:
            # email is validated, but let's check it's not a generic email
            email = m.group(1)
            prefix, suffix = email.split('@')

            # Bad suffix (domain.com, example.com...)
            if suffix in self.email_filtering_data["domains_blacklist"]:
                return None

            # Bad tld in extracted email
            if suffix.split(".")[-1] not in self.tlds:
                self.logger.info(">>> TLD refused : %s" % email)
                return None

            # Email prefix in blacklist (CNIL...)
            if prefix in self.email_filtering_data["prefixes_blacklist"]:
                self.logger.info(">>> Blacklisted email prefix found: %s" % email)
                return None

            # Fuzzy match between the suffix and the domain
            fuzzy_match = fuzz.token_sort_ratio(suffix, domain)
            # This value should be tested against a real database of examples
            fuzzy_threshold = 70
            if fuzzy_match < fuzzy_threshold:
                # Test email providers domains: if we find an email @wanadoo.fr,
                # we can't be sure it's not a real one
                if not any([fuzz.token_sort_ratio(suffix, d) >= fuzzy_threshold
                            for d in self.email_filtering_data["email_providers"]]):
                    return None

            self.logger.info("> found [" + email + "]")

            for pattern in country.generic_emails:
                if re.match(pattern, prefix) is not None:
                    return (email, False)
            return (email, True)

        else:
            self.logger.warning("WARNING>> unvalidated email : " + email)
            return None

    def extract_email(self, dom, raw_txt, domain, country="FR"):
        """
        Tries to extract email adress from mailto structure.
        If nothing found, tries a detection from raw text.
        """
        for node in dom("a[href^='mailto:']").items():
            # <a href="mailto:p.dupond@example.com">Clique ici pour m'envoyer un e-mail</a>
            mail = node.attr.href[7:]
            clear = mail.lower().split('?')
            if len(clear) > 0:
                return self._validate_email(clear[0], domain, country)
            else:
                continue

        # If no mailto found, let's try to extract an email from raw text
        # Not a findall for performance reasons
        m = re.search("[\s:]([\w\.\-]+@[\w\.\-]+)[\s\"<]", raw_txt + " ")
        if m is not None:
            return self._validate_email(m.group(1), domain, country)

        return None


class MonitoringExtractor:
    # Monitoring: Google Analytics, Xiti, Quantcast, Yandex, ComScore, seo
    def __init__(self):
        # self.metas.extend( ["seo"] )
        self.signatures = {"ganalytics": "google-analytics.com",
                           "xiti": "xiti.com/hit.xiti",
                           "quantcast": "quantserve.com",
                           "yandex": "yandex.ru",
                           "comscore": "comscore"}

    def extract(self, dom, raw_txt, relevant_txt, url, firstpage, country="FR", lang="FR"):
        found = {service: False for service in self.signatures}
        results = {"seo": False, "monitoring": []}

        # Looking for a SEO friendly website
        results["seo"] = len(dom("head link[rel='canonical']")) > 0

        # Looking for analytics scripts
        # For each link in dom
        for node in dom("script").items():
            txt = node.text() + (node.attr.src or "")

            if len(txt) > 0:
                for service in self.signatures:
                    if not found[service] and self.signatures[service] in txt:
                        found[service] = True
                        results["monitoring"].append(service)

        return results


class CMSExtractor:
    # CMS: Wordpress, Drupal, Typo3, IsoTools, Joomla, Spip, EzPublish
    def __init__(self, proxy):
        self.fetcher = Fetcher(proxy=proxy)

        # CMS identifiables via a specific URL
        self.paths = {
            "wordpress": {"path": "wp-login.php", "expression": "wordpress"},
            "drupal": {"path": "user", "expression": "user-login"},
            "isotools": {"path": "identification.aspx", "expression": "isotools"},
            "joomla": {"path": "administrator", "expression": "joomla"},
            "spip": {"path": "?page=login", "expression": "spip"}
        }
        # CMS identifiables via a specific pattern in HTML
        self.patterns = {
            "typo3": {"expression": "this website is powered by typo3"},
            "ezpublish": {"expression": "/content/advancedsearch"}
        }

    def extract(self, dom, raw_txt, relevant_txt, url, firstpage, country="FR", lang="FR"):
        results = {"cms": []}
        found = set()

        # CMS identifiables via a specific URL
        # This needs a fetch, we only do it for the first page of the crawl
        if firstpage:
            for cms in self.paths:
                up = urlparse(url)
                domain = up.scheme + "://" + up.netloc
                link = urljoin(domain, self.paths[cms]["path"])
                fr = self.fetcher.fetch(link)

                if fr is not None and fr.webpage is not None and \
                   fr.content_type is not None and "text/html" in fr.content_type.lower() and \
                   self.paths[cms]["expression"] in fr.webpage.lower():

                    if cms not in found:
                        results["cms"].append({"type": cms, "url": link})
                        found.add(cms)
                        # return results

        # CMS identifiables via a specific pattern in HTML
        for cms in self.patterns:
            if self.patterns[cms]["expression"] in raw_txt.lower():
                if cms not in found:
                    results["cms"].append({"type": cms, "url": url})
                    found.add(cms)
                    # return results

        # detect typo3 via meta as well
        if "typo3" not in results and len(dom("meta[name='generator'][content*='TYPO3']")) > 0:
            cms = "typo3"
            if cms not in found:
                results["cms"].append({"type": cms, "url": url})
                found.add(cms)

        return results


class ShoppingExtractor:
    # Shopping: OSCommerce, Prestashop, Magento, Open Cart
    def __init__(self, proxy):
        self.fetcher = Fetcher(proxy=proxy)

        # Shopping engines identifiables via a specific cookie
        self.cookies = {"oscommerce": {"cookie": "osCsid"}}

        # Shopping engines identifiables via a specific pattern in HTML
        self.patterns = {
            "prestashop": {"tag": "meta", "attribute": "content", "expression": "prestashop"},
            "magento": {"tag": "link", "attribute": "href", "expression": "/skin/frontend/"},
            "opencart": {"tag": "a", "attribute": "href", "expression": "route=checkout/cart"}
        }

    def extract(self, dom, raw_txt, relevant_txt, url, firstpage, country="FR", lang="FR"):
        results = {"ecommerce": []}

        # This needs a fetch, we only do it for the first page of the crawl
        if firstpage:
            # CMS identifiables via a specific URL
            for shop in self.cookies:
                fr = self.fetcher.fetch(url)
                if fr is not None and self.cookies[shop]["cookie"] in fr.cookies.keys():
                    results["ecommerce"].append({"type": shop, "url": url})

                    return results

        # CMS identifiables via a specific pattern in HTML
        for shop in self.patterns:
            tags = dom(self.patterns[shop]["tag"] + "[" + self.patterns[shop]["attribute"] + "]")
            for tag in tags.items():
                if self.patterns[shop]["expression"] in (tag.attr[self.patterns[shop]["attribute"]] or "").lower():
                    results["ecommerce"].append({"type": shop, "url": url})
                    return results

        return results


class ECommerceExtractor:
    def __init__(self, check_supported=True):
        self.ece = ECE(check_supported=check_supported)

    def extract(self, dom, raw_txt, relevant_txt, url, firstpage, country="FR", lang="FR"):
        ecommerce = {
            "prices": [], "basket": False, "payment": [],
            "payment_options": [], "delivery_options": []
        }

        # We use outerHtml() over html() or text() to also search in hrefs,
        # alts, as payment word is often hidden in <img>
        dom("script").remove()
        dom("style").remove()
        dom("link").remove()
        raw_html = dom.outerHtml()

        ecommerce["prices"].extend(self.ece.extract_prices(raw_html, country))
        ecommerce["basket"] = self.ece.extract_basket(dom, lang)
        ecommerce["payment_options"] = self.ece.extract_payment_options(raw_html, country, lang)
        ecommerce["delivery_options"] = self.ece.extract_delivery_options(raw_html, country, lang)
        return ecommerce


class StructureExtractor:
    def __init__(self, wrapper):
        self.wrapper = wrapper

    def extract(self, dom, raw_txt, relevant_txt, url, firstpage, country="FR", lang="FR"):
        # Init aggregation dict
        agg = {}

        # For each link in dom
        referer_domain = urlparse(url).hostname
        for link in self.wrapper.extract_doc_links(dom).keys():
            if link.startswith("http") or link.startswith("//"):
                self.aggregate(agg, referer_domain, urljoin(url, link))

        # Return tuples
        results = []
        for key in agg.keys():
            results.append((key, agg[key]))
        return {"outlinks": results}

    def aggregate(self, agg, referer_domain, url):
        domain = urlparse(url).hostname
        if domain != referer_domain:
            if domain in agg:
                agg[domain] += 1
            else:
                agg[domain] = 1


if __name__ == "__main__":
    m = MetaExtractor(proxy=None)
