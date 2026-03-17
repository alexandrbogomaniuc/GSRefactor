# Rehearsal Template Option 2: DSBulk Pilot

## Purpose

This template is for a future approved pilot only. It exists so operators have a ready checklist if the representative snapshot rehearsal shows that `cqlsh COPY` is too slow for production volume.

## Preconditions

- Option 1 snapshot rehearsal has already been attempted or blocked on timing
- approval has been given to evaluate `DSBulk`
- representative source data or source access is available

## Inputs Required

- representative Cassandra 3.11 source data or source access
- target Cassandra 5.0.6 rehearsal environment
- `DSBulk` tool package and operator-approved invocation parameters

## Pilot Scope

- use the same 1-2 largest representative tables chosen for the snapshot rehearsal
- capture:
  - rows copied
  - bytes copied
  - start and end timestamps
  - rows per second
  - MB per second
  - error handling / restart behavior

## Required Outputs

- side-by-side comparison with the current `cqlsh COPY` timing
- operator notes on complexity, observability, and restartability
- updated evidence zip stored outside the repo

## Decision Rule

- only recommend a runbook switch if the DSBulk pilot clearly improves throughput or operational reliability on representative data
- do not switch the production mechanism based on this template alone

## Rollback Reminder

Even during a DSBulk pilot:

- keep the legacy Cassandra 3.11 source untouched as the rollback anchor
- keep application runtime verification unchanged:
  - health `200`
  - gameplay `302 -> 200`
