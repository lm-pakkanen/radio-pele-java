FROM container-registry.oracle.com/graalvm/native-image:25 AS builder

WORKDIR /build
COPY . /build

RUN \
  --mount=type=cache,target=/tmp/build \
  ./mvnw \
  --no-transfer-progress \
  -Dmaven.repo.local=/tmp/build \
  -Pnative \
  native:compile


FROM container-registry.oracle.com/os/oraclelinux:10-slim

COPY --from=builder /build/target/radio-pele-java-bot app
ENTRYPOINT ["/app"]
