# Evidence - Hard-Cut M2 Wave 180A/180B + 181

Timestamp (UTC): 2026-02-27 09:46:23

## Plan
- Group A declarations (`gs.managers.dblink`): 10
- Group B declarations (`gs.singlegames.tools.util`): 7

## Execution
- Declaration migrations completed in both groups.
- Initial narrow rewire plan expanded after importer scan showed broad dblink/util fanout in `common-gs` and related test/web-gs files.
- Applied bounded targeted rewires for direct Java imports only.

## Validation
- Fast gate rerun1: PASS (9/9).
- Full matrix rerun1: PASS (9/9).

## Notes
- Canonical fast-gate and full-matrix logs promoted from rerun1.
- No rollback required.
