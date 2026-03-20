package com.abs.casino.cassandra.persist.engine.configuration;


import static com.abs.casino.cassandra.persist.engine.SchemaCql.*;
import com.abs.casino.cassandra.persist.engine.SchemaCql;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 25.05.16
 */
public class CompactionStrategy {

    public static final int SS_TABLE_SIZE = 100;

    public static final CompactionStrategy DATE_TIRED = new CompactionStrategy(dateTieredStrategy());
    public static final CompactionStrategy LEVELED = new CompactionStrategy(leveledStrategy().ssTableSizeInMB(SS_TABLE_SIZE));
    public static final CompactionStrategy SIZE_TIRED = new CompactionStrategy(sizedTieredStategy());

    private final com.datastax.driver.core.schemabuilder.TableOptions.CompactionOptions compactionOptions;

    private CompactionStrategy(com.datastax.driver.core.schemabuilder.TableOptions.CompactionOptions compactionOptions) {
        this.compactionOptions = compactionOptions;
    }

    public static CompactionStrategy getLeveled(boolean uncheckedTombstoneCompaction, long tombstoneCompactionIntervalInSec) {
        return new CompactionStrategy(
                leveledStrategy()
                        .ssTableSizeInMB(SS_TABLE_SIZE)
                        .uncheckedTombstoneCompaction(uncheckedTombstoneCompaction)
                        .tombstoneCompactionIntervalInDay((int) tombstoneCompactionIntervalInSec)
        );
    }

    public static CompactionStrategy getSizeTired(boolean uncheckedTombstoneCompaction, long tombstoneCompactionIntervalInSec,
                                                  double coldReadsToOmit) {
        return new CompactionStrategy(
                sizedTieredStategy()
                        .uncheckedTombstoneCompaction(uncheckedTombstoneCompaction)
                        .tombstoneCompactionIntervalInDay((int) tombstoneCompactionIntervalInSec)
        );
    }

    public static CompactionStrategy getDateTired(int maxSSTableAge) {
        return new CompactionStrategy(
                dateTieredStrategy()
                        .maxSSTableAgeDays(maxSSTableAge)
                        .baseTimeSeconds(60)
                        .minThreshold(4)
        );
    }

    public com.datastax.driver.core.schemabuilder.TableOptions.CompactionOptions getCompactionOptions() {
        return compactionOptions;
    }
}
