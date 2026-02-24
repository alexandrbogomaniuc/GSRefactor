# Phase 7 Cassandra Full Data Copy

- Timestamp (UTC): 2026-02-24T15:57:57Z
- Source container: gp3-c1-1
- Target container: refactor-c1-refactor-1
- Keyspaces: rcasinoscks,rcasinoks
- truncateTarget: true
- totalTables: 107
- importedTablesOk: 42
- emptyTables: 64
- failedStages: 1
- totalImportedCsvRows: 1524
- runDir: /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/full-copy/run-20260224-155602
- statusTsv: /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/full-copy/run-20260224-155602/status.tsv

## Failures
- rcasinoks.httpcallinfocf [import] <stdin>:1:Failed to process 45 rows; failed rows written to import_rcasinoks_httpcallinfocf.err
