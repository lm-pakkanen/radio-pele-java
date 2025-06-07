#!/bin/bash
set -e

docker build . -t "radio-pele-java"
docker run -it radio-pele-java --name radio-pele-java --rm
