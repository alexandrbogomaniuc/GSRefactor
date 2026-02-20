# Phase 7 Cassandra Driver Inventory (2026-02-20 16:44:10 UTC)

## Goal
Collect concrete Cassandra client/driver versions used by legacy GS and MP codebases.

## Findings
1. Legacy GS monolith
- Driver family: DataStax Java Driver 3.x.
- Version: `3.11.5`.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/cassandra-cache/pom.xml`
  - properties: `<cassandra.driver.version>3.11.5</cassandra.driver.version>`
  - dependencies: `cassandra-driver-core`, `cassandra-driver-mapping`.

2. MP server
- Driver family: DataStax Java Driver 3.x (shaded core).
- Version: `3.5.0`.
- Spring Data Cassandra: `2.0.1.RELEASE`.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/mp-server/pom.xml`
  - `/Users/alexb/Documents/Dev/Dev_new/mp-server/persistance/pom.xml`

## Commands used
```bash
rg -n "cassandra.driver.version|cassandra-driver-core|spring-data-cassandra" \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/cassandra-cache/pom.xml \
  /Users/alexb/Documents/Dev/Dev_new/mp-server/pom.xml \
  /Users/alexb/Documents/Dev/Dev_new/mp-server/persistance/pom.xml -S
```

## Outcome
- Compatibility matrix updated with identified versions.
- Next action: run protocol/connection rehearsal against target Cassandra candidate and capture query compatibility logs.
