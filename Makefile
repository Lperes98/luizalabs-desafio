.PHONY: help build test clean run docker-up docker-down postgres-up

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

format:
	./gradlew spotlessApply

check:
	./gradlew spotlessCheck

build:
	./gradlew build

rebuild:
	./gradlew clean build

test:
	./gradlew test

clean:
	./gradlew clean

run:
	./gradlew :api:bootRun

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down

coverage:
	./gradlew test report:jacocoAggregateReport
