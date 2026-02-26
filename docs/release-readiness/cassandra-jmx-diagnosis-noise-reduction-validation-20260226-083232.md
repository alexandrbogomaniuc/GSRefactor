# Cassandra JMX Diagnosis Noise Reduction Validation

Date (UTC): 2026-02-26 08:32 UTC
Workspace: `/Users/alexb/Documents/Dev/Dev_new`

## Goal
Reduce recurring GS runtime warning noise from Cassandra JMX diagnosis when JMX metrics are unavailable, without affecting game launch runtime.

## Change
Updated:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/system/diagnosis/tasks/CassandraStateCheckTask.java`

Behavior changes:
- Skip JMX diagnosis while keyspace managers are not ready (debug only).
- Keep warnings for true host-discovery failure after managers are ready.
- Avoid verbose debug stack traces for host fallback errors (message-only debug).

## Validation
- Compile PASS:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-083232/mvn-web-gs-compile.log`
- Runtime smoke PASS (launch still healthy):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-083232/refactor-onboard-smoke.log`
- Log-noise check in recent GS tail:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-083232/gs-cassandra-diagnosis-snippet.txt`
  - Result: no `CassandraStateCheckTask` warn/error noise lines matched in this capture.

## Result
The repetitive Cassandra diagnosis warning path is reduced while the refactor launch flow remains functional.
