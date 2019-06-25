for py in entities_extractor/*.py; do python3 -m doctest $py ; done
nosetests                   \
    --all-modules           \
    --traverse-namespace    \
    --with-coverage         \
    --cover-tests           \
    --with-doctest          \
    --cover-package entities_extractor \
    --where test/ $*
