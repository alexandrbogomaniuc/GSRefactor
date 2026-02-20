# Phase 9 ABS Rename Wave Plan (GS Scope v1)

Last updated: 2026-02-20 UTC  
Evidence baseline: `/Users/alexb/Documents/Dev/Dev_new/docs/phase9/legacy-name-inventory-gs-20260220-101048.md`

## Scope guard
- Only `gs-server` is in scope.
- No functional contract changes during rename waves.
- Every wave must be revertible by isolated commit rollback.

## Wave order

| Wave | Target | Action | Risk | Gate |
|---|---|---|---|---|
| W0 | Docs + comments + non-runtime strings | Replace legacy branding words with ABS naming aliases | Low | no build/runtime change |
| W1 | Config aliases | Add ABS keys/aliases while keeping legacy keys active | Medium | parity config load checks |
| W2 | API docs/contracts namespace text | Update provider-facing docs/schemas to ABS naming | Low | contract tests still green |
| W3 | Package/class/file rename candidates with compatibility wrappers | Introduce ABS wrappers/adapters; keep legacy entry points delegating | High | compile + parity suite + canary |
| W4 | Legacy alias deprecation (explicit approval only) | Remove legacy names after canary stability window | High | explicit deprecation approval |

## Current hotspots (from inventory)
1. `com.dgphoenix` usage is dominant and must be handled by staged wrappers, not big-bang renames.
2. `mq` naming is concentrated in socket/service handlers and config XMLs.
3. `struts-config.xml`, launch flows, and websocket handlers require strict parity guards.

## Immediate next implementation
1. Execute W0 in GS docs/config comments only.
2. Add compatibility mapping document for renamed keys/classes before W1/W3 code changes.
3. Add CI check that blocks direct hard-delete of legacy symbols before approval flag is enabled.
