# Master Test Plan (Track A + Track B)

Last updated: 2026-02-25 UTC

## Goal
Provide one shared test plan for both final tracks so completion can be measured consistently.

## Test levels
1. Level 1: Fast checks per commit
- Compile/tests for touched modules.
- Quick script smoke checks.

2. Level 2: Wave-level validation
- Track-specific matrix execution.
- Targeted runtime checks for changed areas.

3. Level 3: Integrated system validation
- Full local verification suite.
- End-to-end launch/wallet/multiplayer flows.
- Data and contract checks.

4. Level 4: Release safety validation
- Performance and resilience checks.
- Rollback rehearsal.

## Mandatory integrated scenarios
1. Single-player launch flow works from start to playable state.
2. Multiplayer launch flow works (lobby, room, game entry).
3. Wallet lifecycle works (auth, balance, bet, settle/refund).
4. History and reconnect flows stay stable.
5. Support/config operations still function for target banks.

## Core commands to run in each major wave
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-evidence-pack.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-runtime-naming-inventory.sh
```

## Evidence publication rule
Every major wave must publish:
1. What changed.
2. What was tested.
3. What passed/failed.
4. What remains.
5. Exact links to raw artifacts.

## Final completion rule
Project is finalized only when both tracks are `SIGN_OFF_READY` and integrated Level 3 + Level 4 checks pass.
