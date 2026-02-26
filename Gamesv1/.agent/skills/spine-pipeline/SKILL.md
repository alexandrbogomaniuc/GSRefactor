---
name: spine-pipeline
description: Use this skill when integrating Spine with PixiJS v8: export conventions, atlas sizing, runtime wrapper components, loading via Pixi Assets bundles, and memory/performance constraints.
---

# Spine Pipeline (Pixi v8)

## Goal
Make Spine the primary premium animation system.

## Instructions
1) Define export conventions: naming, atlas size limits, animation naming.
2) Require per-scene loading and unloading strategy.
3) Provide a wrapper API: play(name, loop), setSkin, setMix, stop, destroy.
4) Add debug toggles to show bounds and current animation.

## Constraints
- Do not load all Spine assets globally by default.
