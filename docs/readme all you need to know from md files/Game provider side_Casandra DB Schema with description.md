This schema represents a high-scale iGaming Platform (likely a Slot or Casino system). It is split into two distinct logical domains:RCasinoKS (Game Engine): Handles the high-speed "hot" data—active game sessions, spins, bets, and round history.RCasinoSCKS (Platform Core): Handles the "cold/warm" data—player wallets, banking configurations, promotions, and immutable history.Since Cassandra is a NoSQL database, there are no "hard" Foreign Keys. Instead, the logic relies on Logical Keys (like gsid, accid, roundid) that thread through different tables.1. The Logic Flow: "Life of a Spin"To understand the tables, it helps to see how they interact during a user's gameplay.Phase A: The Setup (Login & Session Start)User Logs In: The system checks rcasinoscks.accountcf (Player Profile) and rcasinoscks.bankinfocf (Casino Operator/Tenant settings).Game Load: When a game launches, a new session is created in rcasinoks.gamesessioncf.Key Logic: This table is the "Source of Truth" for the active session. It tracks the st (Start Time), accid (Player), bid (Bank/Operator), and current balance.Phase B: The Gameplay (The Spin)When a player presses "Spin":Record the Round: A new entry is written to rcasinoks.roundgamesessioncf.Logic: This links the specific "Spin" (round_id) to the "Session" (game_sid).Place the Bet: The bet details go into rcasinoks.betcf.Determine Result (RNG): The game engine calculates the win/loss.Save Game State: The visual result (e.g., "Lemon, Lemon, Cherry") is saved in rcasinoks.wopcf (Win/Operation).Columns jcn & scn: These likely stand for JSON Content and Serialized Content (Blob). They store the exact state of the screen so the round can be replayed later for audits or "Instant Replay" features.Update Stats: The system updates counters in rcasinoks.gamesessioncf (e.g., increment bets, update income, payout).Phase C: The Aftermath (History & Wallet)History: The round is archived into rcasinoks.battlegroundhistory (sorted by time) so the user can see "My Last 10 Rounds".Wallet: If money moves, it logs to rcasinoscks.paymenttransactioncf2.2. Deep Dive: Table ResponsibilitiesA. The "Game Engine" Keyspace (RCasinoKS)Table NamePurposeKey RelationsgamesessioncfThe Master Table. Stores the current state of every active game session.gsid (Primary Key), accid (Player), bid (Bank)roundgamesessioncfMaps specific Rounds to a Session. Used to "list all spins in this session."game_sid → gamesessioncf.gsidbetcfStores the specific financial details of the wager (how much was bet).sid (Session/Round ID)wopcfStores the Result/Win. jcn/scn columns hold the raw game data (e.g., reel positions).roundid, gamesessidgamesessioncf_agMaterialized View. "Active Games." Allows querying sessions by Account ID instead of Session ID.accid → gamesessioncf.accidgamesessioncf_agmMaterialized View. "Active Game Mode." Same as above, but allows filtering by "Mode" (e.g., Real Money vs. Demo Mode).accid, modebattlegroundhistoryPlayer History. Time-series data of every round a player played. Optimized for "Show my history" UI.accountid, roundidhttpcallinfocfAudit Log. Logs every HTTP request between the Game Client and Server for debugging.gamesessionid, roundidB. The "Platform" Keyspace (RCasinoSCKS)Table NamePurposeKey RelationsaccountcfCore Player Data. (Name, status, etc.).key = accidbankinfocfTenant/Operator Config. Stores settings for the "Bank" (the casino site the player is using).key = bidbonuscf / frbonuscfBonuses. FRB stands for Free Round Bonus. Stores free spins awarded to players.bonusid, frbonusidpromocampaigncfPromotions. Stores metadata about active marketing campaigns.campiddepositscfLogs deposits made into the session (user adds money mid-game).sessidcurrencyratescfFX Rates. Used if a player plays in EUR but the game base currency is USD.source, dest3. Expected Logic & "Hidden" RelationshipsSince you cannot use SQL JOINs, the application logic must perform "Client-Side Joins" or "Chained Lookups."Scenario: "Calculate Player's Total Loss for the Day"Step 1: Query gamesessioncf_ag (by accid + day) to find all gsid (Session IDs) for that player today.Step 2: For each gsid, query gamesessioncf to read the income (Bets) and payout (Wins).Step 3: Sum them up in the application code: Total Loss = Sum(Income - Payout).Scenario: "Replay a Round" (e.g., User claims they won but game crashed)Step 1: Support Agent searches battlegroundhistory by accountid and time. Gets roundid.Step 2: Query wopcf using roundid.Step 3: Retrieve the jcn (JSON) blob.Step 4: The Game Client re-renders the screen using that JSON, showing exactly what happened.4. Why is the schema designed this way?_ag, _agm, _bag tables: These are duplicates of the main session table. In Cassandra, you cannot query by "Account ID" if the table is keyed by "Session ID." So, they duplicate the data 3-4 times to allow different search patterns (Search by Player, Search by Bank, Search by Mode).scn / jcn blobs: iGaming requires strict audit trails. Storing the exact binary state of the game (scn) ensures that even if the game code changes later, the historical proof of that spin remains 100% reproducible.httpcallissues / statistics: These are distinct from game logic. They are likely used by DevOps to monitor latency or API failures without slowing down the main game engine.This design is a textbook High-Throughput / Low-Latency architecture optimized for writing thousands of spins per second while keeping audit data safe.

Project RCasino_Full {
  database_type: 'Cassandra'
  Note: 'Full Schema: RCasinoKS (Gameplay) & RCasinoSCKS (Platform)'
}

// =======================================================
// GROUP: CORE PLATFORM (Accounts, Banks, Wallet)
// =======================================================
TableGroup Platform_Core {
  rcasinoscks_accountcf
  rcasinoscks_bankinfocf
  rcasinoscks_extaccountcf
  rcasinoscks_currencycf
  rcasinoscks_currencyratescf
  rcasinoscks_depositscf
  rcasinoscks_paymenttransactioncf2
  rcasinoscks_playeraliascf
}

Table rcasinoscks_accountcf {
  key bigint [pk, note: 'Player Account ID']
  jcn text
  scn blob
}

Table rcasinoscks_bankinfocf {
  key bigint [pk, note: 'Bank/Tenant ID']
  jcn text
  scn blob
}

Table rcasinoscks_extaccountcf {
  bankid bigint [pk]
  externalid text [pk]
  properties map
}

Table rcasinoscks_currencycf {
  key text [pk, note: 'Currency Code']
  jcn text
  scn blob
}

Table rcasinoscks_currencyratescf {
  source text [pk]
  dest text [pk]
  crate double
  update_date bigint
}

Table rcasinoscks_depositscf {
  sessid text [pk]
  amount bigint
}

Table rcasinoscks_paymenttransactioncf2 {
  bucket int [pk]
  startdate bigint [pk]
  key bigint [pk]
  extid text
  transactionid bigint
  jcn text
  scn blob
}

Table rcasinoscks_playeraliascf {
  ntid bigint [pk]
  pa text [pk]
  ap bigint [pk]
}

// =======================================================
// GROUP: GAME SESSIONS (The "Heart" of the system)
// =======================================================
TableGroup Game_Sessions {
  rcasinoks_gamesessioncf
  rcasinoks_roundgamesessioncf
  rcasinoks_betcf
  rcasinoks_wopcf
  rcasinoks_gamesessioncf_ag
  rcasinoks_gamesessioncf_agm
  rcasinoks_gamesessioncf_bag_idx
}

Table rcasinoks_gamesessioncf {
  gsid bigint [pk, note: 'Game Session ID']
  accid bigint
  bid bigint
  gameid bigint
  day bigint
  extid text
  payout bigint
  income bigint
  curr text
  promoids list
  real boolean
  st bigint
  et bigint
}

Table rcasinoks_roundgamesessioncf {
  round_id bigint [pk]
  game_sid bigint [pk]
  account_id bigint
  game_id bigint
  write_time bigint
}

Table rcasinoks_betcf {
  sid bigint [pk]
  jcn text
  scn blob
}

Table rcasinoks_wopcf {
  key bigint [pk]
  day bigint
  gamesessid bigint
  roundid bigint
  jcn text
  scn blob
}

// Materialized Views (Denormalized Session Data)
Table rcasinoks_gamesessioncf_ag {
  accid bigint [pk]
  gameid bigint [pk]
  et bigint [pk]
  gsid bigint
}

Table rcasinoks_gamesessioncf_agm {
  accid bigint [pk]
  mode int [pk]
  gameid bigint [pk]
  et bigint [pk]
  gsid bigint
}

Table rcasinoks_gamesessioncf_bag_idx {
  bid bigint [pk]
  gameid bigint [pk]
  et bigint [pk]
  accid bigint [pk]
  gsid bigint
}

// =======================================================
// GROUP: HISTORY & LOGS
// =======================================================
TableGroup History_Logs {
  rcasinoks_battlegroundhistory
  rcasinoks_playersessionhistorycf
  rcasinoks_httpcallinfocf
  rcasinoks_httpcallissues
  rcasinoks_httpcallstatistics
  rcasinoks_clientgamestatisticsinfocf
  rcasinoks_metricscf
  rcasinoks_metricsstatcf
  rcasinoks_historytokencf
}

Table rcasinoks_battlegroundhistory {
  accountid bigint [pk]
  datetime bigint [pk]
  gameid int
  gamesessionid bigint
  roundid bigint
  jcn text
  scn blob
}

Table rcasinoks_playersessionhistorycf {
  key text [pk]
  day bigint
  extsid text
  jcn text
  scn blob
}

Table rcasinoks_httpcallinfocf {
  callid text [pk]
  timestamp bigint [pk]
  externalid text
  gamesessionid bigint
  roundid bigint
  transactionid bigint
  tkn text
}

Table rcasinoks_httpcallissues {
  date text [pk]
  url text [pk]
  failcount bigint
  successcount bigint
}

Table rcasinoks_httpcallstatistics {
  date text [pk]
  url text [pk]
  failedcount counter
  successcount counter
}

Table rcasinoks_clientgamestatisticsinfocf {
  key bigint [pk]
  browserinfo blob
  gameclientinfo blob
}

Table rcasinoks_metricscf {
  metricid int [pk]
  gameserverid int [pk]
  logtime bigint [pk]
  metricvalue bigint
}

Table rcasinoks_metricsstatcf {
  metricid int [pk]
  gameserverid int [pk]
  stattime bigint [pk]
  averagevalue bigint
  maxvalue bigint
  minvalue bigint
}

Table rcasinoks_historytokencf {
  historytoken text [pk]
  exptime bigint
  roundid bigint
}

// =======================================================
// GROUP: PROMOTIONS & BONUSES
// =======================================================
TableGroup Promotions {
  rcasinoscks_promocampaigncf
  rcasinoscks_promocampaignmembercf
  rcasinoscks_promocampaignstat
  rcasinoscks_bonuscf
  rcasinoscks_frbonuscf
  rcasinoks_promowincf
  rcasinoks_frbwincf
  rcasinoks_bonusarchcf
  rcasinoks_frbonusarchcf
  rcasinoscks_massawardcf
}

Table rcasinoscks_promocampaigncf {
  campid bigint [pk]
  campdata blob
  jcn text
}

Table rcasinoscks_promocampaignmembercf {
  campid bigint [pk]
  accid bigint [pk]
  memdata blob
}

Table rcasinoscks_promocampaignstat {
  campaignid bigint [pk]
  gsid int [pk]
  betsum double
  roundscount int
}

Table rcasinoscks_bonuscf {
  bonusid bigint [pk]
  expdate bigint
  extbonusid text
  jcn text
  scn blob
}

Table rcasinoscks_frbonuscf {
  frbonusid bigint [pk]
  expdate bigint
  extfrbonusid text
  jcn text
  scn blob
}

Table rcasinoks_promowincf {
  promoid bigint [pk]
  timewin bigint [pk]
  accountid bigint
  amount bigint
  bankid bigint
  gameid bigint
  gamesessionid bigint
}

Table rcasinoks_frbwincf {
  key bigint [pk]
  accid bigint
  gamesessid bigint
  jcn text
}

Table rcasinoks_bonusarchcf {
  accid bigint [pk]
  awardtime bigint [pk]
  bonusid bigint
  statusid int
}

Table rcasinoks_frbonusarchcf {
  accid bigint [pk]
  awardtime bigint [pk]
  frbonusid bigint
  statusid int
}

Table rcasinoscks_massawardcf {
  key bigint [pk]
  jcn text
  scn blob
}

// =======================================================
// GROUP: TOURNAMENTS & BATTLEGROUNDS
// =======================================================
TableGroup Tournaments {
  rcasinoks_tournamentpromosumfeedcf
  rcasinoscks_tournamenthistorycf
  rcasinoscks_tournamenticoncf
  rcasinoks_battlegroundparticipantroundhistory
  rcasinoks_battlegroundprivateroomsettings
  rcasinoscks_battlegroundcf
  rcasinoscks_leaderboardresult
}

Table rcasinoks_tournamentpromosumfeedcf {
  id bigint [pk]
  furl text [pk]
  tournament_id bigint
  bname text
  start_date bigint
  end_date bigint
}

Table rcasinoscks_tournamenthistorycf {
  id bigint [pk]
  t int [pk]
  jcn text
}

Table rcasinoscks_tournamenticoncf {
  id bigint [pk]
  ha text
  n text
}

Table rcasinoks_battlegroundparticipantroundhistory {
  sid text [pk]
  roundid bigint [pk]
  accountids set
  gamesessionid bigint
}

Table rcasinoks_battlegroundprivateroomsettings {
  privateroomid text [pk]
  jcn text
}

Table rcasinoscks_battlegroundcf {
  bankid bigint [pk]
  gameid bigint [pk]
  jcn text
}

Table rcasinoscks_leaderboardresult {
  b bigint [pk]
  l bigint [pk]
  e bigint
  r text
  s bigint
}

// =======================================================
// RELATIONSHIPS (Inferred from schema logic)
// =======================================================

// --- ACCOUNT & BANK CONNECTIONS ---
// Linking sessions to the player account
Ref: rcasinoscks_accountcf.key < rcasinoks_gamesessioncf.accid
Ref: rcasinoscks_accountcf.key < rcasinoks_gamesessioncf_ag.accid
Ref: rcasinoscks_accountcf.key < rcasinoks_battlegroundhistory.accountid
Ref: rcasinoscks_accountcf.key < rcasinoks_bonusarchcf.accid

// Linking sessions to the bank (tenant)
Ref: rcasinoscks_bankinfocf.key < rcasinoks_gamesessioncf.bid
Ref: rcasinoscks_bankinfocf.key < rcasinoscks_extaccountcf.bankid
Ref: rcasinoscks_bankinfocf.key < rcasinoks_promowincf.bankid

// --- GAME SESSION CONNECTIONS ---
// The Game Session ID (gsid) is the central key
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_roundgamesessioncf.game_sid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_wopcf.gamesessid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_gamesessioncf_ag.gsid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_gamesessioncf_agm.gsid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_gamesessioncf_bag_idx.gsid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_battlegroundhistory.gamesessionid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_httpcallinfocf.gamesessionid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_promowincf.gamesessionid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_frbwincf.gamesessid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoks_battlegroundparticipantroundhistory.gamesessionid
Ref: rcasinoks_gamesessioncf.gsid < rcasinoscks_promocampaignstat.gsid

// --- ROUND CONNECTIONS ---
Ref: rcasinoks_roundgamesessioncf.round_id < rcasinoks_wopcf.roundid
Ref: rcasinoks_roundgamesessioncf.round_id < rcasinoks_battlegroundhistory.roundid
Ref: rcasinoks_roundgamesessioncf.round_id < rcasinoks_httpcallinfocf.roundid
Ref: rcasinoks_roundgamesessioncf.round_id < rcasinoks_historytokencf.roundid
Ref: rcasinoks_roundgamesessioncf.round_id < rcasinoks_battlegroundparticipantroundhistory.roundid

// --- PROMO & BONUS CONNECTIONS ---
Ref: rcasinoscks_promocampaigncf.campid < rcasinoscks_promocampaignmembercf.campid
Ref: rcasinoscks_promocampaigncf.campid < rcasinoscks_promocampaignstat.campaignid
Ref: rcasinoscks_bonuscf.bonusid < rcasinoks_bonusarchcf.bonusid
Ref: rcasinoscks_frbonuscf.frbonusid < rcasinoks_frbonusarchcf.frbonusid
