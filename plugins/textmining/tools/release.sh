#!/bin/sh

version=$(./tools/version.py $*) || exit 1

git commit -m "Bump to version $version" */version.txt
git tag -a $version -m "Bump to version $version"
git push origin master
