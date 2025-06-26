ARG JAVA_VERSION=24

FROM maven:3.9.10-eclipse-temurin-${JAVA_VERSION}-alpine AS maven

COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline

COPY ./src ./src
RUN --mount=type=cache,target=/tmp/build mvn clean package -DskipTests

FROM eclipse-temurin:${JAVA_VERSION}-jdk AS jdk

WORKDIR /radio-pele

COPY --from=maven ./target/radio_pele_java-0.0.1-SNAPSHOT.jar ./radio_pele.jar
COPY ./entrypoint.sh ./entrypoint.sh

ENTRYPOINT ["sh", "entrypoint.sh"]
