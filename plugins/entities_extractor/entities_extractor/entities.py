# Used to import
if __name__ == "__main__":  # pragma: no cover
    import sys
    import os
    sys.path.append(os.path.dirname(os.path.abspath(__file__)) + "/..")


from entities_extractor.trie import Trie
from entities_extractor.tokenizer import tokenize
from entities_extractor.match import exact_match


class Entity:
    """
    A class to represent an entity.

    Attributes:
     - entity: the text expression to match
     - data: any user data to return when matching *entity*
    """

    __slots__ = ["data", "entity"]

    def __init__(self, entity, data):
        """
        Construct an entity from:
         - entity: the text to match
         - data: whatever object you want to attach
        """
        self.entity = entity
        self.data = data

    def __str__(self):
        return "[Entity %s]" % self.entity

    __repr__ = __str__


def identity(t):
    return t


class Entities:
    """
    A class to store entities.

    Entities are stored in a :class:`entities_extractor.trie.Trie`.
    """
    def __init__(self, entities=[], tokenizer=tokenize, transform=identity):
        """
        Create an entity set from a list of entities.

        This constructor accepts a list of:
          - strings
          - :class:`entities_extractor.entity.Entity`.

        Additional arguments:
          - tokenizer: a function to transform text into
            (token, position) list
          - transform: a function to transform the text before tokenization.
            Only used during the matching. Can be used to lower() the text
            before matching it, and still have a cased-text in *Match* objects.
        """
        self.tokenizer = tokenizer
        self.entities = Trie()
        self.transform = transform

        for entity in entities:
            if isinstance(entity, str):
                entity = Entity(entity, entity)

            if isinstance(entity, Entity):
                self.add_entity(entity)
            else:
                raise TypeError("Don't know how to add object of type %s "
                                "as an Entity." % type(entity))

    def _tokenize(self, entity):
        """
        Tokenize a text, and convert its tokens in (hashcode, token) pairs
        """
        return [(hash(token.content), token)
                for token in self.tokenizer(self.transform(entity))]

    def add_entity(self, entity):
        """Add an entity to the current stored list"""
        hashcodes = [e[0] for e in self._tokenize(entity.entity)]
        self.entities[hashcodes] = entity

    def match(self, text):
        """
        Match some text against the currently stored entities.
        Returns a list of :class:`entities_extractor.match.Match`.

        >>> entities = Entities(["Data-Publica"])
        >>> text = "Chez Data-Publica, on adore le python"
        >>> match = entities.match(text)
        >>> match[0].text == "Data-Publica"
        True
        """
        tokens = self._tokenize(text)

        all_matches = []
        for pos, token in enumerate(tokens):
            for m in exact_match(self.entities, tokens,
                                 pos=pos):
                all_matches.append(m)

        all_matches = [m._attach_text(text) for m in all_matches]

        # Remove matches included in others.
        # Used to remove matches like
        # choux / choux fleurs
        to_del = set()
        for m1 in all_matches:
            if m1 in to_del:
                continue
            for m2 in all_matches:
                if m1 == m2:
                    continue
                if m2 in to_del:
                    continue
                if m1.pos[0] >= m2.pos[0] and m1.pos[1] <= m2.pos[1]:
                    to_del.add(m1)

        for d in to_del:
            all_matches.remove(d)

        return all_matches

if __name__ == "__main__":  # pragma: no cover
    print(Entities(["data-publica"]).match(" data -publica is cool"))
