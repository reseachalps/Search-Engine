# Modify the import path to find our package
import sys
import os.path
sys.path = [os.path.abspath("../")] + sys.path
from entities_extractor.tokenizer import tokenize


def test_tokenize():
    tokens = tokenize("Hello,world! It's-a beautiful_day.")

    assert [t.content for t in tokens] == ['Hello', ',', 'world', '!', 'It',
                                           "'", 's', '-', 'a', 'beautiful',
                                           '_', 'day', '.']


def test_alpha_num():
    tokens = tokenize("pouet42")
    assert [t.content for t in tokens] == ["pouet", "42"]


def test_num_sym():
    tokens = tokenize("42$")
    assert [t.content for t in tokens] == ["42", "$"]


def test_alpha_punct():
    tokens = tokenize("blah.blah'blah")
    assert [t.content for t in tokens] == ["blah", ".", "blah", "'", "blah"]


def test_alpha_space():
    tokens = tokenize("blah blah")
    assert [t.content for t in tokens] == ["blah", "blah"]
