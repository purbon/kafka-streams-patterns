#!/usr/bin/env bash

./gradlew clean build
 docker build --build-arg JAR_FILE=build/libs/app.jar -t com.purbon.streaming/rules .