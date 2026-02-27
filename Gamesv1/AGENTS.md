# AGENTS.md - Gamesv1 Local Agent Instructions

Scope: This file applies to all work under `e:\Dev\GSRefactor\Gamesv1`.

## Mandatory Startup (Every Session)
Before doing any analysis, coding, or review in this directory:
1. Read `.agent/context.md`.
2. Read all files in `.agent/rules/` and treat them as non-negotiable constraints.
3. Discover skills under `.agent/skills/*/SKILL.md` and use matching skill workflows when the task fits.
4. Check `.agent/workflows/*.md` and follow the relevant workflow when the task is a workflow trigger.

## Execution Rules
1. Keep all edits inside `e:\Dev\GSRefactor\Gamesv1` unless explicitly instructed otherwise.
2. Prioritize protocol/compliance requirements from `.agent/rules/01_rules_protocol.md`.
3. For PixiJS API uncertainty, consult local docs in `docs/refs/pixijs/` before implementation.
4. Prefer reusable monorepo patterns (`packages/*`, `tools/*`) over one-off game-specific duplication.

## Skill Usage Policy
1. If a task clearly matches one of the local skills, use that skill's instructions.
2. If multiple skills match, use the minimal set needed, in the order required by the task.
3. Do not ignore a matching local skill unless blocked by missing files or conflicting user instructions.

## Source of Truth
The `.agent/` directory is authoritative for project context, rules, skills, and workflows.