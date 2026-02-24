# Phase 7 Cassandra Target Bootstrap And Critical Copy

- Timestamp (UTC): 2026-02-24T12:55:57Z
- Source container: gp3-c1-1
- Target service/container: c1-refactor / refactor-c1-refactor-1
- Legacy version snapshot: ` 2.1.20 | Test Cluster`
- Target version snapshot: ` 4.1.10 | refactor-target-upgrade`
- Target image intent: separate upgrade rehearsal target (keep existing c1 unchanged)

## Keyspace Schema Import
- rcasinoks:OK
- rcasinoscks:OK

## Critical Table Data Copy
- rcasinoscks.accountcf:OK:1
- rcasinoscks.accountcf_ext:OK:1
- rcasinoscks.frbonuscf:OK:4
- rcasinoscks.paymenttransactioncf2:EMPTY
- rcasinoks.gamesessioncf:OK:68

## Target Validation Script Results
- preflight code=0 :: dra-preflight-20260224-125552.log
- schema-export code=0 :: dra-schema-refactor-c1-refactor-1-20260224-125553.cql
- table-counts code=0 :: ts-refactor-c1-refactor-1-20260224-125553.txt
- query-smoke code=0 :: dra-query-smoke-refactor-c1-refactor-1-20260224-125555.log

- Result: PARTIAL_SUCCESS_CRITICAL_TABLE_COPY_COMPLETE
- Artifact dir: `/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/target-bootstrap-20260224-125539`
