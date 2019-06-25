import sys
import os.path

sys.path = [os.path.abspath("../fastmatch")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path

from fastmatch import Matcher, LIB_PATH

matcher = Matcher()
with open(os.path.join(LIB_PATH, "../test/streets.txt"), "r") as f:
    matcher.set_words(f.read().splitlines())
    print(len(matcher.words))


def test_all_pass():
    with open(os.path.join(LIB_PATH, "../test/all_pass.txt"), "r") as f:
        words = f.read().splitlines()

    for word in words:
        matched = matcher.match(word, ncandidates=2)
        print("matched [%s] with [%s]" % (word, matched))
        assert(len(matched) == 1)
        assert(matched[0][0] == word)


def test_all_fail():
    with open(os.path.join(LIB_PATH, "../test/all_fail.txt"), "r") as f:
        words = f.read().splitlines()

    for word in words:
        matched = matcher.match(word)
        print("matched [%s] with " % word, matched)
        assert(len(matched) == 0)
