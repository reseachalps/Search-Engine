#!/usr/bin/env python

from setuptools import setup
from importlib import import_module
import re

# replace the package name here
packagename = "comp_coreextractor"


def pip(filename):
    RE_REQUIREMENT = re.compile('^\s*-r\s*(?P<filename>.*)$')
    RE_SCM = re.compile("^(git|svn|hg)\+.*$")
    requirements = []
    for line in open(filename):
        match = RE_REQUIREMENT.match(line)
        scm = RE_SCM.match(line)
        if match:
            requirements.extend(pip(match.group('filename')))
        elif not scm:
            requirements.append(line.strip())
        else:
            # Ignore SCMs for deps.
            pass
    return requirements


setup(
    # the package author
    author="Guillaume Lebourgeois",
    # the package author's email
    author_email="guillaume.lebourgeois@data-publica.com",

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
    # recursive dependencies
    install_requires=pip("deps.txt"),
    # To avoid uploading by mistake our packages to the global pypi
    classifiers=['Private :: Do Not Upload'],
)
