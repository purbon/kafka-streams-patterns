FROM alpine:3.14
MAINTAINER pere.urbon@gmail.com
ENV container docker
ENV BUILDER_HOME /usr/local/app
ENV PATH="${BUILDER_HOME}:${PATH}"

ENV JAVA_TOOL_OPTIONS "-Dcom.sun.management.jmxremote.ssl=false \
 -Dcom.sun.management.jmxremote.authenticate=false \
 -Dcom.sun.management.jmxremote.port=5000 \
 -Dcom.sun.management.jmxremote.rmi.port=5000 \
 -Dcom.sun.management.jmxremote.host=0.0.0.0 \
 -Djava.rmi.server.hostname=0.0.0.0"

USER root

RUN apk add bash openjdk11 krb5-server krb5-libs krb5-conf krb5

RUN mkdir -p /usr/local/app && chmod 755 /usr/local/app

COPY target/KafkaStreamsGrouping.jar /usr/local/app
COPY application.properties /

ENTRYPOINT ["java", "-jar", "/usr/local/app/KafkaStreamsGrouping.jar"]