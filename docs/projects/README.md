# Project Tracks For Final Completion

Last updated: 2026-02-25 UTC

This folder contains two separate project tracks that finish the remaining core goals:

1. `01-cassandra-v4-driver-migration`
- Move runtime from Cassandra 2.x assumptions to Cassandra 4.x compatibility.
- Complete Java driver migration to 4.x line.
- Prove data parity, runtime stability, and rollback readiness.

2. `02-runtime-renaming-refactor`
- Finish class/package/config naming refactor.
- Replace legacy runtime naming safely (no blind global replace).
- Remove transitional compatibility only after proof.

## Why split into two projects
- Cassandra migration and naming migration are both high risk.
- They touch different layers and need separate test evidence.
- Separate tracks reduce merge risk and make sign-off clearer.

## Required shared rules
- Every change must include evidence in each track's `evidence/` folder.
- Every wave must have rollback instructions before apply.
- No direct merge to `main` without test matrix pass and review.
- Keep `/Users/alexb/Documents/Dev/Dev_new/docs/12-work-diary.md` updated during execution.

## Start here
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/PROGRAM-COORDINATION-PLAN.md`
