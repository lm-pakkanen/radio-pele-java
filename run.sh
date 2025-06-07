#!/bin/bash
set -e

docker build . -t "radio-pele-java"
docker run -d radio-pele-java
