#!/bin/bash
set -e

docker compose down
COMPOSE_BAKE=true docker compose up --build -d
