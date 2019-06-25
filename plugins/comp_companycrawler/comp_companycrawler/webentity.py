import collections
from textmining import normalizer


class ReadOnlyDictionaryException(Exception):
    def __init__(self):
        Exception.__init__(self, "Impossible to set a value in this read-only dictionary")


class WebEntity(collections.MutableMapping):
    """
    Represents a website entity and all its potential
    attributes.
    Attributes are accessible as in a dictionary :
      e = Entity()
      print( e['twitter'] )
    """

    def __init__(self):
        """
        Initializes attributes
        """
        self.attributes = {"twitter", "facebook", "linkedin", "siren", "summary", "category", "countpage", "email",
                           "url", "viadeo", "phone", "domain", "has_website", "contact", "languages", "main_lang"}
        self.attr = dict()
        self.normzer = normalizer.Normalizer()

        for a in self.attributes:
            self.attr[a] = set()

    def normalize(self, stemmer):
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

        # select more frequent language in website
        if len(self.attr["languages"]) > 0:
            self.attr["main_lang"] = sorted(self.attr["languages"].items(), key=lambda x: x[1], reverse=True)[0][0]
        else:
            self.attr["main_lang"] = ""

        # Filter summary to take only the main language
        self.attr["summary"] = [page_summary
                                for (lang, page_summary) in self.attr["summary"]
                                if lang == self.attr["main_lang"]]
        self.attr["summary"] = ' '.join(self.attr["summary"])
        # Convert summary into an histogram
        self.attr["summary"] = stemmer.compute_nice_stem_hist(self.attr["summary"], self.attr["main_lang"])

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
            if self.attr[a] is not None:
                s += "[%s] %s\n" % (a, str(self.attr[a]))

        return s
