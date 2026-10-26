# HubServiceApp

## Overview

Serves provinces and sorting centers, the place-name source of truth for the other
services. It does not read the CSV itself: it asks ingestion-service for the cleaned
hubs and answers questions about them.

Part of the [LogisticsConnect](../README.md) project. Independent Maven module, no
parent pom. Listens on port `7051`.

## What it does now

| Endpoint | Returns |
|---|---|
| `GET /health` | `OK` |
| `GET /provinces` | the list of provinces, sorted |
| `GET /provinces/{province}/sorting-centers` | that province's sorting centers (case and padding ignored) |
| `GET /hubs/{id}` | one hub; an alias such as `H-504` resolves to the canonical hub `H-500` |

Example:

    curl localhost:7051/hubs/h-504
    {"hubId":"H-500","province":"Gauteng","sortingCenter":"Johannesburg Central","active":true,"aliases":["H-504","H-510","H-515"]}

Errors: `404` for an unknown province or hub, `503` when ingestion-service cannot be reached.

## Decisions and trade-offs

- The lookups are plain functions over a list (`HubCatalog`), so they are easy to test with no network.
- The data source is an interface (`HubSource`). The real one calls ingestion-service over HTTP and the tests use a fake, so the REST tests need no running services.
- If ingestion-service is down, hub-service answers `503` with a clear message instead of crashing. That is how a partial failure shows up to callers.
- Every request fetches fresh data from ingestion-service. That is simple and always current, but it makes hub-service depend on ingestion being up. Caching would remove that dependency at the cost of possibly stale data.

## Project structure

    hub-service/
    ├── pom.xml
    └── src
        ├── main/java/co/wethinkcode/logisticsconnect/
        │   ├── HubServiceApp.java            routes and error handling
        │   ├── HubCatalog.java               pure lookups (provinces, centers, alias resolution)
        │   ├── HubRecord.java                a cleaned hub
        │   ├── HubSource.java                where hubs come from (interface)
        │   ├── HttpHubSource.java            fetches hubs from ingestion-service
        │   └── SourceUnavailableException.java
        └── test/java/co/wethinkcode/logisticsconnect/
            ├── HubCatalogTest.java
            └── HubServiceAppTest.java

## Build

    mvn package

## Run

Start ingestion-service first (port 7050), then:

    java -jar target/hub-service.jar

## Test

    mvn test

16 tests. Run it from this folder, not the repo root.
