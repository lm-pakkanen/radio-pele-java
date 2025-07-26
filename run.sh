#!/bin/bash
set -e

docker compose down
docker compose pull
ocker compose up --no-build -d
