# Phase 9 ABS W0 Text Replace (apply)

- Patch-plan source: /Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-patch-plan-W0-20260226-062933.md
- Manifest: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json
- Root: /Users/alexb/Documents/Dev/Dev_new/gs-server
- Wave: W0
- Mode: apply
- File sections processed: 24
- Files changed: 18
- Total planned literal replacements (exact-case): 224
- Total applied literal replacements (exact-case): 224
- Patch-plan SHA-256: b9b4aa45b1b423d2c1102507853f03ec5bb80eab26cc3d6470d30ad31b1f7321
- Approval artifact: /Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-w0-apply-approval-20260226-063015.json
- Approval id: phase9-w0-1772087415101
- Approved by: codex
- Approved at: 2026-02-26T06:30:15.103Z
- Allowed files in artifact: 24
- Extra approved files not in patch plan: 0

## File Results

| File | Status | Planned | Applied | Changed |
|---|---|---:|---:|---|
| `game-server/web-gs/src/main/resources/ClusterConfiguration.xml` | OK | 68 | 68 | yes |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | OK | 52 | 52 | yes |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | OK | 28 | 28 | yes |
| `bitbucket-pipelines.bck2.yml` | OK | 0 | 0 | no |
| `bitbucket-pipelines.yml` | OK | 0 | 0 | no |
| `game-server/web-gs/pom.xml` | OK | 19 | 19 | yes |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | OK | 13 | 13 | yes |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | OK | 13 | 13 | yes |
| `game-server/config/common.properties` | OK | 6 | 6 | yes |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | OK | 5 | 5 | yes |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | OK | 3 | 3 | yes |
| `game-server/web-gs/src/main/webapp/free/mp/template.jsp` | OK | 0 | 0 | no |
| `game-server/web-gs/src/main/webapp/real/mp/template.jsp` | OK | 0 | 0 | no |
| `game-server/web-gs/src/main/webapp/standlobby.jsp` | OK | 4 | 4 | yes |
| `game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | OK | 2 | 2 | yes |
| `game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | OK | 2 | 2 | yes |
| `game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsTemplateCache.xml` | OK | 2 | 2 | yes |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.SubCasinoCache.xml` | OK | 2 | 2 | yes |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.SubCasinoCache.xml` | OK | 2 | 2 | yes |
| `deploy/docker/configs/docker-compose.yml` | OK | 1 | 1 | yes |
| `deploy/docker/refactor/docker-compose.yml` | OK | 1 | 1 | yes |
| `game-server/web-gs/src/main/webapp/support/configPortal.jsp` | OK | 0 | 0 | no |
| `game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml` | OK | 0 | 0 | no |
| `k8s/development_maxduel/deployment/mq-gs.yaml` | OK | 1 | 1 | yes |

## File Mapping Results: game-server/web-gs/src/main/resources/ClusterConfiguration.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 38 | 38 | 38 |
| `nucleus` | `abs` / `ABS` | 19 | 19 | 19 |
| `discreetgaming` | `abs` / `ABS` | 11 | 11 | 11 |

## File Mapping Results: game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 32 | 32 | 32 |
| `maxquest` | `abs` / `ABS` | 20 | 20 | 20 |

## File Mapping Results: game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 22 | 22 | 22 |
| `maxquest` | `abs` / `ABS` | 6 | 6 | 6 |

## File Mapping Results: bitbucket-pipelines.bck2.yml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `maxduel` | `abs` / `ABS` | 20 | 0 | 0 |

## File Mapping Results: bitbucket-pipelines.yml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `maxduel` | `abs` / `ABS` | 20 | 0 | 0 |

## File Mapping Results: game-server/web-gs/pom.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `nucleus` | `abs` / `ABS` | 18 | 19 | 19 |

## File Mapping Results: game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 9 | 9 | 9 |
| `maxquest` | `abs` / `ABS` | 4 | 4 | 4 |

## File Mapping Results: game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `maxquest` | `abs` / `ABS` | 13 | 13 | 13 |

## File Mapping Results: game-server/config/common.properties

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 7 | 3 | 3 |
| `nucleus` | `abs` / `ABS` | 3 | 3 | 3 |

## File Mapping Results: game-server/config/mpstress/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 4 | 4 | 4 |
| `maxquest` | `abs` / `ABS` | 1 | 1 | 1 |

## File Mapping Results: game-server/config/mqb/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 2 | 2 | 2 |
| `maxquest` | `abs` / `ABS` | 1 | 1 | 1 |

## File Mapping Results: game-server/web-gs/src/main/webapp/free/mp/template.jsp

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `maxquest` | `abs` / `ABS` | 3 | 0 | 0 |

## File Mapping Results: game-server/web-gs/src/main/webapp/real/mp/template.jsp

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `maxquest` | `abs` / `ABS` | 3 | 0 | 0 |

## File Mapping Results: game-server/web-gs/src/main/webapp/standlobby.jsp

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `maxquest` | `abs` / `ABS` | 3 | 4 | 4 |

## File Mapping Results: game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 1 | 1 | 1 |
| `maxquest` | `abs` / `ABS` | 1 | 1 | 1 |

## File Mapping Results: game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 1 | 1 | 1 |
| `maxquest` | `abs` / `ABS` | 1 | 1 | 1 |

## File Mapping Results: game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsTemplateCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 1 | 1 | 1 |
| `maxquest` | `abs` / `ABS` | 1 | 1 | 1 |

## File Mapping Results: game-server/config/mpstress/com.dgphoenix.casino.common.cache.SubCasinoCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 1 | 1 | 1 |
| `maxquest` | `abs` / `ABS` | 1 | 1 | 1 |

## File Mapping Results: game-server/config/mqb/com.dgphoenix.casino.common.cache.SubCasinoCache.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 1 | 1 | 1 |
| `maxquest` | `abs` / `ABS` | 1 | 1 | 1 |

## File Mapping Results: deploy/docker/configs/docker-compose.yml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 1 | 1 | 1 |

## File Mapping Results: deploy/docker/refactor/docker-compose.yml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `betsoft` | `abs` / `ABS` | 1 | 1 | 1 |

## File Mapping Results: game-server/web-gs/src/main/webapp/support/configPortal.jsp

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `maxquest` | `abs` / `ABS` | 1 | 0 | 0 |

## File Mapping Results: game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `maxquest` | `abs` / `ABS` | 1 | 0 | 0 |

## File Mapping Results: k8s/development_maxduel/deployment/mq-gs.yaml

| Legacy | Replacement | Declared Hits | Planned | Applied |
|---|---|---:|---:|---:|
| `maxduel` | `abs` / `ABS` | 1 | 1 | 1 |
