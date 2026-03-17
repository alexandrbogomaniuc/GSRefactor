# Latest Rehearsal Evidence Summary

## Canonical Truth

- branch: `cassandra-refactoring`
- baseline head under test: `cb00cf441ff9d689c32afd0f726d1111e6a1cf65`

## Evidence Timestamp

- summary generated: `2026-03-17 Europe/London`

## Migration Guard Snapshot

From `runtime_smoke/status/latest.env`:

```text
SCHEMA_OK=1
ROWPROOF_OK=1
ARCHIVER_LEGACY_OK=1
ARCHIVER_TARGET_OK=1
ITER_DIR=/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/logs/iter_01_20260317_045853
```

## Fullstack Health

```text
curl -i http://127.0.0.1:8080/support/health/check.jsp
HTTP/1.1 200
```

## Gameplay Canary

```text
curl -i "http://127.0.0.1:8080/cwguestlogin.do?bankId=271&gameId=838&lang=en"
HTTP/1.1 302
follow-up template: HTTP/1.1 200
```

Current local nuance:

- the direct `Location:` header may omit `:8080` in local curl output
- rewrite it to `http://127.0.0.1:8080/...` before the follow-up curl

## Compose Snapshot

Current playable baseline uses the repo-tracked compose topology:

```text
NAME                                       IMAGE                         COMMAND                  SERVICE               STATUS
refactored_release-fullstack-cassandra-1   cassandra:5.0.6               "docker-entrypoint.s…"   fullstack-cassandra   Up (healthy)
refactored_release-fullstack-kafka-1       confluentinc/cp-kafka:7.6.1   "/etc/confluent/dock…"   fullstack-kafka       Up
refactored_release-fullstack-zookeeper-1   zookeeper:3.9                 "/docker-entrypoint.…"   fullstack-zookeeper   Up
refactored_release-webgs-1                 tomcat:9-jdk11                "catalina.sh run"        webgs                 Up
refactored_release-webgs-static-1          nginx:1.27-alpine             "/docker-entrypoint.…"   webgs-static          Up
```

Expected running services:

- `refactored_release-fullstack-cassandra-1`
- `refactored_release-fullstack-zookeeper-1`
- `refactored_release-fullstack-kafka-1`
- `refactored_release-webgs-1`
- `refactored_release-webgs-static-1`

## Local Archived Evidence

- runtime-only bundle:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_093403/release_rehearsal_04.zip`
- production-scale proof bundle:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_121415/prod_scale_proof_01.zip`
- local volume summary:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_20260317_121415/prod_scale_proof_01/volume_scan_summary.txt`

## Migration Scale Decision Status

What is proven:

- runtime parity is green:
  - migration guard `PASS/PASS`
  - health `200`
  - gameplay `302 -> 200`
- the canonical app baseline referenced by the proof bundles was clean and synced at `cb00cf441ff9d689c32afd0f726d1111e6a1cf65`

What is not proven:

- production-scale Cassandra migration duration
- a credible local choice between `cqlsh COPY` and `DSBulk` for production-volume tables

What local search already proved:

- the older archived full-copy run exists but is tiny:
  - total source rows `1573`
  - largest tables `315 / 241 / 180 / 137`
- `gp3_cassandra-data` is mostly commitlog
- relevant local payloads are tiny:
  - about `1,609,056 bytes` relevant in that volume
  - largest relevant local payload found anywhere is about `4,467,540 bytes`
- local archaeology is closed for this workstation

Next decision artifacts:

- `docs/refactored_release/PROD_MIGRATION_SCALE_DECISION_NOTE.md`
- `docs/refactored_release/PROD_MIGRATION_APPROVAL_REQUEST.md`
- `docs/refactored_release/REHEARSAL_TEMPLATE_OPTION1_SNAPSHOT.md`
- `docs/refactored_release/REHEARSAL_TEMPLATE_OPTION2_DSBULK.md`

These paths are intentionally local evidence and operator references, not release artifacts to publish from the repo.
