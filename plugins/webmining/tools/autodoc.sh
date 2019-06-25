lib="packagename"

read -r -d '' INDEX_HDR <<-"EOF"
Welcome to @lib@'s documentation!
=================================

Contents:

.. toctree::
   :maxdepth: 2
EOF
read -r -d '' INDEX_FTR <<-"EOF"
Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
EOF

gen_index() {
    rep=$(echo $* | xargs -n 1 | sed -re 's,^,   ,g')
    echo "$INDEX_HDR" | sed -re "s,@lib@,$lib,g"
    echo
    echo "$rep"
    echo
    echo
    echo "$INDEX_FTR"
}


read -r -d '' MOD <<"EOF"
@MODULE@
========

.. automodule:: @lib@.@MODULE@ 
   :members:
EOF

gen_mod() {
    echo "$MOD" | sed -re "s,@lib@,$lib,g ; s,@MODULE@,$1,g"
}


modules=""
for py in $lib/*.py
do
    py=$(echo $py | cut -d'/' -f 2)
    test $py == "__init__.py" && continue
    name=$(echo $py | sed -re 's,\.py$,,g')
    rst=$name.rst

    test -e doc/$rst || gen_mod $name > doc/$rst
    modules="$modules $name"
done

gen_index $modules > doc/index.rst
