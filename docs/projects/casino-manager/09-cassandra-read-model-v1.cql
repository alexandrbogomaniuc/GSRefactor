-- Casino Manager read-model schema (phase-1)
-- Scope: Cassandra-only, SQL deferred.

CREATE KEYSPACE IF NOT EXISTS cm_read
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

CREATE KEYSPACE IF NOT EXISTS cm_sync
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

-- playerSearch
CREATE TABLE IF NOT EXISTS cm_read.player_search_by_bank (
  bank_id int,
  account_id bigint,
  account_name text,
  account_nickname text,
  external_id text,
  subcasino_id int,
  subcasino_name text,
  bank_name text,
  account_status text,
  currency_id int,
  currency_code text,
  total_account_session_count int,
  total_bets_count bigint,
  total_income decimal,
  game_revenue decimal,
  balance decimal,
  last_platform text,
  last_login_time timestamp,
  registration_time timestamp,
  category text,
  reg_platform text,
  streamer text,
  updated_at timestamp,
  PRIMARY KEY (bank_id, account_id)
);

-- bankList
CREATE TABLE IF NOT EXISTS cm_read.bank_list_by_subcasino (
  subcasino_id int,
  bank_id int,
  internal_id int,
  external_id int,
  subcasino_name text,
  bank_name text,
  referrer text,
  last_bet timestamp,
  bank_status text,
  updated_at timestamp,
  PRIMARY KEY (subcasino_id, bank_id)
);

-- transactions
CREATE TABLE IF NOT EXISTS cm_read.transactions_by_bank_day (
  bank_id int,
  day text, -- YYYY-MM-DD UTC
  start_time timestamp,
  transaction_id bigint,
  account_id bigint,
  account_name text,
  account_nickname text,
  account_external_id text,
  subcasino_id int,
  subcasino_name text,
  bank_name text,
  type_id int,
  type_name text,
  amount decimal,
  finish_time timestamp,
  status_id int,
  status_name text,
  external_transaction_id text,
  currency_code text,
  updated_at timestamp,
  PRIMARY KEY ((bank_id, day), start_time, transaction_id)
) WITH CLUSTERING ORDER BY (start_time DESC, transaction_id ASC);

-- gameSessionSearch
CREATE TABLE IF NOT EXISTS cm_read.game_session_by_id (
  game_session_id bigint PRIMARY KEY,
  account_id bigint,
  account_name text,
  account_nickname text,
  account_external_id text,
  subcasino_id int,
  subcasino_name text,
  bank_id int,
  bank_name text,
  platform_id int,
  platform_name text,
  game_id int,
  game_title text,
  start_time timestamp,
  end_time timestamp,
  income decimal,
  payout decimal,
  bets_count int,
  gain_loss decimal,
  start_balance decimal,
  end_balance decimal,
  updated_at timestamp
);

-- walletOperationAlerts
CREATE TABLE IF NOT EXISTS cm_read.wallet_alerts_by_bank_day (
  bank_id int,
  day text, -- YYYY-MM-DD UTC
  alert_time timestamp,
  alert_id bigint,
  process text,
  nickname text,
  account_ext_id text,
  account_id bigint,
  account_name text,
  subcasino_id int,
  subcasino_name text,
  cluster_id int,
  bank_name text,
  game_id int,
  game_name text,
  amount decimal,
  currency text,
  operation_type text,
  status text,
  transaction_id bigint,
  external_transaction_id text,
  round_id bigint,
  game_session_id bigint,
  last_request text,
  updated_at timestamp,
  PRIMARY KEY ((bank_id, day), alert_time, alert_id)
) WITH CLUSTERING ORDER BY (alert_time DESC, alert_id ASC);

-- sync checkpoint and audit
CREATE TABLE IF NOT EXISTS cm_sync.checkpoints (
  job_name text PRIMARY KEY,
  checkpoint_ts timestamp,
  checkpoint_key text,
  updated_at timestamp
);

CREATE TABLE IF NOT EXISTS cm_sync.audit_runs (
  day text,
  started_at timestamp,
  run_id uuid,
  job_name text,
  finished_at timestamp,
  status text,
  rows_scanned bigint,
  rows_upserted bigint,
  rows_deleted bigint,
  lag_seconds bigint,
  error_text text,
  PRIMARY KEY (day, started_at, run_id)
) WITH CLUSTERING ORDER BY (started_at DESC, run_id ASC);
