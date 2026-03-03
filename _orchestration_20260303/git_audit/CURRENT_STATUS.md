# CURRENT_STATUS

- current batch label: CURRENT-TRUTH-REFRESH
- branch: codex/phasee-stabilization-20260303
- HEAD commit: f592354bba42f76b8f2558d7d6f1aa0005c1fd0e
- parent commit: 7980b8693cf99bc648fd7002f4e96ef8b89050a2
- concise purpose of the batch: operationally clean local audit clutter with local excludes and refresh canonical GS/MP validation plus exact scan truth from current pushed HEAD before any new refactoring.

## Exact validation commands
- cd /Users/alexb/Documents/Dev/Dev_new/gs-server && mvn -s game-server/build/build-settings.xml -pl game-server/web-gs -am -Dcluster.properties=local/local-machine.properties -DskipTests clean compile
- cd /Users/alexb/Documents/Dev/Dev_new/gs-server && mvn -s game-server/build/build-settings.xml -pl game-server/web-gs -am -Dcluster.properties=local/local-machine.properties test -DskipITs
- cd /Users/alexb/Documents/Dev/Dev_new/mp-server && mvn -s config/settings.xml -pl persistance -am -DskipTests compile
- cd /Users/alexb/Documents/Dev/Dev_new/mp-server && mvn -s config/settings.xml -pl persistance -am test
- rg -n 'com\.dgphoenix' gs-server mp-server --glob '!**/target/**' --glob '!**/.git/**' --glob '!**/_orchestration_20260303/**'
- rg -n 'maxquest|betsoft|canex|mqbase|MQ_|mq_' gs-server mp-server --glob '!**/target/**' --glob '!**/.git/**' --glob '!**/_orchestration_20260303/**'
- rg -n 'com\.datastax\.driver\.core|cassandra-driver-core|cassandra-driver-mapping|spring-data-cassandra|cassandra\.driver\.version' gs-server mp-server --glob '!**/target/**' --glob '!**/.git/**' --glob '!**/_orchestration_20260303/**'

## RC results
- GS compile rc: 124 (canonical command, bounded at 420s due persistent stall in web-gs lane)
- GS test rc: 0
- MP compile rc: 0
- MP test rc: 0

## Scan counts
- package scan count (com.dgphoenix): 788
- branding scan count (maxquest|betsoft|canex|mqbase|MQ_|mq_): 9111
- Cassandra dependency scan count: 2245

## Short blocker summary
- Canonical GS compile command stalls repeatedly in current environment when reaching web-gs lane; bounded run returned timeout rc 124.
- No source edits were applied in this step.

## Local audit hygiene note
- Local orchestration outputs are intentionally excluded via .git/info/exclude.
- _orchestration_20260303/git_audit/CURRENT_STATUS.md remains tracked and committed.
