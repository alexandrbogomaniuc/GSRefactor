package com.betsoft.casino.mp.service;

import com.abs.casino.common.cache.data.currency.ICurrency;
import com.betsoft.casino.mp.common.AbstractGameRoom;
import com.betsoft.casino.mp.maxblastchampions.model.PlayerRoundInfo;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.playerinfo.CrashGameBGRoomPlayerInfo;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.transport.Avatar;
import com.betsoft.casino.mp.transport.Currency;
import com.betsoft.casino.mp.utils.ObjectFileWriterUtil;
import com.dgphoenix.casino.common.util.Pair;
import com.google.cloud.bigquery.*;
import com.google.cloud.http.HttpTransportOptions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class BigQueryClientServiceTest {
  private final String googleCloudProjectId = "test-maxquest";
  private final String datasetName = "mqmp_test";

  @Mock
  private BigQuery mockBigQuery;
  @Mock
  private Table tableRoundResult;
  @Mock
  private Table tableRoomsPlayers;

  @Mock
  private ObjectFileWriterUtil mockObjectFileWriterUtil;

  private BigQueryClientService bigQueryClientService;

  @Before
  public void setUp() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    MockitoAnnotations.initMocks(this);
    bigQueryClientService = new BigQueryClientService();
    bigQueryClientService.setBigquery(mockBigQuery); // Inject mock BigQuery
    bigQueryClientService.setEnabled(true); // Enable the service for testing
    bigQueryClientService.setDatasetName("test-dataset");
    bigQueryClientService.setGoogleCloudProjectId("test-project-id");
    bigQueryClientService.setObjectFileWriterUtil(mockObjectFileWriterUtil);
    bigQueryClientService.setValidCurrencies("MMC,MQC");

  }

  @Test
  public void testSaveRoomsPlayersWhenDisabled() {
    bigQueryClientService.setEnabled(false); // Disable service
    boolean result = bigQueryClientService.saveRoomsPlayers(new ArrayList<>());
    verify(mockBigQuery, never()).insertAll(any(InsertAllRequest.class)); // Verify insertAll was never called
    assertFalse(result); // Assert that the method returned false
  }

  @Test
  public void testSaveRoomsPlayersWithSuccessfulInsert() {

    // Given
    int serverId = 1;
    List<IRMSRoom> trmsRooms = new ArrayList<>();

    RMSRoom trmsRoom1 = new RMSRoom();

    trmsRoom1.setRoomId(101);
    trmsRoom1.setServerId(2);
    trmsRoom1.setIsActive(true);
    trmsRoom1.setIsBattleground(true);
    trmsRoom1.setIsPrivate(true);
    trmsRoom1.setBuyInStake(100);
    trmsRoom1.setCurrency("MMC");
    trmsRoom1.setGameId(867);
    trmsRoom1.setGameName("BG_SECTOR_X");
    trmsRooms.add(trmsRoom1);

    List<IRMSPlayer> trmsPlayers1 = new ArrayList<>();
    trmsRoom1.setPlayers(trmsPlayers1);

    RMSPlayer trmsPlayer11 = new RMSPlayer();
    trmsPlayers1.add(trmsPlayer11);

    trmsPlayer11.setServerId(2);
    trmsPlayer11.setNickname("player11");
    trmsPlayer11.setIsOwner(true);
    trmsPlayer11.setSessionId("session_id_11");
    trmsPlayer11.setSeatNr(1);

    RMSPlayer trmsPlayer12 = new RMSPlayer();
    trmsPlayers1.add(trmsPlayer12);

    trmsPlayer12.setServerId(2);
    trmsPlayer12.setNickname("player12");
    trmsPlayer12.setIsOwner(true);
    trmsPlayer12.setSessionId("session_id_12");
    trmsPlayer12.setSeatNr(2);

    RMSRoom trmsRoom2 = new RMSRoom();
    trmsRooms.add(trmsRoom2);

    trmsRoom2.setRoomId(102);
    trmsRoom2.setServerId(3);
    trmsRoom2.setIsActive(true);
    trmsRoom2.setIsBattleground(true);
    trmsRoom2.setIsPrivate(false);
    trmsRoom2.setBuyInStake(200000);
    trmsRoom2.setCurrency("MQC");
    trmsRoom2.setGameId(867);
    trmsRoom2.setGameName("BG_SECTOR_X");

    List<IRMSPlayer> trmsPlayers2 = new ArrayList<>();
    trmsRoom2.setPlayers(trmsPlayers2);

    RMSPlayer trmsPlayer21 = new RMSPlayer();
    trmsPlayers2.add(trmsPlayer21);

    trmsPlayer21.setServerId(3);
    trmsPlayer21.setNickname("player21");
    trmsPlayer21.setIsOwner(false);
    trmsPlayer21.setSessionId("session_id_21");
    trmsPlayer21.setSeatNr(1);

    RMSPlayer trmsPlayer22 = new RMSPlayer();
    trmsPlayers2.add(trmsPlayer22);

    trmsPlayer22.setServerId(3);
    trmsPlayer22.setNickname("player22");
    trmsPlayer22.setIsOwner(true);
    trmsPlayer22.setSessionId("session_id_22");
    trmsPlayer22.setSeatNr(2);

    InsertAllResponse mockInsertAllResponse = mock(InsertAllResponse.class);

    when(mockBigQuery.getTable(any(TableId.class))).thenReturn(tableRoomsPlayers);
    when(mockInsertAllResponse.hasErrors()).thenReturn(false);
    when(mockBigQuery.insertAll(any(InsertAllRequest.class))).thenReturn(mockInsertAllResponse);

    // When
    List<Map<String, Object>> rows = bigQueryClientService.prepareRoomsPlayers(trmsRooms, serverId);
    boolean result = bigQueryClientService.saveRoomsPlayers(rows);

    // Then
    assertTrue(result);

    ArgumentCaptor<InsertAllRequest> requestCaptor = ArgumentCaptor.forClass(InsertAllRequest.class);
    verify(mockBigQuery).insertAll(requestCaptor.capture());

    InsertAllRequest capturedRequest = requestCaptor.getValue();
    Map<String, Object> content = capturedRequest.getRows().get(0).getContent();

    assertNotNull(content.get("s_date"));
    assertEquals(1, content.get("cluster"));
    assertEquals(101L, content.get("room_id"));
    assertEquals( 2, content.get("room_serverId"));
    assertEquals(true, content.get("room_isActive"));
    assertEquals(true, content.get("room_isBattleground"));
    assertEquals(true, content.get("room_isPrivate"));
    assertEquals(100L, content.get("room_buyInStake"));
    assertEquals("MMC", content.get("room_currency"));
    assertEquals( 867L, content.get("room_gameId"));
    assertEquals("BG_SECTOR_X", content.get("room_gameName"));
    assertEquals(2, content.get("player_serverId"));
    assertEquals("player11", content.get("player_nickname"));
    assertEquals(true, content.get("player_isOwner"));
    assertEquals("session_id_11", content.get("player_sessionId"));
    assertEquals(1, content.get("player_seatNr"));

    assertNotNull(capturedRequest);
  }

  @Test
  public void testStoreRoomsPlayersUnsavedRowsMethodIsCalled() {
    // Given
    int serverId = 1;
    List<IRMSRoom> trmsRooms = new ArrayList<>();

    RMSRoom trmsRoom1 = new RMSRoom();
    trmsRooms.add(trmsRoom1);

    trmsRoom1.setRoomId(101);
    trmsRoom1.setServerId(2);
    trmsRoom1.setIsActive(true);
    trmsRoom1.setIsBattleground(true);
    trmsRoom1.setIsPrivate(true);
    trmsRoom1.setBuyInStake(100);
    trmsRoom1.setCurrency("MMC");
    trmsRoom1.setGameId(867);
    trmsRoom1.setGameName("BG_SECTOR_X");

    List<IRMSPlayer> trmsPlayers1 = new ArrayList<>();
    trmsRoom1.setPlayers(trmsPlayers1);

    RMSPlayer trmsPlayer11 = new RMSPlayer();
    trmsPlayers1.add(trmsPlayer11);

    trmsPlayer11.setServerId(2);
    trmsPlayer11.setNickname("player11");
    trmsPlayer11.setIsOwner(true);
    trmsPlayer11.setSessionId("session_id_11");
    trmsPlayer11.setSeatNr(1);

    doThrow(new BigQueryException(-1, "Test Exception")).when(mockBigQuery).insertAll(any());

    ArgumentCaptor<Object> objectCaptor = ArgumentCaptor.forClass(Object.class);
    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

    InsertAllResponse mockInsertAllResponse = mock(InsertAllResponse.class);

    when(mockBigQuery.getTable(any(TableId.class))).thenReturn(tableRoomsPlayers);
    when(mockInsertAllResponse.hasErrors()).thenReturn(false);

    // When
    List<Map<String, Object>> rows = bigQueryClientService.prepareRoomsPlayers(trmsRooms, serverId);
    bigQueryClientService.saveRoomsPlayers(rows);

    // Then
    verify(mockObjectFileWriterUtil).saveObjectAsJsonToFile(objectCaptor.capture(), stringCaptor.capture());
    assertNotNull("Object passed to saveObjectAsJsonToFile should not be null", objectCaptor.getValue());

    String regex = "/www/logs/tomcat\\.mp/bigquery/room_player_raw_\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}#101#player11#session_id_11\\.json";
    assertTrue("File path passed to saveObjectAsJsonToFile should match expected pattern, File path is: " + stringCaptor.getValue() + " regex: " + regex,
            stringCaptor.getValue().matches(regex));
  }

  @Test
  public void testSaveRoundResultsWhenDisabled() {
    bigQueryClientService.setEnabled(false); // Disable service
    boolean result = bigQueryClientService.saveRoundResults(new ArrayList<>());
    verify(mockBigQuery, never()).insertAll(any(InsertAllRequest.class)); // Verify insertAll was never called
    assertFalse(result); // Assert that the method returned false
  }

  @Test
  public void testSaveRoundResultsWithSuccessfulInsert() {

    // Given
    ISeat mockSeat = mock(ISeat.class);
    IRoundResult mockRoundResult = mock(IRoundResult.class);
    AbstractGameRoom mockRoom = mock(AbstractGameRoom.class);
    IGameState gameState = mock(IGameState.class);
    PlayerRoundInfo playerRoundInfo = createPlayerRoundInfoWithRandomValues();
    IRoomPlayerInfo playerInfo = createPlayerInfo();
    InsertAllResponse mockInsertAllResponse = mock(InsertAllResponse.class);

    when(mockRoom.getLastRoundStartTime()).thenReturn(1707782400000L);
    when(mockRoom.getGameState()).thenReturn(gameState);
    when(gameState.getStartRoundTime()).thenReturn(1707782400000L);
    when(gameState.getEndRoundTime()).thenReturn(1707782460000L);
    when(mockBigQuery.getTable(any(TableId.class))).thenReturn(tableRoundResult);
    when(mockInsertAllResponse.hasErrors()).thenReturn(false);
    when(mockBigQuery.insertAll(any(InsertAllRequest.class))).thenReturn(mockInsertAllResponse);
    when(mockSeat.getCurrentPlayerRoundInfo()).thenReturn(playerRoundInfo);
    when(mockSeat.getPlayerInfo()).thenReturn(playerInfo);
    when(mockRoom.getGameType()).thenReturn(GameType.BG_MAXCRASHGAME);

    Pair<ISeat, IRoundResult> pair = new Pair<>(mockSeat, mockRoundResult);
    List<Pair<ISeat, IRoundResult>> pairs = Collections.singletonList(pair);

    // When
    List<Map<String, Object>> rows = bigQueryClientService.prepareRoundResult(pairs, mockRoom);
    boolean result = bigQueryClientService.saveRoundResults(rows);

    // Then
    assertTrue(result);

    ArgumentCaptor<InsertAllRequest> requestCaptor = ArgumentCaptor.forClass(InsertAllRequest.class);
    verify(mockBigQuery).insertAll(requestCaptor.capture());

    InsertAllRequest capturedRequest = requestCaptor.getValue();
    Map<String, Object> content = capturedRequest.getRows().get(0).getContent();

    assertEquals(1L, content.get("Bank"));
    assertEquals("playerNickname", content.get("Player_ID"));
    assertEquals(0L, content.get("Round_Start_Balance_mc"));
    assertEquals(0.0, content.get("Total_Shots"));
    assertEquals(false, content.get("Is_Owner"));
    assertEquals(0L, content.get("Round_ID"));
    assertEquals(true, content.get("Is_Battleground"));
    assertEquals(false, content.get("Is_Private"));
    assertEquals("MMC", content.get("Currency"));
    assertEquals("BG_MAXCRASHGAME", content.get("Room_Type"));
    assertEquals("session123", content.get("Session_ID"));
    assertEquals(0.0, content.get("Total_winning_shots"));
    assertNotNull(content.get("s_date"));
    assertEquals(0L, content.get("Round_End_Balance_mc"));
    assertEquals(864L, content.get("Group_Game_Name"));
    assertEquals(100L, content.get("Room_ID"));
    assertEquals("2024-02-13 00:00:00.0", content.get("Round_Start_Time"));
    assertEquals("2024-02-13 00:01:00.0", content.get("Round_End_Time"));
    assertEquals(0, content.get("Cluster"));
    assertEquals(10.0, content.get("Rake_value"));
    assertEquals(0L, content.get("Total_Bets_value"));
    assertEquals(0L, content.get("Room_Value"));

    assertNotNull(capturedRequest);
  }

  @Test
  public void testStoreRoundResultsUnsavedRowsMethodIsCalled() {
    // Given
    doThrow(new BigQueryException(-1, "Test Exception")).when(mockBigQuery).insertAll(any());

    ArgumentCaptor<Object> objectCaptor = ArgumentCaptor.forClass(Object.class);
    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

    ISeat mockSeat = mock(ISeat.class);
    IRoundResult mockRoundResult = mock(IRoundResult.class);
    IRoom mockRoom = mock(IRoom.class);
    PlayerRoundInfo playerRoundInfo = createPlayerRoundInfoWithRandomValues();
    IRoomPlayerInfo playerInfo = createPlayerInfo();
    InsertAllResponse mockInsertAllResponse = mock(InsertAllResponse.class);

    when(mockBigQuery.getTable(any(TableId.class))).thenReturn(tableRoundResult);
    when(mockInsertAllResponse.hasErrors()).thenReturn(false);
    when(mockSeat.getCurrentPlayerRoundInfo()).thenReturn(playerRoundInfo);
    when(mockSeat.getPlayerInfo()).thenReturn(playerInfo);
    when(mockRoom.getGameType()).thenReturn(GameType.BG_MAXCRASHGAME);

    Pair<ISeat, IRoundResult> pair = new Pair<>(mockSeat, mockRoundResult);
    List<Pair<ISeat, IRoundResult>> pairs = Collections.singletonList(pair);

    // When
    List<Map<String, Object>> rows = bigQueryClientService.prepareRoundResult(pairs, mockRoom);
    bigQueryClientService.saveRoundResults(rows);

    // Then
    verify(mockObjectFileWriterUtil).saveObjectAsJsonToFile(objectCaptor.capture(), stringCaptor.capture());
    assertNotNull("Object passed to saveObjectAsJsonToFile should not be null", objectCaptor.getValue());

    String regex = "/www/logs/tomcat\\.mp/bigquery/round_result_raw_\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}#0#playerNickname#session123\\.json";
    assertTrue("File path passed to saveObjectAsJsonToFile should match expected pattern, File path is: " + stringCaptor.getValue() + " regex: " + regex,
        stringCaptor.getValue().matches(regex));
  }

  private IRoomPlayerInfo createPlayerInfo() {
    long id = 1L;
    long bankId = 1L;
    long roomId = 100L;
    int seatNumber = 1;
    String sessionId = "session123";
    long gameSessionId = 200L;
    String nickname = "playerNickname";
    IAvatar avatar = new Avatar();
    long enterDate = System.currentTimeMillis();
    ICurrency currency = new Currency("MMC", "MMC");
    IPlayerStats stats = new PlayerStats();
    boolean showRefreshButton = true;
    Map<Integer, Integer> weapons = new HashMap<>();
    weapons.put(1, 10); // Example weapon id and count
    long stake = 500L;
    int stakesReserve = 5;
    MaxQuestWeaponMode weaponMode = MaxQuestWeaponMode.PAID_SHOTS; // Assuming this is an enum or class you have access to
    boolean allowWeaponSaveInAllGames = true;
    double battlegroundRake = 10.0;

    CrashGameBGRoomPlayerInfo playerInfo = new CrashGameBGRoomPlayerInfo(
        id, bankId, roomId, seatNumber, sessionId, gameSessionId, nickname, avatar, enterDate,
        currency, stats, showRefreshButton, weapons, stake, stakesReserve, weaponMode,
        allowWeaponSaveInAllGames, battlegroundRake);
    return playerInfo;

  }

  private PlayerRoundInfo createPlayerRoundInfoWithRandomValues() {
    long roomId = new Random().nextInt(1000); // Example room ID
    int gameId = new Random().nextInt(10); // Example game ID

    PlayerRoundInfo playerRoundInfo = new PlayerRoundInfo(roomId, gameId);

    // Set random or fixed values for other fields
    playerRoundInfo.setTimeOfRoundEnd(1707782460000L);
    playerRoundInfo.setAdditionalBetData(Arrays.asList("data1", "data2")); // Example additional bet data
    playerRoundInfo.setSalt(UUID.randomUUID().toString());
    playerRoundInfo.setCrashMult(Math.round(new Random().nextDouble() * 100.0) / 100.0); // Example crash multiplier
    playerRoundInfo.setTotalPot(new Random().nextInt(10000));
    playerRoundInfo.setRefundAmount(new Random().nextInt(5000));
    playerRoundInfo.setTotalPotWithoutRake(new Random().nextInt(9000));
    playerRoundInfo.setRake(Math.round(new Random().nextDouble() * 100.0) / 100.0); // Example rake
    playerRoundInfo.setKilometerMult(Math.round(new Random().nextDouble() * 10.0) / 10.0); // Example kilometer multiplier

    return playerRoundInfo;
  }

  @Ignore
  @Test
  public void createTableInsertData() {

    try {
      //Assign
      String tableName = "TEST02";
      int connectTimeout = 100;

      BigQueryClientService bigQueryClientService = new BigQueryClientService();
      bigQueryClientService.setEnabled(true);

      BigQuery bigquery = BigQueryOptions.newBuilder()
          .setProjectId(googleCloudProjectId)
          .setTransportOptions(HttpTransportOptions.newBuilder()
              .setConnectTimeout(connectTimeout * 10)
              .setReadTimeout(connectTimeout * 10)
              .build())
          .build()
          .getService();

      TableId tableId = TableId.of(datasetName, tableName);
      Table tableRoundResult = bigquery.getTable(tableId);

      //Action
      if (tableRoundResult == null) {
        // Table does not exist, proceed to create it
        Field dateTime = Field.of("dateTime", LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.TIMESTAMP));
        Field sessionId = Field.of("sessionId", LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));

        Schema schema = Schema.of(dateTime, sessionId);

        bigQueryClientService.createTable(bigquery, tableId, schema);

        debug("Table " + googleCloudProjectId + "." + datasetName + "." + tableName + " created");
      } else {
        debug("Table " + googleCloudProjectId + "." + datasetName + "." + tableName + " exists");
      }


      Map<String, Object> row = new HashMap<>();

      long currentTimeMillis = System.currentTimeMillis();
      Timestamp currentTimestamp = new Timestamp(currentTimeMillis);
      row.put("dateTime", currentTimestamp.toString());
      row.put("sessionId", UUID.randomUUID().toString());

      List<Map<String, Object>> rows = new ArrayList<>();
      rows.add(row);

      bigQueryClientService.insertRows(bigquery, tableId, rows);

      //Assert
      //assertEquals(inputList, outputList);
    } catch (Exception ex) {
      debug(ex.getMessage());
    }

  }

  @Ignore
  @Test
  public void createTableInsertDataInParallel() {

    try {
      //Assign
      String tableName = "TEST02";

      BigQueryClientService bigQueryClientService = new BigQueryClientService();
      bigQueryClientService.setEnabled(true);
      bigQueryClientService.setGoogleCloudProjectId(googleCloudProjectId);

      BigQuery bigquery = bigQueryClientService.getBigquery();

      TableId tableId = TableId.of(datasetName, tableName);


      //Action
      ExecutorService executorCreateTable = Executors.newFixedThreadPool(2);

      // Submit tasks for parallel execution
      Future<?> task1 = executorCreateTable.submit(() -> {
        // Perform operation or function 1
        System.out.println("Task 1 executing in parallel");
        Table tableRoundResult1 = bigquery.getTable(tableId);
        if (tableRoundResult1 == null) {
          // Table does not exist, proceed to create it
          Field dateTime = Field.of("dateTime", LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.TIMESTAMP));
          Field sessionId = Field.of("sessionId", LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));

          Schema schema = Schema.of(dateTime, sessionId);

          bigQueryClientService.createTable(bigquery, tableId, schema);

          debug("Task 1 Table " + googleCloudProjectId + "." + datasetName + "." + tableName + " created");
        } else {
          debug("Task 1 Table " + googleCloudProjectId + "." + datasetName + "." + tableName + " exists");
        }
      });

      Future<?> task2 = executorCreateTable.submit(() -> {
        // Perform operation or function 2
        System.out.println("Task 2 executing in parallel");
        Table tableRoundResult2 = bigquery.getTable(tableId);
        if (tableRoundResult2 == null) {
          // Table does not exist, proceed to create it
          Field dateTime = Field.of("dateTime", LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.TIMESTAMP));
          Field sessionId = Field.of("sessionId", LegacySQLTypeName.legacySQLTypeName(StandardSQLTypeName.STRING));

          Schema schema = Schema.of(dateTime, sessionId);

          bigQueryClientService.createTable(bigquery, tableId, schema);

          debug("Task 2 Table " + googleCloudProjectId + "." + datasetName + "." + tableName + " created");
        } else {
          debug("Task 2 Table " + googleCloudProjectId + "." + datasetName + "." + tableName + " exists");
        }
      });

      // Shutdown the executor service when tasks are complete
      executorCreateTable.shutdown();

      try {
        if (!executorCreateTable.awaitTermination(60, TimeUnit.SECONDS)) {
          // Wait for up to 60 seconds for tasks to complete
          // Handle if tasks don't complete within the timeout
        }
      } catch (InterruptedException e) {
        debug(e.getMessage());
      }

      long currentTimeMillis = System.currentTimeMillis();

      ExecutorService executorInsertRows = Executors.newFixedThreadPool(2);

      // Submit tasks for parallel execution
      Future<?> task3 = executorInsertRows.submit(() -> {
        // Perform operation or function 1
        System.out.println("Task 3 executing in parallel");

        Map<String, Object> row = new HashMap<>();

        Timestamp currentTimestamp = new Timestamp(currentTimeMillis);
        row.put("dateTime", currentTimestamp.toString());
        row.put("sessionId", UUID.randomUUID().toString());

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row);

        bigQueryClientService.insertRows(bigquery, tableId, rows);

      });

      Future<?> task4 = executorInsertRows.submit(() -> {
        // Perform operation or function 2
        System.out.println("Task 4 executing in parallel");

        Map<String, Object> row = new HashMap<>();

        Timestamp currentTimestamp = new Timestamp(currentTimeMillis);
        row.put("dateTime", currentTimestamp.toString());
        row.put("sessionId", UUID.randomUUID().toString());

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row);

        bigQueryClientService.insertRows(bigquery, tableId, rows);
      });

      // Shutdown the executor service when tasks are complete
      executorInsertRows.shutdown();

      try {
        if (!executorInsertRows.awaitTermination(60, TimeUnit.SECONDS)) {
          // Wait for up to 60 seconds for tasks to complete
          // Handle if tasks don't complete within the timeout
        }
      } catch (InterruptedException e) {
        debug(e.getMessage());
      }

      //Assert
      //assertEquals(inputList, outputList);
    } catch (Exception ex) {
      debug(ex.getMessage());
    }

  }

  private void debug(String s) {
    System.out.println(s);
  }
}
