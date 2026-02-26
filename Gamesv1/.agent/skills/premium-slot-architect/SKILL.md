---
name: premium-slot-architect
description: Use this skill when designing or refactoring a PixiJS slot game architecture: scenes, reels, win flows, folder structure, state recovery, and reusable theme packs.
---

# Premium Slot Architect

## Goal
Design a production-grade, reusable slot game client architecture (mobile-first) with clean separation: engine vs game vs theme.

## Instructions
1) Propose a folder structure with clear module boundaries:
   - engine/ (rendering, time, events, assets, audio)
   - game/slots (reels, symbols, wins, math bridge)
   - themes/ (art + config only)
   - scenes/ (boot, preload, base game, bonus)
2) Enforce a “Theme Pack” pattern: theme defines visuals + layout + timings, code is shared.
3) Require a server-authoritative outcome interface and state restore plan.
4) Always produce acceptance criteria for architecture changes.

## Constraints
- Do not bake asset file paths into gameplay logic.
- Do not mix Pixi versions or deprecated APIs.
