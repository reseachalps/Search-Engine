# Read the documentation here:
#  https://nose.readthedocs.org/en/latest/testing.html

# Modify the import path to find our package
import sys
import os.path
from unittest.mock import patch, call
sys.path = [os.path.abspath("../")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path
from scanr_entityextractor import entityprovider as etp


# To skip a test:
from nose.plugins.skip import SkipTest

def test_something():
    raise SkipTest()
    assert False

def test_blacklist():
    blacklist = etp.get_blacklisted_entities()
    bl = ["Europe", "RICHARD", "Mali", "CONTACT", "MARIE", "PARIS"]
    for b in bl:
        assert b in blacklist

def test_admit():
    blacklist = etp.get_blacklisted_entities()
    bl = blacklist.union(etp.Blacklist().bl)
    assert etp.admit("Comprendre", bl) is False
    assert etp.admit("COMPRENDRE", bl) is False
    assert etp.admit("CoMprEndRE", bl) is True
    assert etp.admit("MISSION", bl) is False
    assert etp.admit("PARIS", bl) is False
    assert etp.admit("PaRis", bl) is True
    assert etp.admit("paris", bl) is False
    assert etp.admit("Paris", bl) is False
    assert etp.admit("hey", bl) is False
    assert etp.admit("h e y ", bl) is False
    assert etp.admit("h-ey", bl) is True
    assert etp.admit("123456", bl) is False
    assert etp.admit("Université de Strasbourg", bl) is False
    assert etp.admit("LOD2", bl) is True
    assert etp.admit("ENGINE", bl) is False
    assert etp.admit("hello", bl) is False
    assert etp.admit("heLlo", bl) is True
    assert etp.admit("FINESS", bl) is False
    assert etp.admit("EMOtIONS", bl) is True
    assert etp.admit("lod2", bl) is True

def test_normalization():
    text = "chez Data-  Publica on AimE le Python et les épées  ."
    norm = etp.normalization(text)
    assert norm == "chez Data- Publica on AimE le Python et les épées ."
    hard_norm = etp.ascii_normalization(text)
    assert hard_norm == "chezdatapublicaonaimelepythonetlesepees"

 
