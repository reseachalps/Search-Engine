test -z "$PREFIX" && prefix=$HOME/local/datapublica || prefix=$PREFIX

test -f $prefix/bin/python3
echo $?
if test -f $prefix/bin/python3
then
    echo "$prefix/bin/python3 interpreter already exists. Please delete it before reinstalling a python interpreter."
    exit 1
else
    os=$(uname -a)

    for package in libreadline-dev libxml2-dev libxslt1-dev zlib1g-dev libssl-dev libbz2-dev libsqlite3-dev
    do
      case $os in
        *Ubuntu*) dpkg -s $package | head -n 2 | grep installed > /dev/null || sudo apt-get install $package; break;;
      esac
    done


    (
        cd /tmp
        wget http://www.python.org/ftp/python/3.3.0/Python-3.3.0.tar.bz2
        tar jxvf Python-3.3.0.tar.bz2
        cd Python-3.3.0
        ./configure --prefix $prefix
        make -j4
        make install
        cd ..
        rm -Rf Python-3.3.0
    )
fi
