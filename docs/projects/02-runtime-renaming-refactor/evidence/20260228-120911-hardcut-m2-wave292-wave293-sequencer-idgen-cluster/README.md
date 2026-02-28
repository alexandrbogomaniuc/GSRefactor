# Evidence Summary: Hard-Cut M2 Wave 292 + 293

- Timestamp (UTC): `2026-02-28 12:09-12:22`
- Scope: declaration-first migration of sequencer/id-generator cluster in `sb-utils/common/util`.

## Batch Targets
- Batch A:
  - `IIntegerIdGenerator`
  - `IIntegerSequencer`
  - `IIntegerSequencerPersister`
  - `ILongIdGenerator`
  - `ISequencer`
  - `ISequencerPersister`
- Batch B:
  - `IntegerIdGenerator`
  - `IntegerSequencer`
  - `LongIdGenerator`
  - `LongIdGeneratorFactory`
  - `Sequencer`

## Rerun Timeline
- `rerun1`: failed at `PRE02/STEP03` (`sb-utils`) due moved sequencer classes losing same-package visibility to unmoved `ExecutorUtils`.
- `rerun2`: failed at `STEP06` (`common-gs`) due duplicate-type compatibility drift after initial class-usage rewires.
- Stabilization: added bounded compatibility imports in moved classes and rolled back class-usage rewires for this cluster while retaining declaration migration.
- `rerun3`: canonical profile reached.

## Canonical Validation Outcomes (`rerun3`)
- Fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Canonical Blocker Profile
- Known external smoke blocker remained unchanged:
  - `/startgame` launch alias returns `HTTP 502` during `STEP09`.
