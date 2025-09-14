#!/bin/bash
set -e

docker compose --profile "prod" build
docker tag radio-pele-java-bot us-east1-docker.pkg.dev/flash-bazaar-487/radio-pele/radio-pele-bot:latest
docker push us-east1-docker.pkg.dev/flash-bazaar-487/radio-pele/radio-pele-bot:latest

