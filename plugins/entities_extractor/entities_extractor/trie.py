from collections import defaultdict


class Trie:
    """A Trie implementation (http://en.wikipedia.org/wiki/Trie). """
    __slots__ = ["dict", "value", "has_value"]

    def __init__(self):
        self.dict = defaultdict(Trie)
        self.value = None
        self.has_value = False

    def __setitem__(self, key, value):
        """Insert a new value associated to key"""
        if len(key) == 0:
            self.has_value = True
            self.value = value
        else:
            self.dict[key[0]][key[1:]] = value

    def __getitem__(self, key):
        """Retrieve the value associated to the given key.
           :raise KeyError if the given key does not exist.
        """
        if len(key) == 0:
            if self.has_value:
                return self.value
            else:
                raise KeyError("")
        else:
            try:
                return self.dict[key[0]][key[1:]]
            except KeyError as e:
                raise KeyError(key[0] + e.args[0])

    def get(self, key, default=None):
        """Retrieve the value associated to the given key.
           Returns the default value if the key does not exist.
        """
        try:
            return self[key]
        except KeyError:
            return default

    def __contains__(self, key):
        """Returns true if the trie contains the given key. """
        if len(key) == 0:
            return self.has_value
        else:
            return key[0] in self.dict and key[1:] in self.dict[key[0]]

    def prefix(self, key):
        """Returns true if the prefix exists in the Trie. """
        if len(key) == 0:
            return True
        else:
            return key[0] in self.dict and self.dict[key[0]].prefix(key[1:])

    def __str__(self):  # pragma: no cover
        s = ""
        if self.has_value:
            s += " => " + str(self.value)

        for key, trie in self.dict.items():
            s += key
            s += "\n"
            for line in str(trie).split("\n"):
                s += "  " + line + "\n"

        return s
