FROM eclipse-temurin:17.0.10_7-jdk-jammy
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
COPY build/resources/main/application.yaml application.yaml
ENTRYPOINT ["java","-jar","/app.jar"]