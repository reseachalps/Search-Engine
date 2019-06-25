class Match:
    """
    Represent a positive match of an entity in the text.

    Available attributes:
      - tokens: the list of matched tokens
      - pos: the position in the original text of the match
      - value: the value associated to the match
      - text: the raw original text matched.
    """

    __slots__ = ["tokens", "value", "pos", "text"]

    def __init__(self, value):
        self.tokens = []
        self.value = value
        self.pos = (0, 0)
        self.text = None

    def _prepend_token(self, token):
        self.tokens = [token] + self.tokens
        self.pos = (self.tokens[0].pos,
                    self.tokens[-1].pos + len(self.tokens[-1].content))
        return self

    def _attach_text(self, text):
        self.text = text[self.pos[0]:self.pos[1]]
        return self

    def __str__(self):  # pragma: no cover
        return "[Match <%s>, pos=%s (%s)]" % (str(self.text),
                                              str(self.pos),
                                              str(self.tokens))

    __repr__ = __str__


def exact_match(trie, tokens, pos=0):
    """
    Find a sequence of *tokens* (starting at *pos*) that matches some entities
    in *trie*.
    Return the matches as :class:`Match` objects.
    """
    matches = []

    if trie.has_value:
        #print(" " * pos, "has value")
        matches.append(Match(trie.value))

    if pos >= len(tokens):
        return matches

    (token_hash, token) = tokens[pos]

    if token_hash in trie.dict:
        for m in exact_match(trie.dict[token_hash], tokens, pos + 1):
            matches.append(m._prepend_token(token))

    return matches
