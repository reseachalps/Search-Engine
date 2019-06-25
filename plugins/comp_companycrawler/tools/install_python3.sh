test -z "$PREFIX" && prefix=$HOME/local/datapublica || prefix=$PREFIX
version="3.4.0"

if test -f $prefix/bin/python3
then
    echo "$prefix/bin/python3 interpreter already exists"
    if test "$($prefix/bin/python3 --version 2>&1)" != "Python $version"
    then
        echo "It has not the right version... You probably want to delete it before reinstalling a new version"
        exit 1
    else
        echo "Please delete it before reinstalling a python interpreter."
        exit 0
    fi
else
    os=$(uname -a)

    for package in libreadline-dev libxml2-dev libxslt1-dev zlib1g-dev libssl-dev libbz2-dev libsqlite3-dev
    do
      case $os in
        *Ubuntu*) dpkg -s $package | head -n 2 | grep installed > /dev/null || sudo apt-get install $package;;
      esac
    done


    (
        cd /tmp
        wget http://www.python.org/ftp/python/$version/Python-$version.tgz -O- | tar zx
        cd Python-$version
        ./configure --prefix $prefix
        make -j4
        make install
        cd ..
        rm -Rf Python-$version
    )
fi
