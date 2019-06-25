# Used to import
if __name__ == "__main__":  # pragma: no cover
    import sys
    import os
    sys.path.append(os.path.dirname(os.path.abspath(__file__)) + "/..")


import re
from entities_extractor.unicode_hack import unicode_categories as unire


######################################
# Generate a regexp needed to tokenize
######################################
_regexps = {}

for name, klass in (("alpha", "L"),
                    ("digit", "N"),
                    ("punct", "P"),
                    ("symbol", "S"),
                    ("space", "Z")):

    _regexps[name] = "".join([r for k, r in unire.items() if k[0] == klass])

# Add combining classes to alpha to avoid breaking on
# e + ◌́  = é
_regexps["alpha"] += unire["Mn"] + unire["Mc"]

_re = ['[' + e + ']+' for e in _regexps.values()]
_re = '(' + ('|'.join(_re)) + ')'
_re = re.compile(_re)
######################################


class Token:
    """
    Contains a token from the tokenizer, with its
    start position in the original text.
    """
    __slots__ = ("content", "pos")

    def __init__(self, content, pos):
        self.content = content
        self.pos = pos

    def __str__(self):
        return str((self.content, self.pos))

    __repr__ = __str__


def tokenize(text):
    """
    Tokenize text at unicode-class change transitions.
    >>> tokenize("Hello,world")
    [('Hello', 0), (',', 5), ('world', 6)]

    It also breaks at blank characters, without including them.
    >>> tokenize("Some text")
    [('Some', 0), ('text', 5)]

    >>> tokenize("Some, ,text")
    [('Some', 0), (',', 4), (',', 6), ('text', 7)]

    Alphabetical and digits characters are two separates classes.
    >>> tokenize("TRUC42")
    [('TRUC', 0), ('42', 4)]

    Other classes are punctuation and symbols.
    >>> tokenize("123$.")
    [('123', 0), ('$', 3), ('.', 4)]

    It returns the list of tokens as a list of tuples.
    The first element of the tuple is the token.
    The second argument is the position of the token in the original text.
    The position is absolute, from the beginning of the text, and tracks
    spaces.
    >>> tokenize("Some text")
    [('Some', 0), ('text', 5)]
    >>> tokenize("Some  text")
    [('Some', 0), ('text', 6)]
    """

    tokens = _re.split(text)

    current_position = 0
    pos_tokens = []

    for token in tokens:
        if len(token) == 0:
            continue
        if not token.isspace():
            pos_tokens.append(Token(token, current_position))
        current_position += len(token)

    return pos_tokens


if __name__ == "__main__":  # pragma: no cover
    s = "51TRUC42éblahè_pouet-pouet data-publica c'est data -publicool."
    print(s)
    print(tokenize(s))
