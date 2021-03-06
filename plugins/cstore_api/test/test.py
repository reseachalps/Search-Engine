# Read the documentation here:
#  https://nose.readthedocs.org/en/latest/testing.html

# Modify the import path to find our package
import sys
import os.path
sys.path = [os.path.abspath("../cstore_api")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path


# Import our package
from cstore_api import some_fun


# test.* functions will be executed
def test_package_fun():
    some_fun()
    pass


# To skip a test:
from nose.plugins.skip import SkipTest


def test_something_else():
    raise SkipTest()
    assert False
