# Legacy Source Stats And Snapshot

Use this command pack on a legacy Cassandra 3.11 source host, or on a staging/prod-like clone of that source. Replace placeholders before running anything.

## A. Quick Sizing (Read-Only Safe)

Create a working folder first:

```bash
TS="$(date +%Y%m%d_%H%M%S)"
OUT_ROOT="$HOME/gsrefactor_legacy_source_$TS"
mkdir -p "$OUT_ROOT/stats" "$OUT_ROOT/schema"
```

Collect keyspace sizing and schema:

```bash
nodetool tablestats rcasinoks | tee "$OUT_ROOT/stats/rcasinoks.tablestats.txt"
nodetool tablestats rcasinoscks | tee "$OUT_ROOT/stats/rcasinoscks.tablestats.txt"
```

```bash
nodetool cfstats rcasinoks | tee "$OUT_ROOT/stats/rcasinoks.cfstats.txt"
nodetool cfstats rcasinoscks | tee "$OUT_ROOT/stats/rcasinoscks.cfstats.txt"
```

If `cfstats` is not available on the host build, keep the `tablestats` output and note that `cfstats` was unavailable.

```bash
cqlsh -e "DESCRIBE KEYSPACE rcasinoks" | tee "$OUT_ROOT/schema/rcasinoks.cql"
cqlsh -e "DESCRIBE KEYSPACE rcasinoscks" | tee "$OUT_ROOT/schema/rcasinoscks.cql"
```

## B. Snapshot (If a Representative Dataset Can Be Produced)

Choose a clear rehearsal tag:

```bash
SNAP_TAG="gsrefactor_rehearsal_YYYYMMDD"
```

Take read-only snapshots for the two source keyspaces:

```bash
nodetool snapshot -t "$SNAP_TAG" rcasinoks
nodetool snapshot -t "$SNAP_TAG" rcasinoscks
```

On Cassandra 3.11, snapshot SSTables usually live under:

```text
/var/lib/cassandra/data/<keyspace>/<table-name>-<table-uuid>/snapshots/$SNAP_TAG/
```

List the snapshot directories that were created:

```bash
find /var/lib/cassandra/data/rcasinoks -type d -path "*/snapshots/$SNAP_TAG" | sort | tee "$OUT_ROOT/stats/rcasinoks.snapshot_dirs.txt"
find /var/lib/cassandra/data/rcasinoscks -type d -path "*/snapshots/$SNAP_TAG" | sort | tee "$OUT_ROOT/stats/rcasinoscks.snapshot_dirs.txt"
```

Build tarball inputs for only the two requested keyspaces:

```bash
find /var/lib/cassandra/data/rcasinoks -type f -path "*/snapshots/$SNAP_TAG/*" | sort > "$OUT_ROOT/rcasinoks.snapshot.files.txt"
find /var/lib/cassandra/data/rcasinoscks -type f -path "*/snapshots/$SNAP_TAG/*" | sort > "$OUT_ROOT/rcasinoscks.snapshot.files.txt"
```

Create one tarball per keyspace, plus a small schema/stats bundle:

```bash
tar -czf "$OUT_ROOT/rcasinoks_${SNAP_TAG}.tgz" -T "$OUT_ROOT/rcasinoks.snapshot.files.txt"
tar -czf "$OUT_ROOT/rcasinoscks_${SNAP_TAG}.tgz" -T "$OUT_ROOT/rcasinoscks.snapshot.files.txt"
tar -czf "$OUT_ROOT/legacy_source_stats_and_schema_${SNAP_TAG}.tgz" \
  -C "$OUT_ROOT" stats schema
```

Do not include:

- commitlog files
- system keyspaces
- any secrets or unrelated host files

## C. What To Send Back

Please send back all of the following:

- `rcasinoks_${SNAP_TAG}.tgz`
- `rcasinoscks_${SNAP_TAG}.tgz`
- `legacy_source_stats_and_schema_${SNAP_TAG}.tgz`
- `tablestats` and `cfstats` outputs if they were collected
- schema dumps for `rcasinoks` and `rcasinoscks`
- approximate hardware info if possible: CPU, RAM, disk type
- start and end timestamps for any pilot copy that was run

## D. Rehearsal Success Criteria

The rehearsal is good enough to make a tool decision only if it returns:

- per-table timing evidence for the 1-2 largest tables
- source sizing evidence from `tablestats` and, if available, `cfstats`
- schema dumps for both keyspaces
- a single evidence zip path recorded in the GSRefactor proof flow, for example:
  `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_YYYYMMDD_HHMMSS/prod_scale_proof_02.zip`
