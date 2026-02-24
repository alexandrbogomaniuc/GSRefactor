# Phase 9 ABS Wave Status Report (W0)

- Patch-plan source: /Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-patch-plan-W0-20260224-094711.md
- Blocklist source: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-wave-status-blocklist.json
- Verification suite: /Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260224-103314.md
- Wave: W0
- Patch-plan files total: 19
- Applied files (recorded): 2
- Deferred/blocked files (recorded): 17
- Uncovered files (must be 0): 0
- Orphan applied entries (not in patch-plan): 0
- Orphan blocked entries (not in patch-plan): 0
- Verification pass/fail/skip: 66/0/0
- Wave pilot status: TESTED_CONTROLLED_WAVE_COMPLETE
- Phase 9 broader status: TESTED_NO_GO_PENDING_APPROVALS_AND_WRAPPERS

## Applied Files

- bitbucket-pipelines.bck2.yml
- bitbucket-pipelines.yml

## Deferred/Blocked Files

| File | Reason Code | Reason |
|---|---|---|
| `deploy/docker/configs/docker-compose.yml` | BLOCKED_JAVA_CLASS_FQCN | Contains Java main class FQCN with brand package; wrapper/delegation wave (W3) required before rename. |
| `deploy/docker/refactor/docker-compose.yml` | BLOCKED_JAVA_CLASS_FQCN | Contains Java main class FQCN with brand package; wrapper/delegation wave (W3) required before rename. |
| `game-server/config/common.properties` | BLOCKED_BRAND_PROTOCOL_KEYS | Contains brand/API tag names and property keys used by runtime behavior and external protocol expectations. |
| `game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | BLOCKED_WALLET_URLS_AND_HOSTS | Contains wallet URLs/hostnames; requires environment alias migration approval. |
| `game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | BLOCKED_RUNTIME_CONFIG_AND_KEYS | Contains runtime config values and property names tied to legacy integration naming. |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | BLOCKED_WALLET_URLS_AND_STRESS_HOSTS | Contains wallet endpoints/hostnames with brand tokens; requires coordinated integration aliasing. |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | BLOCKED_RUNTIME_HOST_CONFIG | Contains runtime hostnames for load balancer cache; requires aliasing/migration plan. |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | BLOCKED_RUNTIME_HOST_CONFIG | Contains host/domains and MP cluster JSON payload strings used by runtime configs. |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | BLOCKED_WALLET_URLS_AND_HOSTS | Contains brand-host URLs and integration endpoints; requires alias/host migration approval. |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | BLOCKED_RUNTIME_HOST_CONFIG | Contains runtime hostnames used by environment config. |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | BLOCKED_RUNTIME_CONFIG_AND_SECRETS_KEYS | Contains runtime config names/password property keys and host values tied to existing naming. |
| `game-server/web-gs/pom.xml` | BLOCKED_BUILD_PATH_ASSET_RENAMES | References build directories/asset paths and IDs (nucleus) that require coordinated asset rename wave. |
| `game-server/web-gs/src/main/resources/ClusterConfiguration.xml` | BLOCKED_RUNTIME_ENDPOINTS_HOSTS | Contains live cluster endpoints/transferservice URLs and brand-domain hosts; renaming now may break compatibility contracts. |
| `game-server/web-gs/src/main/webapp/free/mp/template.jsp` | BLOCKED_MP_TEMPLATE_RUNTIME_KEYS | Contains runtime template fields and legacy gameplay naming used by MP/client compatibility paths. |
| `game-server/web-gs/src/main/webapp/real/mp/template.jsp` | BLOCKED_MP_TEMPLATE_RUNTIME_KEYS | Contains runtime template fields and legacy gameplay naming used by MP/client compatibility paths. |
| `game-server/web-gs/src/main/webapp/standlobby.jsp` | BLOCKED_CLIENT_VISIBLE_RUNTIME_TEMPLATE | Contains runtime JSP output/template tokens and legacy variable names; requires UI/runtime parity validation. |
| `k8s/development_maxduel/deployment/mq-gs.yaml` | BLOCKED_ENV_INFRA_NAMES_AND_MQ_TOKEN | Contains environment/project naming and mq token path segments; requires infra rename plan + explicit approval. |

## Deferred Counts By Reason

| Reason Code | Count |
|---|---:|
| BLOCKED_BRAND_PROTOCOL_KEYS | 1 |
| BLOCKED_BUILD_PATH_ASSET_RENAMES | 1 |
| BLOCKED_CLIENT_VISIBLE_RUNTIME_TEMPLATE | 1 |
| BLOCKED_ENV_INFRA_NAMES_AND_MQ_TOKEN | 1 |
| BLOCKED_JAVA_CLASS_FQCN | 2 |
| BLOCKED_MP_TEMPLATE_RUNTIME_KEYS | 2 |
| BLOCKED_RUNTIME_CONFIG_AND_KEYS | 1 |
| BLOCKED_RUNTIME_CONFIG_AND_SECRETS_KEYS | 1 |
| BLOCKED_RUNTIME_ENDPOINTS_HOSTS | 1 |
| BLOCKED_RUNTIME_HOST_CONFIG | 3 |
| BLOCKED_WALLET_URLS_AND_HOSTS | 2 |
| BLOCKED_WALLET_URLS_AND_STRESS_HOSTS | 1 |

## Blocked Mappings (Global)

| Legacy | Reason Code | Reason |
|---|---|---|
| `mq` | BLOCKED_REVIEW_ONLY_TOKEN | High collision risk token; explicit context filters and approvals required. |
| `com.dgphoenix` | BLOCKED_W3_WRAPPER_REQUIRED | Package/class rename deferred to wrapper/delegation wave (W3). |
| `dgphoenix` | BLOCKED_W3_WRAPPER_REQUIRED | Package/class rename deferred to wrapper/delegation wave (W3). |

## Gaps / Validation

- No uncovered patch-plan files.
- No orphan applied entries.
- No orphan blocked entries.
