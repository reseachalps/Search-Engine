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
    echo "Missing pyenv: It seems you have a wrong version of python installed (<3.4)."
    return 1
fi

if test ! -d $venv
then
    echo "Setupping venv..."
    $prefix/bin/pyvenv $venv || return 1
    if test ! -f $venv/bin/pip
    then
        ln -s ~/local/datapublica/bin/pip $venv/bin/pip
    fi
    source $venv/bin/activate
    pip install -U pip
    pip install wheel
else
    if test ! -f $venv/bin/pip
    then
        echo "Missing pip: It seems you have a wrong version of python installed (<3.4)."
        return 1
    fi
fi

# if present, use greadlink (useful on mac)
readlink_cmd="readlink"
if command -v greadlink 2>&1 > /dev/null;
then
    readlink_cmd="greadlink"
fi

absvenv=$($readlink_cmd -f $venv)
curvenv=$($readlink_cmd -f $(grep "VIRTUAL_ENV=" $venv/bin/activate | cut -d= -f2 | tr -d \"))

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

# export CFLAGS as this is necessary for installing lxml on mac yosemite (future versions may be impacted)
if command -v sw_vers 2>&1 > /dev/null;
then
    sw_vers | grep ProductVersion | cut -f 2 | grep 10.10. > /dev/null
    if test $? == 0
    then
        export CFLAGS="$CFLAGS -I/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.10.sdk/usr/include/libxml2"
    fi
fi

echo OK
