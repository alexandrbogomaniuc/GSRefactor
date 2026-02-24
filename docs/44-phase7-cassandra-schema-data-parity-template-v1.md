# Phase 7 Cassandra Schema/Data Parity Template v1

Last updated: 2026-02-20 UTC

## Purpose
Standard template for old-vs-target Cassandra rehearsal comparison.

## 1) Schema parity
- Source schema dump file: `________________________`
- Target schema dump file: `________________________`
- Diff command used: `________________________`
- Diff output file: `________________________`
- Result summary:
  - Unexpected keyspace diffs: `YES/NO`
  - Unexpected table diffs: `YES/NO`
  - Option-level diffs accepted: `YES/NO`

## 2) Critical table count parity
Fill for each critical table:
| Keyspace | Table | Source Count | Target Count | Delta | Pass/Fail | Notes |
|---|---|---:|---:|---:|---|---|
| rcasinoscks | accountcf | | | | | |
| rcasinoscks | accountcf_ext | | | | | |
| rcasinoscks | paymenttransactioncf2 | | | | | |
| rcasinoks | gamesessioncf | | | | | |
| rcasinoscks | frbonuscf | | | | | |

## 3) Sample hash parity (high-risk rows)
- Sampling strategy: `PK list / random sample / bank-scoped sample`
- Hash method: `SHA256(canonical row json)`
- Mismatch rows count: `_____`
- Mismatch details file: `________________________`

## 4) Runtime parity checks
- Query smoke log: `________________________`
- Launch flow: PASS/FAIL
- Wager flow: PASS/FAIL
- Settle flow: PASS/FAIL
- History/reconnect: PASS/FAIL
- FRB flow: PASS/FAIL

## 5) Decision
- Go for canary: YES/NO
- Blocking issues:
  1. ________________________
  2. ________________________
