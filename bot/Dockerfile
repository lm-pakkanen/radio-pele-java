FROM container-registry.oracle.com/graalvm/native-image:25-ol9 AS builder

WORKDIR /build
COPY . /build

# Build
RUN \
  --mount=type=cache,target=/tmp/build \
  ./mvnw \
  --no-transfer-progress \
  -Dmaven.repo.local=/tmp/build \
  native:compile \
  -Pnative

FROM container-registry.oracle.com/os/oraclelinux:8-slim

COPY --from=builder /build/target/radio-pele-java-bot app
ENTRYPOINT ["/app"]
