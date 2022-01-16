#!/usr/bin/env bash


JAVA_OPTS="-Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

mvn clean package -DskipTests
java $JAVA_OPTS -jar target/DynamicMessageRouter.jar