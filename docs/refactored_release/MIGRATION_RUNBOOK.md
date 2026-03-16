# Refactored Release Migration Runbook

This runbook documents the tested migration path from legacy Cassandra 3.11 to target Cassandra 5.0.6 while preserving the current application behavior.

## Tested Migration Method

The working migration proof is:

1. start legacy Cassandra 3.11
2. start target Cassandra 5.0.6
3. export schema from legacy
4. sanitize schema for target compatibility
5. import sanitized schema into target
6. copy seed/data rows into target
7. run the archiver against both clusters
8. compare row proof + archiver success classification

## Chosen Data Copy Strategy

For the current production-track release candidate, the chosen copy tool is `cqlsh COPY`.

### Why `cqlsh COPY`

- it is the only row-copy path already proven by the current PASS/PASS smoke evidence
- it fits the existing migration smoke pipeline without introducing a new operational dependency right before release
- the current proof artifacts already show schema import plus row copy success on Cassandra 5.0.6

### Tradeoff

`cqlsh COPY` is not the fastest option for very large production datasets. If throughput becomes the blocking concern later, review `DSBulk` as a separate post-release hardening task instead of changing the release-candidate path now.

## Validated Runtime Inputs

- legacy source node: `cassandra-legacy` on host port `9042`
- target node: `cassandra-target` on host port `9043`
- ZooKeeper: `zookeeper-smoke` on `2181`
- latest green status file:
  - `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/status/latest.env`

These migration-proof containers are separate from the `fullstacksmoke` Cassandra/ZooKeeper/Kafka services used by the playable web-gs rehearsal.

## Operator Procedure

### 1. Start Both Cassandra Clusters

- legacy: `cassandra:3.11`
- target: `cassandra:5.0.6`

Keep both running until migration proof and release validation are complete.

### 2. Export Authoritative Schema

Use legacy Cassandra as the authoritative source and export schema with `DESCRIBE KEYSPACE`.

### 3. Normalize Keyspace Replication

For the one-node smoke topology, keyspaces are normalized to:

```cql
{'class':'SimpleStrategy','replication_factor':1}
```

This avoids false negatives caused by production replication settings in a one-node local environment.

### 4. Sanitize Legacy Schema

The tested path sanitizes schema differences before import into Cassandra 5.x:

- normalize replication maps for one-node smoke
- sanitize compression options so target 5.x accepts the map
- keep table structure/data semantics intact

The important rule is to change compatibility metadata only, not business data semantics.

### 5. Apply Schema to Target

Apply the sanitized CQL to the target cluster before row copy.

### 6. Copy Data with `cqlsh COPY`

The current proven path copies rows after schema import and then validates:

- schema exists on target
- sample row proof matches
- archiver runs succeed on both clusters

### 7. Validate PASS/PASS

Run:

```bash
bash /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/bin/run_migration_smoke_loop.sh --once
cat /Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/status/latest.env
```

Release-candidate PASS/PASS means:

- `SCHEMA_OK=1`
- `ROWPROOF_OK=1`
- `ARCHIVER_LEGACY_OK=1`
- `ARCHIVER_TARGET_OK=1`

## Rollback Plan

Rollback does not require rewriting legacy data. The safe rollback is to repoint the application back to the legacy cluster.

### Rollback Steps

1. Keep `cassandra-legacy` running and healthy.
2. Stop only the `webgs` service.
3. Change the runtime topology so `webgs` points to legacy Cassandra instead of the target:
   - either set `-Dcassandra.hosts=<legacy-host>:9042`
   - or remap the `fullstack-cassandra` alias to the legacy node in the runtime topology
4. Start `webgs` again.
5. Re-run:
   - healthcheck `200`
   - gameplay canary `302 -> 200`

### Rollback Goal

Restore the app to the known-good legacy source of truth without deleting target data or mutating legacy state.

## What Is Proven Today

- migration PASS/PASS on fresh iteration `iter_01_20260316_081816`
- target Cassandra version `5.0.6`
- application can still boot and pass the gameplay canary after the migration guard succeeds

## What Is Not Assumed

- no assumption that local one-node replication settings equal production replication
- no assumption that target rollback should copy target data back into legacy
- no assumption that a runtime-only stack file should be promoted automatically into the repo without a separate review
