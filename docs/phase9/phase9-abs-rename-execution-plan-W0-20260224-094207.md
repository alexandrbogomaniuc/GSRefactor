# Phase 9 ABS Rename Execution Plan (W0)

- Source scan report: /Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-candidate-scan-20260224-093904.md
- Manifest: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json
- Wave: W0
- Plan mode: review-only (no file changes applied)
- Auto-candidate mappings: 5
- Review-only blockers with hits (excluded): 1
- Candidate top-file rows (summed): 27

## Review Checklist

- Confirm scan report uses expected wave profile (for W0, `w0_safe_targets`).
- Confirm review-only mappings (especially `mq`) remain excluded from execution plan.
- Review top files per mapping for external samples/secrets/generated artifacts before editing.
- Apply replacements in isolated commit(s) by mapping or file group, then rerun verification suite.

## Candidate Mappings (Auto-Candidate Only)

| Legacy | Replacement | Hits | Files |
|---|---|---:|---:|
| `betsoft` | `abs` / `ABS` | 121 | 14 |
| `nucleus` | `abs` / `ABS` | 40 | 3 |
| `maxquest` | `abs` / `ABS` | 69 | 17 |
| `maxduel` | `abs` / `ABS` | 106 | 3 |
| `discreetgaming` | `abs` / `ABS` | 11 | 1 |

## Excluded Review-Only Mappings With Hits

| Legacy | Hits | Files | Disposition |
|---|---:|---:|---|
| `mq` | 285 | 63 | REVIEW_ONLY_HIT |

## File Shortlist: betsoft -> abs

| File | Hits | Suggested Action |
|---|---:|---|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/ClusterConfiguration.xml` | 38 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 32 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 22 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 9 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/common.properties` | 7 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | 4 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | 2 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/docker-compose.yml` | 1 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml` | 1 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 1 | review-and-replace-nonruntime-string |

## File Shortlist: nucleus -> abs

| File | Hits | Suggested Action |
|---|---:|---|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/ClusterConfiguration.xml` | 19 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/pom.xml` | 18 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/common.properties` | 3 | review-and-replace-nonruntime-string |

## File Shortlist: maxquest -> abs

| File | Hits | Suggested Action |
|---|---:|---|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 20 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 13 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines.yml` | 10 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 6 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 4 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/standlobby.jsp` | 3 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/free/mp/template.jsp` | 2 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/real/mp/template.jsp` | 2 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 1 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 1 | review-and-replace-nonruntime-string |

## File Shortlist: maxduel -> abs

| File | Hits | Suggested Action |
|---|---:|---|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines.yml` | 63 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines.bck2.yml` | 42 | review-and-replace-nonruntime-string |
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/k8s/development_maxduel/deployment/mq-gs.yaml` | 1 | review-and-replace-nonruntime-string |

## File Shortlist: discreetgaming -> abs

| File | Hits | Suggested Action |
|---|---:|---|
| `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/ClusterConfiguration.xml` | 11 | review-and-replace-nonruntime-string |
