ARG JAVA_VERSION=24

FROM eclipse-temurin:${JAVA_VERSION}-jdk

WORKDIR /radio-pele

COPY . .

RUN --mount=type=cache,target=/tmp/build ./mvnw clean package -DskipTests

ENTRYPOINT ["sh", "entrypoint.sh"]
