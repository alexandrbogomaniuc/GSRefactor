# Legacy Mixed-Topology Validation Pack (Refactored GS + Legacy MP/Client)

## What Was Added
- Added a dedicated mixed-topology validation pack generator:
  - `gs-server/deploy/scripts/legacy-mixed-topology-validation-pack.sh`
- Added a smoke test:
  - `gs-server/deploy/scripts/legacy-mixed-topology-validation-pack-smoke.sh`
- Integrated both into the shared local verification suite.

## Purpose
- Standardize the deferred compatibility validation wave for:
  - refactored GS
  - legacy MP server
  - legacy client/static stack
- Produce one repeatable operator checklist + endpoint reachability preflight report before manual launch/reconnect/FRB parity runs.

## Real Evidence (This Run)
- Generated report:
  - `docs/validation/legacy-mixed-topology/legacy-mixed-topology-validation-20260224-112913.md`
- Result:
  - `status=NO_GO_RUNTIME_ENDPOINTS_UNREACHABLE`
- Reachability probes (this shell):
  - refactor GS `127.0.0.1:18081` -> `000`
  - legacy MP `127.0.0.1:8088` -> `000`
  - legacy client `127.0.0.1:8090` -> `000`

## Fixed During Implementation
- Corrected false-READY classification caused by duplicated `000000` curl fallback output.
- Removed shell command substitution bug in report text (`cwstartgamev2.do` checklist line).

## Notes
- This report is expected to show `NO_GO_RUNTIME_ENDPOINTS_UNREACHABLE` until the target runtime stack is started and reachable from the current shell.
- After services are reachable, rerun the pack and use the included checklist for the full mixed-topology compatibility validation wave.
