FROM container-registry.oracle.com/graalvm/native-image:25 AS builder

WORKDIR /build

COPY ./pom.xml ./
COPY ./bot ./bot

WORKDIR /build/bot

RUN \
  --mount=type=cache,target=/root/.m2 \
  ./mvnw clean package -Pnative \
  -Dmaven.repo.local=/root/.m2 \
  -Dmaven.test.skip \
  -e

FROM ubuntu:24.04

RUN apt-get update && apt-get install -y --no-install-recommends \
  libstdc++6 \
  libgcc1 \
  zlib1g \
  ca-certificates \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /radio-pele

COPY --from=builder /build/bot/target/radio-pele-java-bot ./radio_pele
RUN chmod +x ./radio_pele

ENTRYPOINT ["./radio_pele"]
