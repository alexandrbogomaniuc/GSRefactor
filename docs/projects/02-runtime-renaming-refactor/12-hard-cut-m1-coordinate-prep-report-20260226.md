# Hard-Cut M1 Build-Coordinate Prep Report

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M1 - Build-coordinate transition prep`
Status: `COMPLETE`

## Purpose
Prepare a safe dependency/coordinate transition path before package renaming waves.

## Evidence folder
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091920-hardcut-m1-coordinate-prep`

## Findings
1. Legacy Maven coordinates are still widespread.
- GS pom legacy coordinate hits: 57
- MP pom legacy coordinate hits: 40

2. MP has direct dependency bridge points to GS legacy coordinates.
- `com.dgphoenix.casino:gsn-cache-restricted`
- `com.dgphoenix.casino:utils-restricted`
- `com.dgphoenix.casino.tools:kryo-validator`

3. A broad reactor selector probe failed due duplicate project identity.
- Error: `Project 'com.dgphoenix.casino:gsn-common-gs:1.0' is duplicated in the reactor`
- Action: use the proven per-module verification matrix for reliability.

## Validation executed
All commands below passed and are saved in evidence files:
- `mvn test` in `gs-server/sb-utils`
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## M1 decision
Use compatibility bridge coordinates during migration waves:
- Keep legacy coordinates available until GS+MP package migrations are complete.
- Move package/code namespace in controlled waves first.
- Switch pom coordinates in a dedicated late wave when both sides are ready.

This avoids immediate dependency breakage in MP while GS modules migrate.

## Next (M2)
Start package migration with smallest high-confidence module group and keep strict guardrails:
1. choose one module wave,
2. run pre-scan,
3. apply rename in that wave only,
4. run full matrix,
5. capture evidence and commit.

