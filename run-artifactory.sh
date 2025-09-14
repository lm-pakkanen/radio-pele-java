#!/bin/bash
set -e

REFRESH_TOKEN=$(cat .env)

docker compose --profile "prod" pull --policy "always"
docker compose \
   --profile "prod" \
   --env-file ".env" \
    up \
    --no-build \
    -d

