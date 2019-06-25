#!/usr/bin/env python

from distutils.core import setup
from importlib import import_module

# replace the package name here
packagename = "entities_extractor"


setup(
    # the package author
    author="Samuel Charron",
    # the package author's email
    author_email="samuel.charron@gmail.com",

    ##### Lines you don't need to modify
    # the package name
    name=packagename,
    # Additional files to package
    package_data={'': ['version.txt']},
    # Version number, from the import above
    version=import_module(packagename).__version__,
    # the package url
    url='http://pypi.data-publica.com/simple/%s' % packagename,
    # the package name
    packages=[packagename],
    # the package description
    description=open("README").read().strip(),
)
