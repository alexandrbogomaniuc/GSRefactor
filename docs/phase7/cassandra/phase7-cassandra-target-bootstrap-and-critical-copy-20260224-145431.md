# Phase 7 Cassandra Target Bootstrap And Critical Copy

- Timestamp (UTC): 2026-02-24T14:54:37Z
- Source container: gp3-c1-1
- Target service/container: c1-refactor / refactor-c1-refactor-1
- Legacy version snapshot: ` 2.1.20 | Test Cluster`
- Target version snapshot: ` 4.1.10 | refactor-target-upgrade`
- Target image intent: separate upgrade rehearsal target (keep existing c1 unchanged)

## Keyspace Schema Import
- rcasinoscks:FAIL

## Critical Table Data Copy
- rcasinoscks.currencycf:OK:15

## Target Validation Script Results
- preflight code=0 :: dra-preflight-20260224-145434.log
- schema-export code=0 :: dra-schema-refactor-c1-refactor-1-20260224-145435.cql
- table-counts code=0 :: ts-refactor-c1-refactor-1-20260224-145435.txt
- query-smoke code=0 :: dra-query-smoke-refactor-c1-refactor-1-20260224-145436.log

- Result: NO_GO_PARTIAL_FAILURE_REVIEW_ERRORS
- Artifact dir: `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/target-bootstrap-20260224-145431`
