# Runtime Config/Template/Script Inventory

Date (UTC): 2026-02-25

## Purpose
Capture non-code runtime references that control class loading, `MQ_*` behavior, and Phase 9 rename tooling gates.

## Scan Scope
- Runtime configs and templates: `xml`, `jsp`, `properties`, `json`
- Runtime scripts: `sh`, `mjs`
- Excluded docs

## Key Findings

### A) Runtime class-string references in config/templates
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml` (many class mappings)
- `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/web.xml` (servlet/filter class bindings)
- `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml` (`WPM_CLASS`)
- `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml` (`CLOSE_GAME_PROCESSOR`, `START_GAME_PROCESSOR`, `WPM_CLASS`)
- `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache.xml` (`gsClassName`)

### B) `MQ_*` keys in templates/config
- `gs-server/game-server/web-gs/src/main/webapp/real/mp/template.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/free/mp/template.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/support/EditTemplateProperty.jsp`
- `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache.xml`
- `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml`

### C) Phase 9 compatibility-map wiring in scripts/config
- `gs-server/deploy/config/phase9-abs-compatibility-map.json`
- `gs-server/deploy/scripts/phase9-abs-compatibility-map-validate.sh`
- `gs-server/deploy/scripts/phase9-abs-compatibility-map-smoke.sh`
- `gs-server/deploy/scripts/phase9-abs-rename-candidate-scan.sh`
- `gs-server/deploy/scripts/phase9-abs-rename-execution-plan.sh`
- `gs-server/deploy/scripts/phase9-abs-rename-patch-plan-export.sh`
- `gs-server/deploy/scripts/phase9-abs-rename-w0-text-replace.sh`
- `gs-server/deploy/scripts/phase5-6-local-verification-suite.sh`

## Top Priority Non-Code Targets (Next Waves)
1. `gs-server/deploy/config/phase9-abs-compatibility-map.json`
2. `gs-server/deploy/scripts/phase9-abs-rename-candidate-scan.sh`
3. `gs-server/deploy/scripts/phase9-abs-rename-w0-text-replace.sh`
4. `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
5. `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/web.xml`
6. `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
7. `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
8. `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache.xml`
9. `gs-server/game-server/web-gs/src/main/webapp/real/mp/template.jsp`
10. `gs-server/game-server/web-gs/src/main/webapp/free/mp/template.jsp`

## Evidence Files
- `docs/phase9/runtime-naming-cleanup/evidence/20260225-class_refs.txt`
- `docs/phase9/runtime-naming-cleanup/evidence/20260225-mq_refs.txt`
- `docs/phase9/runtime-naming-cleanup/evidence/20260225-phase9_map_refs.txt`

