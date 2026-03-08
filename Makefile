stop:
	docker compose --profile "prod" down || true

run:
	make stop
	COMPOSE_BAKE=true docker compose --profile "prod" up --build -d

run-artifactory:
	make stop
	docker compose --profile "prod" pull --policy "always"
	docker compose \
	  --profile "prod" \
	  --env-file ".env" \
	  up \
	  --no-build \
	  -d

push-pull:
	echo "Building project"
	COMPOSE_BAKE=true docker compose --profile "prod" build
	echo "Pushing image"
	docker push us-east1-docker.pkg.dev/flash-bazaar-487/radio-pele/radio-pele-bot:latest

upgrade-major:
	 mvn versions:use-latest-versions@upgrade-major versions:update-properties@upgrade-major-properties versions:commit

format:
	cd bot && mvn ktlint:format