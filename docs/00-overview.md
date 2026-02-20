# 00 Overview

## Purpose
Ground-truth overview of the GS/MP platform and runtime dependencies.

## Scope
- Game Server (GS)
- MP engine
- Cassandra
- Kafka
- Zookeeper
- Static assets and game client launch path

## Current Status
- Phase 0 forensic scan is active with reproducible local runtime baseline.
- End-to-end launch path is proven up to lobby/game bootstrap for `6274 + 838`.
- Major deployment/runtime fixes were applied (documented in changelog and deployment error log).
- Current integration focus:
  - keep `external user id` mapping consistent between GS and casino-side wallet,
  - clear/avoid stuck pending operations,
  - continue deep flow tracing with SID/rid correlation.

## Latest Proven Runtime State
- GS startup succeeds and stays healthy.
- MP websocket listeners are active on `6300` and `6301` after MP restart.
- Static route for directory game handoff is fixed (`try_files` with index fallback).
- Casino-side BAV endpoints now accept token-style `userId` and return `200` for balance/wager/refund test calls.

## Requirement Checklist Sources
- Main requirements checklist (phase-0 extracted outline):
  - `/Users/alexb/Documents/Dev/docs/09-game-client-requirements-checklist.md`
- Operator quick-check commands:
  - `/Users/alexb/Documents/Dev/docs/10-operator-command-pack.md`
- Source PDF:
  - `/Users/alexb/Documents/Dev/readme all you need to know from md files/New Game requirements to work with bsg GS/Game_Client_Requirements_MAIN-1.pdf`

## Source Repositories
- /Users/alexb/Documents/Dev/mq-gs-clean-version
- /Users/alexb/Documents/Dev/mq-mp-clean-version
- /Users/alexb/Documents/Dev/mq-client-clean-version
