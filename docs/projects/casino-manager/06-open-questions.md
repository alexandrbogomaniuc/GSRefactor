# Open Questions (Closed by Best Practice for Phase-1)

## 1) Scope of Parity
- Decision:
  - prioritized rollout, not all `94` routes in phase-1.
- Reason:
  - fastest safe delivery with measurable operator value.

## 2) Data Freshness Target
- Decision:
  - hourly sync (`~60 min` max lag) for phase-1.
- Reason:
  - matches current cost/simplicity requirement and minimizes runtime risk.

## 3) Write Capabilities
- Decision:
  - strictly read-only in phase-1, except local CM auth password management.
- Reason:
  - avoids accidental provider-impacting behavior while mapping parity.

## 4) Environment Boundary
- Decision:
  - isolate CM copy datastore from GS runtime datastore (logical or physical isolation).
- Reason:
  - protects game runtime and simplifies rollback/testing.

## 5) Security Model
- Decision:
  - separate CM auth/roles for our module; do not mirror provider identity directly.
- Reason:
  - independent control plane and safer operational boundary.

## 6) Priority Report Pack
- Decision:
  - phase-1 report pack:
    - `playerSearch`
    - `bankList`
    - `transactions`
    - `gameSessionSearch`
    - `walletOperationAlerts`
- Reason:
  - covers player, bank, financial, session, and alert operations.
