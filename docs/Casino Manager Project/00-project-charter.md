# Project Charter

## Objective
Build an internal Casino Manager module that mirrors the provider CM behavior while operating on a safe copied dataset.

## Current Baseline
- Provider CM URL analyzed in read-only mode: `https://cm.discreetgaming.com/reports/playerSearch/layout`.
- Full menu/API route inventory captured from authenticated session.
- Local DB stack baseline captured for Cassandra (`rcasinoscks`, `rcasinoks`).

## Non-Negotiable Constraints
- Read-only discovery only on provider CM; no mutating actions in provider environment.
- Our CM must use copied/synced data, not direct writes into live provider-like runtime DB.
- Casino-side SQL DB is out of scope for this phase and will be integrated later.
- Credentials and secrets must never be stored in repository docs.

## Success Criteria
- 100% route inventory coverage for CM menu links.
- Route-to-table mapping for core management/reporting domains.
- Approved sync model (schedule + consistency + rollback).
- Dedicated CM container deployable independently from GS/MP runtime services.
