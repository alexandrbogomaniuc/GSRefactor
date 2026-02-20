# Cluster Hosts Centralization and Portal Visibility

## Goal
- Remove hardcoded host endpoints from refactor deployment wiring.
- Keep host values in one cluster config file.
- Expose current host config in GS support portal.

## Source of truth
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`

## Sync command
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/sync-cluster-hosts.sh
```

## Generated outputs
- Docker env for refactor compose:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.cluster-hosts.env`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.env`
- Docker env for legacy compose:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/.env`
- Nginx include for static proxy:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/static/cluster-hosts.inc`
- GS portal classpath copy:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/cluster-hosts.properties`

## Portal visibility
- Main support page link:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/index.jsp`
- Config viewer page:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/clusterHosts.jsp`
- Runtime URL:
  - `http://127.0.0.1:18081/support/clusterHosts.jsp`

## Template Manager host centralization (GS)
- Removed hardcoded cluster host list from:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/templateManager/ClusterList.jsp`
- New config-driven keys in:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`
  - `TEMPLATE_MANAGER_LOCAL_CLUSTERS`
  - `TEMPLATE_MANAGER_COPY_CLUSTERS`
  - `TEMPLATE_MANAGER_LIVE_CLUSTERS`
- Value format:
  - `LABEL|URL;LABEL|URL`
- Visibility:
  - These keys are shown in the GS config portal (`/support/configPortal.jsp`) Level 1 (Cluster Hosts).

## Refactor deployment wiring updated
- Static proxy host routing now resolved from config include:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/static/games`
- Static image copies config include:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/static/Dockerfile`
- Refactor compose uses config-driven host values for GS/MP/Kafka host endpoints:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml`
- Legacy compose uses config-driven host values for GS/MP/Kafka host endpoints:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/docker-compose.yml`
- GS startup wait script uses env-driven endpoints:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/configs/gs/wait-for-cassandra-and-start.sh`
