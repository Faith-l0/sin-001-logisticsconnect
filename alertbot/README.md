# AlertBotApp

## Overview

Posts proactive delay notifications for hubs that become seriously delayed. The posts are
simulated: they are logged and kept in a list, with no real social media call.

Part of the [LogisticsConnect](../README.md) project, and the second consumer of the topic
described in [common](../common/README.md). Independent Maven module, no parent pom.
Listens on port `7054`.

## What it does now

It subscribes to the ActiveMQ topic `package-status-topic`. Each message says a hub's delay
stage changed. The bot decides whether that deserves a public post.

- A **delay alert** is posted when a hub's stage crosses up to 6 or higher.
- A **recovery** post is made when a hub drops back below 6.
- Nothing is posted while a hub stays above the threshold, or while it stays below it.

| Endpoint | Returns |
|---|---|
| `GET /health` | `OK` |
| `GET /alerts` | every simulated post so far, oldest first |

Example after setting H-500 to stage 7, then 8, then 2:

    curl localhost:7054/alerts
    ["Delay alert: hub H-500 is now at delay stage 7. Expect longer delivery times.","Update: hub H-500 is down to delay stage 2. Delays are easing."]

## Decisions and trade-offs

- The threshold is 6 out of a 0 to 8 scale, so it is the top third. It is a constant in `AlertPolicy`, so it is easy to change.
- It alerts on **crossing** the threshold, not on every high message. Otherwise a hub going from stage 7 to 8 would post twice for the same problem.
- The rule is a pure function (`AlertPolicy.decide`), so it is tested on its own, with no broker.
- Posting is behind an interface (`AlertPoster`). The simulated version logs and stores the post. A real social media client could replace it without touching the rule.
- The bot remembers each hub's last stage in memory. A hub it has never heard about counts as stage 0. After a restart it forgets everything, so a hub already at stage 7 could get a repeat alert on its next update.
- The subscription is durable and has its own client ID, so it does not clash with transit-service and it receives messages sent while the bot was down.
- It connects on a background thread with a failover URL, so it still starts when the broker is down. Malformed messages are logged and dropped.
- The `/alerts` list is also kept in memory, so it empties on restart.

## Project structure

    alertbot/
    ├── pom.xml
    └── src
        ├── main/java/co/wethinkcode/logisticsconnect/
        │   ├── AlertBotApp.java               starts the subscriber and the /alerts endpoint
        │   ├── AlertBot.java                  remembers last stage per hub, posts when the rule says so
        │   ├── AlertPolicy.java               the alert rule (threshold 6)
        │   ├── AlertPoster.java               where alerts go (interface)
        │   ├── SimulatedSocialPoster.java     logs and stores the post
        │   ├── ActiveMqAlertSubscriber.java   durable topic subscriber
        │   ├── StageMessage.java              parses messages from the topic
        │   ├── StageUpdate.java
        │   └── mq/MqConfig.java               broker URL and topic name
        └── test/java/co/wethinkcode/logisticsconnect/
            ├── AlertPolicyTest.java
            ├── AlertBotTest.java
            ├── AlertSubscriberTest.java
            ├── StageMessageTest.java
            └── AlertBotAppTest.java

## Build

    mvn package

## Run

Start the broker first, then this service before delay-stage-service, so its subscription exists:

    cd ../common && docker compose up -d
    cd ../alertbot
    java -jar target/alertbot.jar

## Test

    mvn test

21 tests. They need no broker and no other services.
