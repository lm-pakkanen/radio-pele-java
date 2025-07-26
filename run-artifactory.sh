#!/bin/bash
set -e

docker pull us-east1-docker.pkg.dev/flash-bazaar-487/radio-pele/latest
docker container run \
    --rm \
    --detach \
    us-east1-docker.pkg.dev/flash-bazaar-487/radio-pele/latest

