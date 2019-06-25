nosetests                   \
    --all-modules           \
    --traverse-namespace    \
    --with-coverage         \
    --cover-tests           \
    --with-doctest          \
    --cover-package comp_coreextractor \
    --where test/ $*
