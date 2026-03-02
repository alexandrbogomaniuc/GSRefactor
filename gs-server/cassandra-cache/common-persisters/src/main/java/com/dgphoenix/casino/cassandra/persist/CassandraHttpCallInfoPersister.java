package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.util.StreamUtils;
import com.abs.casino.common.util.support.HttpCallInfo;
import com.abs.casino.common.util.support.HttpMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.datastax.driver.core.DataType.*;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.abs.casino.common.util.support.AdditionalInfoAttribute.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 03.02.2020
 */
public class CassandraHttpCallInfoPersister extends AbstractCassandraPersister<String, Long> {

    private static final Logger LOG = LogManager.getLogger(CassandraHttpCallInfoPersister.class);

    private static final String CF_NAME = "HttpCallInfoCF";
    private static final String ID_FIELD = "callId"; // sessionId or supportTicketId
    private static final String TIMESTAMP_FIELD = TIMESTAMP.getAttributeName();
    private static final String TOKEN_FIELD = "tkn";
    private static final String EXTERNAL_ID_FIELD = EXTERNAL_ID.getAttributeName();
    private static final String GAME_SESSION_ID_FIELD = GAME_SESSION_ID.getAttributeName();
    private static final String ROUND_ID_FIELD = ROUND_ID.getAttributeName();
    private static final String TRANSACTION_ID_FIELD = TRANSACTION_ID.getAttributeName();

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ID_FIELD, text(), false, false, true),
                    new ColumnDefinition(TIMESTAMP_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(TOKEN_FIELD, text(), false, true, false),
                    new ColumnDefinition(EXTERNAL_ID_FIELD, text(), false, true, false),
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, bigint(), false, true, false),
                    new ColumnDefinition(ROUND_ID_FIELD, bigint(), false, true, false),
                    new ColumnDefinition(TRANSACTION_ID_FIELD, bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ),
            ID_FIELD
    );

    private CassandraHttpCallInfoPersister() {
        super();
    }

    public void persist(HttpCallInfo httpCallInfo) {
        Map<String, String> additionalInfo = httpCallInfo.getAdditionalInfo();
        String sessionId = additionalInfo.get(SESSION_ID.getAttributeName());
        String supportTicketId = additionalInfo.get(SUPPORT_TICKET_ID.getAttributeName());
        com.datastax.driver.core.querybuilder.Insert insert = getInsertQuery();

        if (sessionId != null) {
            insert.value(ID_FIELD, sessionId);
        } else if (supportTicketId != null) {
            insert.value(ID_FIELD, supportTicketId);
        } else {
            LOG.error("persist: sessionId or supportTicketId must be provided");
            return;
        }

        HttpMessage httpMessage = httpCallInfo.getHttpMessage();
        long timestamp = httpMessage != null ? httpMessage.getRequest().getTime() : System.currentTimeMillis();
        insert.value(TIMESTAMP_FIELD, timestamp);

        Optional.ofNullable(additionalInfo.get(TOKEN.getAttributeName()))
                .ifPresent(token -> insert.value(TOKEN_FIELD, token));

        Optional.ofNullable(additionalInfo.get(EXTERNAL_ID.getAttributeName()))
                .ifPresent(externalId -> insert.value(EXTERNAL_ID_FIELD, externalId));

        Optional.ofNullable(additionalInfo.get(GAME_SESSION_ID.getAttributeName()))
                .map(Long::parseLong)
                .ifPresent(gameSessionId -> insert.value(GAME_SESSION_ID_FIELD, gameSessionId));

        Optional.ofNullable(additionalInfo.get(ROUND_ID.getAttributeName()))
                .map(Long::parseLong)
                .ifPresent(roundId -> insert.value(ROUND_ID_FIELD, roundId));
        Optional.ofNullable(additionalInfo.get(TRANSACTION_ID.getAttributeName()))
                .map(Long::parseLong)
                .ifPresent(transactionId -> insert.value(TRANSACTION_ID_FIELD, transactionId));

        String json = TABLE.serializeToJson(httpCallInfo);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(httpCallInfo);
        try {
            insert.value(SERIALIZED_COLUMN_NAME, byteBuffer);
            insert.value(JSON_COLUMN_NAME, json);
            execute(insert, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public List<HttpCallInfo> getById(String id) {
        if (isEmpty(id)) {
            return emptyList();
        }
        return getMany(ID_FIELD, id);
    }

    public List<HttpCallInfo> getByToken(String token) {
        if (isEmpty(token)) {
            return emptyList();
        }
        return getMany(TOKEN_FIELD, token);
    }

    public List<HttpCallInfo> getByGameSessionId(long gameSessionId) {
        return getMany(GAME_SESSION_ID_FIELD, gameSessionId);
    }

    public List<HttpCallInfo> getByRoundId(long roundId) {
        return getMany(ROUND_ID_FIELD, roundId);
    }

    public List<HttpCallInfo> getByExternalId(long bankId, String externalId) {
        if (isEmpty(externalId)) {
            return emptyList();
        }
        return getMany(EXTERNAL_ID_FIELD, composeKey(bankId, externalId));
    }

    public List<HttpCallInfo> getByTransactionId(long transactionId) {
        return getMany(TRANSACTION_ID_FIELD, transactionId);
    }

    private List<HttpCallInfo> getMany(String columnName, Object value) {
        com.datastax.driver.core.Statement query = select(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .from(CF_NAME)
                .where(eq(columnName, value));
        com.datastax.driver.core.ResultSet rows = execute(query, "getMany");
        return StreamUtils.asStream(rows)
                .map(this::toHttpCallInfoOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<HttpCallInfo> toHttpCallInfoOptional(com.datastax.driver.core.Row row) {
        Optional<HttpCallInfo> httpCallInfo = Optional.ofNullable(row.getString(JSON_COLUMN_NAME))
                .map(json -> TABLE.deserializeFromJson(json, HttpCallInfo.class));

        if (httpCallInfo.isPresent()) {
            return httpCallInfo;
        }
        return Optional.ofNullable(row.getBytes(SERIALIZED_COLUMN_NAME))
                .map(bytes -> TABLE.deserializeFrom(bytes, HttpCallInfo.class));
    }

    public static String composeKey(long bankId, String externalId) {
        return bankId + ICassandraPersister.ID_DELIMITER + externalId;
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
