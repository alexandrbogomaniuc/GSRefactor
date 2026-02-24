# Legacy Mixed-Topology Validation Pack

- refactorGsBaseUrl: http://127.0.0.1:18081
- legacyMpBaseUrl: http://127.0.0.1:6300
- legacyClientBaseUrl: http://127.0.0.1:80
- bankId: 6275
- gameId: 838
- dryRun: false
- status: READY_FOR_MANUAL_FULL_FLOW_EXECUTION
- probe_refactor_gs_http: 200
- probe_legacy_mp_http: 000
- probe_legacy_mp_tcp: open
- probe_legacy_client_http: 502

## Validation Flow Checklist

1. Start game from refactored GS using legacy-compatible launch endpoint (cwstartgamev2.do).
2. Verify legacy MP routing/bypass behavior for bank/game combination.
3. Verify legacy client asset/template load and launch handoff.
4. Run reconnect scenario after session handoff.
5. Run FRB scenario (award/check/use/cancel or end-of-flow return path) if enabled for bank/game.
6. Capture GS/MP/client logs and timestamps for each step.

## Notes

- This pack validates environment reachability and provides a repeatable operator checklist.
- Use live legacy infrastructure endpoints in non-prod first; canary only after parity evidence passes.
