import collections
from textmining import normalizer
from statistics import mean, variance
from math import floor


class ReadOnlyDictionaryException(Exception):
    def __init__(self):
        Exception.__init__(self, "Impossible to set a value in this read-only dictionary")


class WebEntity(collections.MutableMapping):
    """
    Represents a web entity and all its potential
    attributes.
    Attributes are accessible as in a dictionary
    """

    def __init__(self):
        """
        Initializes attributes
        """
        self.set_attributes = {"localId", "summary", "email", "url", "phone", "fax", "domain", "contact", "contactform",
                               "legal", "useterms", "rss", "mobile", "responsive", "capital", "outlinks", "delivery_options", "payment_options"}
        self.set_attributes.update(["monitoring", "seo"])
        self.list_attributes = {"cms", "ecommerce", "addresses", "basket", "prices", "prices_per_page"}
        self.str_attributes = {"description", "metadescription", "country"}
        self.social_attributes = {"twitter", "facebook", "linkedin", "viadeo", "googleplus", "instagram", "youtube",
                                  "dailymotion", "vimeo"}
        self.dict_attributes = {"ecommerce_meta"}

        self.attr = dict()
        self.normzer = normalizer.Normalizer()

        for a in self.set_attributes:
            self.attr[a] = set()

        for a in self.list_attributes:
            self.attr[a] = list()

        for a in self.str_attributes:
            self.attr[a] = None

        for a in self.social_attributes:
            self.attr[a] = {}

        for a in self.dict_attributes:
            self.attr[a] = {}

        self.attributes = self.set_attributes | self.str_attributes | self.list_attributes | self.social_attributes | self.dict_attributes

    def export(self):
        """
        Export all attributes in a dictionary
        which can be rendered in json.
        """
        attr = self.attr.copy()

        # Json needs different social structure for JBM
        for a in self.social_attributes:
            social = []
            for account in attr[a].values():
                social.append(
                    {"account": account.account, "score": account.score, "profilePictureUrl": account.profile_picture})

            attr[a] = social

        # Json loader can't manage set objects
        for a in self.set_attributes:
            if a in ["responsive", "legal", "useterms", "seo", "mobile"]:
                if True in attr[a]:
                    attr[a] = True
                else:
                    attr[a] = False

            elif a == "contact":
                cts = []
                for c in attr[a]:
                    cts.append(c.to_dict())

                attr[a] = cts

            elif a == "email":
                emails = []
                for e in attr[a]:
                    emails.append({"email": e[0], "generic": not e[1]})

                attr[a] = emails

            elif a == "rss":
                rss = []
                for r in attr[a]:
                    if r[0] is not None and r[1] is not None:
                        rss.append({"url": r[0], "frequency": r[1]})

                attr[a] = rss

            elif a == "summary":
                attr[a] = attr[a].get_best_words(20, dic=True)

            elif type(attr[a]) == set:
                attr[a] = list(attr[a])

        # Managing addresses
        la = []
        venues = set()
        for addr in attr["addresses"]:
            if addr.address not in venues:
                a = {"address": addr.address, "zipcode": addr.zipcode, "city": addr.city}
                venues.add(addr.address)
                la.append(a)
        attr["addresses"] = la

        return attr

    def normalize(self, pages_count):
        """
        Normalizes attributes
        """

        # Normalizes phone numbers
        np = set()
        for phone in self.attr["phone"]:
            n = self.normzer.normalize_phone_number(phone)

            if n is not None:
                np.add(n)

            # If normalization failed, we do not record phone
            else:
                pass

        self.attr["phone"] = np

        # Normalizes fax numbers
        nf = set()
        for fax in self.attr["fax"]:
            f = self.normzer.normalize_phone_number(fax)

            if f is not None:
                nf.add(f)

            # If normalization failed, we do not record fax
            else:
                pass

        self.attr["fax"] = nf

        # Normalize CMS found
        cms = set()
        res = []
        for c in self.attr["cms"]:
            if c["type"] not in cms:
                res.append(c)
                cms.add(c["type"])

        self.attr["cms"] = res

        # Normalize shopping platform found
        shop = set()
        res = []
        for c in self.attr["ecommerce"]:
            if c["type"] not in shop:
                res.append(c)
                shop.add(c["type"])

        self.attr["ecommerce"] = res

        if pages_count > 0:
            baskets = len([x for x in self.attr["basket"] if x is True])
            self.attr["ecommerce_meta"]["perc_pages_with_prices"] = self.attr["ecommerce_meta"]["pages_with_prices"] / pages_count
            self.attr["ecommerce_meta"]["pages_with_basket"] = baskets
            self.attr["ecommerce_meta"]["perc_pages_with_basket"] = baskets / pages_count
            self.attr["ecommerce_meta"]["avg_price"] = mean(self.attr["prices"]) if len(self.attr["prices"]) > 0 else None
            self.attr["ecommerce_meta"]["variance"] = variance(self.attr["prices"]) if len(self.attr["prices"]) > 1 else None
            self.attr["ecommerce_meta"]["avg_prices_per_page"] = mean(self.attr["prices_per_page"]) if len(self.attr["prices"]) > 0 else None

            # Computing quartiles
            if len(self.attr["prices"]) > 0:
                prices = sorted(self.attr["prices"])
                tot = len(prices)
                median = prices[floor(tot / 2)]
                quart1 = prices[floor(tot / 4)]
                quart3 = prices[floor(tot / 4 * 3)]
            else:
                median = quart1 = quart3 = None

            self.attr["ecommerce_meta"]["median_price"] = median
            self.attr["ecommerce_meta"]["first_quart_price"] = quart1
            self.attr["ecommerce_meta"]["third_quart_price"] = quart3

        # No pages crawled, values representing volumes must be initialized at 0
        else:
            for bkey in ["perc_pages_with_prices", "pages_with_basket", "perc_pages_with_basket", "pages_with_prices"]:
                self.attr["ecommerce_meta"][bkey] = 0

        self.attr["ecommerce_meta"]["payment_options"] = list(self.attr["ecommerce_meta"]["payment_options"])
        self.attr["ecommerce_meta"]["delivery_options"] = list(self.attr["ecommerce_meta"]["delivery_options"])

        # Remove potentially big fields unnecessary for JBM
        del self.attr["prices"]
        del self.attr["basket"]
        del self.attr["prices_per_page"]

    def __getitem__(self, key):
        """
        Overrides dict class method
        """
        return self.attr[key]

    def __setitem__(self, key, value):
        """
        Overrides dict class method.
        Our dict is read only, no set possible.
        """
        if key not in self.attributes:
            raise ReadOnlyDictionaryException
        else:
            self.attr[key] = value

    def __delitem__(self, key):
        del self.attr[key]

    def __iter__(self):
        return iter(self.attr)

    def __len__(self):
        return len(self.attr)

    def __str__(self):
        s = ""
        for a in self.attributes:
            if a in self.attr and self.attr[a] is not None:
                s += "[%s] %s\n" % (a, str(self.attr[a]))

        return s
