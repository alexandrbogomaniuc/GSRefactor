
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
