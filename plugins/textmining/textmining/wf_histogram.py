# -*- coding: utf8 -*-

import operator
import math
import logging
from textmining.blacklist import Blacklist
from textmining.normalizer import Normalizer

__author__ = "Guillaume Lebourgeois"
__email__ = "guillaume.lebourgeois@data-publica.com"
__status__ = "dev"


class WFHistogram:

    """
    Word Frequencies Histogram : representing a document as
    token frequencies.
    This class aims at tokenizing text, records it as histograms,
    and then enable comparisons, extractions, some semantic.
    """

    def __init__(self, text="", locale="FR", bl_words=set(), separators=set(), normalize=False):
        """
        param: text: text to be histogrammed
        param: locale: indicate a tld to apply special rules (plural management)
        param: bl_words: additional blacklisted words
        param: separators: additional separator characters
        """

        self.raw = text
        self.freq = {}
        self.tokens = set()
        self.blacklist = Blacklist().bl
        self.locale = locale
        self.sep = set([' ', '\n', '\t', '<', '>', '.', ',', ':', '/', '\r', ';', '!',
                        '?', '(', ')', '"', '|', "'", '’', '+', '*', '}', '{', '»', '[',
                        ']', '«', '=', '#', "·", "“", "”"])

        self.plurals = {
            "FR": ["s"],
            "NL": ["en", "s", "eren", "a"],
        }

        # Logging initialization
        self.logger = logging.getLogger("textmining:wf_histogram")
        self.logger.setLevel(logging.INFO)

        # Adding eventual additional blacklisted words
        for tok in bl_words:
            self.blacklist.add(tok)

        if normalize:
            diacritic_bl = set()
            normalizer = Normalizer()
            for tok in self.blacklist:
                ntok = normalizer.normalize_text(tok)
                diacritic_bl.add(ntok)
            self.blacklist = diacritic_bl
            self.raw = normalizer.normalize_text(self.raw)
        # Adding eventual additional separators
        for sep in separators:
            self.sep.add(sep)

        # Building histogram
        self._make_histogram(self.raw)

    def get_best_words(self, limit=10, dic=False):
        best = []
        count = 0
        f = self.freq

        s = sorted(f.items(), key=lambda f: f[1], reverse=True)

        for tupl in s:
            if not dic:
                best.append(tupl[0])
            else:
                best.append({tupl[0]: tupl[1]})
            count += 1

            if count >= limit:
                break

        return best

    def add_text(self, t):
        if t is not None:
            self.raw += t
            self._make_histogram(t)
        else:
            self.logger.warning("Ignored None text in add_text.")

    def _merge_plural_token(self, token, plural_form):
        """
        Merges frequencies from a singular and its plural form,
        keeping only the singular one
        """

        plural = token + plural_form
        try:
            self.freq[token] += self.freq[plural]
        except:
            self.freq[token] = self.freq[plural]
            self.tokens.add(token)
        del self.freq[plural]
        self.tokens.remove(plural)

    def _make_histogram(self, t):
        t = t.lower()

        for s in self.sep:
            t = t.replace(s, ' ')

        for token in t.split():
            if len(token) > 2 and \
               token not in self.blacklist and \
               not token.isdigit():
                tok = token

                # Singularizing tokens
                plural_form = None
                for plural in self.plurals[self.locale]:
                    if token.endswith(plural):
                        plural_form = plural

                # If the word is a singular, we check if the plural is not yet
                # in the tokens
                if plural_form is None:
                    for plural in self.plurals[self.locale]:
                        if token + plural in self.tokens:
                            self._merge_plural_token(token, plural)
                # If the plural the token is plural, we replace it by its
                # singular form if it exists
                else:
                    singular = token[:-len(plural_form)]
                    if singular in self.tokens:
                        tok = singular

                # Recording the token occurence
                try:
                    self.freq[tok] += 1
                except:
                    self.freq[tok] = 1
                    self.tokens.add(tok)

    def merge(self, hist):
        """
        Merge this histogram with another one.
        """
        for t in hist.tokens:
            # new token
            if t not in self.tokens:
                self.freq[t] = hist.freq[t]
                self.tokens.add(t)
            # know token, add new frequency
            else:
                self.freq[t] += hist.freq[t]

    def get_new_words(self, hist):
        """
        Given an other hist, returns tokens not know in
        current hist.
        """
        diff = []
        res = {}
        num_words = len(self.tokens)

        for t in hist.tokens:
            if t not in self.tokens:
                diff.append(t)

        res["tokens"] = diff
        if num_words < 1:
            res["percent"] = 1
        else:
            res["percent"] = float(len(diff)) / float(num_words)

        return res

    def compare(self, hist):
        """
        Will compare current histogram with an external histogram,
        and returns a score of similarity between 0 and 1
        """
        score = 0.0
        maxm = 0.0
        computed = set()  # computed keys

        # Iterating local keys
        for key in self.freq:
            num = self.freq[key]
            computed.add(key)

            if key in hist.freq.keys():
                cmp_num = hist.freq[key]
                maxm += max(num, cmp_num)
                diff = abs(num - cmp_num)
                score += diff

            else:
                score += num
                maxm += num

        # Iterating other dict keys
        for key in hist.freq:
            if key in computed:
                continue

            num = hist.freq[key]
            if key in self.freq.keys():
                cmp_num = self.freq[key]
                maxm += max(num, cmp_num)
                diff = abs(num - cmp_num)
                score += diff

            else:
                score += num
                maxm += num

        if maxm < 1:
            return 0.0

        score = 1 - (float(score) / float(maxm))

        return score

    def get_vocabulary_coverture_rate(self, hist):
        """
        Warning: this is a directionnal method
        Gives the rate of this histogram vocabulary that is
        covered by the histogram given in parameter.
        """
        count = 0
        for t in self.tokens:
            if t in hist.tokens:
                count += 1

        rate = count / len(self.tokens)

        return rate

    def uni_compare(self, hist):
        """
        Will compare current histogram with an external histogram,
        in an unilateral way (are tokens from reference in comparison).
        It takes account of tokens frequencies, to give better importance to
        the more frequent tokens.
        Returns a score of similarity between 0 and 1
        """
        score = 0.0
        maxm = 0.0
        for key in self.freq:
            num = self.freq[key]

            try:
                cmp_num = hist.freq[key]
                maxm += max(num, cmp_num)
                diff = abs(num - cmp_num)
                score += diff
            except:
                maxm += num

        if maxm < 1:
            return 0.0
        score = float(score) / float(maxm)

        return score

    def __str__(self):
        """
        Transforms histogram in a human readable string
        """

        return str(self.get_best_words())

    def csv_print(self, cutoff=0, total=0):
        """
        Transforms histogram in a human readable string
        (CSV formatting)
        Normalization by the total number of elements containing text, if total > 0.
        """
        s_hist = sorted(iter(self.freq.items()), key=operator.itemgetter(1))
        s_hist.reverse()

        if len(s_hist) < 1:
            return ""
        out = ""

        for w in s_hist:
            score = 0
            if total > 0:
                score = float(w[1]) / total
            else:
                score = float(w[1])

            if score > cutoff:
                out += "%s,%f\n" % (w[0], score)

        return out

    def filter_print(self, filter):
        """
        Print only tokens and their frequencies found in filter (which must be a set),
        with a freq > 0.25
        (CSV formatting)
        """
        filtered = {}

        if filter is None:
            return "\n"

        for t in self.freq:
            if t in filter:
                filtered[t] = self.freq[t]

        s_hist = sorted(iter(filtered.items()), key=operator.itemgetter(1))
        s_hist.reverse()

        if len(s_hist) < 1:
            return ""
        max = float(s_hist[0][1])
        out = ""
        for w in s_hist:
            score = float(w[1]) / max
            if score > 0.25:
                out += "%s;%f\n" % (w[0], score)

        return out

    def idf_print(self, idf, num_pages):
        """
        Given a dictionary containing tokens idf, and
        the number of documents, computes tf/idf for each token and returns
        only ones with a score > 0.25
        (YAML formatting)
        """
        filtered = {}
        tot = 0

        for t in self.freq:
            tot += self.freq[t]

        for t in self.freq:
            filtered[t] = (self.freq[t] / tot) * (math.log(num_pages / idf[t]))

        s_hist = sorted(iter(filtered.items()), key=operator.itemgetter(1))
        s_hist.reverse()

        if len(s_hist) < 1:
            return ""

        max = float(s_hist[0][1])
        if max <= 0:
            return ""

        out = ""
        for w in s_hist:
            score = float(w[1]) / max
            if score > 0.25:
                out += "  - !token\n"
                out += "    word: %s\n" % w[0]
                out += "    score: %f\n" % score
                # out += "%s;%f\n" % (w[0], score)

        return out

    def get_focus_score(self, vocabulary):
        """
        Computes a score of similarity between vocabulary frequencies
        and constant vocabulary.
        """
        s_hist = sorted(iter(self.freq.items()), key=operator.itemgetter(1))
        s_hist.reverse()

        if len(s_hist) < 1:
            return 0.0

        max = float(s_hist[0][1])
        normalized_scores = {}
        score = 0
        tot = 0

        if vocabulary is None:
            return 0.0

        for t in s_hist:
            s = float(t[1]) / max
            if s > 0.25:
                tot += s
                normalized_scores[t[0]] = s

        for t in vocabulary:
            try:
                score += normalized_scores[t]
            except:
                pass

        # score = score / ( tot * len( normalized_scores ) )
        score = score / len(normalized_scores)

        return score
