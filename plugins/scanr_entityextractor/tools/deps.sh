test -z "$pipdir" && pipdir=$HOME/local/datapublica/pipdir || pipdir=$pipdir

action() {
	echo
	echo
	printf "%0.s=" {1..80}
	echo
	#echo "$1"
	printf "%*s\n" $(((${#1}+80)/2)) "$1"
	printf "%0.s=" {1..80}
	echo
	echo
	echo
}

subaction() {
	echo
	echo ">>> $1"
	echo
}

result() {
	echo "$1" | while read line 
	do
		echo "-> $line"
	done
}

install() {
    subaction "Downloading deps..."
    pip wheel \
        --wheel-dir $pipdir             \
        --find-links $pipdir            \
        $*
#        -r deps.txt

    subaction "Installing deps..."
    pip install                     \
        --upgrade                   \
        --no-index                  \
        --find-links=file://$pipdir \
        $*
#        -r deps.txt
}

action "Installing tools..." && install nose coverage pep8 sphinx wheel pip

numpy_required=$(grep -q -E 'matplotlib|numpy|scipy|nestor|scikit[-_]learn' deps.txt >/dev/null && echo 1)
scipy_required=$(grep -q -E 'scipy|nestor|scikit[-_]learn' deps.txt >/dev/null && echo 1)

numpy=$(grep -E '^numpy' deps.txt || echo numpy)
scipy=$(grep -E '^scipy' deps.txt || echo scipy)

test $numpy_required && action "Making sure latest numpy is available" && install $numpy
test $scipy_required && action "Making sure latest scipy is available" && install $scipy

action "Installing deps..."
test "$(grep -v '^#' deps.txt | wc -l)" -ge 1 && install -r deps.txt || result 'No deps found'


action "Searching for outdated packages..."

# --pre is for pre-releases (used by nltk)
outdated=$(pip list --outdated | grep -v 'uses an insecure transport scheme')
if test "$outdated" != ""
then
	result "$outdated"
else
	result "No outdated packages :-)"
fi

