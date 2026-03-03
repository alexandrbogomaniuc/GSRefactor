# /release_game - GS Release Workflow

When triggered:

1. Read canonical contracts first:
- `docs/gs/README.md`
- `docs/gs/release-registration-contract.md`
- `docs/gs/enable-disable-canary-rollback.md`
- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/fixtures/*`

2. Confirm spec split:
- `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md` for client behavior/capabilities
- `docs/gs/*` for runtime/release wire contract truth

3. Build game:
```bash
corepack pnpm --filter @games/<gameId> build
```

4. Generate release pack:
```bash
corepack pnpm run release:pack -- --game <gameId> --version <version> --static-origin <cdnBase>
```

5. Validate artifact set against `docs/gs/release-registration-contract.md`.

6. Register and enable canary.

7. Run smoke checklist.

8. Promote or rollback per contract.

Mandatory runtime verification endpoints:
- `/slot/v1/bootstrap`
- `/slot/v1/opengame`
- `/slot/v1/playround`
- `/slot/v1/featureaction` (if enabled)
- `/slot/v1/resumegame`
- `/slot/v1/gethistory`

Mandatory verification behavior:
- requestCounter monotonic behavior
- idempotency duplicate behavior
- restore behavior

Legacy note:
- `/v1/placebet`, `/v1/collect`, `/v1/readhistory`, and `/v1/*` canonical endpoint assumptions are obsolete.
