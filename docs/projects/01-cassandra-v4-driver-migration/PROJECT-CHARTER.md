# Project 01 Charter: Cassandra V4 + Driver Migration

Last updated: 2026-02-25 UTC
Project code: `CASS-V4`

## Objective
Upgrade the data layer to Cassandra 4-compatible runtime behavior and finish Java driver migration to the 4.x line, with proven data safety and stable game runtime behavior.

## Why this project exists
Current system still carries Cassandra 2.x-era assumptions and mixed driver behavior. This project finishes the migration path so future work is not blocked by legacy database constraints.

## Scope in
1. Driver migration plan and implementation for Java services that access Cassandra.
2. Protocol/session/query compatibility updates for Cassandra 4.
3. Full schema and data parity validation between legacy source and v4 target.
4. Runtime verification for launch, wallet, wager, settle, history, reconnect.
5. Rollback runbook and rehearsal.

## Scope out
- New game feature development.
- Business logic redesign unrelated to Cassandra migration.
- Production rollout scheduling (handled separately by release process).

## Baseline facts (as of 2026-02-25)
- Existing docs and scripts already exist in `docs/phase7` and `gs-server/deploy/scripts/phase7-*`.
- Existing evidence shows rehearsal progress but final closure requires explicit driver migration completion and integrated sign-off.

## Success criteria
1. All Cassandra Java consumers run on approved driver 4.x line.
2. Full parity checks pass for required keyspaces/tables.
3. Runtime smoke and regression suite pass on v4 target.
4. Rollback drill is proven and documented.
5. Evidence package is complete and reviewable by non-developers.

## Exit status options
- `SIGN_OFF_READY`: all criteria passed.
- `NO_GO`: any safety gate failed.
- `PARTIAL`: technical migration done but evidence/sign-off incomplete.

## Linked plan files
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/01-cassandra-v4-driver-migration/WORK-BREAKDOWN-AND-SCHEDULE.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/01-cassandra-v4-driver-migration/TEST-STRATEGY.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/01-cassandra-v4-driver-migration/DOCUMENTATION-AND-EVIDENCE-CHECKLIST.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/01-cassandra-v4-driver-migration/RISKS-ROLLBACK-SIGNOFF.md`
