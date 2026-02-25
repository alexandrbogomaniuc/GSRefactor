# Safe Rename Execution Plan With Compatibility Mapping

Date (UTC): 2026-02-25

## Goal
Finish runtime naming cleanup in controlled waves, without breaking launch/wallet/multiplayer flows.

## Current State
- Completed:
  - RN2 Wave A: `MQ_*` alias reads in `BankInfo` (`feed2f3f`)
  - RN2 Wave B: runtime class-loading fallback for `com.abs.*` <-> `com.dgphoenix.*` in critical loaders (`1045b5ec`)
  - RN3 Wave A/B: compatibility fallback extended to additional GS/MP runtime reflection hotspots (`62df498e`, `d5776764`)
  - RN4 Wave A: class-string config aliases for `WPM_CLASS`, `START_GAME_PROCESSOR`, `CLOSE_GAME_PROCESSOR` (`ad156b6c`)
- Remaining:
  - Some reflection points may still use direct `Class.forName(...)` and need ongoing inventory recheck
  - Runtime XML/JSP configs still contain legacy naming references
  - `MQ_*` payload/template contract still active between GS and MP (dual-field transition started)

## Compatibility Mapping (Working Baseline)
| Legacy surface | Target surface | Current compatibility status | Where handled now | Next step |
|---|---|---|---|---|
| `com.dgphoenix.*` class string | `com.abs.*` class string | Broad partial coverage | `ReflectionUtils` + GS/MP runtime loaders | Keep extending based on inventory delta and runtime traces |
| `WPM_CLASS` | `ABS_WPM_CLASS` (future optional) | Partial | `BankInfo.getWPMClass()` dual-read | Validate support UI save/load and rollout strategy |
| `START_GAME_PROCESSOR` | `ABS_START_GAME_PROCESSOR` (future optional) | Partial | `BankInfo.getStartGameProcessorClass()` dual-read | Validate restart + processor loading with alias key |
| `CLOSE_GAME_PROCESSOR` | `ABS_CLOSE_GAME_PROCESSOR` (future optional) | Partial | `BankInfo.getCloseGameProcessorClass()` dual-read | Validate restart + processor loading with alias key |
| `MQ_*` bank keys (selected) | `ABS_*` key aliases | Partial (selected keys) | `BankInfo` alias accessors | Expand alias coverage based on GS/MP runtime usage inventory |
| `MQ_*` template payload keys | `ABS_*` payload keys | Partial | JSP dual fields + GS/MP stakes/start-bonus dual keys | Continue migrating remaining keys and verify client/runtime behavior |

## Controlled Next Waves

### RN3 (Code Compatibility Completion)
- Scope:
  - Extend compatibility class loading to remaining runtime `Class.forName(...)` hotspots (GS + MP high-risk list)
  - Keep behavior backward compatible
- Required validations:
  - startup smoke
  - `/startgame` launch
  - wallet wager/refund path
  - multiplayer entry path

### RN4 (Config Key Aliasing for Class Strings)
- Scope:
  - Add dual-read aliases for runtime class config keys (`WPM_CLASS`, `START_GAME_PROCESSOR`, `CLOSE_GAME_PROCESSOR`)
  - Keep legacy keys as source-of-truth until all environments are converted
- Required validations:
  - support portal config save/load
  - bank template cloning
  - restart + processor loading

### RN5 (`MQ_*` Runtime Contract Migration)
- Scope:
  - Add dual-field support for GS->MP runtime payload where `MQ_*` keys are consumed
  - Migrate templates/config in controlled sequence
- Required validations:
  - lobby enter flow
  - multiplayer room flow
  - weapon/client-log/background-loading behaviors

### RN6 (Decommission)
- Scope:
  - remove fallback only after evidence proves all environments use new names
  - keep emergency rollback path and compatibility toggle until sign-off

## No-Go Rules
- No global find/replace of `com.dgphoenix` or `MQ*`
- No compatibility removal in same wave as first migration write
- No runtime template key deletion without MP-side proof

## Evidence and Tooling
- Inventory docs:
  - `docs/phase9/runtime-naming-cleanup/05-runtime-class-string-inventory.md`
  - `docs/phase9/runtime-naming-cleanup/06-runtime-config-template-script-inventory.md`
- Raw evidence:
  - `docs/phase9/runtime-naming-cleanup/evidence/20260225-class_refs.txt`
  - `docs/phase9/runtime-naming-cleanup/evidence/20260225-mq_refs.txt`
  - `docs/phase9/runtime-naming-cleanup/evidence/20260225-phase9_map_refs.txt`
- Repeatable scan command:
  - `gs-server/deploy/scripts/phase9-runtime-naming-inventory.sh`
