# CURRENT_STATE_AUDIT

Code-grounded audit of Gamesv1 based on commands run on 2026-03-01.

## 1) Commands executed and results

### Build
Command:
```bash
corepack pnpm run build
```
Result: PASS (`exit 0`)

Observed output summary:
- Root script executed `corepack pnpm --filter @games/premium-slot build`
- `@games/premium-slot` build executed `tsc`

### Tests
Command:
```bash
corepack pnpm run test
```
Result: PASS (`exit 0`)

Observed output summary:
- `test:config`: 8 passed, 0 failed
- `test:animation-policy`: 6 passed, 0 failed
- `test:contract`: 8 passed, 0 failed
- Contract suite now validates browser runtime operations (`bootstrap`, `opengame`, `playround`, `featureaction`, `resumegame`, `gethistory`) including requestCounter/idempotency/restore behavior.

### Output capture file
Command outputs and root tree are recorded in:
- `docs/generated/ORCHESTRATOR_OUTPUTS.md`

## 2) Repo hygiene checks

### Tracked generated/dependency paths
Command:
```bash
git ls-files | rg "(^|/)node_modules/|(^|/)dist/|(^|/)build/|(^|/)\.cache/|(^|/)release-packs/"
```
Result: no matches (command exit 1 due no matches)

### Canonical scaffolder path
Command:
```bash
Test-Path tools/create-game.ts
Test-Path tools/create-game
```
Result:
- `tools/create-game.ts`: `True`
- `tools/create-game`: `False`

## 3) Canonical vs legacy scope

Canonical runtime docs:
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/browser-error-codes.md`
- `docs/gs/browser-runtime-sequence-diagrams.md`

Legacy/experimental transport adapters still present:
- `packages/core-protocol/src/ws/GsWsTransport.ts`
- `packages/core-protocol/src/http/ExtGameTransport.ts`

Status: legacy adapters are retained but non-canonical.

## 4) Runtime boundary checks in premium-slot

Command:
```bash
rg -n "@gamesv1/operator-pariplay|window\.postMessage|new WebSocket" games/premium-slot/src games/premium-slot/package.json games/premium-slot/vite.config.ts
```
Result: no matches (command exit 1 due no matches)

Interpretation:
- Canonical premium-slot runtime path does not directly depend on operator bridge APIs.
- No direct `window.postMessage` or raw `WebSocket` calls in premium-slot source.

## 5) Current honest status

Confirmed by command output in this audit:
1. Build passes.
2. Test suite passes.
3. Browser-runtime contract tests replace canonical WS/ExtGame contract dependency.
4. Canonical docs point to `docs/gs/*` contract set.
5. Legacy WS/ExtGame/operator tests are moved under `tests/_archive/` and removed from canonical test scripts.
