# Isolation and Repository Synchronization Policy

Last updated: 2026-02-19 UTC

## 1. Workspace Boundary
- `/Users/alexb/Documents/Dev` is treated as immutable source baseline.
- All active modernization changes happen only inside `/Users/alexb/Documents/Dev/Dev_new`.
- Runtime assets required by refactor are copied into `Dev_new` and referenced from there.

## 2. Runtime Isolation
- Existing stack remains untouched.
- New stack uses compose project `refactor` from:
  - `/Users/alexb/Documents/Dev/Dev_new/mq-gs-clean-version/deploy/docker/refactor/docker-compose.yml`
- Refactor stack uses dedicated host ports and dedicated Docker volume `refactor-cassandra-data`.

## 3. Source Copy Baseline
Copied from upper baseline into `Dev_new` for isolation:
- `/Users/alexb/Documents/Dev/Doker` -> `/Users/alexb/Documents/Dev/Dev_new/Doker`
- `/Users/alexb/Documents/Dev/Casino side` -> `/Users/alexb/Documents/Dev/Dev_new/Casino side`

## 4. Git Synchronization Target
- Target repository: `https://github.com/alexandrbogomaniuc/GSRefactor`
- Branch target: `main`
- Sync mode: batched commits and batched push to reduce long transfer failures.

## 5. Batched Push Strategy
1. Prepare `main` as isolated snapshot branch in `Dev_new`.
2. Add and commit by top-level batches (for example: docs, gs/mp/new-games, runtime assets).
3. Push each batch to `main` immediately.
4. Verify remote tree after each batch.

## 6. Rollback and Safety
- If any batch push fails, retry only failed batch after network/size fix.
- No destructive changes are performed in upper `Dev`.
