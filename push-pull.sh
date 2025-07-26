#!/bin/bash
set -e

docker tag radio-pele-java-bot us-east1-docker.pkg.dev/flash-bazaar-487/radio-pele/latest
docker push us-east1-docker.pkg.dev/flash-bazaar-487/radio-pele/latest
