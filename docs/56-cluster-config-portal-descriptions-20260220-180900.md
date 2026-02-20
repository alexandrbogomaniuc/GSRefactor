# Cluster Config Portal Descriptions (2026-02-20 18:09 UTC)

## What was done
- Added a dedicated description catalog for cluster host/settings keys.
- Extended `/support/clusterHosts.jsp` to show a third column `Description` for each key.
- Included new Redis/gameplay state cache settings in the description catalog.

## Files changed
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/cluster-hosts-descriptions.properties`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/clusterHosts.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`

## Result
- Operators now see what each configuration key does directly in the GS support portal, reducing misconfiguration risk.
