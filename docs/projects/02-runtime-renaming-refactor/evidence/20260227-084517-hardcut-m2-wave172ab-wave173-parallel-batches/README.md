# Evidence - Hard-Cut M2 Wave 172A/172B + 173

Timestamp (UTC): 2026-02-27 08:45:17

## Plan
- Group A declarations:
  - `IConfigsInitializer`
  - `CassandraRemoteCallPersister`
- Group B declarations:
  - `PersisterDependencyInjector`
  - `AbstractLockManager`

## Execution
- Declaration-only migration in cache module sources.
- No cross-module rewires in this wave.

## Validation
- Fast gate rerun1: PASS (9/9)
- Full matrix rerun1: PASS (9/9)

## Notes
- Canonical fast-gate and full-matrix logs promoted from rerun1.
