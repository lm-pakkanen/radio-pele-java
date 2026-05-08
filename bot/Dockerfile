# syntax=docker/dockerfile:experimental
ARG JAVA_VERSION=21

FROM maven:3.9.10-eclipse-temurin-${JAVA_VERSION}-alpine AS maven-parent

WORKDIR /build
COPY ./pom.xml ./pom.xml
RUN --mount=type=cache,target=/tmp/build mvn install -Dmaven.repo.local=/tmp/build

FROM maven-parent AS maven-bot

WORKDIR /build/bot
COPY ./bot/pom.xml ./pom.xml
COPY ./bot/src ./src
RUN --mount=type=cache,target=/tmp/build mvn package -Dmaven.repo.local=/tmp/build -Dmaven.test.skip

FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS jdk

WORKDIR /radio-pele

COPY --from=maven-bot /build/bot/target/radio-pele-java-bot-0.0.1-SNAPSHOT.jar ./radio_pele.jar

# Using SerialGC for low-memory environments with limited heap allocation
# Reduced heap for E2-micro instances: 64m min (lazy allocation), 180m max
# Added GC tuning for predictable behavior and memory efficiency
ENV JAVA_TOOL_OPTIONS "-XX:+UseSerialGC -Xms64m -Xmx180m -XX:+ExitOnOutOfMemoryError -XX:+AlwaysActualCommitMemory -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40 -XX:MaxMetaspaceSize=64m"
ENTRYPOINT ["java", "-jar", "-noverify", "./radio_pele.jar"]
