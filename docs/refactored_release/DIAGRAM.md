# Refactored Release Diagram

## Before vs After

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

After
  Browser
    |
    v
  refactored_versoin compose project
    |
    +--> webgs-smoke-fullstack      :8080
    |      |
    |      +--> fullstack-cassandra alias -> cassandra-target :9043->9042
    |      +--> fullstack-zookeeper alias -> zookeeper-smoke  :2181
    |      +--> fullstack-kafka alias     -> kafka-smoke      :9092
    |
    +--> webgs-static-fullstack     :18080
    |
    +--> cassandra-legacy           :9042
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

- `cassandra-target` is the runtime database for the refactored stack.
- `cassandra-legacy` remains online so migration parity stays provable and rollback stays low-risk.
- The playable minimum is:
  - target Cassandra
  - ZooKeeper
  - Kafka
  - static asset nginx
  - web-gs
- The release-candidate topology is operationally simpler than the earlier scattered smoke layout, even though the validated Compose project id must remain lowercase: `refactored_versoin`.
