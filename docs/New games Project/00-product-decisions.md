# Product Decisions (Confirmed)

Last updated: 2026-02-11

## Confirmed By Stakeholder
- GS remains the core system for this project.
- GS must continue to own core platform concerns:
  - session lifecycle and protection,
  - wallet integration calls to casino side,
  - error handling and support tooling,
  - history/reporting and existing operational tools.
- New Games backend must not call casino wallet endpoints directly.
  - New Games backend will integrate through GS-internal APIs/contracts.
- First game target: Plinko.
  - Creative direction can include controlled experimentation.
  - Brand direction must stay aligned with BETONLINE.
- Initial launch target:
  - 200 concurrent players,
  - 100 bets per second.
- Compliance scope for phase 1:
  - no mandatory Reality Check/loss-limit/Pariplay gating at kickoff.

## Technology Direction
- Keep GS Java stack unchanged for core responsibilities.
- Build new game client with latest PixiJS in a separate modern frontend workspace.
- Avoid replacing legacy GS client pipeline globally in phase 1.
  - Legacy clients continue operating as-is.
  - New game client is isolated to reduce regression risk.

## High-Level Delivery Goal
- Deliver a Plinko production path that uses GS capabilities as much as possible instead of replacing them.

