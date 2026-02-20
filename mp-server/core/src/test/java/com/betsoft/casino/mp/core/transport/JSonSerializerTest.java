package com.betsoft.casino.mp.core.transport;

import com.betsoft.casino.mp.common.BonusType;
import com.betsoft.casino.mp.common.math.Paytable;
import com.betsoft.casino.mp.model.AvatarParts;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.MoneyType;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.transport.Currency;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.utils.GsonClassSerializer;
import com.betsoft.casino.utils.TObject;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * User: flsh
 * Date: 03.06.17.
 */
public class JSonSerializerTest {
    private static final GsonBuilder builder = new GsonBuilder();

    static {
        GsonClassSerializer typeAdapter = new GsonClassSerializer();
        typeAdapter.register(Error.class);
        typeAdapter.register(EnterLobby.class);
        typeAdapter.register(EnterLobbyResponse.class);
        typeAdapter.register(GetRoomInfo.class);
        typeAdapter.register(GetRoomInfoResponse.class);
        typeAdapter.register(Seat.class);
        typeAdapter.register(Room.class);
        typeAdapter.register(Weapon.class);
        typeAdapter.register(Enemy.class);
        typeAdapter.register(Ok.class);
        typeAdapter.register(GetStartGameUrl.class);
        typeAdapter.register(GetStartGameUrlResponse.class);
        typeAdapter.register(OpenRoom.class);
        typeAdapter.register(CloseRoom.class);
        typeAdapter.register(SitIn.class);
        typeAdapter.register(SitInResponse.class);
        typeAdapter.register(SitOut.class);
        typeAdapter.register(SitOutResponse.class);
        typeAdapter.register(FullGameInfo.class);
        typeAdapter.register(RoomEnemy.class);
        typeAdapter.register(GameStateChanged.class);
        typeAdapter.register(EnemyMove.class);
        typeAdapter.register(EnemiesMoved.class);
        typeAdapter.register(NewEnemy.class);
        typeAdapter.register(BuyIn.class);
        typeAdapter.register(BuyInResponse.class);
        typeAdapter.register(Shot.class);
        typeAdapter.register(ShotResponse.class);
        typeAdapter.register(Hit.class);
        typeAdapter.register(Miss.class);
        typeAdapter.register(EnemyDestroyed.class);
        typeAdapter.register(GetFullGameInfo.class);
        typeAdapter.register(RoundResult.class);
        typeAdapter.register(BalanceUpdated.class);
        typeAdapter.register(ChangeNickname.class);
        typeAdapter.register(ChangeAvatar.class);
        typeAdapter.register(CheckNicknameAvailability.class);
        typeAdapter.register(FRBEnded.class);
        typeAdapter.register(RefreshBalance.class);
        typeAdapter.register(SwitchWeapon.class);
        typeAdapter.register(WeaponSwitched.class);
        typeAdapter.register(WeaponLootBox.class);
        typeAdapter.register(PurchaseWeaponLootBox.class);
        typeAdapter.register(SyncLobby.class);
        typeAdapter.register(ShortRoomInfo.class);
        typeAdapter.register(GetLobbyTime.class);
        typeAdapter.register(UpdateTrajectories.class);
        typeAdapter.register(CloseRoundResults.class);
        typeAdapter.register(Stats.class);
        typeAdapter.register(LevelUp.class);
        typeAdapter.register(RoundFinishSoon.class);
        typeAdapter.register(NewTreasure.class);
        typeAdapter.register(TournamentStateChanged.class);
        typeAdapter.register(BonusStatusChanged.class);
        typeAdapter.register(BetLevel.class);
        typeAdapter.register(BetLevelResponse.class);
        builder.registerTypeHierarchyAdapter(Object.class, typeAdapter);
        //builder.registerTypeAdapter(IAvatar.class, (InstanceCreator<Avatar>) type -> new Avatar());
    }

    private static final Gson Gson = builder.create();

    //todo: replace this to dynamically formed based on current playerBalance and stake
    private final static Map<Float, List<Integer>> DEFAULT_STAKE_AMMOS_MAP = new HashMap<>();

    static {
        DEFAULT_STAKE_AMMOS_MAP.put(Money.fromCents(1).toFloatCents(), new ArrayList<>(Arrays.asList(100, 200, 500,
                1000, 2000, 5000, 10000, 20000, 50000, 1000000)));
        DEFAULT_STAKE_AMMOS_MAP.put(Money.fromCents(2).toFloatCents(), new ArrayList<>(Arrays.asList(50, 100, 200, 500,
                1000, 2000, 5000, 10000, 20000, 50000)));
        DEFAULT_STAKE_AMMOS_MAP.put(Money.fromCents(5).toFloatCents(), new ArrayList<>(Arrays.asList(20, 100, 200, 500,
                1000, 2000, 5000, 10000, 20000, 50000)));
        DEFAULT_STAKE_AMMOS_MAP.put(Money.fromCents(10).toFloatCents(), new ArrayList<>(Arrays.asList(10, 100, 200, 500,
                1000, 2000, 5000, 10000, 20000, 50000)));
    }

    @Test
    public void test() {
        long date = System.currentTimeMillis();
        testEntry(new Error(1, "Internal error", date, 1));
        testEntry(new EnterLobby(date, "4_eb0bf169cbb477b6f8430000015cbe83_fwRFDR4FWFZTV142PiQUCQ4H", "en", 1, 1,
                "real", true, 779, 32443L, false, false));
        EnterLobbyResponse enterLobbyResponse = new EnterLobbyResponse(date, 600, "Taras70", 20000, 2,
                new Currency("USD", "$"), 40, 0, 1,
                new Avatar(1, 1, 2), Collections.singletonList(1.0f),
                new ArrayList<>(AvatarParts.BORDER.getFreeParts()),
                new ArrayList<>(AvatarParts.HERO.getFreeParts()),
                new ArrayList<>(AvatarParts.BACKGROUND.getFreeParts()),
                new Paytable(new ArrayList<>(), 50, new ArrayList<>(), new ArrayList<>(), new HashMap<>()),
                new FRBonusInfo(111, date, date + 10000000, 500, 350, 20000, 1), true,
                100, 20, 10, 235, 200, 300, true, IRoom.DEFAULT_STAKES_RESERVE,
                IRoom.DEFAULT_STAKES_LIMIT, 1.0f,
                false, "", "", 1,
                new EnterLobbyBattlegroundInfo(Arrays.asList(12L, 123L), 100L, "https://ggg.com", null, false));
        enterLobbyResponse.setCashBonusInfo(new CashBonusInfo(1, System.currentTimeMillis(),
                System.currentTimeMillis() + 10000, 10000, 1000000, 2342, "ACTIVE"));
        enterLobbyResponse.setTournamentInfo(new TournamentInfo(2, "New Year tournament", "ACTIVE",
                System.currentTimeMillis(), System.currentTimeMillis() + 10000, 10000L, 1000L, 20000L, true, 1000L, 5000, 501,
                50, true));
        testEntry(enterLobbyResponse);
        testEntry(new ShortRoomInfo(date, 6, 123123, (short) 2, RoomState.PLAY));
        testEntry(new GetRoomInfo(date, 1, 5));
        testEntry(new GetRoomInfoResponse(date, 1, 5, "VIP #1", (short) 8, 10000, 2, 1, RoomState.WAIT,
                Arrays.asList(
                        new Seat(0, "Taras", System.currentTimeMillis(), 700.1, 0, new Avatar(0, 1, 2), -1, 2, 0, 20.34, 0),
                        new Seat(1, "Mike", System.currentTimeMillis(), 230, 0, new Avatar(0, 1, 3), -1, 7, 0, 0, 1),
                        new Seat(3, "Fred", System.currentTimeMillis(), 0, 0, new Avatar(0, 1, 4), -1, 8, 0, 10, 2)), 7, 800, 600,
                Arrays.asList(
                        new Enemy(1, 10, 30, 70, 3, 6.0, 0, false),
                        new Enemy(2, 15, 40, 70, 4, 0.0, 50, true)
                ),
                Arrays.asList(
                        new RoomEnemy(10, 2, false,9.5f, "1, 3", 4.0, 45, 1,
                                new Trajectory(3.7f)
                                        .addPoint(10, 10, 0)
                                        .addPoint(20, 30, 10)
                                        .addPoint(50, 20, 50), -1, 1, new ArrayList<>(), 1, 2, 4),
                        new RoomEnemy(11, 3, false,5.3f, "1, 3", 4.0, 45, 1,
                                new Trajectory(4)
                                        .addPoint(30, 20, 10)
                                        .addPoint(40, 50, 20)
                                        .addPoint(20, 60, 60), -1, 1, new ArrayList<>(), 1, 2, 5)
                ), 0, 10, 200, 0.25f, 1, "BOSS",
                DEFAULT_STAKE_AMMOS_MAP.entrySet().iterator().next().getValue(),
                Arrays.asList(new MinePlace(22221, 1, 0, 2234.4f, 2234.4f, "")),
                new HashMap<>(), false, 123123, new HashMap<>(), null,
                null, 5, new HashMap<>(), new HashSet<>(), null, new HashMap<>(), Collections.emptyMap()));
        testEntry(new Ok(date, 1));
        testEntry(new GetStartGameUrl(date, 1L, 5, 1L));
        testEntry(new GetStartGameUrlResponse(date, 1, 5, "https://host/mpgameloader.jsp?" +
                "sid=eb0bf169cbb477b6f8430000015cbe83&roomId=5"));
        testEntry(new OpenRoom(date, 1, 5, "eb0bf169cbb477b6f8430000015cbe83", 1, "real", "zh-cn"));
        testEntry(new CloseRoom(date, 1, 5));
        testEntry(new SitIn(date, 1, "en"));
        testEntry(new SitInResponse(date, 1, 4, "Andrey", System.currentTimeMillis(),
                15000, 200000, new Avatar(0, 1, 3),
                Arrays.asList(new Weapon(1, 10)), Arrays.asList(100., 200., 400.),
                true, 5, false, 0, MoneyType.REAL.name(), 0.0));
        testEntry(new SitOut(date, 1));
        testEntry(new SitOutResponse(date, 1, 4, "Andrey", System.currentTimeMillis(),
                Money.ZERO.toCents(), Money.ZERO.toCents(), 0, -1, false));
        testEntry(new FullGameInfo(date, 5, 1, "BOSS", 1000L, RoomState.PLAY,
                Arrays.asList(
                        new RoomEnemy(1, 1, false,40, "1,2", 3.0, 0, 1,
                                new Trajectory(1.5f)
                                        .addPoint(10, 10, 0)
                                        .addPoint(20, 30, 10)
                                        .addPoint(50, 20, 50), -1, 1, new ArrayList<>(), 1, 2, 5),
                        new RoomEnemy(10, 2, false, 100, "1, 3", 4.0, 45, 1,
                                new Trajectory(2.5f)
                                        .addPoint(30, 20, 10)
                                        .addPoint(40, 50, 20)
                                        .addPoint(20, 60, 60), -1, 1, new ArrayList<>(), 1, 2, 5)),
                Arrays.asList(
                        new Seat(0, "John", System.currentTimeMillis(), 700, 30,
                                new Avatar(0, 1, 2), -1, 1, 0, 12.23, 7),
                        new Seat(1, "Mike", System.currentTimeMillis(), 230, 100,
                                new Avatar(0, 1, 2), -1, 2, 0, 1.2, 8),
                        new Seat(3, "Fred", System.currentTimeMillis(), 10, 10,
                                new Avatar(0, 1, 2), -1, 3, 0, 4, 9),
                        new Seat(4, "Andrey", System.currentTimeMillis(), 0, 10,
                                new Avatar(0, 1, 2), -1, 4, 0, 0, 10)
                ), Arrays.asList(new MinePlace(22221, 1, 0, 2234.4f, 2234.4f, "")),
                new HashMap<>(), true, 12222, new HashMap<>(), 2, new HashMap<>(),
                new HashSet<>(), 0, new HashMap<>(), 10, Collections.emptyMap()));
        testEntry(new GameStateChanged(date, RoomState.PLAY, 600, 12222, System.currentTimeMillis()));
        testEntry(new EnemiesMoved(date, Arrays.asList(new EnemyMove(1, 70, 30, 123))));
        testEntry(new NewEnemy(date, new RoomEnemy(1, 1, false, 60, "", 0.0, 0, 1,
                new Trajectory(4.2f)
                        .addPoint(30, 20, 10)
                        .addPoint(40, 50, 20)
                        .addPoint(20, 60, 60), -1, 1, new ArrayList<>(), 1, 2, 4)));
        testEntry(new BuyIn(date, 10, 100));
        testEntry(new BuyInResponse(date, 10, 120, 15000));
        testEntry(new Shot(date, 34, 3, 5, 12.5f, 24.3f, false, ""));
//        testEntry(new ShotResponse(date, 23, 3, 1, 5, Arrays.asList(
//                new Hit(1, 50, -1, 10,
//                        new RoomEnemy(1, 1, 50, "1,2,3", 6.0, -1, 1,
//                                new Trajectory()
//                                        .addPoint(30, 20, 10)
//                                        .addPoint(40, 50, 20)
//                                        .addPoint(20, 60, 60)), 3422223),
//                new Miss(true, -1, 5, 10))));
        testEntry(new Hit(date, 10, 1, 1, 50, -1, 3, 5, 10.1,
                new RoomEnemy(1, 1, false,50, "1,2,3", 6.0, -1, 1,
                        new Trajectory(3.5f)
                                .addPoint(30, 20, 10)
                                .addPoint(40, 50, 20)
                                .addPoint(20, 60, 60), -1, 1, new ArrayList<>(), 1, 2, 4), true, 1.0f,
                3422223, 1.2f, 2.3f, 1, true, "", 1, -1, false,
                0, 123L, 234L));
        testEntry(new Miss(date, -1, 3, true, -1, 5,
                3, 5, 10.2,
                true, 1.2f, 2.3f, 1, "", 234L, true));
        testEntry(new EnemyDestroyed(date, -1, 100, 0));
        testEntry(new GetFullGameInfo(date, 1));
        testEntry(new RoundResult(date, -1, 500, 100, 12000, 30, 350, 5,
                25, 3,
                Arrays.asList(
                        new Seat(0, "John", System.currentTimeMillis(), 700, 0,
                                new Avatar(0, 1, 2), -1, 1, 0, 0, 11),
                        new Seat(1, "Mike", System.currentTimeMillis(), 230, 0,
                                new Avatar(0, 1, 3), -1, 3, 0, 0, 12),
                        new Seat(3, "Fred", System.currentTimeMillis(), 0, 0,
                                new Avatar(0, 1, 4), -1, 10, 0, 0, 13)),
                1, 100, 1, 1,
                1, 100, new ArrayList<>(),
                1000, 10, 500,
                new LevelInfo(2, 100, 0, 100),
                new LevelInfo(4, 1000, 500, 5000), 0,
                1, 500, 1, new ArrayList<>(), 1, 300,
                1, 2, 3, 100.25,
                Collections.singletonList(new BattlegroundRoundResult(123L, 231L, 52L, 43L, ""))));
        testEntry(new BalanceUpdated(date, 15000, 122));
        testEntry(new ChangeNickname(date, 234, "Taras"));
        testEntry(new CheckNicknameAvailability(date, 234, "Taras"));
        testEntry(new ChangeAvatar(date, 234, 1, 2, 3));
        testEntry(new FRBEnded(date, 1200, "Completed", false, 11));
        testEntry(new SwitchWeapon(date, 5, 3));
        testEntry(new WeaponSwitched(date, 5, 3, 1, new ArrayList<>()));
        testEntry(new PurchaseWeaponLootBox(date, 5, 2));
        testEntry(new WeaponLootBox(date, 5, 2, 10, 10000, 1.5f, 0));
        testEntry(new SyncLobby(date, 1));
        testEntry(new UpdateTrajectories(date, 23, ImmutableMap.of(
                10L, new Trajectory(4.2f).addPoint(30, 20, 10).addPoint(40, 50, 20).addPoint(20, 60, 60),
                23L, new Trajectory(1.5f).addPoint(10, 10, 0).addPoint(20, 30, 10).addPoint(50, 20, 50)),
                0, -1));
        testEntry(new CloseRoundResults(100, 10));
        testEntry(new Stats(date, 100, 12, 23, 200, 150, 250, 3));
        testEntry(new RoundFinishSoon(date));
        testEntry(new TournamentStateChanged(System.currentTimeMillis(), -1, 11, "ACTIVE", "EXPIRED", "kuku"));
        testEntry(new BonusStatusChanged(System.currentTimeMillis(), -1, 11, "ACTIVE", "FINISHED",
                "released", BonusType.CASHBONUS.name()));
    }

    private void testEntry(TObject obj) {
        String json = Gson.toJson(obj);
        System.out.println(json);
        assertEquals(obj, Gson.fromJson(json, TObject.class));
    }
}
