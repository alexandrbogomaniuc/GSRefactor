package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.common.AbstractGameRoom;
import com.betsoft.casino.mp.maxcrashgame.model.PlayerRoundInfo;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.battleground.IBgPlace;
import com.betsoft.casino.mp.model.playerinfo.AbstractBattlegroundRoomPlayerInfo;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.utils.ObjectFileWriterUtil;
import com.dgphoenix.casino.common.util.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigquery.*;
import com.google.cloud.http.HttpTransportOptions;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class BigQueryClientService implements IAnalyticsDBClientService {
    private static final Logger LOG = LogManager.getLogger(BigQueryClientService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String credentialsEnvVariableName = "GOOGLE_APPLICATION_CREDENTIALS";
    private final String credentialsEnvVariableNameValue;

    @Value("${google.cloud.project.id}")
    private String googleCloudProjectId;

    @Value("${google.cloud.bigquery.dataset.name}")
    private String datasetName;

    @Value("${google.cloud.bigquery.enabled}")
    private boolean enabled;

    @Value("${google.cloud.bigquery.connect.timeout}")
    private int connectTimeout;

    @Value("${google.cloud.bigquery.valid.currencies}")
    private String validCurrencies;

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    private BigQuery bigquery;

    private ObjectFileWriterUtil objectFileWriterUtil;

    public static String ROUND_RESULT_TABLE_NAME = "round_result";
    public static String ROUND_RESULT_FIELD_S_DATE = "s_date";
    public static String ROUND_RESULT_FIELD_CLUSTER = "Cluster";
    public static String ROUND_RESULT_FIELD_SUB_CASINO = "Subcasino";
    public static String ROUND_RESULT_FIELD_BANK = "Bank";
    public static String ROUND_RESULT_FIELD_CURRENCY = "Currency";
    public static String ROUND_RESULT_FIELD_GROUP_GAME_NAME = "Group_Game_Name";
    public static String ROUND_RESULT_FIELD_PLAYER_ID = "Player_ID";
    public static String ROUND_RESULT_FIELD_PLAYER_GROUP = "Player_Group";
    public static String ROUND_RESULT_FIELD_SESSION_ID = "Session_ID";
    public static String ROUND_RESULT_FIELD_GAME_SESSION_ID = "Game_Session_ID";
    public static String ROUND_RESULT_FIELD_ROUND_ID = "Round_ID";
    public static String ROUND_RESULT_FIELD_ROUND_START_TIME = "Round_Start_Time";
    public static String ROUND_RESULT_FIELD_ROUND_END_TIME = "Round_End_Time";
    public static String ROUND_RESULT_FIELD_ROOM_ID = "Room_ID";
    public static String ROUND_RESULT_FIELD_IS_BATTLEGROUND = "Is_Battleground";
    public static String ROUND_RESULT_FIELD_IS_PRIVATE = "Is_Private";
    public static String ROUND_RESULT_FIELD_IS_OWNER = "Is_Owner";
    public static String ROUND_RESULT_FIELD_ROOM_TYPE = "Room_Type";
    public static String ROUND_RESULT_FIELD_ROOM_VALUE = "Room_Value";
    public static String ROUND_RESULT_FIELD_RAKE_VALUE = "Rake_value";
    public static String ROUND_RESULT_FIELD_TOTAL_SHOTS = "Total_Shots";
    public static String ROUND_RESULT_FIELD_TOTAL_WINNING_SHOTS = "Total_winning_shots";
    public static String ROUND_RESULT_FIELD_TOTAL_BETS_VALUE = "Total_Bets_value";
    public static String ROUND_RESULT_FIELD_TOTAL_WIN_VALUE = "Total_Win_value";
    public static String ROUND_RESULT_FIELD_GGR = "GGR";
    public static String ROUND_RESULT_FIELD_TOTAL_BONUS_BET = "Total_Bonus_Bet";
    public static String ROUND_RESULT_FIELD_TOTAL_BONUS_WIN = "Total_Bonus_Win";
    public static String ROUND_RESULT_FIELD_ROUND_START_BALANCE = "Round_Start_Balance_mc";
    public static String ROUND_RESULT_FIELD_ROUND_END_BALANCE = "Round_End_Balance_mc";
    public static String ROUND_RESULT_FIELD_PLAYER_ROUND_INFO = "Player_Round_Info";
    public static String ROUND_RESULT_FIELD_MISSED_SHOTS_COUNT = "Missed_shots_count";
    public static String ROUND_RESULT_FIELD_HIT_SHOTS_COUNT = "Hit_shots_count";
    public static String ROUND_RESULT_FIELD_TOTAL_SHOTS_COUNT = "Total_shots_count";
    public static String ROUND_RESULT_CRASH_GAME_MULTIPLIERS = "Crash_game_multipliers";
    public static String ROUND_RESULT_PLAYER_SEAT_NUMBER = "Player_seat_number";

    public static String ROOMS_PLAYERS_TABLE_NAME = "rooms_players";
    public static String ROOMS_PLAYERS_FIELD_S_DATE = "s_date";
    public static String ROOMS_PLAYERS_FIELD_CLUSTER = "cluster";
    public static String ROOMS_PLAYERS_FIELD_ROOM_ID = "room_id";
    public static String ROOMS_PLAYERS_FIELD_ROOM_SERVER_ID = "room_serverId";
    public static String ROOMS_PLAYERS_FIELD_ROOM_IS_ACTIVE = "room_isActive";
    public static String ROOMS_PLAYERS_FIELD_ROOM_IS_BATTLEGROUND = "room_isBattleground";
    public static String ROOMS_PLAYERS_FIELD_ROOM_IS_PRIVATE = "room_isPrivate";
    public static String ROOMS_PLAYERS_FIELD_ROOM_BUY_IN_STAKE = "room_buyInStake";
    public static String ROOMS_PLAYERS_FIELD_ROOM_CURRENCY = "room_currency";
    public static String ROOMS_PLAYERS_FIELD_ROOM_GAME_ID = "room_gameId";
    public static String ROOMS_PLAYERS_FIELD_ROOM_GAME_NAME = "room_gameName";
    public static String ROOMS_PLAYERS_FIELD_PLAYER_SERVER_ID = "player_serverId";
    public static String ROOMS_PLAYERS_FIELD_PLAYER_NICKNAME = "player_nickname";
    public static String ROOMS_PLAYERS_FIELD_PLAYER_IS_OWNER = "player_isOwner";
    public static String ROOMS_PLAYERS_FIELD_PLAYER_SESSION_ID = "player_sessionId";
    public static String ROOMS_PLAYERS_FIELD_PLAYER_SEAT_NR = "player_seatNr";


    public BigQueryClientService() {
        this.objectFileWriterUtil = new ObjectFileWriterUtil();
        this.credentialsEnvVariableNameValue = System.getenv(this.credentialsEnvVariableName);

        if (this.credentialsEnvVariableNameValue != null) {
            LOG.debug("GOOGLE_APPLICATION_CREDENTIALS path: {}", this.credentialsEnvVariableNameValue);
        } else {
            LOG.error("GOOGLE_APPLICATION_CREDENTIALS is not set.");
        }
    }

    @PostConstruct
    public void init() {
        // Only initialize BigQuery client if enabled
        if (enabled) {
            validateConfiguration();

            this.bigquery = BigQueryOptions.newBuilder()
                    .setProjectId(googleCloudProjectId)
                    .setTransportOptions(HttpTransportOptions.newBuilder()
                            .setConnectTimeout(connectTimeout)
                            .setReadTimeout(connectTimeout)
                            .build())
                    .build()
                    .getService();
            LOG.info("init: BigQuery client initialized successfully.");
        } else {
            LOG.info("init: BigQuery is disabled initialization skipped.");
        }
    }

    private void validateConfiguration() {
        if (googleCloudProjectId == null || googleCloudProjectId.trim().isEmpty()) {
            LOG.error("Google Cloud Project ID is not set. Please specify 'google.cloud.project.id' in your application properties.");
            throw new IllegalArgumentException("Google Cloud Project ID is not set.");
        }
    }

    public boolean isEnabled() {
        return enabled && bigquery != null;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setGoogleCloudProjectId(String googleCloudProjectId) {
        this.googleCloudProjectId = googleCloudProjectId;
    }

    public void setBigquery(BigQuery bigquery) {
        this.bigquery = bigquery;
    }

    public BigQuery getBigquery() {
        if (this.bigquery == null) {
            this.init();
        }

        return this.bigquery;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getValidCurrencies() {
        return validCurrencies;
    }

    public void setValidCurrencies(String validCurrencies) {
        this.validCurrencies = validCurrencies;
    }

    public void createTable(BigQuery bigquery, TableId tableId, Schema schema) {
        LOG.debug("createTable: googleCloudProjectId={}, datasetName={}, enabled={}, connectTimeout={}(ms)", googleCloudProjectId, datasetName, enabled, connectTimeout);

        if (!isEnabled()) {
            LOG.debug("createTable: skip function, google.cloud.bigquery.enabled={}, {}={}", enabled, credentialsEnvVariableName, credentialsEnvVariableNameValue);
        } else {
            TableDefinition tableDefinition = StandardTableDefinition.of(schema);
            TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

            bigquery.create(tableInfo);
        }
    }

    public boolean insertRows(BigQuery bigquery, TableId tableId, List<Map<String, Object>> rows) {
        LOG.debug("insertRows: googleCloudProjectId={}, datasetName={}, enabled={}, connectTimeout={}(ms)", googleCloudProjectId, datasetName, enabled, connectTimeout);
        if (!isEnabled()) {
            LOG.debug("insertRows: skip function, google.cloud.bigquery.enabled={}, {}={}", enabled, credentialsEnvVariableName, credentialsEnvVariableNameValue);
            return true;
        }
        // Inserting data
        InsertAllRequest.Builder requestBuilder = InsertAllRequest.newBuilder(tableId);
        for (Map<String, Object> rowRoundResult : rows) {
            requestBuilder.addRow(rowRoundResult);
        }

        if (rows.isEmpty()) {
            return false;
        }
        InsertAllResponse response = bigquery.insertAll(requestBuilder.build());

        if (response.hasErrors()) {
            // If any of the insertions failed, this lets you inspect the errors
            for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
                LOG.error("save: response error: {} ", entry.getValue());
            }
            return false;
        }
        return true;
    }

    private void createTableForRoundResults(BigQuery bigQuery, TableId tableId) {
        // Inserting data types
        Field sDate = Field.of(ROUND_RESULT_FIELD_S_DATE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.TIMESTAMP));
        Field cluster = Field.of(ROUND_RESULT_FIELD_CLUSTER, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field subCasino = Field.of(ROUND_RESULT_FIELD_SUB_CASINO, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field bank = Field.of(ROUND_RESULT_FIELD_BANK, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.INT64));
        Field currency = Field.of(ROUND_RESULT_FIELD_CURRENCY, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field groupGameName = Field.of(ROUND_RESULT_FIELD_GROUP_GAME_NAME, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field playerId = Field.of(ROUND_RESULT_FIELD_PLAYER_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field playerGroup = Field.of(ROUND_RESULT_FIELD_PLAYER_GROUP, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field sessionId = Field.of(ROUND_RESULT_FIELD_SESSION_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field gameSessionId = Field.of(ROUND_RESULT_FIELD_GAME_SESSION_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field roundId = Field.of(ROUND_RESULT_FIELD_ROUND_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.INT64));
        Field roundStartTime = Field.of(ROUND_RESULT_FIELD_ROUND_START_TIME, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.DATETIME));
        Field roundEndTime = Field.of(ROUND_RESULT_FIELD_ROUND_END_TIME, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.DATETIME));
        Field roomType = Field.of(ROUND_RESULT_FIELD_ROOM_TYPE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field roomId = Field.of(ROUND_RESULT_FIELD_ROOM_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.INT64));
        Field isBattleground = Field.of(ROUND_RESULT_FIELD_IS_BATTLEGROUND, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.BOOL));
        Field isPrivate = Field.of(ROUND_RESULT_FIELD_IS_PRIVATE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.BOOL));
        Field isOwner = Field.of(ROUND_RESULT_FIELD_IS_OWNER, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.BOOL));
        Field roomValue = Field.of(ROUND_RESULT_FIELD_ROOM_VALUE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field rakeValue = Field.of(ROUND_RESULT_FIELD_RAKE_VALUE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field totalShots = Field.of(ROUND_RESULT_FIELD_TOTAL_SHOTS, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field totalWinningShots = Field.of(ROUND_RESULT_FIELD_TOTAL_WINNING_SHOTS, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field totalBetsValue = Field.of(ROUND_RESULT_FIELD_TOTAL_BETS_VALUE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field totalWinValue = Field.of(ROUND_RESULT_FIELD_TOTAL_WIN_VALUE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field ggr = Field.of(ROUND_RESULT_FIELD_GGR, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field totalBonusBet = Field.of(ROUND_RESULT_FIELD_TOTAL_BONUS_BET, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field totalBonusWin = Field.of(ROUND_RESULT_FIELD_TOTAL_BONUS_WIN, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field roundStartBalanceMC = Field.of(ROUND_RESULT_FIELD_ROUND_START_BALANCE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field roundEndBalanceMC = Field.of(ROUND_RESULT_FIELD_ROUND_END_BALANCE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field missedShotsCount = Field.of(ROUND_RESULT_FIELD_MISSED_SHOTS_COUNT, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field hitShotsCount = Field.of(ROUND_RESULT_FIELD_HIT_SHOTS_COUNT, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field totalShotsCount = Field.of(ROUND_RESULT_FIELD_TOTAL_SHOTS_COUNT, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field crashGameMultipliers = Field.of(ROUND_RESULT_CRASH_GAME_MULTIPLIERS, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field playerSeatNumber = Field.of(ROUND_RESULT_PLAYER_SEAT_NUMBER, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field playerRoundInfo = Field.of(ROUND_RESULT_FIELD_PLAYER_ROUND_INFO, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));

        Schema schema = Schema.of(sDate, cluster, subCasino, bank, currency, groupGameName, playerId, playerGroup, sessionId, gameSessionId,
                roundId, roundStartTime, roundEndTime, roomType, roomId, isBattleground, isPrivate, isOwner, roomValue, rakeValue, totalShots,
                totalWinningShots, totalBetsValue, totalWinValue, ggr, totalBonusBet, totalBonusWin, roundStartBalanceMC, roundEndBalanceMC,
                missedShotsCount, hitShotsCount, totalShotsCount,
                playerRoundInfo, crashGameMultipliers, playerSeatNumber);

        this.createTable(bigQuery, tableId, schema);
    }

    private void createTableForRoomsPlayers(BigQuery bigQuery, TableId tableId) {
        // Inserting data types
        Field sDate = Field.of(ROOMS_PLAYERS_FIELD_S_DATE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.TIMESTAMP));
        Field cluster = Field.of(ROOMS_PLAYERS_FIELD_CLUSTER, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field roomId = Field.of(ROOMS_PLAYERS_FIELD_ROOM_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.INT64));
        Field roomServerId = Field.of(ROOMS_PLAYERS_FIELD_ROOM_SERVER_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field isActive = Field.of(ROOMS_PLAYERS_FIELD_ROOM_IS_ACTIVE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.BOOL));
        Field isBattleground = Field.of(ROOMS_PLAYERS_FIELD_ROOM_IS_BATTLEGROUND, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.BOOL));
        Field isPrivate = Field.of(ROOMS_PLAYERS_FIELD_ROOM_IS_PRIVATE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.BOOL));
        Field buyInStake = Field.of(ROOMS_PLAYERS_FIELD_ROOM_BUY_IN_STAKE, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.NUMERIC));
        Field currency = Field.of(ROOMS_PLAYERS_FIELD_ROOM_CURRENCY, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field gameId = Field.of(ROOMS_PLAYERS_FIELD_ROOM_GAME_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.INT64));
        Field gameName = Field.of(ROOMS_PLAYERS_FIELD_ROOM_GAME_NAME, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field playerServerId = Field.of(ROOMS_PLAYERS_FIELD_PLAYER_SERVER_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field nickname = Field.of(ROOMS_PLAYERS_FIELD_PLAYER_NICKNAME, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field isOwner = Field.of(ROOMS_PLAYERS_FIELD_PLAYER_IS_OWNER, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.BOOL));
        Field sessionId = Field.of(ROOMS_PLAYERS_FIELD_PLAYER_SESSION_ID, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));
        Field seatNr = Field.of(ROOMS_PLAYERS_FIELD_PLAYER_SEAT_NR, LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.INT64));

        Schema schema = Schema.of(sDate, cluster, roomId, roomServerId, isActive, isBattleground, isPrivate, buyInStake, currency, gameId, gameName,
                playerServerId, nickname, isOwner, sessionId, seatNr);

        this.createTable(bigQuery, tableId, schema);
    }

    public List<Map<String, Object>> prepareRoomsPlayers(List<IRMSRoom> trmsRooms, int serverId) {

        LOG.debug("prepareRoomsPlayers: serverId={}, trmsRooms={}", serverId, trmsRooms);

        if (trmsRooms == null || trmsRooms.isEmpty()) {
            LOG.debug("prepareRoomsPlayers: trmsRooms is null or empty, return empty array");
            return new ArrayList<>();
        }

        List<Map<String, Object>> roomsPlayersRows = new ArrayList<>();
        try {

            long currentTimeMillis = System.currentTimeMillis();
            Timestamp currentTimestamp = new Timestamp(currentTimeMillis);

            for (IRMSRoom trmsRoom : trmsRooms) {

                LOG.debug("prepareRoomsPlayers: process trmsRoom={}", trmsRoom);

                List<IRMSPlayer> trmsPlayers = trmsRoom.getPlayers();
                if(trmsPlayers == null || trmsPlayers.isEmpty()) {
                    continue;
                }

                for(IRMSPlayer trmsPlayer : trmsPlayers) {


                    Map<String, Object> roomPlayerRow = new HashMap<>();

                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_S_DATE, currentTimestamp.toString());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_CLUSTER, serverId);
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_ROOM_ID, trmsRoom.getRoomId());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_ROOM_SERVER_ID, trmsRoom.getServerId());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_ROOM_IS_ACTIVE, trmsRoom.isIsActive());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_ROOM_IS_BATTLEGROUND, trmsRoom.isIsBattleground());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_ROOM_IS_PRIVATE, trmsRoom.isIsPrivate());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_ROOM_BUY_IN_STAKE, trmsRoom.getBuyInStake());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_ROOM_CURRENCY, trmsRoom.getCurrency());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_ROOM_GAME_ID, trmsRoom.getGameId());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_ROOM_GAME_NAME, trmsRoom.getGameName());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_PLAYER_SERVER_ID, trmsPlayer.getServerId());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_PLAYER_NICKNAME, trmsPlayer.getNickname());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_PLAYER_IS_OWNER, trmsPlayer.isIsOwner());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_PLAYER_SESSION_ID, trmsPlayer.getSessionId());
                    roomPlayerRow.put(ROOMS_PLAYERS_FIELD_PLAYER_SEAT_NR, trmsPlayer.getSeatNr());

                    LOG.debug("prepareRoomsPlayers: roomPlayerRow={}", roomPlayerRow);

                    roomsPlayersRows.add(roomPlayerRow);
                }
            }
        } catch (BigQueryException e) {
            LOG.error("prepareRoomsPlayers: Insert operation not performed", e);
            storeRoomsPlayersUnsavedRows(roomsPlayersRows);
        }

        LOG.debug("prepareRoomsPlayers: roomsPlayersRows.size()={}", roomsPlayersRows.size());

        return ImmutableList.copyOf(roomsPlayersRows);
    }

    public boolean saveRoomsPlayers(List<Map<String, Object>> roomsPlayersRows) {
        if (!isEnabled()) {
            LOG.debug("saveRoomsPlayers: skip function, google.cloud.bigquery.enabled={}, {}={}", enabled, credentialsEnvVariableName, credentialsEnvVariableNameValue);
            return false;
        }

        LOG.debug("saveRoomsPlayers: roomsPlayersRows={}", roomsPlayersRows);

        long saveRoomsPlayersStart = System.currentTimeMillis();

        try {

            TableId roomsPlayersTableId = TableId.of(googleCloudProjectId, datasetName, ROOMS_PLAYERS_TABLE_NAME);

            LOG.debug("saveRoomsPlayers: roomsPlayersTableId={}, " +
                    "googleCloudProjectId={}, datasetName={}, ROOMS_PLAYERS_TABLE_NAME={}",
                    roomsPlayersTableId, googleCloudProjectId, datasetName, ROOMS_PLAYERS_TABLE_NAME);

            BigQuery bigQuery = this.getBigquery();

            if(bigQuery == null) {
                LOG.error("saveRoomsPlayers: bigQuery is null, roomsPlayersTableId={}", roomsPlayersTableId);
                return false;
            }

            Table tableRoomsPlayers = bigQuery.getTable(roomsPlayersTableId);

            if (tableRoomsPlayers == null) {
                // Table does not exist, proceed to create it
                LOG.debug("saveRoomsPlayers: Table does not exist, proceed to create it");
                this.createTableForRoomsPlayers(bigQuery, roomsPlayersTableId);
            }

            boolean saved = this.insertRows(bigQuery, roomsPlayersTableId, roomsPlayersRows);

            long saveRoundResultsFinish = System.currentTimeMillis();
            LOG.debug("saveRoomsPlayers: function time {} ms to save rows success={} to table {},Google Cloud " +
                            "ProjectId={}, " + "Dataset Name={} round results: {} ", saveRoundResultsFinish - saveRoomsPlayersStart,
                    saved, ROOMS_PLAYERS_TABLE_NAME, googleCloudProjectId, datasetName, roomsPlayersRows);

            if (!saved) {
                storeRoomsPlayersUnsavedRows(roomsPlayersRows);
            }
            return saved;

        } catch (BigQueryException e) {
            LOG.error("saveRoomsPlayers: Insert operation not performed", e);
            storeRoomsPlayersUnsavedRows(roomsPlayersRows);
        }
        return false;
    }


    private void storeRoomsPlayersUnsavedRows(List<Map<String, Object>> roomsPlayersRows) {
        for (Map<String, Object> roomPlayerRow : roomsPlayersRows) {

            String id = String.format("room_player_raw_%s#%s#%s#%s",
                    roomPlayerRow.get(ROOMS_PLAYERS_FIELD_S_DATE),
                    roomPlayerRow.get(ROOMS_PLAYERS_FIELD_ROOM_ID),
                    roomPlayerRow.get(ROOMS_PLAYERS_FIELD_PLAYER_NICKNAME),
                    roomPlayerRow.get(ROOMS_PLAYERS_FIELD_PLAYER_SESSION_ID));

            storeToLocalFile(roomPlayerRow, id);
        }
    }

    public List<Map<String, Object>> prepareRoundResult(List<Pair<ISeat, IRoundResult>> seatsRoundResultsPairs, IRoom room) {


        if (seatsRoundResultsPairs == null || room == null) {
            LOG.error("prepareRoundResult: wrong function parameters seatsRoundResultsPairs={}, room={}", seatsRoundResultsPairs, room);
            return new ArrayList<>();
        }

        List<Map<String, Object>> roundResultRows = new ArrayList<>();
        try {

            for (Pair<ISeat, IRoundResult> seatRoundResultPair : seatsRoundResultsPairs) {

                ISeat seat = seatRoundResultPair.getKey();
                IRoundResult roundResult = seatRoundResultPair.getValue();
                IRoomPlayerInfo roomPlayerInfo = seat.getPlayerInfo();

                //we store only MMC records
                if (!isValidCurrency(roomPlayerInfo)) continue;

                int serverId = seat.getSocketClient() == null ? 0 : seat.getSocketClient().getServerId();
                boolean isBattleGroundRoom = room != null && room.isBattlegroundMode();
                boolean isPrivateRoom = false;
                boolean isRoomOwner = false;
                double rake = 0.0;

                if (roomPlayerInfo instanceof AbstractBattlegroundRoomPlayerInfo) {
                    isBattleGroundRoom = true;
                    AbstractBattlegroundRoomPlayerInfo abstractBattlegroundRoomPlayerInfo = (AbstractBattlegroundRoomPlayerInfo) roomPlayerInfo;
                    isPrivateRoom = abstractBattlegroundRoomPlayerInfo.isPrivateRoom();
                    isRoomOwner = abstractBattlegroundRoomPlayerInfo.isOwner();
                    rake = abstractBattlegroundRoomPlayerInfo.getBattlegroundRake();
                }
                IPlayerRoundInfo playerRoundInfo = seat.getCurrentPlayerRoundInfo();

                long endRoundTime = 0;
                long startRoundTime = 0;
                if (room != null && room instanceof AbstractGameRoom) {
                    endRoundTime = room.getGameState().getEndRoundTime();
                    startRoundTime = room.getGameState().getStartRoundTime();
                }

                long roundEndBalance = room != null ? room.getBalance(seat) : 0;
                List<String> additionalBetData = new ArrayList<>();
                double totalShots;
                double winningShots = 0;
                if (room.getGameType() == GameType.TRIPLE_MAX_BLAST) {
                    if (playerRoundInfo instanceof  PlayerRoundInfo) {
                        startRoundTime = ((PlayerRoundInfo) playerRoundInfo).getTimeOfRoundStart();
                    }
                    endRoundTime = roundResult.getDate();
                    roundEndBalance += (long) roundResult.getWinAmount();
                    if(seat instanceof com.betsoft.casino.mp.maxcrashgame.model.Seat) {
                        long canceledBetAmount = ((com.betsoft.casino.mp.maxcrashgame.model.Seat)seat).getCanceledBetAmount();
                        roundEndBalance += canceledBetAmount;
                    }
                    additionalBetData = parseMultipliers((PlayerRoundInfo) playerRoundInfo);
                    totalShots = roundResult.getCrashMultiplier();
                  /* In case of TRIPLE_MAX_BLAST we store all information in one record.
                     The other records (without starting balance) should be skipped */
                    if (playerRoundInfo != null && playerRoundInfo.getRoundStartBalance().getValue() == 0) {
                        continue;
                    }
                } else {
                    roundEndBalance = playerRoundInfo.getRoundStartBalance().toCents() -
                            (playerRoundInfo.getTotalBets().toCents() - playerRoundInfo.getTotalPayouts().toCents());
                    totalShots = roundResult.getHitCount() + roundResult.getMissCount();
                    winningShots = roundResult.getHitCount();
                }

                long currentTimeMillis = System.currentTimeMillis();
                Timestamp currentTimestamp = new Timestamp(currentTimeMillis);

                Timestamp roundStartTimestamp = new Timestamp(startRoundTime);
                Timestamp roundEndTimestamp = new Timestamp(endRoundTime);

                Map<String, Object> roundResultRow = new HashMap<>();

                roundResultRow.put(ROUND_RESULT_FIELD_S_DATE, currentTimestamp.toString());
                roundResultRow.put(ROUND_RESULT_FIELD_CLUSTER, serverId);
                //roundResultRow.put(ROUND_RESULT_FIELD_SUB_CASINO, null);
                roundResultRow.put(ROUND_RESULT_FIELD_BANK, roomPlayerInfo.getBankId());
                roundResultRow.put(ROUND_RESULT_FIELD_CURRENCY, roomPlayerInfo.getCurrency().getCode());
                roundResultRow.put(ROUND_RESULT_FIELD_GROUP_GAME_NAME, room.getGameType().getGameId());
                roundResultRow.put(ROUND_RESULT_FIELD_PLAYER_ID, roomPlayerInfo.getNickname());
                //roundResultRow.put(ROUND_RESULT_FIELD_PLAYER_GROUP, null);
                roundResultRow.put(ROUND_RESULT_FIELD_SESSION_ID, roomPlayerInfo.getSessionId());
                roundResultRow.put(ROUND_RESULT_FIELD_GAME_SESSION_ID, roomPlayerInfo.getGameSessionId());
                roundResultRow.put(ROUND_RESULT_FIELD_ROUND_ID, roundResult.getRoundId());
                roundResultRow.put(ROUND_RESULT_FIELD_ROUND_START_TIME, roundStartTimestamp.toString());
                roundResultRow.put(ROUND_RESULT_FIELD_ROUND_END_TIME, roundEndTimestamp.toString());
                roundResultRow.put(ROUND_RESULT_FIELD_ROOM_TYPE, room.getGameType().name());
                roundResultRow.put(ROUND_RESULT_FIELD_ROOM_ID, roomPlayerInfo.getRoomId());
                roundResultRow.put(ROUND_RESULT_FIELD_IS_BATTLEGROUND, isBattleGroundRoom);
                roundResultRow.put(ROUND_RESULT_FIELD_IS_PRIVATE, isPrivateRoom);
                roundResultRow.put(ROUND_RESULT_FIELD_IS_OWNER, isRoomOwner);
                roundResultRow.put(ROUND_RESULT_FIELD_ROOM_VALUE, playerRoundInfo.getRoomStake());
                roundResultRow.put(ROUND_RESULT_FIELD_RAKE_VALUE, rake);
                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_SHOTS, totalShots);
                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_WINNING_SHOTS, winningShots);

                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_BETS_VALUE, playerRoundInfo.getTotalBets().toCents());
                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_WIN_VALUE, playerRoundInfo.getTotalPayouts().toCents());
                roundResultRow.put(ROUND_RESULT_FIELD_GGR, playerRoundInfo.getTotalBets().toCents() - playerRoundInfo.getTotalPayouts().toCents());
                //roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_BONUS_BET, null);
                //roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_BONUS_WIN, null);
                roundResultRow.put(ROUND_RESULT_FIELD_ROUND_START_BALANCE, playerRoundInfo.getRoundStartBalance().toCents());
                roundResultRow.put(ROUND_RESULT_FIELD_ROUND_END_BALANCE, roundEndBalance);
                roundResultRow.put(ROUND_RESULT_FIELD_MISSED_SHOTS_COUNT, roundResult.getMissCount());
                roundResultRow.put(ROUND_RESULT_FIELD_HIT_SHOTS_COUNT, roundResult.getHitCount());
                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_SHOTS_COUNT, roundResult.getMissCount() + roundResult.getHitCount());
                roundResultRow.put(ROUND_RESULT_PLAYER_SEAT_NUMBER, roomPlayerInfo.getSeatNumber());

                try {
                    roundResultRow.put(ROUND_RESULT_CRASH_GAME_MULTIPLIERS, objectMapper.writeValueAsString(additionalBetData));
                    roundResultRow.put(ROUND_RESULT_FIELD_PLAYER_ROUND_INFO, objectMapper.writeValueAsString(seat.getCurrentPlayerRoundInfo()));
                } catch (JsonProcessingException e) {
                    LOG.error("prepareRoundResult: Json objects are not stored", e);
                }

                roundResultRows.add(roundResultRow);
            }
        } catch (BigQueryException e) {
            LOG.error("prepareRoundResult: Insert operation not performed", e);
            storeRoundResultsUnsavedRows(roundResultRows);
        }
        return ImmutableList.copyOf(roundResultRows);
    }

    public boolean saveRoundResults(List<Map<String, Object>> roundResultRows) {
        if (!isEnabled()) {
            LOG.debug("saveRoundResults: skip function, google.cloud.bigquery.enabled={}, {}={}", enabled, credentialsEnvVariableName, credentialsEnvVariableNameValue);
            return false;
        }
        long saveRoundResultsStart = System.currentTimeMillis();

        try {

            TableId roundResultTableId = TableId.of(googleCloudProjectId, datasetName, ROUND_RESULT_TABLE_NAME);

            LOG.debug("saveRoundResults: roundResultTableId={}, " +
                            "googleCloudProjectId={}, datasetName={}, ROOMS_PLAYERS_TABLE_NAME={}",
                    roundResultTableId, googleCloudProjectId, datasetName, ROOMS_PLAYERS_TABLE_NAME);

            BigQuery bigQuery = this.getBigquery();

            if(bigQuery == null) {
                LOG.error("saveRoundResults: bigQuery is null, roundResultTableId={}", roundResultTableId);
                return false;
            }

            Table tableRoundResult = bigQuery.getTable(roundResultTableId);

            if (tableRoundResult == null) {
                // Table does not exist, proceed to create it
                LOG.debug("saveRoundResults: Table does not exist, proceed to create it");
                this.createTableForRoundResults(bigQuery, roundResultTableId);
            }

            boolean saved = this.insertRows(bigQuery, roundResultTableId, roundResultRows);

            long saveRoundResultsFinish = System.currentTimeMillis();
            LOG.debug("saveRoundResults: function time {} ms to save rows success={} to table {},Google Cloud " +
                            "ProjectId={}, " + "Dataset Name={} round results: {} ", saveRoundResultsFinish - saveRoundResultsStart,
                    saved, ROUND_RESULT_TABLE_NAME, googleCloudProjectId, datasetName, roundResultRows);

            if (!saved) {
                storeRoundResultsUnsavedRows(roundResultRows);
            }
            return saved;

        } catch (BigQueryException e) {
            LOG.error("saveRoundResults: Insert operation not performed", e);
            storeRoundResultsUnsavedRows(roundResultRows);
        }

        return false;
    }

    private void storeRoundResultsUnsavedRows(List<Map<String, Object>> roundResultRows) {
        for (Map<String, Object> roundResultRow : roundResultRows) {

            String id = String.format("round_result_raw_%s#%s#%s#%s",
                    roundResultRow.get(ROUND_RESULT_FIELD_S_DATE),
                    roundResultRow.get(ROUND_RESULT_FIELD_ROUND_ID),
                    roundResultRow.get(ROUND_RESULT_FIELD_PLAYER_ID),
                    roundResultRow.get(ROUND_RESULT_FIELD_SESSION_ID));

            storeToLocalFile(roundResultRow, id);
        }
    }

    private void storeToLocalFile(Object object, String id) {
        String filepath = String.format("/www/logs/tomcat.mp/bigquery/%s.json", id);
        objectFileWriterUtil.saveObjectAsJsonToFile(object, filepath);
    }

    @Override
    public List<Map<String, Object>> prepareBattlegroundRoundResults(List<Pair<ISeat, IRoundResult>> seatsRoundResultsPairs, IRoom room) {

        if (seatsRoundResultsPairs == null || room == null) {
            LOG.warn("saveBattlegroundRoundResults: seatsRoundResultsPairs seatsRoundResultsPairs list is empty skip to save it to bigQuery ");
            return new ArrayList<>();
        }

        List<Map<String, Object>> roundResultRows = new ArrayList<>();


        for (Pair<ISeat, IRoundResult> seatRoundResultPair : seatsRoundResultsPairs) {

            ISeat seat = seatRoundResultPair.getKey();
            IRoundResult roundResult = seatRoundResultPair.getValue();
            IRoomPlayerInfo roomPlayerInfo = seat.getPlayerInfo();
            roomPlayerInfo.getSeatNumber();
            if (!isValidCurrency(roomPlayerInfo)) continue;


            int serverId = seat.getSocketClient() == null ? 0 : seat.getSocketClient().getServerId();
            boolean isBattleGroundRoom = room != null && room.isBattlegroundMode();
            boolean isPrivateRoom = false;
            boolean isRoomOwner = false;
            double rake = 0.0;
            double roundBuyInValue = 0;
            IBgPlace bgPlace = null;

            if (roomPlayerInfo instanceof AbstractBattlegroundRoomPlayerInfo) {
                isBattleGroundRoom = true;
                AbstractBattlegroundRoomPlayerInfo abstractBattlegroundRoomPlayerInfo = (AbstractBattlegroundRoomPlayerInfo) roomPlayerInfo;
                isPrivateRoom = abstractBattlegroundRoomPlayerInfo.isPrivateRoom();
                isRoomOwner = abstractBattlegroundRoomPlayerInfo.isOwner();
                rake = abstractBattlegroundRoomPlayerInfo.getBattlegroundRake();
                IBattlegroundRoundInfo battlegroundRoundInfo = abstractBattlegroundRoomPlayerInfo.getBattlegroundRoundInfo();
                if (battlegroundRoundInfo != null) {
                    roundBuyInValue = battlegroundRoundInfo.getBuyIn();
                    List<IBgPlace> bgPlaces = battlegroundRoundInfo.getPlaces();
                    if (bgPlaces != null) {
                        bgPlace = bgPlaces.stream().filter(p -> p.getAccountId() == roomPlayerInfo.getId()).findFirst().orElse(null);
                    }
                }
            }

            IPlayerRoundInfo playerRoundInfo = seat.getCurrentPlayerRoundInfo();

            long winValue = bgPlace != null ? bgPlace.getWin() : 0;
            long roundEndBalance = room != null ? room.getBalance(seat) : 0;
            Long score = null;

            if (room.getGameType().isBattleGroundGame()) {
                if (room.getGameType() == GameType.BG_MAXCRASHGAME) {
                    winValue = (long) roundResult.getWinAmount();
                }
                roundEndBalance += winValue;
                if(seat instanceof com.betsoft.casino.mp.maxblastchampions.model.Seat) {
                    long canceledBetAmount = ((com.betsoft.casino.mp.maxblastchampions.model.Seat)seat).getCanceledBetAmount();
                    roundEndBalance += canceledBetAmount;
                }
            }

            long endRoundTime = 0;
            long startRoundTime = 0;
            if (room != null && room instanceof AbstractGameRoom) {
                endRoundTime = room.getGameState().getEndRoundTime();
                startRoundTime = room.getGameState().getStartRoundTime();
            }

            if (room.getGameType() == GameType.BG_MAXCRASHGAME) {
                endRoundTime = playerRoundInfo.getTimeOfRoundEnd();
                if (playerRoundInfo instanceof com.betsoft.casino.mp.maxblastchampions.model.PlayerRoundInfo) {
                    startRoundTime = ((com.betsoft.casino.mp.maxblastchampions.model.PlayerRoundInfo) playerRoundInfo).getTimeOfRoundStart();
                }
            } else {
                Double winAmount = roundResult.getWinAmount();
                score = winAmount.longValue();
            }

            long currentTimeMillis = System.currentTimeMillis();
            Timestamp currentTimestamp = new Timestamp(currentTimeMillis);

            Timestamp roundStartTimestamp = new Timestamp(startRoundTime);
            Timestamp roundEndTimestamp = new Timestamp(endRoundTime);

            Map<String, Object> roundResultRow = new HashMap<>();

            roundResultRow.put(ROUND_RESULT_FIELD_S_DATE, currentTimestamp.toString());
            roundResultRow.put(ROUND_RESULT_FIELD_CLUSTER, serverId);
            //roundResultRow.put(ROUND_RESULT_FIELD_SUB_CASINO, null);
            roundResultRow.put(ROUND_RESULT_FIELD_BANK, roomPlayerInfo.getBankId());
            roundResultRow.put(ROUND_RESULT_FIELD_CURRENCY, roomPlayerInfo.getCurrency().getCode());
            roundResultRow.put(ROUND_RESULT_FIELD_GROUP_GAME_NAME, room.getGameType().getGameId());
            roundResultRow.put(ROUND_RESULT_FIELD_PLAYER_ID, roomPlayerInfo.getNickname());
            //roundResultRow.put(ROUND_RESULT_FIELD_PLAYER_GROUP, null);
            roundResultRow.put(ROUND_RESULT_FIELD_SESSION_ID, roomPlayerInfo.getSessionId());
            roundResultRow.put(ROUND_RESULT_FIELD_GAME_SESSION_ID, roomPlayerInfo.getGameSessionId());
            roundResultRow.put(ROUND_RESULT_FIELD_ROUND_ID, roundResult.getRoundId());
            roundResultRow.put(ROUND_RESULT_FIELD_ROUND_START_TIME, roundStartTimestamp.toString());
            roundResultRow.put(ROUND_RESULT_FIELD_ROUND_END_TIME, roundEndTimestamp.toString());
            roundResultRow.put(ROUND_RESULT_FIELD_ROOM_TYPE, room.getGameType().name());
            roundResultRow.put(ROUND_RESULT_FIELD_ROOM_ID, roomPlayerInfo.getRoomId());
            roundResultRow.put(ROUND_RESULT_FIELD_IS_BATTLEGROUND, isBattleGroundRoom);
            roundResultRow.put(ROUND_RESULT_FIELD_IS_PRIVATE, isPrivateRoom);
            roundResultRow.put(ROUND_RESULT_FIELD_IS_OWNER, isRoomOwner);
            roundResultRow.put(ROUND_RESULT_FIELD_ROOM_VALUE, roundBuyInValue);
            roundResultRow.put(ROUND_RESULT_FIELD_RAKE_VALUE, rake);
            roundResultRow.put(ROUND_RESULT_FIELD_MISSED_SHOTS_COUNT, roundResult.getMissCount());
            roundResultRow.put(ROUND_RESULT_FIELD_HIT_SHOTS_COUNT, roundResult.getHitCount());
            roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_SHOTS_COUNT, roundResult.getMissCount() + roundResult.getHitCount());
            roundResultRow.put(ROUND_RESULT_PLAYER_SEAT_NUMBER, roomPlayerInfo.getSeatNumber());

            if (room.getGameType() == GameType.BG_MAXCRASHGAME) {
                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_SHOTS, roundResult.getCrashMultiplier());
                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_WINNING_SHOTS, bgPlace != null ? bgPlace.getEjectPoint() : 0);
                roundResultRow.put(ROUND_RESULT_CRASH_GAME_MULTIPLIERS, bgPlace != null ? bgPlace.getEjectPoint() : 0);

            } else {
                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_SHOTS, roundResult.getHitCount() + roundResult.getMissCount());
                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_WINNING_SHOTS, roundResult.getHitCount());
            }

            roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_BETS_VALUE, roundBuyInValue);
            roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_WIN_VALUE, winValue);
            roundResultRow.put(ROUND_RESULT_FIELD_GGR, roundBuyInValue - winValue);
            //roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_BONUS_BET, null);
            if(score != null) {
                roundResultRow.put(ROUND_RESULT_FIELD_TOTAL_BONUS_WIN, score);
            }
            roundResultRow.put(ROUND_RESULT_FIELD_ROUND_START_BALANCE, playerRoundInfo.getRoundStartBalance().toCents());
            roundResultRow.put(ROUND_RESULT_FIELD_ROUND_END_BALANCE, roundEndBalance);

            try {
                roundResultRow.put(ROUND_RESULT_FIELD_PLAYER_ROUND_INFO, objectMapper.writeValueAsString(seat.getCurrentPlayerRoundInfo()));
            } catch (JsonProcessingException e) {
                LOG.error("saveBattlegroundRoundResults: Json objects are not stored", e);
            }
            roundResultRows.add(roundResultRow);
        }
        return ImmutableList.copyOf(roundResultRows);
    }

    private boolean isValidCurrency(IRoomPlayerInfo roomPlayerInfo) {
        String[] split = validCurrencies.split(",");

        //we store only MMC and MQC records
        if (roomPlayerInfo.getCurrency() != null && Arrays.stream(split).noneMatch(s -> s.equals(roomPlayerInfo.getCurrency().getCode()))) {
            LOG.debug("saveRoundResults: row is skipped. We do not store rows with {} currency ", roomPlayerInfo.getCurrency().getCode());
            return false;
        }
        return true;
    }

    public void setObjectFileWriterUtil(ObjectFileWriterUtil objectFileWriterUtil) {
        this.objectFileWriterUtil = objectFileWriterUtil;
    }

    private static List<String> parseMultipliers(PlayerRoundInfo playerRoundInfo) {
        List<String> additionalBetData;
        additionalBetData = playerRoundInfo.getAdditionalBetData();
        additionalBetData = additionalBetData.stream().map(s ->
                {
                    String[] parts = s.split("\\|");
                    if (!parts[1].equals("0")) {
                        return parts[2];
                    }
                    return "0";
                }

        ).collect(Collectors.toList());
        return additionalBetData;
    }
}
