# DB Schema Baseline

## Runtime Datastore In Scope
- Cassandra container: `gp3-c1-1`

## Cassandra Keyspaces (live)
- `rcasinoscks`
- `rcasinoks`
- `mpmain`
- `mpmqb2`

## Core Cassandra Tables for CM Mapping
- `rcasinoscks.subcasinocf`
- `rcasinoscks.bankinfocf`
- `rcasinoscks.gameinfocf`
- `rcasinoscks.accountcf`
- `rcasinoscks.accountcf_ext`
- `rcasinoscks.paymenttransactioncf2`
- `rcasinoks.betcf`

## Important Cassandra Structure Notes
- Many key entities are serialized in `jcn`/`scn` blobs:
  - `subcasinocf`, `bankinfocf`, `accountcf`, `betcf`.
- Indexed lookup tables are present for CM-like search:
  - `gameinfocf` indexes: `bankidx`, `bankandcuridx`.
  - `accountcf_ext` maps `(bankid, extid) -> accountid`.
  - `paymenttransactioncf2` indexes: `extid`, `transactionid`.

## Deferred Datastores
- Casino-side SQL DB is explicitly excluded from this project phase.
- SQL integration will be planned in a later milestone after Cassandra-first CM parity.
