# CURRENT_STATE_AUDIT

Code-grounded audit of Gamesv1 using commands executed on this machine.

Date: 2026-03-01  
Branch: `main`

## 1) Validation Commands Actually Run

### Build
Command:
```bash
corepack pnpm run build
```
Result: PASS (`exit 0`)

### Tests
Command:
```bash
corepack pnpm run test
```
Result: PASS (`exit 0`)

Observed test summary from output:
- `test:config`: 8 passed, 0 failed
- `test:animation-policy`: 6 passed, 0 failed
- `test:contract`: all scenarios passed

### Notes
- `test:layout` was **not** executed in this audit pass.
- Node emitted `MODULE_TYPELESS_PACKAGE_JSON` warnings during tests.

## 2) Repo Hygiene Checks (Actual Commands)

### Tracked generated/dependency paths
Command:
```bash
git ls-files | rg "(^|/)node_modules/|(^|/)dist/|(^|/)build/|(^|/)\\.cache/|(^|/)release-packs/"
```
Result: no matches (clean tracked source for those generated paths)

### Scaffolder duplication
Command:
```bash
Test-Path tools/create-game
```
Result: `False` (duplicate scaffolder package removed)

Canonical scaffolder is:
- `tools/create-game.ts`
- script: `corepack pnpm run create-game -- --gameId <gameId> --name "<name>" --themeId <themeId> --languages en,es,de`

### Package manager pin
Command output (from `package.json`):
- `packageManager=pnpm@10.30.3`

## 3) Canonical vs Legacy Boundaries

Canonical runtime docs:
- `docs/protocol/gs-http-runtime.md`
- `docs/protocol/browser-runtime-api-contract.md`

Legacy/optional:
- `docs/protocol/abs-gs-v1.md` (legacy/experimental)
- `packages/operator-pariplay/*` (optional legacy/operator integration surface)

Non-canonical generated docs:
- `docs/generated/*` (machine-specific outputs)
- `docs/examples/release-pack/*` (one intentionally committed example)

## 4) Premium-Slot Runtime Scope Check

Command used:
```bash
rg -n "@gamesv1/operator-pariplay|window\\.postMessage|new WebSocket" games/premium-slot/src games/premium-slot/package.json games/premium-slot/vite.config.ts
```
Result: no matches.

Interpretation:
- Canonical game path does not include operator bridge dependency or direct browser messaging/socket calls.
- Transport is expected via `@gamesv1/core-protocol`.

## 5) Documentation Alignment Snapshot

Updated canonical map:
- `docs/DOCS_MAP.md` points transport truth to `docs/protocol/gs-http-runtime.md`.
- `docs/RELEASE_PROCESS.md` code map references current files:
  - `packages/core-protocol/src/http/GsHttpRuntimeTransport.ts`
  - `packages/core-compliance/src/ResolvedRuntimeConfig.ts`
  - `packages/core-compliance/src/ConfigResolver.ts`

Generated orchestration output moved out of canonical docs:
- `docs/generated/ORCHESTRATOR_OUTPUTS.md`

## 6) Current Honest Status

What is confirmed by command output in this audit:
1. Build passes.
2. Root test command passes.
3. Tracked generated/dependency artifacts (`node_modules/dist/build/.cache/release-packs`) are clean.
4. Duplicate scaffolder package path was removed; one canonical create-game path remains.
5. Canonical runtime doc naming is GS HTTP specific (`gs-http-runtime.md`) and legacy WS is explicitly separate.
