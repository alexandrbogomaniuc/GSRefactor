package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.Row;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.session.BrowserInfo;
import com.abs.casino.common.cache.data.session.GameClientInfo;
import com.abs.casino.common.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

public class CassandraClientStatisticsPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraClientStatisticsPersister.class);

    private static final String CLIENT_STATISTICS_INFO_CF = "ClientGameStatisticsInfoCF";

    private static final String GAME_CLIENT_INFO = "gameClientInfo";
    private static final String GAME_CLIENT_INFO_JSON = GAME_CLIENT_INFO + "_json";

    private static final String BROWSER_INFO = "browserInfo";
    private static final String BROWSER_INFO_JSON = BROWSER_INFO + "_json";

    private static final TableDefinition TABLE = new TableDefinition(CLIENT_STATISTICS_INFO_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, bigint(), false, false, true),
                    new ColumnDefinition(BROWSER_INFO, blob()),
                    new ColumnDefinition(GAME_CLIENT_INFO, blob()),
                    new ColumnDefinition(BROWSER_INFO_JSON, text()),
                    new ColumnDefinition(GAME_CLIENT_INFO_JSON, text())
            ), KEY);

    public void persistGameClientInfo(long gameSessionId, GameClientInfo gameClientInfo) {
        ByteBuffer byteBuffer = TABLE.serializeToBytes(gameClientInfo);
        String json = TABLE.serializeToJson(gameClientInfo);
        try {
            insert(gameSessionId, new HashMap<String,Object>() {{ 
                put(GAME_CLIENT_INFO, byteBuffer); put(GAME_CLIENT_INFO_JSON, json); 
            }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public Optional<Pair<BrowserInfo, GameClientInfo>> getByGameSessionId(long gameSessionId) {
        Row row = getByKeyWrapped(gameSessionId);
        if (row == null) {
            return Optional.empty();
        }
        BrowserInfo browserInfo = null;
        GameClientInfo gameClientInfo = null;
        if (!row.isNull(BROWSER_INFO)) {
            browserInfo = TABLE.deserializeFromJson(row.getString(BROWSER_INFO_JSON), BrowserInfo.class);

            if (browserInfo == null) {
                browserInfo = TABLE.deserializeFrom(row.getBytes(BROWSER_INFO), BrowserInfo.class);
            }
        }
        if (!row.isNull(GAME_CLIENT_INFO)) {
            gameClientInfo = TABLE.deserializeFromJson(row.getString(GAME_CLIENT_INFO_JSON), GameClientInfo.class);

            if (gameClientInfo == null) {
                gameClientInfo = TABLE.deserializeFrom(row.getBytes(GAME_CLIENT_INFO), GameClientInfo.class);
            }
        }
        return Optional.of(new Pair<>(browserInfo, gameClientInfo));
    }

    public void persistBrowserInfo(long gameSessionId, BrowserInfo browserInfo) {
        ByteBuffer byteBuffer = TABLE.serializeToBytes(browserInfo);
        String json = TABLE.serializeToJson(browserInfo);
        try {
            insert(gameSessionId, new HashMap<String, Object>() {{ 
                put(BROWSER_INFO, byteBuffer); put(BROWSER_INFO_JSON, json);
            }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
