# Cassandra Database Schema - Complete Reference

## Connection Info
- **Container**: `gp3-c1-1`
- **Cluster**: Test Cluster
- **Version**: Cassandra 2.1.20
- **CQL Version**: 3.2.1
- **Connect**: `docker exec -it gp3-c1-1 cqlsh`

---

## ⚠️ CRITICAL: Keyspace & Table Names

| Incorrect (Won't Work) | Correct (Works) |
|----------------------|-----------------|
| ❌ `RCasinoKS.banks` | ✅ `RCasinoSCKS.bankinfocf` |
| ❌ `RCasinoSCKS.banks` | ✅ `RCasinoSCKS.bankinfocf` |

**Key Points**:
- Bank configuration is in `RCasinoSCKS.bankinfocf` (stored as JSON)
- There is NO `banks` table in any keyspace
- `RCasinoKS` contains game/session data tables (40+ tables)
- `RCasinoSCKS` contains bank configuration

---

## Keyspaces Overview

### RCasinokS ✅
**Tables**: 40+ tables for game sessions, bets, history, and statistics

**Main Categories**:
- Game Sessions: `gamesessioncf`, `roundgamesessioncf`
- Bets & Transactions: `betcf`, `shortbetinfocf3`, `wopcf`
- Bonuses: `frbwincf`, `frbonusarchcf`, `bonusarchcf`
- History: `playersessionhistorycf`, `battlegroundhistory`
- Monitoring: `httpcallstatistics`, `httpcallissues`, `support cf`

### RCasinoSCKS ✅
**Tables**: `bankinfocf` (bank/wallet configuration)

---

## 🔥 PRIMARY TABLE: RCasinoSCKS.bankinfocf

### Schema Definition
```sql
CREATE TABLE rcasinoscks.bankinfocf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800;
```

### Column Descriptions
| Column | Type | Purpose |
|--------|------|---------|
| `key` | bigint (PK) | Bank ID (e.g., 6274, 6275) |
| `jcn` | text | Complete bank configuration as JSON string |
| `scn` | blob | Serialized configuration (binary) |

### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraBankInfoPersister`
- **Location**: `cassandra-cache/common-persisters/src/main/java/`
- **Constant**: `BANK_INFO_CF = "BankInfoCF"`

### Critical JSON Properties in `jcn`
- `id`: Bank ID
- `subCasinoId`: Sub-casino identifier (507 for MaxQuest)
- `properties.COMMON_WALLET_REQUEST_CLIENT_CLASS`: ⚠️ Must be `StandartRESTCWClient` (typo)
- `properties.COMMON_WALLET_AUTH_URL`: Authentication endpoint  
- `properties.COMMON_WALLET_BALANCE_URL`: Balance endpoint
- `properties.COMMON_WALLET_WAGER_URL`: Bet endpoint
- `properties.COMMON_WALLET_REFUND_URL`: Refund endpoint
- `properties.COMMON_WALLET_AUTH_PASS`: Authentication key
- `properties.WPM_CLASS`: Wallet manager class (`CommonWalletManager`)
- `properties.CWM_TYPE`: Wallet type (e.g., `SEND_WIN_ONLY_AND_ISROUNDFINISHED`)

### Query Patterns
```cql
-- Read bank configuration
SELECT key, jcn FROM RCasinoSCKS.bankinfocf WHERE key = 6274;

-- Update bank (MUST replace entire JSON)
UPDATE RCasinoSCKS.bankinfocf 
SET jcn = '{...complete_json_here...}' 
WHERE key = 6274;
```

---

## 🎮 GAME SESSION TABLES

### gamesessioncf

**Purpose**: Stores active and completed game sessions

#### Schema Definition
```sql
CREATE TABLE rcasinoks.gamesessioncf (
    gsid bigint PRIMARY KEY,
    accid bigint,             -- Account ID
    bcr double,               -- Base Currency Rate
    bets int,                 -- Number of bets
    bid bigint,               -- Bank ID
    bonst text,               -- Bonus state
    bonus bigint,             -- Bonus amount
    bonusbet bigint,          -- Bonus bet amount
    bonuswin bigint,          -- Bonus win amount
    cltype text,              -- Client type
    contribjp blob,           -- Jackpot contribution (binary)
    contribjp_json text,      -- Jackpot contribution (JSON)
    curr text,                -- Currency code
    day bigint,               -- Day timestamp (indexed)
    dblupi bigint,            -- Double up info
    dblupp bigint,            -- Double up pointer
    dbluprc int,              -- Double up round count
    enbbalance bigint,        -- Ending bonus balance
    enterdate bigint,         -- Enter date/time
    et bigint,                -- Exit time
    extid text,               -- External user ID (indexed)
    fraction text,            -- Fraction info
    frbonst text,             -- Free round bonus state
    frbonus bigint,           -- Free round bonus
    gameid bigint,            -- Game ID
    income bigint,            -- Income amount
    lang text,                -- Language code
    lbid bigint,              -- Load balancer ID
    mdl double,               -- Model?
    nb bigint,                -- ?
    nextid bigint,            -- Next session ID (indexed)
    payout bigint,            -- Payout amount
    pcr double,               -- Player currency rate
    previd bigint,            -- Previous session ID (indexed)
    promoids list<bigint>,    -- List of promotion IDs
    rc int,                   -- Round count
    real boolean,             -- Is real money
    st bigint,                -- Start time
    stbalance bigint,         -- Starting balance
    stbbalance bigint,        -- Starting bonus balance
    unjid bigint,             -- ?
    unjsc double,             -- ?
    unjsw bigint              -- ?
) WITH default_time_to_live = 10368000;  -- TTL: 120 days

CREATE INDEX gamesessioncf_day_idx ON rcasinoks.gamesessioncf (day);
CREATE INDEX gamesessioncf_extid_idx ON rcasinoks.gamesessioncf (extid);
CREATE INDEX gamesessioncf_nextid_idx ON rcasinoks.gamesessioncf (nextid);
CREATE INDEX gamesessioncf_previd_idx ON rcasinoks.gamesessioncf (previd);
```

#### Java Classes
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister`
- **Related Tables**:
  - `GameSessionCF` - Main table
  - `GameSessionCF_AG` - Aggregation index
  - `GameSessionCF_AGM` - Aggregation monthly index
  - `GameSessionCF_BAG_idx` - Bank/Account/Game index

#### Key Features
- **TTL**: 120 days (10,368,000 seconds)
- **Indexes**: day, extid, nextid, previd
- **Use Case**: Session tracking, player history, reporting

---

### roundgamesessioncf

**Purpose**: Round-level session data for multiplayer games

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraRoundGameSessionPersister`
- **Constant**: `COLUMN_FAMILY_NAME = "RoundGameSessionCF"`

---

## 💰 BET & TRANSACTION TABLES

### betcf

**Purpose**: Individual bet records

#### Schema Definition
```sql
CREATE TABLE rcasinoks.betcf (
    sid bigint PRIMARY KEY,   -- Session ID
    jcn text,                 -- Bet details as JSON
    scn blob                  -- Bet details as binary
) WITH default_time_to_live = 10368000;  -- TTL: 120 days
```

#### Java Classes
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraBetPersister`
- **Constant**: `COLUMN_FAMILY_NAME = "BetCF"`
- **Related**: `CassandraBigStorageBetPersister` (for large bets)
- **Temp Table**: `BetCfTmp` (`CassandraTempBetPersister`)

#### Key Features
- **TTL**: 120 days
- **Storage**: Both JSON (`jcn`) and binary (`scn`) formats
- **Use Case**: Bet history, audit trails

---

### shortbetinfocf3

**Purpose**: Condensed bet information for quick queries

#### Schema Definition
```sql
CREATE TABLE rcasinoks.shortbetinfocf3 (
    bid int,                  -- Bank ID
    btime bigint,             -- Bet timestamp
    aid bigint,               -- Account ID
    jcn text,                 -- Bet summary JSON
    scn blob,                 -- Bet summary binary
    PRIMARY KEY (bid, btime, aid)
) WITH CLUSTERING ORDER BY (btime DESC, aid ASC)
    AND default_time_to_live = 259200;  -- TTL: 3 days
```

#### Key Features
- **TTL**: 3 days (259,200 seconds)
- **Clustering**: Time descending (most recent first)
- **Composite Key**: (bank, time, account)
- **Use Case**: Recent bet queries, player dashboards

---

### wopcf

**Purpose**: Wallet Operations (debits/credits)

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraWalletOperationPersister`

---

## 🎁 BONUS & PROMOTION TABLES

### frbwincf
**Purpose**: Free Round Bonus wins

### frbonusarchcf
**Purpose**: Free Round Bonus archive

#### Java Classes
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraFRBonusArchivePersister`

### bonusarchcf
**Purpose**: General bonus archive

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraBonusArchivePersister`

### promowincf
**Purpose**: Promotional wins

### tournamentpromosumfeedcf
**Purpose**: Tournament promotion summary

### dmassawardhistorycf
**Purpose**: Delayed mass award history

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraDelayedMassAwardHistoryPersister`

### dmassawardfailedsendcf
**Purpose**: Failed mass award delivery tracking

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraDelayedMassAwardFailedDeliveryPersister`

---

## 👤 PLAYER TABLES

### playersessionhistorycf
**Purpose**: Player session login/logout history

### currentplayersessionstate
**Purpose**: Active player sessions

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraCurrentPlayerSessionStatePersister`

---

## ⚔️ BATTLEGROUND/MULTIPLAYER TABLES

### battlegroundhistory
**Purpose**: Battleground game mode history

### battlegroundparticipantroundhistory  
**Purpose**: Per-player round statistics

### battlegroundprivateroomsettings
**Purpose**: Private room configurations

### mqroundkpiinfo
**Purpose**: MaxQuest round KPIs

---

## 📊 MONITORING & STATISTICS TABLES

### httpcallstatistics
**Purpose**: HTTP call performance metrics

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraCallStatisticsPersister`

### httpcallissues
**Purpose**: HTTP call errors and failures

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraCallIssuesPersister`

### httpcallinfocf
**Purpose**: Detailed HTTP call information

### metricscf
**Purpose**: System performance metrics

### metricsstatcf
**Purpose**: Metrics statistics aggregation

### clientgamestatisticsinfocf
**Purpose**: Client-side game statistics

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraClientStatisticsPersister`

---

## 🔧 OPERATIONAL TABLES

### supportcf
**Purpose**: Support ticket/error logging
- **Access**: `http://localhost:8081/support/logviewer.do`
- **Contains**: Error tickets, stack traces, diagnostics

### batchopstatus
**Purpose**: Batch operation tracking

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraBatchOperationStatusPersister`

### archivercf
**Purpose**: Archive management

#### Java Class
**Persister**: `com.dgphoenix.casino.cassandra.persist.CassandraArchiverPersister`

### concurrentnotcf
**Purpose**: Notification queue management

### laststattimecf
**Purpose**: Statistics calculation timestamps

### trackinfocf
**Purpose**: User tracking/analytics

---

## 📚 COMPLETE PERSISTER CLASS MAPPING

| Table | Java Persister Class |
|-------|---------------------|
| `bankinfocf` | `CassandraBankInfoPersister` |
| `gamesessioncf` | `CassandraGameSessionPersister` |
| `roundgamesessioncf` | `CassandraRoundGameSessionPersister` |
| `betcf` | `CassandraBetPersister` |
| `shortbetinfocf3` | (Likely handled by bet persister) |
| `wopcf` | `CassandraWalletOperationPersister` |
| `frbonusarchcf` | `CassandraFRBonusArchivePersister` |
| `bonusarchcf` | `CassandraBonusArchivePersister` |
| `dmassawardhistorycf` | `CassandraDelayedMassAwardHistoryPersister` |
| `dmassawardfailedsendcf` | `CassandraDelayedMassAwardFailedDeliveryPersister` |
| `currentplayersessionstate` | `CassandraCurrentPlayerSessionStatePersister` |
| `httpcallstatistics` | `CassandraCallStatisticsPersister` |
| `httpcallissues` | `CassandraCallIssuesPersister` |
| `clientgamestatisticsinfocf` | `CassandraClientStatisticsPersister` |
| `batchopstatus` | `CassandraBatchOperationStatusPersister` |
| `archivercf` | `CassandraArchiverPersister` |

**All persisters located in**: `cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/`

---

## Common Patterns

### Table Naming Conventions
| Suffix | Meaning | Example |
|--------|---------|---------|
| `cf` | Column Family (table) | `betcf` |
| `cf3` | Version 3 | `shortbetinfocf3` |
| `_ag` | Aggregation | `gamesessioncf_ag` |
| `_idx` | Index table | `gamesessioncf_bag_idx` |
| `arch` | Archive | `bonusarchcf` |

### Common Column Names
| Column | Type | Meaning |
|--------|------|---------|
| `jcn` | text | JSON configuration/data |
| `scn` | blob | Serialized binary data |
| `gsid` | bigint | Game session ID |
| `accid` / `aid` | bigint | Account ID |
| `bid` | bigint / int | Bank ID |
| `extid` | text | External user ID |
| `gameid` | bigint | Game ID |
| `st` | bigint | Start time (milliseconds) |
| `et` | bigint | End time (milliseconds) |

### TTL (Time To Live) Values
- **120 days** (10,368,000 sec): `gamesessioncf`, `betcf`
- **3 days** (259,200 sec): `shortbetinfocf3`
- **No TTL** (0): `bankinfocf`

---

## Troubleshooting

### "unconfigured columnfamily banks"
✅ **Fix**: Use `RCasinoSCKS.bankinfocf` not `banks`

### "unconfigured keyspace"
✅ **Fix**: Use correct keyspace names:
- `RCasinoKS` (game data)
- `RCasinoSCKS` (bank config)

### ClassNotFoundException for wallet client
✅ **Fix**: In `bankinfocf` JSON, use:
```json
"COMMON_WALLET_REQUEST_CLIENT_CLASS": "com.dgphoenix.casino.payment.wallet.client.v4.StandartRESTCWClient"
```
**Note**: "Standart" is correct (typo in codebase)

---

**Last Updated**: 2026-01-14  
**Created By**: Antigravity AI  
**Referenced in**: `AI_INSTRUCTIONS.md`
