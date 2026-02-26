---
name: pixi-v8-api-guardian
description: Use this skill when writing PixiJS code to ensure Pixi v8 API correctness, correct imports, correct Assets usage, and no outdated Pixi v5/v6/v7 patterns.
---

# Pixi v8 API Guardian

## Goal
Prevent outdated/hallucinated Pixi usage.

## Instructions
1) Before implementing Pixi code, ensure Pixi LLM docs exist locally:
   - docs/refs/pixijs/llms-medium.txt
   - docs/refs/pixijs/llms-full.txt
2) When uncertain, consult those docs first (ask user to create them if missing).
3) Use Pixi Assets (bundle-based loading) and modern patterns.
4) If a method/class doesn’t exist in v8 docs, do NOT use it.

## Constraints
- No “guessing” Pixi APIs.
