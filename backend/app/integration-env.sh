#!/usr/bin/env bash
ssh_host=""
if [[ $# == 0 ]]; then
    ssh_host="researchalps1.data-publica.com"
else
    ssh_host="$*"
fi
echo "Importing configuration"
scp -q researchalps2.data-publica.com:./scanr/main-config.properties application.properties
apps=""
while read -r line; do
    line=($line)
    apps="$apps ${line[0]}"
    sed -E 's/'${line[0]}'(:[0-9]*)?/localhost:'${line[2]}'/g' application.properties > .tmp
    mv .tmp application.properties
done < .integration-tunnels
sed -E 's!/var/db/scanr-screenshots!/tmp/screenshots!g' application.properties > .tmp
mv .tmp application.properties
echo "Replaced host entries"
# horrible one-liner to support multiple bridge ip resolutions
ssh ${ssh_host} "for h in \`echo ${apps}\`; do echo -n \$h ' '; sudo nsenter -t \`sudo docker inspect --format '{{.State.Pid}}' \$h\` -n ip route get 8.8.8.8 | grep src | sed 's/.*src //g'; done" > .ips

tunnel_opts=""
join .ips .integration-tunnels > .tmp
while read -r line; do
    line=($line)
    tunnel_opts="${tunnel_opts} -L ${line[3]}:${line[1]}:${line[2]}"
    echo "Tunneling ${line[0]} port ${line[2]} to ${line[3]}"
done < .tmp
rm .tmp
ssh -C ${tunnel_opts} ${ssh_host} 'echo "Ready..." && cat > /dev/null < /dev/stdin'
