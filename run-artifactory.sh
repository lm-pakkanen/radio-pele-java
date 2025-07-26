#!/bin/bash
set -e

docker compose pull #us-east1-docker.pkg.dev/flash-bazaar-487/radio-pele/latest
docker compose up --no-build -d

