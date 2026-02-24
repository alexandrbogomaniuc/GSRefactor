# Legacy Mixed-Topology Manual Full Flow Result

- status: MANUAL_FULL_FLOW_PASS
- scope: refactored GS + legacy MP/client manual mixed-topology validation (launch/handoff + reconnect)
- timestamp_utc: 2026-02-24T16:27:10Z
- prerequisites:
  - Cassandra 4 target fully migrated from legacy with row-count parity 107/107
  - refactor GS runtime Cassandra host `c1-refactor:9042`
  - legacy MP/client endpoints reachable (`6300` / `80`)

## Preconditions / Corrections Applied

- Manual localhost launches for banks `6274`/`6275` require `subCasinoId=507` in the query string.
- Using synthetic login/session values fails as expected (`Incorrect credentials`).
- Valid token used for this manual wave: `bav_game_session_001`.

## Launch / Handoff Validation (PASS)

- Bank `6274` (`USD`) launch with `subCasinoId=507` + valid token returned:
  - `302` GS redirect to legacy MP template (`/real/mp/template.jsp`)
  - `200` legacy MP template HTML with lobby/game asset URLs and websocket `ws://localhost:6300/websocket/mplobby`
- Bank `6275` (`VND`) launch with `subCasinoId=507` + valid token returned:
  - `302` GS redirect to legacy MP template (`/real/mp/template.jsp`)
  - `200` legacy MP template HTML with lobby/game asset URLs and websocket `ws://localhost:6300/websocket/mplobby`

Launch evidence dirs:
- `/Users/alexb/Documents/Dev/dev_new/docs/validation/legacy-mixed-topology/manual-20260224-161112-b6274-sc507-token`
- `/Users/alexb/Documents/Dev/dev_new/docs/validation/legacy-mixed-topology/manual-20260224-161112-b6275-sc507-token`

## Reconnect Scenario Validation (PASS)

### Bank 6274 (USD)
- Two sequential launches succeeded (`200` after redirect chain for both requests)
- SID changed across reconnect launch (`request1-sid` != `request2-sid`)
- GS logs show reconnect handling path (`finishGameSessionAndMakeSitOut`) and new session creation
- Cassandra `CurrentPlayerSessionState` row for key `bav_game_session_001` updated to the second SID and `isfinishgamesession=false`

### Bank 6275 (VND)
- Two sequential launches succeeded (`200` after redirect chain for both requests)
- SID changed across reconnect launch (`request1-sid` != `request2-sid`)
- GS logs show reconnect handling path (`finishGameSessionAndMakeSitOut`) and new session creation
- Cassandra `CurrentPlayerSessionState` row for key `8` updated to the second SID and `isfinishgamesession=false`

Reconnect evidence dirs:
- `/Users/alexb/Documents/Dev/dev_new/docs/validation/legacy-mixed-topology/manual-20260224-162654-b6274-sc507-reconnect`
- `/Users/alexb/Documents/Dev/dev_new/docs/validation/legacy-mixed-topology/manual-20260224-162654-b6275-sc507-reconnect`

## FRB Scenario (N/A for this bank/game wave)

- `FRB_GAMES_ENABLE` is `null` in `rcasinoscks.bankinfocf` for both banks `6274` and `6275`.
- FRB checklist step treated as not applicable for this mixed-topology manual wave.
- Evidence source: `/Users/alexb/Documents/Dev/dev_new/docs/phase7/cassandra/full-copy/run-20260224-155602/csv/rcasinoscks_bankinfocf.clean.csv`

## Notes

- This result validates manual mixed-topology launch/handoff and reconnect behavior against the migrated Cassandra 4 target.
- It does not replace Phase 4/5/6 strict runtime evidence packs or security dependency audit/lockfile work.
