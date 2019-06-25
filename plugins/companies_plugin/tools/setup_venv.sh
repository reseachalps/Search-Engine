#!/bin/bash

if test "$1" != "-f" -a $(echo $0 | grep setup_venv.sh > /dev/null; echo $?) -eq 0
then
    echo 'you must use `source ./tools/setup_venv.sh`'
    exit 1
fi

test -z "$PREFIX" && prefix=$HOME/local/datapublica || prefix=$PREFIX
test -z "$VENV" && venv=./virtualenv || prefix=$VENV
echo "Installing virtualenv in $venv using interpreter $prefix/bin/python3"

if test ! -f $prefix/bin/pyvenv
then
    echo "It seems you have a wrong version of python installed (<3.4)."
    return 1
fi

if test ! -d $venv
then
    echo "Setupping venv..."
    $prefix/bin/pyvenv $venv || return 1
    source $venv/bin/activate
    pip install wheel
else
    if test ! -f $venv/bin/pip
    then
        echo "It seems you have a wrong version of python installed (<3.4)."
        return 1
    fi
fi

absvenv=$(readlink -f $venv)
curvenv=$(readlink -f $(grep "VIRTUAL_ENV=" $venv/bin/activate | cut -d= -f2 | tr -d \"))

if test "$absvenv" != "$curvenv"
then
    echo "The current installed configuration does not match the one you are trying to load."
    echo
    echo "Installed path: $curvenv"
    echo "Trying to load: $absvenv"
    echo
    echo "Maybe you moved or copyed this package. If this is the case, you should remove your virtualenv $venv , and generate a new one."
    return 1
fi

source $venv/bin/activate

export VIRTUALENV=$absvenv

echo OK
