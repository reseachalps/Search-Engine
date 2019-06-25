nosetests                   \
    --all-modules           \
    --traverse-namespace    \
    --with-coverage         \
    --cover-tests           \
    --with-doctest          \
    --where test/ $*
