# Modify the import path to find our package
import sys
import os.path
sys.path = [os.path.abspath("../")] + sys.path


from nose.tools import raises


def create_trie():
    from entities_extractor.trie import Trie

    t = Trie()

    t["blah"] = 42
    t["blahblah"] = 51
    t["pouet"] = 1

    t["None"] = None

    return t


def test_trie_get():
    t = create_trie()

    assert t["blah"] == 42
    assert t["blahblah"] == 51
    assert t["pouet"] == 1
    assert t["None"] is None

    assert t.get("None") is None
    assert t.get("other", None) is None
    assert t.get("other", 1) == 1


def test_trie_in():
    t = create_trie()

    assert "other" not in t
    assert "pouet" in t


def test_trie_prefix():
    t = create_trie()

    assert t.prefix("other") is False
    assert t.prefix("b") is True
    assert t.prefix("bl") is True
    assert t.prefix("bla") is True
    assert t.prefix("blah") is True
    assert t.prefix("blahb") is True
    assert t.prefix("blahc") is False


@raises(KeyError)
def test_trie_key_error():
    t = create_trie()

    t["key not found"]
