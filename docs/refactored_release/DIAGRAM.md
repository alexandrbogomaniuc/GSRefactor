# Refactored Release Diagram

## Before vs Current Release-Candidate Baseline

```text
Before
  Browser
    |
    v
  webgs-smoke-fullstack (standalone docker run)
    |
    +--> fullstacksmoke-fullstack-cassandra-1
    +--> fullstacksmoke-fullstack-zookeeper-1
    +--> fullstacksmoke-fullstack-kafka-1
    |
    +--> webgs-static-fullstack (standalone nginx)
    |
    +--> cassandra-legacy (separate migration container)
    +--> kafka-smoke / zookeeper-smoke (separate migration containers)
    +--> mp-smoke-fullstack (legacy gameplay helper)

Current verified baseline
  Browser
    |
    v
  webgs-smoke-fullstack            :8080
    |
    +--> webgs-static-fullstack    :18080
    |
    +--> fullstacksmoke compose project
    |      |
    |      +--> fullstacksmoke-fullstack-cassandra-1
    |      +--> fullstacksmoke-fullstack-zookeeper-1
    |      +--> fullstacksmoke-fullstack-kafka-1
    |
    +--> cassandra-legacy           :9042
    +--> cassandra-target           :9043
    +--> zookeeper-smoke            :2181
           ^
           |
      migration source + rollback anchor
```

## Data Flow

```text
Legacy Cassandra 3.11
  |
  | export schema
  | sanitize compatibility metadata
  | copy rows
  v
Target Cassandra 5.0.6
  |
  v
web-gs runtime (health + guest launch + gameplay canary)
```

## Verification Flow

```text
1. migration guard
   latest.env => PASS/PASS

2. fullstack health
   /support/health/check.jsp => 200

3. gameplay canary
   /cwguestlogin.do => 302
   follow-up template.jsp => 200
```

## Release-Candidate Interpretation

- The current green baseline is still a hybrid rehearsal topology rather than a single tracked deploy asset.
- `cassandra-legacy` remains online so migration parity stays provable and rollback stays low-risk.
- The playable minimum is:
  - fullstack Cassandra
  - fullstack ZooKeeper
  - fullstack Kafka
  - static asset nginx
  - web-gs
- Migration proof remains separate and authoritative until a production deployment topology is promoted and re-validated.
