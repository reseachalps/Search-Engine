# Read the documentation here:
#  https://nose.readthedocs.org/en/latest/testing.html

# Modify the import path to find our package
import sys
import os.path
sys.path = [os.path.abspath("../companies_plugin"), os.path.abspath("..")] + sys.path

from companies_plugin.extractor import Main, Extractor
from companies_plugin import LIB_PATH

# Import our package
from companies_plugin import some_fun


def test_main_skeleton():
    m = Main("test", "test", Extractor, mod_path=LIB_PATH)
    print(LIB_PATH)
    m.conf.load(LIB_PATH + "resources/conf.json")
    assert(m.conf.rabbit["hostname"] == "localhost")
    assert(m.conf.proxy["password"] == "Xi9ahca1iG")
    assert(m.conf.database["port"] == "27017")
    assert(m.conf.crawler["depth"] == 2)

# test.* functions will be executed
def test_package_fun():
    some_fun()
    pass


# To skip a test:
from nose.plugins.skip import SkipTest


def test_something_else():
    raise SkipTest()
    assert False
