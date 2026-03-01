# /release_game - GS Release Workflow

When triggered:
1. Read canonical contracts:
- `docs/gs/release-registration-contract.md`
- `docs/gs/enable-disable-canary-rollback.md`
- `docs/gs/browser-runtime-api-contract.md`
2. Build game:
```bash
corepack pnpm --filter @games/<gameId> build
```
3. Generate release pack:
```bash
corepack pnpm run release:pack -- --game <gameId> --version <version> --static-origin <cdnBase>
```
4. Validate artifact set against release-registration contract.
5. Register and enable canary.
6. Run smoke checklist.
7. Promote or rollback per contract.

Mandatory runtime verification:
- `/v1/bootstrap`
- `/v1/opengame`
- `/v1/playround`
- `/v1/featureaction` (if enabled)
- `/v1/resumegame`
- `/v1/gethistory`
- requestCounter + idempotency duplicate behavior

Legacy note:
- `/v1/placebet` + `/v1/collect` are deprecated in canonical scope.
