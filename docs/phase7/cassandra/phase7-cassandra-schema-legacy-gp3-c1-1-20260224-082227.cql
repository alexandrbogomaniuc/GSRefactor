
CREATE KEYSPACE rcasinoscks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE rcasinoscks.frbonuscf_acc (
    accid bigint,
    frbonusid bigint,
    PRIMARY KEY (accid, frbonusid)
) WITH CLUSTERING ORDER BY (frbonusid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.mqreservednicknames (
    rnregion text,
    rnnick text,
    rnowner bigint,
    PRIMARY KEY (rnregion, rnnick)
) WITH CLUSTERING ORDER BY (rnnick ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX mqreservednicknames_rnowner_idx ON rcasinoscks.mqreservednicknames (rnowner);

CREATE TABLE rcasinoscks.penddataarchcf (
    accid bigint,
    dataname text,
    creationtime bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (accid, dataname, creationtime)
) WITH CLUSTERING ORDER BY (dataname ASC, creationtime ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.lasthand_cf (
    accountid bigint,
    gameid bigint,
    lh text,
    PRIMARY KEY (accountid, gameid)
) WITH CLUSTERING ORDER BY (gameid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.periodictaskscf (
    key text PRIMARY KEY,
    data blob,
    extime bigint
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 8640000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.depositscf (
    sessid text PRIMARY KEY,
    amount bigint
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 259200
    AND gc_grace_seconds = 86400000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.promocampaignstat (
    campaignid bigint,
    gsid int,
    betsum double,
    roundscount int,
    PRIMARY KEY (campaignid, gsid)
) WITH CLUSTERING ORDER BY (gsid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 30368000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.sequencercf (
    key text PRIMARY KEY,
    v bigint
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.frbonuswincf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.extgameids (
    bank int,
    key text,
    id bigint,
    PRIMARY KEY (bank, key)
) WITH CLUSTERING ORDER BY (key ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.dmassawardcf (
    gsid int,
    key bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (gsid, key)
) WITH CLUSTERING ORDER BY (key ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX dmassawardcf_key_idx ON rcasinoscks.dmassawardcf (key);

CREATE TABLE rcasinoscks.promocampaignbybankandgamecf (
    bankid bigint,
    gameid bigint,
    campid bigint,
    campstatus ascii,
    PRIMARY KEY (bankid, gameid, campid)
) WITH CLUSTERING ORDER BY (gameid ASC, campid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX promocampaignbybankandgamecf_campstatus_idx ON rcasinoscks.promocampaignbybankandgamecf (campstatus);

CREATE TABLE rcasinoscks.localizationscf (
    key ascii,
    lan ascii,
    it ascii,
    loc text,
    PRIMARY KEY (key, lan, it)
) WITH CLUSTERING ORDER BY (lan ASC, it ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.playeraliascf (
    ntid bigint,
    pa text,
    ap bigint,
    PRIMARY KEY (ntid, pa, ap)
) WITH CLUSTERING ORDER BY (pa ASC, ap ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 7776000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.pgscf (
    accountid bigint,
    gameid bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (accountid, gameid)
) WITH CLUSTERING ORDER BY (gameid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.bankinfocf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.currencyratesconfigcf (
    currency_name text PRIMARY KEY,
    currency_param text,
    currency_target text,
    update_period bigint
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.promocampaignarchcf (
    campid bigint PRIMARY KEY,
    campdata blob,
    jcn text
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.gsessionextprops (
    g bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.dlm_cf (
    lock_id text PRIMARY KEY,
    llocker int,
    lock_time bigint,
    locker int
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"10000"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'unchecked_tombstone_compaction': 'true', 'tombstone_compaction_interval': '3600', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 0
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX dlm_cf_locker_idx ON rcasinoscks.dlm_cf (locker);

CREATE TABLE rcasinoscks.summarytransformercf (
    id bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 5184000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.subcasinocf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.promocampaigncf (
    campid bigint PRIMARY KEY,
    campdata blob,
    jcn text
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.acc_dlm_cf (
    lock_id text PRIMARY KEY,
    llocker int,
    lock_time bigint,
    locker int
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"10000"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'unchecked_tombstone_compaction': 'true', 'tombstone_compaction_interval': '3600', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 0
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = 'ALWAYS';
CREATE INDEX acc_dlm_cf_locker_idx ON rcasinoscks.acc_dlm_cf (locker);

CREATE TABLE rcasinoscks.bonuscf (
    bonusid bigint PRIMARY KEY,
    expdate bigint,
    extbonusid text,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX bonuscf_expdate_idx ON rcasinoscks.bonuscf (expdate);
CREATE INDEX bonuscf_extbonusid_idx ON rcasinoscks.bonuscf (extbonusid);

CREATE TABLE rcasinoscks.tournamenthistorycf (
    id bigint,
    t int,
    jcn text,
    scn blob,
    PRIMARY KEY (id, t)
) WITH CLUSTERING ORDER BY (t ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 5184000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.leaderboardresult (
    b bigint,
    l bigint,
    e bigint,
    r text,
    s bigint,
    PRIMARY KEY (b, l)
) WITH CLUSTERING ORDER BY (l ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX leaderboardresult_e_idx ON rcasinoscks.leaderboardresult (e);

CREATE TABLE rcasinoscks.promotmaxbalancecf (
    cid bigint,
    aid bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (cid, aid)
) WITH CLUSTERING ORDER BY (aid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.extaccountcf (
    bankid bigint,
    externalid text,
    properties map<text, text>,
    PRIMARY KEY ((bankid, externalid))
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.lasthand_bonus_cf (
    accountid bigint,
    bonustype int,
    bonusid bigint,
    gameid bigint,
    lh text,
    PRIMARY KEY ((accountid, bonustype, bonusid), gameid)
) WITH CLUSTERING ORDER BY (gameid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.subcasinogroupf (
    key text PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.usendedpwicf (
    gamesessionid bigint,
    windate bigint,
    jcn text,
    roundid bigint,
    scn blob,
    PRIMARY KEY (gamesessionid, windate)
) WITH CLUSTERING ORDER BY (windate ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 604800
    AND gc_grace_seconds = 86400000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX usendedpwicf_roundid_idx ON rcasinoscks.usendedpwicf (roundid);

CREATE TABLE rcasinoscks.massawardcf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.expiredbonustrackedcf (
    key text PRIMARY KEY,
    lastprocesseddate bigint
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.promocampaignmembercf (
    campid bigint,
    accid bigint,
    jcn text,
    memdata blob,
    PRIMARY KEY (campid, accid)
) WITH CLUSTERING ORDER BY (accid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.paymenttransactioncf2 (
    bucket int,
    startdate bigint,
    key bigint,
    extid text,
    jcn text,
    scn blob,
    transactionid bigint,
    PRIMARY KEY (bucket, startdate, key)
) WITH CLUSTERING ORDER BY (startdate ASC, key ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX paymenttransactioncf2_extid_idx ON rcasinoscks.paymenttransactioncf2 (extid);
CREATE INDEX paymenttransactioncf2_transactionid_idx ON rcasinoscks.paymenttransactioncf2 (transactionid);

CREATE TABLE rcasinoscks.trdata_cf (
    key text PRIMARY KEY,
    a blob,
    bonus blob,
    frbn blob,
    frbonus blob,
    frbwin blob,
    gs blob,
    lb blob,
    lh blob,
    lui text,
    pmembers blob,
    ps blob,
    pt blob,
    ssea blob,
    ssga blob,
    sspa blob,
    track text,
    v bigint,
    w blob
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'unchecked_tombstone_compaction': 'true', 'tombstone_compaction_interval': '3600', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 14400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.promomemberaliasescf (
    campid bigint,
    bankid bigint,
    campalias text,
    accid bigint,
    PRIMARY KEY (campid, bankid, campalias)
) WITH CLUSTERING ORDER BY (bankid ASC, campalias ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.bonuscf_acc (
    accid bigint,
    bonusid bigint,
    PRIMARY KEY (accid, bonusid)
) WITH CLUSTERING ORDER BY (bonusid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.trdata_tracking_cf (
    ts int,
    si int,
    key text,
    jcn text,
    scn blob,
    PRIMARY KEY (ts, si, key)
) WITH CLUSTERING ORDER BY (si ASC, key ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'unchecked_tombstone_compaction': 'true', 'tombstone_compaction_interval': '3600', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 14400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.accountcf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.DeflateCompressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.srvinfocf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.votesrvmastercf (
    key text PRIMARY KEY,
    gsid int
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.delayedmassawardindexcf (
    delayedmassawardid bigint PRIMARY KEY,
    massawardid bigint
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX delayedmassawardindexcf_massawardid_idx ON rcasinoscks.delayedmassawardindexcf (massawardid);

CREATE TABLE rcasinoscks.cgw_cf (
    accountid bigint,
    gameid int,
    addroundinfo text,
    betamount bigint,
    clienttype text,
    finround boolean,
    gamesessionid bigint,
    jpcontribution double,
    jpwin bigint,
    lnbet bigint,
    nbet bigint,
    newround boolean,
    roundid bigint,
    temptoken text,
    winamount bigint,
    PRIMARY KEY (accountid, gameid)
) WITH CLUSTERING ORDER BY (gameid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = 'ALWAYS';

CREATE TABLE rcasinoscks.promofeedcf (
    id bigint,
    t int,
    f text,
    PRIMARY KEY (id, t)
) WITH CLUSTERING ORDER BY (t ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 5184000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.currencycf (
    key text PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.massawardrestrictioncf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.rccf (
    g int,
    key bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (g, key)
) WITH CLUSTERING ORDER BY (key ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'unchecked_tombstone_compaction': 'true', 'tombstone_compaction_interval': '3600', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 14400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.gameinfocf (
    key text PRIMARY KEY,
    bankandcuridx text,
    bankidx text,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX gameinfocf_bankandcuridx_idx ON rcasinoscks.gameinfocf (bankandcuridx);
CREATE INDEX gameinfocf_bankidx_idx ON rcasinoscks.gameinfocf (bankidx);

CREATE TABLE rcasinoscks.hostcdncf (
    ip text,
    cdn text,
    last_update bigint,
    time int,
    PRIMARY KEY (ip, cdn)
) WITH CLUSTERING ORDER BY (cdn ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 2592000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.battlegroundcf (
    bankid bigint,
    gameid bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (bankid, gameid)
) WITH CLUSTERING ORDER BY (gameid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.dwlcf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.mqdata (
    a bigint,
    g bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (a, g)
) WITH CLUSTERING ORDER BY (g ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.frbonuscf (
    frbonusid bigint PRIMARY KEY,
    expdate bigint,
    extfrbonusid text,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX frbonuscf_expdate_idx ON rcasinoscks.frbonuscf (expdate);
CREATE INDEX frbonuscf_extfrbonusid_idx ON rcasinoscks.frbonuscf (extfrbonusid);

CREATE TABLE rcasinoscks.tournamenticoncf (
    id bigint PRIMARY KEY,
    ha text,
    n text
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.currencyratescf (
    source text,
    dest text,
    crate double,
    update_date bigint,
    PRIMARY KEY ((source, dest))
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.blockedcountriescf (
    key text PRIMARY KEY
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.accountcf_bank_tester (
    bankid int,
    extid text,
    accountid bigint,
    PRIMARY KEY (bankid, extid)
) WITH CLUSTERING ORDER BY (extid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.accountcf_ext (
    bankid int,
    extid text,
    accountid bigint,
    PRIMARY KEY (bankid, extid)
) WITH CLUSTERING ORDER BY (extid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"10000"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.promoplatformscf (
    promoid bigint PRIMARY KEY,
    jcn text,
    platform blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.betcftmp (
    sid bigint,
    bid int,
    jcn text,
    scn blob,
    PRIMARY KEY (sid, bid)
) WITH CLUSTERING ORDER BY (bid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'unchecked_tombstone_compaction': 'true', 'tombstone_compaction_interval': '3600', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 172800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.intsequencercf (
    key text PRIMARY KEY,
    v int
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.exttransactioncf (
    key text PRIMARY KEY,
    internalid text,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX exttransactioncf_internalid_idx ON rcasinoscks.exttransactioncf (internalid);

CREATE TABLE rcasinoscks.gametinfocf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.serverconfcf (
    key int PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.countryrestrictionscf (
    objectid bigint,
    restrictiontype int,
    jcn text,
    scn blob,
    PRIMARY KEY (objectid, restrictiontype)
) WITH CLUSTERING ORDER BY (restrictiontype ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoscks.currencyratesbydatecf (
    source text,
    dest text,
    update_date bigint,
    crate double,
    PRIMARY KEY ((source, dest), update_date)
) WITH CLUSTERING ORDER BY (update_date ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 7776000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX currencyratesbydatecf_update_date_idx ON rcasinoscks.currencyratesbydatecf (update_date);

CREATE TABLE rcasinoscks.promotrankcf (
    gmpid bigint,
    accountid bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (gmpid, accountid)
) WITH CLUSTERING ORDER BY (accountid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';


CREATE KEYSPACE rcasinoks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE rcasinoks.supportcf (
    key text,
    timestamp bigint,
    info text,
    PRIMARY KEY (key, timestamp)
) WITH CLUSTERING ORDER BY (timestamp ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 5184000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.battlegroundhistory (
    accountid bigint,
    datetime bigint,
    gameid int,
    gamesessionid bigint,
    jcn text,
    roundid bigint,
    scn blob,
    PRIMARY KEY (accountid, datetime)
) WITH CLUSTERING ORDER BY (datetime DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX battlegroundhistory_gameid_idx ON rcasinoks.battlegroundhistory (gameid);
CREATE INDEX battlegroundhistory_gamesessionid_idx ON rcasinoks.battlegroundhistory (gamesessionid);
CREATE INDEX battlegroundhistory_roundid_idx ON rcasinoks.battlegroundhistory (roundid);

CREATE TABLE rcasinoks.historyinformeritemmaxiterationscf (
    bankid bigint,
    createtime bigint,
    sessionid bigint,
    jcn text,
    lastattempttime bigint,
    scn blob,
    PRIMARY KEY (bankid, createtime, sessionid)
) WITH CLUSTERING ORDER BY (createtime ASC, sessionid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 864000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.roundgamesessioncf (
    round_id bigint,
    game_sid bigint,
    account_id bigint static,
    game_id bigint static,
    write_time bigint,
    PRIMARY KEY (round_id, game_sid)
) WITH CLUSTERING ORDER BY (game_sid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 5184000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.gamesessioncf (
    gsid bigint PRIMARY KEY,
    accid bigint,
    bcr double,
    bets int,
    bid bigint,
    bonst text,
    bonus bigint,
    bonusbet bigint,
    bonuswin bigint,
    cltype text,
    contribjp blob,
    contribjp_json text,
    curr text,
    day bigint,
    dblupi bigint,
    dblupp bigint,
    dbluprc int,
    enbbalance bigint,
    enterdate bigint,
    et bigint,
    extid text,
    fraction text,
    frbonst text,
    frbonus bigint,
    gameid bigint,
    income bigint,
    lang text,
    lbid bigint,
    mdl double,
    nb bigint,
    nextid bigint,
    payout bigint,
    pcr double,
    previd bigint,
    promoids list<bigint>,
    rc int,
    real boolean,
    st bigint,
    stbalance bigint,
    stbbalance bigint,
    unjid bigint,
    unjsc double,
    unjsw bigint
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.DeflateCompressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX gamesessioncf_day_idx ON rcasinoks.gamesessioncf (day);
CREATE INDEX gamesessioncf_extid_idx ON rcasinoks.gamesessioncf (extid);
CREATE INDEX gamesessioncf_nextid_idx ON rcasinoks.gamesessioncf (nextid);
CREATE INDEX gamesessioncf_previd_idx ON rcasinoks.gamesessioncf (previd);

CREATE TABLE rcasinoks.wopcf (
    key bigint PRIMARY KEY,
    day bigint,
    gamesessid bigint,
    jcn text,
    roundid bigint,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 2592000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX wopcf_day_idx ON rcasinoks.wopcf (day);
CREATE INDEX wopcf_roundid_idx ON rcasinoks.wopcf (roundid);

CREATE TABLE rcasinoks.metricscf (
    metricid int,
    gameserverid int,
    logtime bigint,
    metricvalue bigint,
    PRIMARY KEY ((metricid, gameserverid), logtime)
) WITH CLUSTERING ORDER BY (logtime ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 2592000
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.clientgamestatisticsinfocf (
    key bigint PRIMARY KEY,
    browserinfo blob,
    browserinfo_json text,
    gameclientinfo blob,
    gameclientinfo_json text
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 259200
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.mqroundkpiinfo (
    gsid bigint,
    rid bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (gsid, rid)
) WITH CLUSTERING ORDER BY (rid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 86400
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.httpcallissues (
    date text,
    url text,
    failcount bigint,
    successcount bigint,
    updatetime bigint,
    PRIMARY KEY (date, url)
) WITH CLUSTERING ORDER BY (url ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 5184000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.frbwincf (
    key bigint PRIMARY KEY,
    accid bigint,
    gamesessid bigint,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX frbwincf_accid_idx ON rcasinoks.frbwincf (accid);
CREATE INDEX frbwincf_gamesessid_idx ON rcasinoks.frbwincf (gamesessid);

CREATE TABLE rcasinoks.archivercf (
    key text PRIMARY KEY,
    lastprocesseddate bigint
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.gamesessioncf_agm (
    accid bigint,
    mode int,
    gameid bigint,
    et bigint,
    gsid bigint,
    gsn bigint,
    PRIMARY KEY ((accid, mode, gameid), et)
) WITH CLUSTERING ORDER BY (et DESC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.DeflateCompressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX gamesessioncf_agm_gsn_idx ON rcasinoks.gamesessioncf_agm (gsn);

CREATE TABLE rcasinoks.bonusarchcf (
    accid bigint,
    awardtime bigint,
    bonusid bigint,
    extbonusid text,
    jcn text,
    persistday bigint,
    scn blob,
    statusid int,
    PRIMARY KEY (accid, awardtime)
) WITH CLUSTERING ORDER BY (awardtime DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX bonusarchcf_bonusid_idx ON rcasinoks.bonusarchcf (bonusid);
CREATE INDEX bonusarchcf_extbonusid_idx ON rcasinoks.bonusarchcf (extbonusid);
CREATE INDEX bonusarchcf_persistday_idx ON rcasinoks.bonusarchcf (persistday);

CREATE TABLE rcasinoks.trackinfocf (
    key text,
    object_id text,
    jcn text,
    scn blob,
    PRIMARY KEY (key, object_id)
) WITH CLUSTERING ORDER BY (object_id ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'unchecked_tombstone_compaction': 'true', 'tombstone_compaction_interval': '3600', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 14400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.gamesessioncf_bag_idx (
    bid bigint,
    gameid bigint,
    et bigint,
    accid bigint,
    gsid bigint,
    PRIMARY KEY ((bid, gameid), et, accid)
) WITH CLUSTERING ORDER BY (et DESC, accid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.DeflateCompressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.laststattimecf (
    metricid int,
    gameserverid int,
    laststattime bigint,
    PRIMARY KEY ((metricid, gameserverid))
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 2592000
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.historyinformeritemcf (
    bankid bigint,
    createtime bigint,
    sessionid bigint,
    iterations int,
    jcn text,
    lastattempttime bigint,
    scn blob,
    PRIMARY KEY (bankid, createtime, sessionid)
) WITH CLUSTERING ORDER BY (createtime ASC, sessionid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 864000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.betcf (
    sid bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.DeflateCompressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 0
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.batchopstatus (
    room_id bigint,
    round_id bigint,
    op_type text,
    change_date bigint,
    status text,
    PRIMARY KEY ((room_id, round_id), op_type)
) WITH CLUSTERING ORDER BY (op_type ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'unchecked_tombstone_compaction': 'true', 'tombstone_compaction_interval': '28800', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 259200
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.concurrentnotcf (
    key text,
    serverid int,
    jcn text,
    scn blob,
    PRIMARY KEY ((key, serverid))
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 5184000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.promowincf (
    promoid bigint,
    timewin bigint,
    accountid bigint,
    amount bigint,
    amountinplayercurrency bigint,
    bankid bigint,
    gameid bigint,
    gamesessionid bigint,
    transferstatus text,
    PRIMARY KEY (promoid, timewin)
) WITH CLUSTERING ORDER BY (timewin ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 2592000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.httpcallstatistics (
    date text,
    url text,
    failedcount counter,
    successcount counter,
    PRIMARY KEY (date, url)
) WITH CLUSTERING ORDER BY (url ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.currentplayersessionstate (
    key text PRIMARY KEY,
    daytime bigint,
    isfinishgamesession boolean,
    privateroomid text,
    sid text
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX currentplayersessionstate_privateroomid_idx ON rcasinoks.currentplayersessionstate (privateroomid);
CREATE INDEX currentplayersessionstate_sid_idx ON rcasinoks.currentplayersessionstate (sid);

CREATE TABLE rcasinoks.shortbetinfocf3 (
    bid int,
    btime bigint,
    aid bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (bid, btime, aid)
) WITH CLUSTERING ORDER BY (btime DESC, aid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.DeflateCompressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 259200
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.dmassawardhistorycf (
    key bigint PRIMARY KEY,
    gsid int,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.gamesessioncf_ag (
    accid bigint,
    gameid bigint,
    et bigint,
    gsid bigint,
    gsn bigint,
    PRIMARY KEY ((accid, gameid), et)
) WITH CLUSTERING ORDER BY (et DESC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.DeflateCompressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX gamesessioncf_ag_gsn_idx ON rcasinoks.gamesessioncf_ag (gsn);

CREATE TABLE rcasinoks.historytokencf (
    historytoken text PRIMARY KEY,
    exptime bigint,
    roundid bigint
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 3600
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.dmassawardfailedsendcf (
    key bigint PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.tournamentpromosumfeedcf (
    id bigint,
    furl text,
    bname text,
    end_date bigint static,
    feed_body text,
    mask_name boolean,
    pchecksum text,
    start_date bigint static,
    tournament_id bigint,
    ttype text,
    update_time bigint,
    PRIMARY KEY (id, furl)
) WITH CLUSTERING ORDER BY (furl ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX tournamentpromosumfeedcf_tournament_id_idx ON rcasinoks.tournamentpromosumfeedcf (tournament_id);

CREATE TABLE rcasinoks.battlegroundparticipantroundhistory (
    sid text,
    roundid bigint,
    accountids set<bigint>,
    gamesessionid bigint,
    jcn text,
    scn blob,
    PRIMARY KEY (sid, roundid)
) WITH CLUSTERING ORDER BY (roundid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX battlegroundparticipantroundhistory_gamesessionid_idx ON rcasinoks.battlegroundparticipantroundhistory (gamesessionid);

CREATE TABLE rcasinoks.frbonusarchcf (
    accid bigint,
    awardtime bigint,
    extfrbonusid text,
    frbonusid bigint,
    jcn text,
    persistday bigint,
    scn blob,
    statusid int,
    PRIMARY KEY (accid, awardtime)
) WITH CLUSTERING ORDER BY (awardtime DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX frbonusarchcf_extfrbonusid_idx ON rcasinoks.frbonusarchcf (extfrbonusid);
CREATE INDEX frbonusarchcf_frbonusid_idx ON rcasinoks.frbonusarchcf (frbonusid);
CREATE INDEX frbonusarchcf_persistday_idx ON rcasinoks.frbonusarchcf (persistday);

CREATE TABLE rcasinoks.playersessionhistorycf (
    key text PRIMARY KEY,
    day bigint,
    extsid text,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.DeflateCompressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX playersessionhistorycf_day_idx ON rcasinoks.playersessionhistorycf (day);
CREATE INDEX playersessionhistorycf_extsid_idx ON rcasinoks.playersessionhistorycf (extsid);

CREATE TABLE rcasinoks.battlegroundprivateroomsettings (
    privateroomid text PRIMARY KEY,
    jcn text,
    scn blob
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

CREATE TABLE rcasinoks.promopredefineduserspersistercf (
    bankid bigint,
    promoid bigint,
    extuserid text,
    accountid bigint,
    PRIMARY KEY (bankid, promoid, extuserid)
) WITH CLUSTERING ORDER BY (promoid ASC, extuserid ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 15552000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX promopredefineduserspersistercf_accountid_idx ON rcasinoks.promopredefineduserspersistercf (accountid);

CREATE TABLE rcasinoks.httpcallinfocf (
    callid text,
    timestamp bigint,
    externalid text,
    gamesessionid bigint,
    jcn text,
    roundid bigint,
    scn blob,
    tkn text,
    transactionid bigint,
    PRIMARY KEY (callid, timestamp)
) WITH CLUSTERING ORDER BY (timestamp ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 5184000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX httpcallinfocf_externalid_idx ON rcasinoks.httpcallinfocf (externalid);
CREATE INDEX httpcallinfocf_gamesessionid_idx ON rcasinoks.httpcallinfocf (gamesessionid);
CREATE INDEX httpcallinfocf_roundid_idx ON rcasinoks.httpcallinfocf (roundid);
CREATE INDEX httpcallinfocf_tkn_idx ON rcasinoks.httpcallinfocf (tkn);
CREATE INDEX httpcallinfocf_transactionid_idx ON rcasinoks.httpcallinfocf (transactionid);

CREATE TABLE rcasinoks.tdrawconfig (
    id bigint PRIMARY KEY,
    cc blob,
    ed bigint,
    jcn text,
    sd bigint,
    ss int,
    ud bigint
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 10368000
    AND gc_grace_seconds = 10800
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE INDEX tdrawconfig_ss_idx ON rcasinoks.tdrawconfig (ss);

CREATE TABLE rcasinoks.metricsstatcf (
    metricid int,
    gameserverid int,
    stattime bigint,
    averagevalue bigint,
    maxvalue bigint,
    maxvaluetime bigint,
    minvalue bigint,
    minvaluetime bigint,
    PRIMARY KEY ((metricid, gameserverid), stattime)
) WITH CLUSTERING ORDER BY (stattime ASC)
    AND bloom_filter_fp_chance = 0.1
    AND caching = '{"keys":"NONE", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'sstable_size_in_mb': '100', 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy'}
    AND compression = {}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 2592000
    AND gc_grace_seconds = 86400
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';

