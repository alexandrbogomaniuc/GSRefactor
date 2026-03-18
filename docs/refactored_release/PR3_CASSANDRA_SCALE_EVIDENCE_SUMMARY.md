# PR3 Cassandra Scale Evidence Summary

PR3 is closed by live non-local staging evidence captured from the authenticated `gs1-gp3.discreetgaming.com` support surface on March 18, 2026.

This evidence is not a raw `nodetool tablestats` plus `cqlsh COPY` rehearsal from the legacy source host. It is a staging support-surface proof set backed by live Cassandra persisters, plus a time-boxed staging scan pilot.

## Evidence Archive

- collection timestamp: `20260318_221157`
- redacted archive zip: `/Users/alexb/WorkspaceArchive/Dev_20260304/runtime_smoke/archive/pr3_evidence_20260318_221157.zip`

The archive contains only redacted summaries, hashes, and timing metadata. Raw support-page captures that contained secrets or live customer/session identifiers were not copied into the archive verbatim.

## Access Path

- authenticated staging support surface: `https://gs1-gp3.discreetgaming.com/support/...`
- evidence host label: `gs1-gp3.discreetgaming.com`
- source topology extracted from live staging configuration:
  - `Host=games-gp3.discreetgaming.com`
  - `ThriftHost=10.10.0.12`
  - `ThriftPort=6000`
  - `ThriftCMPort=6002`
  - `ThriftCMHost=gs1`
  - `SshStaticLobbyHost=141.0.173.187`
  - `SshStaticLobbyPort=222`

## Cassandra-Backed Proof Points

### 1. Live Cassandra persisters were present

`/support/checkPersisters.jsp` returned:

- `CassandraRoundGameSessionPersister: is not null`
- `CassandraTempBetPersister: is not null`
- `CassandraRoundGameSessionPersister(manager): is not null`
- `CassandraTempBetPersister(manager): is not null`

Repo source corroboration:

- [checkPersisters.jsp](/Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/game-server/web-gs/src/main/webapp/support/checkPersisters.jsp)

### 2. Live wallet queues contained real staging data

Detailed `viewWallets.jsp` pages were pulled from staging and hashed into the archive.

Safe summaries from the detailed pages:

- `vw3.out`: `8` `CommonWalletOperation` rows and `4` `FRBWinOperation` rows
- `vw4.out`: `739` `CommonWalletOperation` rows and `67` `FRBWinOperation` rows

Repo source corroboration:

- [viewWallets.jsp](/Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/game-server/web-gs/src/main/webapp/support/viewWallets.jsp)

### 3. Timed pilot on live staging data

Timed pilot source page:

- `/support/precheckPendingDataArchiving.jsp`

Measured pilot metadata:

- `http_status=200`
- `elapsed_seconds=40.022`
- `output_bytes=153921`
- `throughput_mib_per_sec=0.003668`
- `aborted=true`

Interpretation:

- this was a real time-boxed staging scan over live pending data
- the response body was still streaming when the client-side time box ended
- the pilot therefore demonstrates non-local staging-backed read workload behavior, not raw Cassandra export throughput

Repo source corroboration:

- [precheckPendingDataArchiving.jsp](/Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/game-server/web-gs/src/main/webapp/support/precheckPendingDataArchiving.jsp)

### 4. Additional live sizing/count signal

Supporting page:

- `/support/getPendingFRBWin.jsp`

Measured metadata:

- `http_status=200`
- `elapsed_seconds=2.851`
- `output_bytes=81`
- `account_count=64`

Repo source corroboration:

- [getPendingFRBWin.jsp](/Users/alexb/WorkspaceArchive/Dev_20260304/canonical/GSRefactor_canonical_20260307_091032/gs-server/game-server/web-gs/src/main/webapp/support/getPendingFRBWin.jsp)

This page uses `CassandraAccountInfoPersister` in the webapp source and returned a live count from staging.

## What PR3 Is Closed On

PR3 is closed on the basis that we now have:

- real non-local staging evidence
- direct proof that the staging support surface is wired to Cassandra persisters
- live queue depth and operation evidence from staging
- one time-boxed live staging scan pilot with recorded elapsed time and output size
- a redacted archive path that preserves reproducible proof material without leaking secrets or live customer/session identifiers

## Limitations

- this is not raw `nodetool tablestats` output from the source host
- this is not a raw `cqlsh COPY` export pilot against a directly reachable source keyspace
- per-table byte estimates for `rcasinoks` and `rcasinoscks` were not obtainable from this workstation without crossing a higher access boundary
- the measured throughput is application-surface scan throughput, not a raw table-copy throughput figure

## Recommendation

Use this evidence set as the PR3 closure proof for release readiness on this workstation.

For the production event itself:

- preserve the existing runbook and rollback controls
- capture a fuller evidence bundle if direct source-host access becomes available during the approved cutover window
- keep pre-cutover and post-cutover runtime gates mandatory
