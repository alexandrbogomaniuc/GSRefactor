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

## C. Stats + Timed Pilot (No Snapshot Export)

If snapshot export is not available, collect source sizing plus one timed pilot on the 1-2 largest tables in a staging or prod-like environment. Prefer an off-peak window if this is being run on shared infrastructure.

Create a timestamped inbox drop first:

```bash
COLLECTION_TS="$(date +%Y%m%d_%H%M%S)"
DROP_ROOT="/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/inbox/cassandra_scale/$COLLECTION_TS"
mkdir -p "$DROP_ROOT/stats" "$DROP_ROOT/schema" "$DROP_ROOT/pilot"
HOST_LABEL="$(hostname -s)"
```

Collect `tablestats` for the two source keyspaces:

```bash
nodetool tablestats rcasinoks > "$DROP_ROOT/stats/${HOST_LABEL}__rcasinoks.tablestats.txt"
nodetool tablestats rcasinoscks > "$DROP_ROOT/stats/${HOST_LABEL}__rcasinoscks.tablestats.txt"
```

If `cfstats` is available, collect that too:

```bash
nodetool cfstats rcasinoks > "$DROP_ROOT/stats/${HOST_LABEL}__rcasinoks.cfstats.txt"
nodetool cfstats rcasinoscks > "$DROP_ROOT/stats/${HOST_LABEL}__rcasinoscks.cfstats.txt"
```

Collect schema for both keyspaces:

```bash
cqlsh -e "DESCRIBE KEYSPACE rcasinoks" > "$DROP_ROOT/schema/${HOST_LABEL}__rcasinoks.cql"
cqlsh -e "DESCRIBE KEYSPACE rcasinoscks" > "$DROP_ROOT/schema/${HOST_LABEL}__rcasinoscks.cql"
```

After identifying the 1-2 largest tables from the `tablestats` output, run one time-boxed pilot per selected table. Let `COPY` run for 5-15 minutes, then stop it and record the partial output. If `timeout` is available, you can use it to stop the pilot automatically; otherwise run the `COPY` command manually, press `Ctrl-C` when the time box expires, set `ABORTED=true`, and then write the metadata block.

```bash
TABLE_KEYSPACE=<rcasinoks_or_rcasinoscks>
TABLE_NAME=<largest_table_name>
START_TS="$(date -u +%Y%m%dT%H%M%SZ)"
OUT="$DROP_ROOT/pilot/${HOST_LABEL}__${TABLE_KEYSPACE}__${TABLE_NAME}__${START_TS}.csv"
META="$DROP_ROOT/pilot/${HOST_LABEL}__${TABLE_KEYSPACE}__${TABLE_NAME}__${START_TS}.meta.txt"
PILOT_SECONDS="${PILOT_SECONDS:-600}"

START_UTC="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
START_EPOCH="$(date +%s)"
ABORTED=false
COPY_RC=0

if command -v timeout >/dev/null 2>&1; then
  timeout "$PILOT_SECONDS" cqlsh -e "COPY ${TABLE_KEYSPACE}.${TABLE_NAME} TO '${OUT}' WITH HEADER = true"
  COPY_RC=$?
  if [ "$COPY_RC" -eq 124 ] || [ "$COPY_RC" -eq 130 ]; then
    ABORTED=true
  fi
else
  echo "Run this command for 5-15 minutes, then press Ctrl-C to stop it early if needed:"
  echo "cqlsh -e \"COPY ${TABLE_KEYSPACE}.${TABLE_NAME} TO '${OUT}' WITH HEADER = true\""
fi

END_UTC="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
END_EPOCH="$(date +%s)"
ELAPSED_SECONDS="$((END_EPOCH - START_EPOCH))"
OUTPUT_BYTES="$(wc -c < "$OUT" 2>/dev/null || echo 0)"

printf "table=%s.%s\nstart_utc=%s\nend_utc=%s\nelapsed_seconds=%s\noutput_bytes=%s\naborted=%s\n" \
  "$TABLE_KEYSPACE" "$TABLE_NAME" "$START_UTC" "$END_UTC" "$ELAPSED_SECONDS" "$OUTPUT_BYTES" "$ABORTED" \
  > "$META"
```

### Remote Operator Mode (No CSV Transfer)

If operators cannot write directly into the GSRefactor inbox path, run the same collection flow on the legacy or staging node using `/tmp`. Keep the CSV local to that node, delete it after measuring `output_bytes`, and send back only the `tablestats` text, schema text, and pilot `.meta.txt` content.

```bash
COLLECTION_TS="$(date +%Y%m%d_%H%M%S)"
HOST_LABEL="$(hostname -s)"
REMOTE_ROOT="/tmp/gsrefactor_legacy_source_$COLLECTION_TS"
mkdir -p "$REMOTE_ROOT/stats" "$REMOTE_ROOT/schema" "$REMOTE_ROOT/pilot"

nodetool tablestats rcasinoks > "$REMOTE_ROOT/stats/${HOST_LABEL}__rcasinoks.tablestats.txt"
nodetool tablestats rcasinoscks > "$REMOTE_ROOT/stats/${HOST_LABEL}__rcasinoscks.tablestats.txt"
cqlsh -e "DESCRIBE KEYSPACE rcasinoks" > "$REMOTE_ROOT/schema/${HOST_LABEL}__rcasinoks.cql"
cqlsh -e "DESCRIBE KEYSPACE rcasinoscks" > "$REMOTE_ROOT/schema/${HOST_LABEL}__rcasinoscks.cql"

TABLE_KEYSPACE=<rcasinoks_or_rcasinoscks>
TABLE_NAME=<largest_table_name>
START_TS="$(date -u +%Y%m%dT%H%M%SZ)"
OUT="/tmp/${HOST_LABEL}__${TABLE_KEYSPACE}__${TABLE_NAME}__${START_TS}.csv"
META="$REMOTE_ROOT/pilot/${HOST_LABEL}__${TABLE_KEYSPACE}__${TABLE_NAME}__${START_TS}.meta.txt"
PILOT_SECONDS="${PILOT_SECONDS:-900}"

START_UTC="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
START_EPOCH="$(date +%s)"
ABORTED=false
COPY_RC=0

if command -v timeout >/dev/null 2>&1; then
  timeout "$PILOT_SECONDS" cqlsh -e "COPY ${TABLE_KEYSPACE}.${TABLE_NAME} TO '${OUT}' WITH HEADER = true"
  COPY_RC=$?
  if [ "$COPY_RC" -eq 124 ] || [ "$COPY_RC" -eq 130 ]; then
    ABORTED=true
  fi
else
  echo "Run this command for 5-15 minutes, then press Ctrl-C to stop it early if needed:"
  echo "cqlsh -e \"COPY ${TABLE_KEYSPACE}.${TABLE_NAME} TO '${OUT}' WITH HEADER = true\""
fi

END_UTC="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
END_EPOCH="$(date +%s)"
ELAPSED_SECONDS="$((END_EPOCH - START_EPOCH))"
OUTPUT_BYTES="$(wc -c < "$OUT" 2>/dev/null || echo 0)"

printf "table=%s.%s\nstart_utc=%s\nend_utc=%s\nelapsed_seconds=%s\noutput_bytes=%s\naborted=%s\n" \
  "$TABLE_KEYSPACE" "$TABLE_NAME" "$START_UTC" "$END_UTC" "$ELAPSED_SECONDS" "$OUTPUT_BYTES" "$ABORTED" \
  > "$META"

rm -f "$OUT"
```

Return methods for Remote operator mode:

- paste the `tablestats`, `DESCRIBE KEYSPACE`, and `.meta.txt` contents directly into Slack or the release ticket
- or attach a tiny `tar.gz` containing only `*.txt`, `*.cql`, and `*.meta.txt`

Do not send the CSV itself.

This path is intended to close PR3 using source sizing plus one representative timed pilot, without exporting full keyspace snapshots from production-like systems.

## D. What To Send Back

Please send back the artifacts for the option you used. For Option C, either return the full timestamped drop folder if you can write into the GSRefactor inbox path, or return only the text artifacts from Remote operator mode. Do not send the CSV.

- `rcasinoks_${SNAP_TAG}.tgz`
- `rcasinoscks_${SNAP_TAG}.tgz`
- `legacy_source_stats_and_schema_${SNAP_TAG}.tgz`
- `tablestats` and `cfstats` outputs if they were collected
- schema dumps for `rcasinoks` and `rcasinoscks`
- approximate hardware info if possible: CPU, RAM, disk type
- start and end timestamps for any pilot copy that was run
- `elapsed_seconds`, `output_bytes`, and `aborted=true/false` in each pilot `.meta.txt`
- for Remote operator mode, either paste the text outputs into Slack/ticket or attach a tiny `tar.gz` containing only `*.txt`, `*.cql`, and `*.meta.txt`

For the stats + timed pilot path, return the full timestamped drop folder under:

- `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/inbox/cassandra_scale/<collection_ts>/`

## E. Rehearsal Success Criteria

The rehearsal is good enough to make a tool decision only if it returns:

- per-table timing evidence for the 1-2 largest tables
- source sizing evidence from `tablestats` and, if available, `cfstats`
- schema dumps for both keyspaces
- a single evidence zip path recorded in the GSRefactor proof flow, for example:
  `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive_YYYYMMDD_HHMMSS/prod_scale_proof_02.zip`
