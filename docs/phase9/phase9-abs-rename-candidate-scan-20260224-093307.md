# Phase 9 ABS Rename Candidate Scan (2026-02-24 09:33:09Z)

- Root scanned: /Users/alexb/Documents/Dev/Dev_new/gs-server
- Manifest: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json
- Wave filter: W0
- Enforce auto-apply: true
- Safe targets only: true
- Total mappings scanned: 7
- Total line hits: 632
- Total unique-file hits (sum per mapping): 101
- Filtered-out line hits (path profile): 1026
- Auto-candidate mappings: 5
- Review-only mappings with hits: 1
- Auto-apply status: BLOCKED
- Block reason: BLOCKED_REVIEW_ONLY:mq

| Legacy | Category | Wave | ReviewOnly | Line Hits | Files | Disposition |
|---|---|---|---:|---:|---:|---|
| `betsoft` | brand | W0 | no | 121 | 14 | AUTO_CANDIDATE |
| `betsoft gaming` | brand | W0 | no | 0 | 0 | NO_HIT |
| `nucleus` | brand | W0 | no | 40 | 3 | AUTO_CANDIDATE |
| `maxquest` | brand | W0 | no | 69 | 17 | AUTO_CANDIDATE |
| `maxduel` | brand | W0 | no | 106 | 3 | AUTO_CANDIDATE |
| `discreetgaming` | brand | W0 | no | 11 | 1 | AUTO_CANDIDATE |
| `mq` | token | W0 | yes | 285 | 63 | REVIEW_ONLY_HIT |

## Top Files: betsoft (AUTO_CANDIDATE)

| File | Hits |
|---|---:|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/ClusterConfiguration.xml` | 38 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 32 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 22 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 9 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/common.properties` | 7 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | 4 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | 2 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/docker-compose.yml` | 1 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml` | 1 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 1 |

## Top Files: nucleus (AUTO_CANDIDATE)

| File | Hits |
|---|---:|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/ClusterConfiguration.xml` | 19 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/pom.xml` | 18 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/common.properties` | 3 |

## Top Files: maxquest (AUTO_CANDIDATE)

| File | Hits |
|---|---:|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 20 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 13 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines.yml` | 10 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 6 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 4 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/standlobby.jsp` | 3 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/free/mp/template.jsp` | 2 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/real/mp/template.jsp` | 2 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 1 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 1 |

## Top Files: maxduel (AUTO_CANDIDATE)

| File | Hits |
|---|---:|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines.yml` | 63 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines.bck2.yml` | 42 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/k8s/development_maxduel/deployment/mq-gs.yaml` | 1 |

## Top Files: discreetgaming (AUTO_CANDIDATE)

| File | Hits |
|---|---:|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/ClusterConfiguration.xml` | 11 |

## Top Files: mq (REVIEW_ONLY_HIT)

| File | Hits |
|---|---:|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 42 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 31 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/real/mp/template.jsp` | 18 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/free/mp/template.jsp` | 14 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml` | 10 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/k8s/development_maxduel/deployment/mq-gs.yaml` | 10 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines.bck2.yml` | 8 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines.yml` | 8 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache.xml` | 8 |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BaseGameCache.xml` | 8 |
