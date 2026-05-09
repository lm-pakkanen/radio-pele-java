FROM container-registry.oracle.com/graalvm/native-image:25 AS builder

WORKDIR /build

COPY ./pom.xml ./
COPY ./bot ./bot

WORKDIR /build/bot

# Build native image - mvnw is in bot directory
RUN \
  ./mvnw clean package -Pnative \
  -Dmaven.repo.local=/root/.m2 \
  -Dmaven.test.skip \
  -e && \
  ls -la /build/bot/target/

# Minimal runtime image
FROM ubuntu:24.04

RUN apt-get update && apt-get install -y --no-install-recommends \
  libstdc++6 \
  libgcc1 \
  zlib1g \
  ca-certificates \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /radio-pele

# Copy executable
COPY --from=builder /build/bot/target/radio-pele-java-bot ./radio_pele
RUN chmod +x ./radio_pele

ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["./radio_pele"]
