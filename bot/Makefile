.PHONY: format
format:
	mvn ktlint:format

.PHONY: check
check:
	mvn ktlint:check

.PHONY: test
test:
	mvn clean test

.PHONY: push
push:
	../push-pull.sh
