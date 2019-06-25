# Modify the import path to find our package
import sys
import os.path
sys.path = [os.path.abspath("../")] + sys.path


from entities_extractor.entities import Entities


def test_accents_in_entities():
    entities = Entities(["abcéefg"])
    match = entities.match("abc abcéefg abc")
    assert len(match) == 1


def test_oud():
    entities = Entities(["oud"])
    match = entities.match("ou d")
    assert len(match) == 0
    match = entities.match("oud")
    assert len(match) == 1


def test_extra_spaces():
    entities = Entities(["data-publica"])
    match = entities.match("data -    publica")
    assert len(match) == 1


def test_deleted_spaces():
    entities = Entities(["data - publica"])
    match = entities.match("data-publica")
    assert len(match) == 1


def test_transform():
    entities = Entities(["Data-Publica"], transform=lambda t: t.lower())
    match = entities.match("data-publica")
    assert len(match) == 1
    assert match[0].text == "data-publica"

    entities = Entities(["data-publica"], transform=lambda t: t.lower())
    match = entities.match("Data-Publica")
    assert len(match) == 1
    assert match[0].text == "Data-Publica"


def test_choux_de_bruxelles():
    e = ["Choux de Bruxelles", "Choux", "Choux Fleur"]
    entities = Entities(e)
    match = entities.match(e[0])
    assert len(match) == 1
    assert match[0].text == e[0]


def test_chou_rave():
    e = ["choux rave", "rave party"]
    entities = Entities(e)
    match = entities.match("choux rave party")
    assert len(match) == 2
    assert match[0].text == e[0]
    assert match[1].text == e[1]


def test_chou_rouge():
    e = ["choux rouge au vinaigre", "vinaigre balsamique"]
    entities = Entities(e)
    match = entities.match("choux rouge au vinaigre balsamique")
    assert len(match) == 2
    assert match[0].text == e[0]
    assert match[1].text == e[1]
