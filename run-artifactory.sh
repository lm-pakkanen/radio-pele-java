#!/bin/bash
set -e

docker compose pull --policy "always"
docker compose up --no-build -d

