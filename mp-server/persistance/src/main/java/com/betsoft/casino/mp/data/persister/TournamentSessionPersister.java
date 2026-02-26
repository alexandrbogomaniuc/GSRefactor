package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.ITournamentSession;
import com.betsoft.casino.mp.model.TournamentSession;
import com.betsoft.casino.mp.service.ITournamentService;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TournamentSessionPersister extends AbstractCassandraPersister<Long, String> implements ITournamentService {
    private static final Logger LOG = LogManager.getLogger(TournamentSessionPersister.class);

    private static final String CF_NAME = "TournamentSession";
    private static final String TOURNAMENT_ID_COLUMN = "tid";
    private static final String ACCOUNT_ID_COLUMN = "aid";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(TOURNAMENT_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), TOURNAMENT_ID_COLUMN);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public TournamentSession get(long tournamentId, long accountId) {
        Row result = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                        .where(eq(TOURNAMENT_ID_COLUMN, tournamentId))
                        .and(eq(ACCOUNT_ID_COLUMN, accountId))
                        .limit(1),
                "get").one();
        if (result == null) {
            return null;
        }
        TournamentSession ts = TABLE.deserializeWithClassFromJson(result.getString(JSON_COLUMN_NAME));
        if (ts == null) {
            ts = TABLE.deserializeWithClassFrom(result.getBytes(SERIALIZED_COLUMN_NAME));
        }
        return ts;
    }

    @Override
    public List<ITournamentSession> getByTournament(long tournamentId) {
        com.datastax.driver.core.ResultSet rows = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME)
                        .where(eq(TOURNAMENT_ID_COLUMN, tournamentId)),
                "getButTournament");
        List<ITournamentSession> result = new ArrayList<>();
        for (Row row : rows) {
            TournamentSession session = TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));

            if (session == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                if (bytes != null) {
                    session = TABLE.deserializeWithClassFrom(bytes);
                }
            }
            if (session != null) {
                result.add(session);
            }
        }
        return result;
    }

    @Override
    public void persist(ITournamentSession session) {

        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(session);
        String json = TABLE.serializeWithClassToJson(session);
        try {
            execute(getInsertQuery()
                            .value(TOURNAMENT_ID_COLUMN, session.getTournamentId())
                            .value(ACCOUNT_ID_COLUMN, session.getAccountId())
                            .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                            .value(JSON_COLUMN_NAME, json),
                    "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }
}
