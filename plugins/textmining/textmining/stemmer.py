import re
import unicodedata
import os
import textmining
os.environ["NLTK_DATA"] = os.path.join(textmining.LIB_PATH, "resources")
from nltk.stem.snowball import SnowballStemmer
from textmining.blacklist import Blacklist
import string
import logging
from collections import Counter
from itertools import groupby


logging.basicConfig(level=logging.WARNING,
                    format='[%(levelname)s][%(name)s][%(asctime)s] %(message)s')
logger = logging.getLogger("textmining.stemmer")
logger.setLevel(logging.INFO)


nltk_lang_mapping = {
    "DA": 'danish',
    "DE": 'german',
    "EN": 'english',
    "ES": 'spanish',
    "FI": 'finnish',
    "FR": 'french',
    "HU": 'hungarian',
    "IT": 'italian',
    "NL": 'dutch',
    "NO": 'norwegian',
    "PT": 'portuguese',
    # "RO": 'romanian', # Disabled: has no stopwords lists
    "RU": 'russian',
    "SV": 'swedish'
}


class Stemmer:
    stemmers = {iso: SnowballStemmer(stem_lang, ignore_stopwords=True) for iso, stem_lang in nltk_lang_mapping.items()}

    def __init__(self, keep_digits=False):
        self.valid_chars = set(string.ascii_letters)
        if keep_digits:
            self.valid_chars |= set(string.digits)

        self.stopwords = set([''.join(x for x in unicodedata.normalize('NFKD', elem) if x in self.valid_chars).lower() for elem in Blacklist().bl])

    def normalize_text(self, text):
        """
        Normalizes a string by replacing special characters with a space and removing multiple spaces.
        :param text: string to normalize
        :returns: a normalized string
        """
        # dash not replaced as we want to keep compound words (ex. porte-avions)
        # list of chars is from textmining.wf_histogram
        text = re.sub('[\.,;!\?|&\(\)\'\\\\<>:/"’\+\*\}\{«»\[\]=#·“”]+', ' ', text)
        text = re.sub('[\s]+', ' ', text)
        return text

    def strip_accents_and_lower(self, text):
        """
        Replaces the accentuated letters and converts to lowercase.
        :param text: string to normalize
        :returns: a normalized string
        """
        output = []
        for t in text.split():
            output.append(''.join(x for x in unicodedata.normalize('NFKD', t) if x in self.valid_chars).lower())
        return ' '.join(output)

    def stem_word(self, word, lang="FR"):
        """
        Simply stems a word.
        :param word: input word
        :returns: the word normalized and stemmed; empty string if stopword
        """
        stemmer = Stemmer.stemmers.get(lang)
        if stemmer is None:
            logger.warning("Language %s not found. Fallbacking to language FR" % lang)
            stemmer = self.stemmers.get("FR")

        # Normalizing input text
        word = self.normalize_text(word)
        word = self.strip_accents_and_lower(word)

        if len(word) < 2 or word in self.stopwords:
            return ""
        else:
            return stemmer.stem(word)

    def stem_text(self, text, lang="FR"):
        """
        Simply stems a text.
        :param text: input text
        :returns: a list of normalized, stemmed text; stopwords are removed
        """
        text = self.normalize_text(text)
        text = self.strip_accents_and_lower(text)

        output = [self.stem_word(t, lang=lang) for t in text.split()]

        # Filtering out empty strings
        output = list(filter(lambda x: x != "", output))

        return output

    def compute_stem_hist(self, text, lang="FR"):
        """
        Computes a bag-of-words representation on a string but uses stems instead of simple tokens.
        :param text: input text
        :returns: a dictionary: for each stem found in the text, a dictionary with all the possible
        forms and their count is associated
        """
        text = self.normalize_text(text)

        # Stem individual words
        stemmed_text = [(self.stem_word(t, lang=lang), t) for t in text.split()]

        # Filter out empty stems
        stemmed_text = filter(lambda x: x[0] != "", stemmed_text)

        # Group by stems
        stemmed_text = groupby(sorted(stemmed_text), key=lambda x: x[0])

        # Detail original words with associated count for each stem; elem is of shape (stem, word)
        stem_dict = {stem: dict(Counter([elem[1] for elem in group])) for stem, group in stemmed_text}

        # OR, you can do all of this in one big, full-o'swag one-liner:
        # return {k: dict(Counter([elem[1] for elem in v])) for k,v in groupby(sorted(filter(lambda x: x[0] != "", [(self.stem_word(t, lang=lang), t) for t in self.normalize_text(text).split()])), key=lambda x: x[0])}
        return stem_dict

    def compute_nice_stem_hist(self, text, lang="FR"):
        """
        Almost same functionality than method compute_stem_hist, but returns true word forms instead of stems.
        :param text: input text
        :returns: a dictionary: for each stem found in the text, a dictionary with the most present form and
        the total number of occurences
        """
        stem_hist = self.compute_stem_hist(text, lang)
        nice_stem_hist = {}

        for stem, form_dict in stem_hist.items():
            most_present_form = max(form_dict.items(), key=lambda x: x[1])[0]
            nice_stem_hist[most_present_form] = sum(form_dict.values())

        return nice_stem_hist

    def get_complete_vocabulary(self, corpus):
        """
        Computes recursively the vocabulary on a corpus.
        :param corpus: a list of texts, each one represented by a stem_hist, i.e. a dictionary of the form
        {stem: {possible_form: count}}
        :returns: a list of stems.
        """
        if len(corpus) == 1:
            return set(corpus[0].keys())
        else:
            l = len(corpus)
            return self.get_complete_vocabulary(corpus[:int(l / 2)]).union(self.get_complete_vocabulary(corpus[int(l / 2):]))

    def flatten_stem_corpus(self, corpus):
        """
        Flattens a corpus made of stem histogramms by computing the whole vocabulary of stems across
        the corpus and then for each doc, computing the sum of occurences of all possible forms for the current stem
        :param corpus: a list of texts, each one represented by a stem_hist, i.e. a dictionary
        of the form {stem: {possible_form: count}}
        :returns: a corpus: a list of texts, each one represented by a dictionary associating the number
        of occurences of each stem in the vocabulary
        """
        data_reduced = []
        for doc in corpus:
            d = {}
            for v in doc:
                d[v] = sum(doc[v].values())
            data_reduced.append(d)
        return data_reduced
