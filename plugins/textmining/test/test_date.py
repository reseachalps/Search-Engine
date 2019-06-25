
import sys
import os.path
sys.path = [os.path.abspath("../textmining")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path

from textmining.normalizer import Normalizer
from textmining import LIB_PATH


def test_date_normalization():
    n = Normalizer()
    tests = {"20110504 12:00": "04/05/2011",
             "09092011": "09/09/2011",
             "09 09 2011": "09/09/2011",
             "051214": "05/12/2014",
             "12 janvier 2014": "12/01/2014"}

    for t in tests:
        d = n.normalize_date(t)
        print(t, " --> ", tests[t], " - got ", str(d))
        assert str(d) == tests[t]
