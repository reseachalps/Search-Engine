java \
`# Most are taken from http://blog.sokolenko.me/2014/11/javavm-options-production.html` \
`# Disable random seed search (faster startup)` \
    -Djava.security.egd=file:/dev/./urandom \
`# Server optimal JVM` \
    -server \
`# Keep CMS GC instead of G1` \
    -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled \
`# CMS tuning to avoid app pause` \
    -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 \
`# Prefer collecting young gen` \
    -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark \
`# GC Logging` \
    -XX:+PrintGCDateStamps -verbose:gc -XX:+PrintGCDetails -Xloggc:/tmp/gc.log \
    -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M \
`# Make sure DNS TTL is not infinite` \
    -Dsun.net.inetaddr.ttl=60 \
`# Heap dump on OOM` \
    -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heap.hprof \
`# RMI is container IP for outbound traffic (getting route to 8.8.8.8 to find that out)` \
    -Djava.rmi.server.hostname=`ip route get 8.8.8.8 | head -n 1 | sed 's/.*src //g'` \
`# Enable JMX on 9090 without auth/ssl` \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=9090 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
`# Start the jar` \
    -jar /app.jar
