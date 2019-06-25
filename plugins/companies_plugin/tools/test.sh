nosetests                   \
    --all-modules           \
    --traverse-namespace    \
    --with-coverage         \
    --cover-tests           \
    --with-doctest          \
    --cover-package companies_plugin \
    --cover-package companies_plugin.utils \
    --where test/ $*
