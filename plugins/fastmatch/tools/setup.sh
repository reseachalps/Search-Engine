#!/bin/bash
name=$(echo $(cat ~/.gitconfig | grep "name\s*=" | cut -d= -f2-))
email=$(echo $(cat ~/.gitconfig | grep "email\s*=" | cut -d= -f2-))

if test -z "$name" -o -z "$name"
then
    echo "You need to setup your name and email in ~/.gitconfig"
    exit 1
fi

echo "Hello $name."
echo "Please enter the package name:"
read package
echo "Please enter the git repository path [git@gitorious.data-publica.com:dppylib/$package.git]:"
read repo

test -z "$repo" && repo="git@gitorious.data-publica.com:dppylib/$package.git"

echo
echo "==============================="
echo "name: '$name'"
echo "email: '$email'"
echo "package name: '$package'"
echo "repo url: '$repo'"
echo "==============================="
echo 
while true
do
    echo "Is this ok ? [y/n]"
    read ok
    if test "$ok" == "n" -o "$ok" == "N"
    then
        $0
        exit 0
    fi
    if test "$ok" == "y" -o "$ok" == "Y"
    then
        break
    fi
done

rm -Rf .git
git init

sed -i -r -e 's,"packagename","'$package'",' setup.py
sed -i -r -e 's,"Pythonista #1","'"$name"'",' setup.py
sed -i -r -e 's,"python.rox@data-publica.com","'$email'",' setup.py
sed -i -r -e 's,\.\./packagename,../'"$package"',g' test/test.py
sed -i -r -e 's,from packagename import some_fun,from '"$package"' import some_fun,g' test/test.py
sed -i -r -e 's,packagename,'"$package"',g' doc/index.rst
sed -i -r -e 's,packagename,'"$package"',g' tools/test.sh
sed -i -r -e 's,packagename,'"$package"',g' tools/autodoc.sh


mv packagename $package

vim README

git add $(git status --porcelain | cut -d? -f3)
git commit -m "Create the project structure"
git remote add -f -m master origin $repo
git push origin master

./tools/install_python3.sh
source ./tools/setup_venv.sh
./tools/deps.sh
./tools/test.sh

exit 0
