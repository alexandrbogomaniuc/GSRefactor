# Sync/Copy Strategy

## Goal
Run our CM against a copied/synced dataset to protect live runtime performance and reduce operational risk.

## Current Decision
- Use cron-based differential sync once per hour for phase-1.
- Defer CDC/live-sync design to a later milestone.

## Phase-1 Cron Model
- Frequency: every `1 hour` (top of hour).
- Scope:
  - key CM source entities and derived CM read-model tables.
- Mode:
  - differential upsert by checkpoint (`last_sync_ts` + tie-break key),
  - soft-delete propagation where required.

## Job Contract
1. Acquire distributed lock (`cm_sync_hourly`).
2. Load previous checkpoint.
3. Pull changed rows per source mapping.
4. Upsert into CM copy/read-model tables.
5. Write audit row (`run_id`, start/end, rows changed, lag, status).
6. Advance checkpoint only on full success.

## Failure Handling
- Retry policy: `3` retries with exponential backoff.
- On repeated failure:
  - keep previous checkpoint,
  - emit alert,
  - require manual acknowledge.
- Daily reconcile:
  - run a full compare checksum to detect drift.

## Guardrails
- CM-copy DB must be read-only for CM UI users.
- Sync jobs must be idempotent and checkpointed.
- Every sync run writes audit metadata (start/end, rows changed, errors, lag).
