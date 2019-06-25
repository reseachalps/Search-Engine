import difflib
import time
import sys
from fastmatch.matcher import Matcher

matcher = Matcher()

# Compares results between difflib and fastmatch
# first argument: file containing known words (one per line)
# second argument: file containing words to search in the first list

with open(sys.argv[1], "r") as f:
    words = set(f.read().splitlines())
    matcher.set_words(list(words))

with open(sys.argv[2], "r") as f:
    test_data = f.read().splitlines()
    print("comparing difflib and fastmatch")
    print("difflib running")
    matched = 0
    to_match = 0
    begin = time.time()
    for test in test_data:
        to_match += 1
        matches = difflib.get_close_matches(test, words, 1, 0.9)
        if len(matches) == 0:
            continue
        if test == matches[0]:
            matched += 1
    duration = time.time() - begin
    print("difflib took %0.3f ms to complete, with %d/%d matched" % (duration * 1000, matched, to_match))
    print("fastmatch running")
    matched = 0
    to_match = 0
    begin = time.time()
    for test in test_data:
        to_match += 1
        matches = matcher.match(test)
        if len(matches) == 0:
            continue
        if test == matches[0][0]:
            matched += 1
    duration = time.time() - begin
    print("fastmatch took %0.3f ms to complete, with %d/%d matched" % (duration * 1000, matched, to_match))
