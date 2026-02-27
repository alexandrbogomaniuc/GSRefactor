# Evidence - Hard-Cut M2 Wave 170A/170B + 171

Timestamp (UTC): 2026-02-27 08:26:39

## Plan
- Initial parallel plan:
  - Group A: IEntityUpdateListener + CassandraRemoteCallPersister with bounded rewires
  - Group B: IConfigsInitializer + PersisterDependencyInjector with bounded rewires

## Stabilization
- Fast gate rerun1 failed at `common-persisters` due missing `com.abs.casino.cassandra.IEntityUpdateListener` in that compile path.
- Rolled back broad A/B rewires.
- Retained safer cache-internal subset:
  - `ColumnIteratorCallback` declaration
  - `FakeNotAppliedResultSet` declaration
  - `AbstractCassandraPersister` bounded import rewire

## Validation
- Fast gate rerun2: PASS (9/9)
- Full matrix rerun1: PASS (9/9)

## Notes
- Canonical fast-gate logs promoted from rerun2.
- Canonical full-matrix logs promoted from rerun1.
