push-pull:
	echo "Building project"
	docker compose --profile "prod" build
	echo "Pushing image"
	docker push us-east1-docker.pkg.dev/flash-bazaar-487/radio-pele/radio-pele-bot:latest

upgrade-major:
	 mvn versions:use-latest-versions@upgrade-major versions:update-properties@upgrade-major versions:commit
