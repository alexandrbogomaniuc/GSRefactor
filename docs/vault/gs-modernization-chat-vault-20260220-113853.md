# GS Modernization Chat Vault Record

- Recorded at (UTC): 2026-02-20 11:38:00
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Scope: GS modernization thread decisions, constraints, and implemented outcomes.

## User Directives Captured
- Keep `/Users/alexb/Documents/Dev` unchanged; only modify isolated `/Users/alexb/Documents/Dev/Dev_new`.
- Keep current Docker runtime operational; build new `refactor` container group for modernization work.
- Push/carry `Dev_new` as modernization source (empty `GSRefactor` repo bootstrap requirement noted).
- Focus architecture on GS only; preserve backward compatibility and no big-bang rewrite.
- Introduce clean launch endpoint naming (`/startgame` replacing public `cw*.do` style) without browser-visible redirects.
- Rename folder intent around MQ naming; GS scope only.
- Keep communication minimal and execution-first.
- New requirement: all hosts must be non-hardcoded, moved to separate cluster config, visible in GS config portal.

## Architecture / Product Decisions Captured
- Strangler migration with rollback gates and canary rollout remained mandatory.
- Legacy protocol compatibility retained (Casino Side, MP/client, New Games).
- Bank-level protocol mode (JSON/XML) planned via adapter boundary.
- Kafka remained event/control backbone target.
- Redis state-blob + deterministic functional math guidance accepted as useful (documented as ADR direction).

## Implemented Work in This Thread
- Added clean launch alias endpoint `/startgame` at static proxy boundary.
- Enforced no browser-visible redirect behavior on launch routes:
  - internal redirect follow for 301/302/303/307/308;
  - location-header suppression on launch paths.
- Expanded coverage to launch routes:
  - `/startgame`, `/cwstartgamev2.do`, `/cwstartgame.do`, `/bsstartgame.do`, `/btbstartgame.do`.
- Integrated `/startgame` into Phase 0 parity harness as `P0-LA-03`.
- Captured multiple parity reports (dry-run and run-mode) with artifacted request bodies.
- Identified refactor-positive launch bank fixture (`BANK_ID=271`) and updated parity default example.
- Implemented cluster host centralization:
  - source config: `gs-server/deploy/config/cluster-hosts.properties`.
  - sync script: `gs-server/deploy/scripts/sync-cluster-hosts.sh`.
  - generated compose env + nginx include + classpath copy.
- Rewired refactor runtime host usage:
  - static nginx routes now use generated host config include (no hardcoded GS backend host).
  - refactor compose host settings parameterized via generated `.env`.
  - GS wait-for dependency script now env-driven.
- Added GS portal visibility:
  - `support/clusterHosts.jsp` renders centralized keys/values.
  - linked from `support/index.jsp`.

## Key Evidence Produced
- Start endpoint no-redirect reports:
  - `docs/phase0/parity-execution/phase0-no-browser-redirect-start-endpoints-*.md`
- Parity reports and body artifacts:
  - `docs/phase0/parity-execution/phase0-parity-*.md`
  - `docs/phase0/parity-execution/P0-*.body.txt`
- Cluster host centralization doc:
  - `docs/31-cluster-hosts-centralization-and-portal-visibility.md`
- Runtime verification highlights:
  - `http://127.0.0.1:18080/startgame?...` => 200 with no redirect header leakage.
  - `http://127.0.0.1:18081/support/clusterHosts.jsp` => 200 and renders centralized host keys.

## Open Items / Next Execution Targets
- Move remaining legacy compose host wiring (`deploy/docker/configs/docker-compose.yml`) to same central config model.
- Complete positive wager/settle parity (`P0-WA-01`, `P0-SE-01`) with valid bank-specific bonus/hash fixture values.
- Continue phase-by-phase modernization deliverables with rollback evidence per phase.

## 2026-02-20 12:08 UTC - Continued Requirement Capture
- User reminder: end-state must include a user-friendly portal with explanation of all GS configuration settings for all levels.
- Implemented increment: `/support/configPortal.jsp` with 3-level model (cluster hosts, bank catalog, effective bank values) and support-menu entry.
- Constraint reaffirmed: Dev_new remains isolated implementation workspace; no main-source edits in parent Dev tree.
- Continued per user request: added Level 4 safe workflow scaffold to config portal (`draft/validate/approve/publish/rollback`) with no runtime write-path impact.
- Continued: extended Level 4 with session-persistent draft registry (`configPortalDraftStore`) for versioned operator audit in safe mode.
- Continued main project: launched first extracted microservice (`config-service`) in refactor stack with workflow API + outbox baseline on port `18072` (centralized config).
- Continued microservice path: config portal workflow now bridges to `config-service` via feature flag with automatic local fallback and operator-visible sync status.
- Continued extraction: added `session-service` microservice in refactor stack (idempotent lifecycle API + outbox) on port `18073`.
- Added versioned OpenAPI contracts for `config-service` and `session-service` under `refactor-services/contracts/openapi`.
- Added session-service canary routing controls (`SESSION_SERVICE_ROUTE_ENABLED`, `SESSION_SERVICE_CANARY_BANKS`) and rollout policy doc.
- Added temporary visual modernization dashboard (HTML + checklist JSON + progress bars + checkboxes) and linked it from support index.
- Continued with next step: added `session-service` canary routing decision endpoint and wired env controls from centralized cluster config.
- Fixed progress dashboard checklist loading with multi-context URL fallbacks.
- Added session canary routing helper script (`set-session-canary.sh`) to toggle route flag + canary banks and auto-sync env.
