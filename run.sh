#!/bin/bash
set -e

docker ps -aq | xargs docker stop || true
docker build . -t "radio-pele-java"
docker run -it radio-pele-java --name radio-pele-java --rm
