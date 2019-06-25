test -z "$pipdir" && pipdir=$HOME/local/datapublica/pipdir || pipdir=$pipdir

pip wheel \
        --wheel-dir $pipdir             \
        --find-links $pipdir            \
        .

