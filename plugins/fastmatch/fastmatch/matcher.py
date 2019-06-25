from Levenshtein import ratio
from itertools import groupby


class Matcher:
    """
    Matcher finds similar words to a given candidate in a list
    it is smarter than difflib.get_close_matches in that it groups
    the list of words by length and searches matches only in the length
    groups that could match with the string (currently it searches only
    for shorter matches.

    The score is very similar to difflib.get_close_matches.
    """

    def __init__(self):
        self.words = {}
        self.min_len = self.max_len = 0

    def set_words(self, words):
        """
        Takes a list of words (can be sentences too) and groups them by
        length, it’ll search matches in this list
        """

        sorted_words = sorted(words, key=lambda x: len(x))
        grouped = groupby(sorted_words, len)
        for key, group in grouped:
            self.words[key] = list(group)
        lengths = sorted(self.words.keys())
        self.min_length = lengths[0]
        self.max_length = lengths[-1]

    def match(self, word, threshold=0.9, ncandidates=1):
        """
        Searches for words similar to word in the list given to #set_words
        it returns the best ncandidates above the threshold
        """

        candidates = []
        begin = max(round(len(word) * threshold) - 1, self.min_length)
        end = min(len(word), self.max_length)

        if end not in self.words:
            if end < self.min_length:
                # We don’t want to match longer words that the given one
                return candidates
            else:
                end = self.max_length

        # Look for a direct match if possible
        if len(word) == end and word in self.words[end]:
            return [(word, 1)]

        # If no direct match, we look for words in descending length from the
        # length of the word
        for i in range(end, begin - 1, -1):
            if i not in self.words:
                continue
            for known_word in self.words[i]:
                d = ratio(word, known_word)
                if d > threshold:
                    candidates.append((known_word, d))

        return list(sorted(candidates, key=lambda x: x[1], reverse=True)[:ncandidates])
