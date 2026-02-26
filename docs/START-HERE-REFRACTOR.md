# START HERE (Refactor Environment)

Last updated (UTC): 2026-02-26

This is the single starting point for a new machine.

## 1) Use this repository only

Work in:
- `/Users/alexb/Documents/Dev/Dev_new`

Do not start legacy stacks for this workflow.

## 2) One clean startup path

Run from repo root:

```bash
node ./gs-server/deploy/scripts/refactor-onboard.mjs preflight
node ./gs-server/deploy/scripts/refactor-onboard.mjs up
node ./gs-server/deploy/scripts/refactor-onboard.mjs smoke
```

If smoke passes, environment is considered ready.

## 3) Main test URL

Open this in browser:

[http://127.0.0.1:18080/startgame?bankId=6275&subCasinoId=507&gameId=838&mode=real&token=bav_game_session_001&lang=en](http://127.0.0.1:18080/startgame?bankId=6275&subCasinoId=507&gameId=838&mode=real&token=bav_game_session_001&lang=en)

Expected result: HTTP 200 and game launch page.

Optional second bank test (Betonline subcasino):

[http://127.0.0.1:18080/startgame?bankId=6274&subCasinoId=508&gameId=838&mode=real&token=bav_game_session_001&lang=en](http://127.0.0.1:18080/startgame?bankId=6274&subCasinoId=508&gameId=838&mode=real&token=bav_game_session_001&lang=en)

Important:
- `6276` is an internal bank id under subcasino `508`.
- External launch id for that bank is currently `6274`, so `bankId=6276` is expected to return `Bank is incorrect`.

## 4) Stop command

```bash
node ./gs-server/deploy/scripts/refactor-onboard.mjs down
```

## 5) If you need full onboarding details

- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/README-ONBOARDING.md`

## 6) Latest validated proof

- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/onboarding-lifecycle-validation-20260226-074518.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-074518/`

## 7) Current finalization status

- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/Project-Finalization-Report.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/PROJECTS-CLOSURE-SUMMARY-20260226.md`
