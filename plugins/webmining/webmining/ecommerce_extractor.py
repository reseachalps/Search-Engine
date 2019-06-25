__author__ = 'glebourg, alcio'

import re
import os
import logging
from webmining import LIB_PATH


def load_translation_table(file_path):
    table = {}
    with open(file_path, "r") as f:
        lines = f.read().splitlines()
        for line in lines:
            id, name = line.split(",")
            table[id] = name
    return table


class ECommerceExtractor:
    def __init__(self, check_supported=True, fallback_country="US"):
        # --WARNING--
        # Last string is an horrible workaround for encoding issues
        # which should be resolved in amont (crawling, encoding detection, ...)
        cur_euro = ["EUR", "€", "euro", "âŹ", "\x80", "&euro;", "euros"]
        cur_pound = ["GBP", "£", "pound", "&pound;", "pounds"]
        cur_dollar = ["USD", "$", "dollar", "&#36;", "dollars"]

        self.check_supported = check_supported
        self.fallback_country = fallback_country
        # Key is country
        self.CURRENCY = {"FR": cur_euro, "BE": cur_euro, "UK": cur_pound, "US": cur_dollar}

        # Key is language
        # Some keywords are too small/common and need typical delimiters to avoid false positives (for ex. "cart")
        self.BASKET = {"FR": {"partial": ["panier", "caddie", "/cart/", "/mycart"],
                              "exact": ['/cart']},
                       "NL": {"partial": ["winkelmand", "winkelmandje", "winkelwagen", "winkelkarretje", "shoppingcart", "/cart/", "winkeltasje", "/mycart", "basket"],
                              "exact": ['/cart']},
                       "EN": {"partial": ["basket", "shoppingcart", "/cart/", "add to cart",  "/mycart"],
                              "exact": ['/cart']}
        }

        # "options" lists all delivery options available as a dict "name"->group id, then,
        # for each language, a key (the country ISO code) for a set containing trigger
        # words for detection
        # {"options": [{"fedex": 1}, …]), "FR": set(["livraison", …]), … }
        self.DELIVERY = {}

        # Same structure as DELIVERY
        self.PAYMENT = {}

        root_resources_dir = os.path.join(LIB_PATH, "resources")
        lang_resources_dir = os.path.join(root_resources_dir, "langs")
        country_resources_dir = os.path.join(root_resources_dir, "localization")

        # Logging initialization
        self.logger = logging.getLogger("webmining:ecommerce")
        self.logger.setLevel(logging.INFO)

        if not os.path.exists(root_resources_dir) or not os.path.exists(lang_resources_dir) \
           or not os.path.exists(country_resources_dir):
            raise RuntimeError("No resources")

        with open(os.path.join(root_resources_dir, "delivery.txt")) as f:
            options = set(f.read().splitlines())
            # Options are tuples <id>,<name> as a service may have several designations
            mapped_options = {}
            for option in options:
                id, name = option.split(",")
                mapped_options[name] = id

            self.DELIVERY["options"] = mapped_options

        with open(os.path.join(root_resources_dir, "payment.txt")) as f:
            options = set(f.read().splitlines())
            # Options are tuples <id>,<name> as a service may have several designation
            mapped_options = {}
            for option in options:
                id, name = option.split(",")
                mapped_options[name] = id

            self.PAYMENT["options"] = mapped_options

        self.TRANSLATION = {"payment": {}, "delivery": {}}
        # Loading services’ names translations
        for path in os.listdir(country_resources_dir):
            country_path = os.path.join(country_resources_dir, path)

            if os.path.isdir(country_path):
                # Translation table from id to name
                payment_path = os.path.join(country_path, "payment_services_names.txt")
                self.TRANSLATION["payment"][path] = load_translation_table(payment_path)
                delivery_path = os.path.join(country_path, "delivery_services_names.txt")
                self.TRANSLATION["delivery"][path] = load_translation_table(delivery_path)

        # Loading language specific resources
        for path in os.listdir(lang_resources_dir):
            lang_path = os.path.join(lang_resources_dir, path)
            if os.path.isdir(lang_path):
                self.PAYMENT[path] = {}

                # List of trigger words for delivery detection
                with open(os.path.join(lang_path, "delivery_triggers.txt")) as f:
                    self.DELIVERY[path] = set(f.read().splitlines())

                # List of trigger words for payment detection
                with open(os.path.join(lang_path, "payment_triggers.txt")) as f:
                    self.PAYMENT[path]["triggers"] = set(f.read().splitlines())

    def _extract_options(self, raw_html, triggers, options, translation):
        """
        :param dom: pyquery object
        :param triggers: list of words to trigger options search
        :param options: dict of options name->id to search in dom
        if a name has been matched, those with the same ids won’t be
        :param translations: dict of id->name in which translate the ids
        found
        :return: the set of options ids found in the dom, empty set
        if detection not triggerred or no options found
        """

        raw_html = raw_html.lower()

        # We search for trigger words in the html, if found
        # we use expensive regex searches
        triggered = False
        for trigger in triggers:
            if trigger in raw_html:
                triggered = True
                break

        if not triggered:
            return set()

        found = set()
        matched_ids = set()
        for name, id in options.items():
            # We don’t search for «master card» if we previously matched
            # «mastercard»
            if id in matched_ids:
                continue

            # An option must be surrounded by non alphanumericals
            # it avoids false positive such as «cups» matching «UPS»
            match = re.search("([^-\\w]%s\\W)" % name, raw_html)
            if match is not None:
                if id not in translation:
                    raise ValueError("No translation for [%s]" % name)
                found.add(translation[id])
                matched_ids.add(id)

        return found

    def extract_delivery_options(self, raw_html, country, lang):
        """
        :param lang: language of the webpage
        :param country: country in which localize the options’ names
        :return: a list of delivery options available on this webpage if a
        trigger word for detection is present in this page else return an
        empty list
        """

        if lang not in self.DELIVERY:
            self.logger.info("Language not managed for deliveries [%s]" % lang)
            return set()

        if country not in self.TRANSLATION["delivery"]:
            if self.check_supported:
                self.logger.warning("Country with no translations [%s]" % country)
                return set()
            else:
                # country is not supported but we don't want to fail
                # let's use the fallback country
                country = self.fallback_country

        options = self._extract_options(raw_html, self.DELIVERY[lang], self.DELIVERY["options"],
                                        self.TRANSLATION["delivery"][country])
        # Special case for DE post, as it is case sensitive in order to avoid false positives.
        match = re.search("(\\WDE Post\\W)", raw_html)
        if match is not None:
            options.add("DE Post")

        return options

    def extract_payment_options(self, raw_html, country, lang):
        """
        :param lang: language of the webpage
        :param country: country in which localize the options’ names
        :return: a list of payment options available on this webpage if a
        trigger word for detection is present in this page else return an
        empty list
        """

        if lang not in self.PAYMENT:
            self.logger.info("Language not managed for payments [%s]" % lang)
            return set()

        if country not in self.TRANSLATION["payment"]:
            if self.check_supported:
                self.logger.warning("Country with no translations [%s]" % country)
                return set()
            else:
                # country is not supported but we don't want to fail
                # let's use the fallback country
                country = self.fallback_country

        return self._extract_options(raw_html, self.PAYMENT[lang]["triggers"],
                                     self.PAYMENT["options"],
                                     self.TRANSLATION["payment"][country])

    def extract_basket(self, dom, lang):
        """
        :param dom: pyquery object
        :param lang: lang of the website
        :return: true if at least ecommerce basket link was found
        """
        if lang not in self.BASKET:
            self.logger.info("Language not managed [%s]" % lang)
            return False

        # basket in h2 or a or button (input, button or submit tags)
        for node in dom("h2, a, input[type=button], input[type=submit], button, submit, span, div").items():
            txt = value = href = _id = clas = ""
            alts = []

            # We do not want to match content text on these tags to avoid
            # false positives
            if node[0].tag == "div":
                _id = (node.attr.id or "").lower()

            elif node[0].tag == "span":
                _id = (node.attr.id or "").lower()
                clas = (node.attr["class"] or "").lower()

            else:
                txt = node.text().lower()
                value = (node.attr.value or "").lower()
                href = (node.attr.href or "").lower()
                _id = (node.attr.id or "").lower()
                clas = (node.attr["class"] or "").lower()

                if node[0].tag == "a":
                    for nd in node[0]:
                        if nd.tag == "img" and "alt" in nd.attrib:
                            alts.append(nd.attrib["alt"])

            # partial match
            for b in self.BASKET[lang]["partial"]:
                if b in txt or b in value or b in href or b in _id or b in clas:
                    return True
                for alt in alts:
                    if b in alt:
                        return True

            # exact match
            for b in self.BASKET[lang]["exact"]:
                if b == value or b == href or b == _id or b == clas or b == txt:
                    return True

            # Special case



        return False

    def extract_prices(self, raw_html, country):
        """
        :param raw_html: raw html extracted from webpage
        :param country: country of the website
        :return:
        """

        # Replacing span tags by a space (for cases where the currency symbol
        # is in a span tag, as in test/resources/ecommerce/judith_leviant_tasse_koala.html)
        raw_txt = re.sub("</?span.*?>", " ", raw_html)

        # We create one big regex combining all the possible forms of prices.
        # This ensures that all the matches are non-overlapping, useful for
        # strings like '10€50'
        re_prices = []
        prices = []

        if not self.check_supported and country not in self.CURRENCY:
            # If country is not found in current currency, and we don't want to check if country is supported
            # Let's use fallback country
            country = self.fallback_country

        for cur in self.CURRENCY[country]:
            # Matches things like : 2,00 €   2000€   2.000.000,00 €
            re_prices.append(re.compile(
                "("
                "(?:\d{1,3})"  # First digits of the price (ex 10 for 10.000€ or 10 for 10€53)
                "(?:[ .]?\d{3}){0,3}"  # Eventual groups of three digits (ex: .000 for 10.000€)
                "(?:[.,]\d{1,2})?"  # Eventual cents for 10,53€
                "(?:\s?%s\s?)"  # Currency symbol
                "\d{0,2}?"  # Eventual cents for 10€53
                ")"
                "\W"  # We want a non alphanumerical char to end the price
                % cur)
            )

            # Matches things like : €2,00   € 2000  € 2.000.000,00
            re_prices.append(re.compile(
                "\W"  # We want an non alphanumerical char to begin the price
                "\s?%s\s?"  # Currency symbol (maybe surrounded by whitespace
                "("  # Price group
                "(?:\d{1,3})"  # First group of digits (ex 10 for €10.000 or 10 for €10,53)
                "(?:[ .]?\d{3}){0,3}"  # Eventual groups of three digits (ex .000 for €10.000)
                "(?:[.,]\d{1,2})?"  # Cents
                ")"
                % cur)
            )

        for r in re_prices:
            prices.extend(r.findall(raw_txt))

        # Normalize prices and removed prices <= 0
        prices = [self.normalize_price(p) for p in prices]
        prices = [p for p in prices if p > 0.0]

        return prices

    @staticmethod
    def normalize_price(price):
        """
        :param price: a string containing a price
        :return: a float
        """
        p = price.replace(" ", "")

        # prices as 23.000€ turned into 23000€
        # Whe the \., because without it,
        # any number with more than 3 digits would be truncated by one digit
        p = re.sub(r"\.(\d{3})", r"\1", p)

        p = re.sub("[,.]", ".", p)

        # Replace the currency symbol by a .
        p = re.sub("[^0-9]+", ".", p)

        # Only in two cases may the currency symbol be present in the price to normalize :
        # 10,53€ or 10€53
        # As we have replaced it (as well as ,) by a . we may have either of those :
        # 10.53. or 10.53
        # So if the last char of p is a dot, it’s superfluous
        if p[-1] == ".":
            p = p[:-1]

        p = float(p)
        return p
