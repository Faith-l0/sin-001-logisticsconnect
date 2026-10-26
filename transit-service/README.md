# TransitServiceApp

## Overview

Calculates estimated arrival windows from a hub and its current delay stage. It
learns about stage changes from messages instead of asking delay-stage-service.

Part of the [LogisticsConnect](../README.md) project. Independent Maven module, no
parent pom. Listens on port `7053`. It is the consumer for the topic described in
[common](../common/README.md).

## What it does now

| Endpoint | Returns |
|---|---|
| `GET /health` | `OK` |
| `GET /eta/{hubId}` | the arrival window in hours for that hub |

The window is a base of 24 to 48 hours, and each delay stage adds 12 hours to both
ends (stage 8 gives 120 to 144).

Example:

    curl localhost:7053/eta/H-500
    {"hubId":"H-500","stage":3,"minHours":60,"maxHours":84}

Errors: `404` unknown hub, `409` the hub is inactive (no ETA can be promised), `503` hub-service unreachable (the message names the service).

## Messaging

It subscribes to the ActiveMQ topic `package-status-topic` and keeps the latest stage
per hub in memory (`StageCache`). It no longer calls delay-stage-service over HTTP.

## Decisions and trade-offs

- The ETA rule is a pure function (`EtaCalculator`), so it is tested on its own.
- The subscription is durable, so messages sent while transit was restarting are delivered when it comes back.
- It connects on a background thread with a failover URL, so the service still starts when the broker is down and reconnects when the broker returns.
- Malformed or out-of-range messages are logged and dropped, never fatal.
- Because it uses the last known stage, it keeps answering when delay-stage-service is down. The trade-off is that a missed message means a stale ETA, and a hub it has never heard about is treated as stage 0.
- Messages published before transit first subscribes are lost, so start transit before delay-stage-service.
- An inactive hub gets a `409` rather than a guessed ETA. Hubs with an unknown active flag are treated as active.

## Project structure

    transit-service/
    ├── pom.xml
    └── src
        ├── main/java/co/wethinkcode/logisticsconnect/
        │   ├── TransitServiceApp.java           routes, wiring, error handling
        │   ├── EtaCalculator.java               the ETA rule
        │   ├── EtaWindow.java
        │   ├── StageMessage.java                parses messages from the topic
        │   ├── StageUpdate.java
        │   ├── StageCache.java                  latest stage per hub
        │   ├── StageLookup.java                 stage lookup (interface)
        │   ├── ActiveMqStageSubscriber.java     durable topic subscriber
        │   ├── HubLookup.java                   hub lookup (interface)
        │   ├── HubInfo.java
        │   ├── HttpHubLookup.java               asks hub-service
        │   ├── HttpJson.java                    shared HTTP call and failure handling
        │   ├── DependencyUnavailableException.java
        │   └── mq/MqConfig.java                 broker URL and topic name
        └── test/java/co/wethinkcode/logisticsconnect/
            ├── EtaCalculatorTest.java
            ├── StageMessageTest.java
            ├── StageCacheTest.java
            ├── StageSubscriberTest.java
            └── TransitServiceAppTest.java

## Build

    mvn package

## Run

Start the broker, ingestion-service and hub-service first, then this service, then delay-stage-service:

    cd ../common && docker compose up -d
    cd ../transit-service
    java -jar target/transit-service.jar

## Test

    mvn test

27 tests. They need no broker and no other services.
