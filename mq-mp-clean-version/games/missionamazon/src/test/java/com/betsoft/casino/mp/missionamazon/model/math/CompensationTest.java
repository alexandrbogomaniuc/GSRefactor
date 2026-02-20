package com.betsoft.casino.mp.missionamazon.model.math;

import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.testmodel.*;
import com.betsoft.casino.mp.missionamazon.model.*;
import com.betsoft.casino.mp.missionamazon.model.math.config.GameConfig;
import com.betsoft.casino.mp.missionamazon.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.missionamazon.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.missionamazon.model.math.config.SpawnConfigLoader;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.betsoft.casino.mp.common.testmodel.StubSocketService.getConnection;
import static org.junit.Assert.assertEquals;

public class CompensationTest {
//    public static void main(String[] args) {
//        Money stake = Money.fromCents(300);
//        GameConfig gameConfig = new GameConfigLoader().loadDefaultConfig();
//        int weaponId = SpecialWeaponType.ArtilleryStrike.getId();
//        MathData.getFullCompensationForWeapon(gameConfig, stake, weaponId, 0, 3, 1);
//    }
    static GameRoom currentRoom;
    static Seat testSeat;
    static PlayGameState playGameState;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Money stake = Money.fromCents(10);
        StubCurrency stubCurrency = new StubCurrency("EUR", "EUR");
        StubRoomInfoService stubRoomInfoService = new StubRoomInfoService();
        GameType gameType = GameType.MISSION_AMAZON;
        StubRoomTemplate stubRoomTemplate = new StubRoomTemplate(1, 271, gameType,
                (short) 6, (short) 1, MoneyType.REAL, 100, 100, 1, 1,
                1, 1, "Test Room", 5);

        ISingleNodeRoomInfo roomInfo = stubRoomInfoService.createForTemplate(
                stubRoomTemplate,
                271,
                stake, stubCurrency.getCode(),
                10,
                gameType,
                "TestRoom"
        );

        GameMap currentMap;
        GameMapStore gameMapStore = new GameMapStore();
        gameMapStore.init();
        currentMap = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(1001));
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();

        long accountId = 111222;
        StubPlayerInfo stubPlayerInfo = new StubPlayerInfo(271L, accountId, "PlayerTest", "PlayerTest",
                1000000L, new StubCurrency("USD", "$"), false, false);

        List<Seat> seats = new ArrayList<>();

        currentRoom = getCurrentRoom(config, spawnConfig, roomInfo, seats, currentMap);

        StubRoomPlayerInfo roomPlayerInfo = new StubRoomPlayerInfo(accountId, 271,
                currentRoom.getId(),
                1,
                "sid_123123", 11,
                "testUser_" + accountId,
                new StubAvatar(0, 1, 2),
                System.currentTimeMillis(),
                new StubCurrency("USD", "$"),
                new StubPlayerStatsService().load(271, gameType.getGameId(), 1), false,
                null,
                null, stake.toCents(),
                10,
                0.02, MaxQuestWeaponMode.LOOT_BOX, false);

        StubGameSocketClient client = new StubGameSocketClient(accountId, 271L, "", getConnection(),
                new StubGsonMessageSerializer(new Gson()), gameType);
        testSeat= new Seat(roomPlayerInfo, client, 1);
        StubPlayerQuests playerQuestsForStake = new StubPlayerQuests(new HashSet<>());
        testSeat.getPlayerInfo().setPlayerQuests(playerQuestsForStake);
        playGameState = new PlayGameState(currentRoom);
    }

    static void resetSeatTestData(){
        testSeat.resetWeaponFromWC();
        testSeat.getWeaponSurplus().clear();
    }

    @Test
    public void testPoorPlayCompensation() {
        testSeat.setBetLevel(10);
        testSeat.addWC(9,1);
        playGameState.makeCompensationForPoorPlaying(testSeat,9, 7,6,false);
        assertEquals(testSeat.getWeaponSurplus().get(0).getWinBonus(),135);

        resetSeatTestData();
        playGameState.makeCompensationForPoorPlaying(testSeat,9, 7,6,false);
        assertEquals(testSeat.getWeaponSurplus().get(0).getWinBonus(),135);


        resetSeatTestData();
        testSeat.setStake(Money.fromCents(50000));
        testSeat.setBetLevel(1);
        playGameState.makeCompensationForPoorPlaying(testSeat,9, 7,6,false);
        assertEquals(testSeat.getWeaponSurplus().get(0).getWinBonus(),67711);


    }


    private static GameRoom getCurrentRoom(GameConfig config, SpawnConfig spawnConfig, ISingleNodeRoomInfo roomInfo, List<Seat> seats, GameMap currentMap) {
        TestApplicationContext ctx = TestApplicationContext.createContextWithStubBeans(new StubSocketService() {
            @Override
            public IStartNewRoundResult startNewRound(int serverId, long accountId, String sessionId,
                                                      long gameSessionId, long roomId, long roomRoundId,
                                                      long roundStartDate, boolean battlegroundRoom, long stakeOrBuyInAmount) {
                IStartNewRoundResult iStartNewRoundResult = super.startNewRound(serverId, accountId, sessionId,
                        gameSessionId, roomId, roomRoundId, roundStartDate, battlegroundRoom, stakeOrBuyInAmount);
                return iStartNewRoundResult;
            }
        }, seats);
        return new GameRoom(ctx, new EmptyLogger(), roomInfo, currentMap,
                new StubPlayerStatsService(), new StubWeaponService(),
                null, null, new StubPlayerProfileService(),
                null, null, null, null,
                new StubGameConfigProvider(config), new StubSpawnConfigProvider(spawnConfig));
    }
}
