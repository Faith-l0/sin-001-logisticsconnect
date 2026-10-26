# DelayStageServiceApp

## Overview

Tracks the transit delay stage (0 to 8, where 0 means no delay, e.g. weather
shutdowns) for each hub, and announces changes on a message topic so other services
do not have to ask.

Part of the [LogisticsConnect](../README.md) project. Independent Maven module, no
parent pom. Listens on port `7052`. It is the producer for the topic described in
[common](../common/README.md).

## What it does now

| Endpoint | Does |
|---|---|
| `GET /health` | `OK` |
| `GET /stage/{hubId}` | returns the current stage (0 if never set) |
| `POST /stage/{hubId}` | sets the stage from a body like `{"stage": 3}` |

Hub IDs are checked with hub-service, and aliases resolve to the canonical hub, so
`H-504` and `H-500` share one stage.

Example:

    curl -X POST localhost:7052/stage/h-504 -H 'Content-Type: application/json' -d '{"stage":3}'
    {"hubId":"H-500","stage":3,"changed":true,"published":true}

Errors: `404` unknown hub, `400` bad stage or bad JSON (stage must be a whole number from 0 to 8), `503` hub-service unreachable.

## Messaging

When a stage really changes, it publishes `{"hubId":"H-500","stage":3}` to the
ActiveMQ topic `package-status-topic` (broker `tcp://localhost:61616`, see `MqConfig`).

## Decisions and trade-offs

- It only publishes when the stage actually changes, so repeated identical updates do not flood the topic. The response says `"changed": false` in that case.
- If the broker is down, the stage is still saved. The response says `"published": false` and a warning is logged, so a broker outage does not stop stage updates. The downside is that the change is not re-sent later, so consumers will not hear about it until the next change.
- Stages are kept in memory, so they reset when the service restarts.
- Aliases are resolved through hub-service and published under the canonical ID, so consumers only ever see one ID per hub.
- It opens a short-lived broker connection per message. That is simple and fine at this volume, but slower than keeping one connection open.
- Publishing is behind an interface (`StageChangePublisher`), so the tests check what would be published without a broker.

## Project structure

    delay-stage-service/
    ├── pom.xml
    └── src
        ├── main/java/co/wethinkcode/logisticsconnect/
        │   ├── DelayStageServiceApp.java        routes, publishing, error handling
        │   ├── StageStore.java                  in-memory stage per hub, validates 0-8
        │   ├── StageRequest.java                parses the POST body
        │   ├── StageMessage.java                the JSON sent to the topic
        │   ├── StageChangePublisher.java        publishing (interface)
        │   ├── ActiveMqStagePublisher.java      ActiveMQ implementation
        │   ├── PublishFailedException.java
        │   ├── HubDirectory.java                hub lookup (interface)
        │   ├── HttpHubDirectory.java            asks hub-service
        │   ├── HubServiceUnavailableException.java
        │   └── mq/MqConfig.java                 broker URL and topic name
        └── test/java/co/wethinkcode/logisticsconnect/
            ├── StageStoreTest.java
            ├── StageRequestTest.java
            ├── StageMessageTest.java
            ├── StagePublishingTest.java
            └── DelayStageServiceAppTest.java

## Build

    mvn package

## Run

Start the broker and hub-service first (and ingestion-service, which hub-service needs):

    cd ../common && docker compose up -d
    cd ../delay-stage-service
    java -jar target/delay-stage-service.jar

## Test

    mvn test

31 tests. They need no broker and no other services.
