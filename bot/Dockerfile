# syntax=docker/dockerfile:experimental
ARG JAVA_VERSION=24

FROM maven:3.9.10-eclipse-temurin-${JAVA_VERSION}-alpine AS maven-parent

WORKDIR /build
COPY ./pom.xml ./pom.xml
RUN --mount=type=cache,target=/tmp/build mvn install -Dmaven.repo.local=/tmp/build

FROM maven-parent AS maven-bot

WORKDIR /build/bot
COPY ./bot/pom.xml ./pom.xml
RUN --mount=type=cache,target=/tmp/build mvn dependency:go-offline -Dmaven.repo.local=/tmp/build

COPY ./bot/src ./src
RUN --mount=type=cache,target=/tmp/build mvn package -Dmaven.repo.local=/tmp/build -Dmaven.test.skip --offline

FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS jdk

WORKDIR /radio-pele

COPY --from=maven-bot /build/bot/target/radio-pele-java-bot-0.0.1-SNAPSHOT.jar ./radio_pele.jar
COPY ./bot/entrypoint.sh ./entrypoint.sh

ENTRYPOINT ["java", "-jar", "-noverify", "./radio_pele.jar"]
