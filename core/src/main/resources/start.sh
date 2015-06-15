#!/bin/bash

JAVA_OPTS="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=samplethreads=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9191 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost"
JAVA_OPTS="$JAVA_OPTS -verbose:gc -Xloggc:vm.log"

java_opts=$JAVA_OPTS