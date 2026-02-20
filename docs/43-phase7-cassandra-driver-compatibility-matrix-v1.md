# Phase 7 Cassandra Driver Compatibility Matrix v1

Last updated: 2026-02-20 UTC

## Objective
Track component-level Cassandra client compatibility before cutover.

## Matrix
| Component | Runtime Language | Current Client/Driver | Current Cluster Baseline | Target Cluster Candidate | Status | Notes |
|---|---|---|---|---|---|---|
| Legacy GS monolith | Java | DataStax Java Driver `3.11.5` (`cassandra-driver-core`, `cassandra-driver-mapping`) | 2.1.20 | 4.1.x / 5.x | identified | version from `gs-server/cassandra-cache/pom.xml` |
| MP server | Java | DataStax Java Driver `3.5.0` (`cassandra-driver-core` shaded), Spring Data Cassandra `2.0.1.RELEASE` | 2.1.20 | 4.1.x / 5.x | identified | versions from `mp-server/pom.xml` |
| Config service | Node.js | n/a (file store now) | n/a | n/a | not_applicable | Cassandra persistence planned later |
| Session service | Node.js | n/a (file store now) | n/a | n/a | not_applicable | Cassandra persistence planned later |
| Gameplay orchestrator | Node.js | n/a (file store now) | n/a | n/a | not_applicable | scaffold stage |
| Wallet adapter | Node.js | n/a (file store now) | n/a | n/a | not_applicable | scaffold stage |
| Bonus/FRB service | Node.js | n/a (file store now) | n/a | n/a | not_applicable | scaffold stage |
| History service | Node.js | n/a (file store now) | n/a | n/a | not_applicable | scaffold stage |

## Mandatory evidence before GO
- Driver inventory file paths and versions.
- Protocol negotiation test logs.
- Representative query smoke results.
- Latency/timeout comparison baseline vs target.
- Inventory script: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-driver-inventory.sh`

## Evidence sources
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/cassandra-cache/pom.xml`
- `/Users/alexb/Documents/Dev/Dev_new/mp-server/pom.xml`
- `/Users/alexb/Documents/Dev/Dev_new/mp-server/persistance/pom.xml`
