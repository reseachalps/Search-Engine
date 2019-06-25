#!/usr/bin/env python3

import glob
import argparse


parser = argparse.ArgumentParser(
    description='Change the version number of the project.')
parser.add_argument('--major', action='store_true',
                    help='Major version (defaults to minor)')
args = parser.parse_args()


for f in glob.glob("*/version.txt"):
    content = open(f).read().split(".")
    content = map(int, content)
    content = list(content)

    assert len(content) == 2
    if args.major:
        content[0] += 1
        content[1] = 0
    else:
        content[1] += 1

    content = map(str, content)
    content = ".".join(content)
    open(f, "w").write(content)
    print("%s" % content)
