#!/bin/bash

if [ -z "${HOST}" ]; then
    echo "HOST environment variable is not set!"
    exit 1
fi

if [ ! -d /usr/share/nginx/html ] || [ ! -d /etc/nginx/vhost.d ]; then
    echo "Volumes from nginx are not available!"
    exit 2
fi

echo "Cleaning previous deployment"

rm -f /etc/nginx/vhost.d/${HOST}
rm -f /etc/nginx/vhost.d/${HOST}_location
rm -rf /usr/share/nginx/html/${HOST}

echo "Copying content"
cp -r /app /usr/share/nginx/html/${HOST}

if [ -f /proxy.conf ]; then
    cp /proxy.conf /etc/nginx/vhost.d/${HOST}_location
else
    echo > /etc/nginx/vhost.d/${HOST}_location
fi

echo "location / {" > tmp.conf
echo "    root /usr/share/nginx/html/${HOST};" >> tmp.conf

if [ -f /root_location.conf ]; then
    cat /root_location.conf >> tmp.conf
fi
echo "}" >> tmp.conf

echo "location /map {" >> tmp.conf
echo "    root /usr/share/nginx/html/${HOST};" >> tmp.conf
echo "    try_files $uri /map/index.html;" >> tmp.conf
echo "}" >> tmp.conf

if [ -f /root.conf ]; then
    cat /root.conf >> tmp.conf
fi

# Activate configuration
cp tmp.conf /etc/nginx/vhost.d/${HOST}

# Remove previous links to $HOST if needed
cd /etc/nginx/vhost.d/
for a in `find . -type l`; do
    link=`realpath --relative-to=$PWD $a`
    if [[ $link == ${HOST} ]] || [[ $link == ${HOST}_location ]]; then 
        rm $a;
    fi;
done

# And add new links if needed as well
if [ ! -z "${ALIASES}" ]; then
    for a in "${ALIASES//,/ }"; do
        ln -f -s ${HOST} ${a}
        ln -f -s ${HOST}_location ${a}_location
    done
fi

# Touch the default.conf to ensure it is reloaded
echo >> /etc/nginx/conf.d/default.conf

echo "Configuration deployment done"
