# Production Migration Approval Request

## Why This Request Exists

The local Option A release-track baseline is green, but production-scale migration timing is still unproven. Local smoke data and archived local volumes are too small to make a credible decision between `cqlsh COPY` and `DSBulk` for production-volume tables.

## What Is Already Proven

- migration guard remains `PASS/PASS`
- healthcheck remains `200`
- gameplay canary remains `302 -> 200`
- the repo-tracked `refactored_release` topology is the current release-path baseline

## Approval Needed

Approve one of the following inputs so a representative timing rehearsal can be run.

### Option 1: Representative Snapshot Rehearsal

Provide a representative Cassandra 3.11 snapshot, SSTable tar, or equivalent export for:

- `rcasinoks`
- `rcasinoscks`

This is the preferred path because it keeps the current documented migration mechanism under test before any tool switch is considered.

### Option 2: Read-Only Environment Access

Provide SSH or read-only access to a staging or production-like legacy Cassandra node so operators can collect:

- `nodetool tablestats` or `cfstats`
- `DESCRIBE KEYSPACE rcasinoks`
- `DESCRIBE KEYSPACE rcasinoscks`
- a timing rehearsal on the 1-2 largest tables

## Explicit Non-Approval

Do not treat this request as approval to:

- switch the production migration mechanism now
- replace `cqlsh COPY` in the runbook now
- continue local archaeology on this workstation

## Requested Decision

Please approve one of the following:

1. provide a representative 3.11 dataset for a snapshot-based rehearsal
2. provide read-only access to a staging/prod-like legacy Cassandra source
3. defer production migration timing approval and keep release status at runtime-ready but scale-unproven
