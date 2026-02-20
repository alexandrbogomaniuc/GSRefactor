package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.room.IRoom;

import java.util.*;

/**
 * User: flsh
 * Date: 09.11.17.
 */
public enum GameType {
    PIRATES(808, 960, 540, Arrays.asList(201, 202, 203), (short) 1, (short) 6,
            false, (short) 0, true, true,
            false, false, false),
    AMAZON(821, 960, 540, Arrays.asList(301, 302), (short) 1, (short) 6,
            false, (short) 0, true, true,
            false, false, false),
    PIRATES_POV(826, 960, 540, Arrays.asList(401, 402), (short) 1, (short) 1,
            false, (short) 10, true, false,
            true, false, false),
    REVENGE_OF_RA(829, 960, 540, Arrays.asList(504, 505, 506), (short) 1, (short) 6,
            false, (short) 10, false, false,
            true, false, false),
    DRAGONSTONE(838, 960, 540, Arrays.asList(601, 602, 603), (short) 1, (short) 6,
            false, (short) 10, false, false,
            true, true, false),
    CLASH_OF_THE_GODS(843, 960, 540, Arrays.asList(701, 702, 703), (short) 1, (short) 6,
            false, (short) 10, false, false,
            true, false, false),
    DMC_PIRATES(848, 960, 540, Arrays.asList(801, 802, 803), (short) 1, (short) 6,
            false, (short) 10, true, false,
            true, false, false),
    BG_DRAGONSTONE(856, 960, 540, Arrays.asList(901, 902, 903), (short) 1, (short) 6,
            false, (short) 10, false, false,
            false, false, true),
    MISSION_AMAZON(859, 960, 540, Arrays.asList(1001, 1002), (short) 1, (short) 6,
            false, (short) 10, false, false,
            true, false, false),
    BG_MISSION_AMAZON(862, 960, 540, Arrays.asList(1101, 1102), (short) 1, (short) 6,
            false, (short) 10, false, false,
            false, false, true),
    MAXCRASHGAME(863, 960, 540, Collections.singletonList(1201), (short) 1, (short) 100,
            false, (short) 0, false, false,
            true, false, false, true, false),
    BG_MAXCRASHGAME(864, 960, 540, Collections.singletonList(1201), (short) 2, (short) 50,
            false, (short) 0, false, false,
            true, false, true, true, false),
    SECTOR_X(866, 960, 540, Arrays.asList(1401, 1402, 1403, 1404), (short) 1, (short) 6,
            false, (short) 10, false, false,
            false, false, false),
    BG_SECTOR_X(867, 960, 540, Arrays.asList(1501, 1502, 1503, 1504), (short) 1, (short) 6,
            false, (short) 10, false, false,
            false, false, true),
    TRIPLE_MAX_BLAST(875, 960, 540, Collections.singletonList(1201), (short) 1, (short) 50,
            false, (short) 0, false, false,
            true, false, false, true, false),
    LUNARCASH(30429, 960, 540, Collections.singletonList(1201), (short) 1, (short) 100,
            false, (short) 0, false, false,
            true, false, false, true, false);



    private final long gameId;
    private final int screenWidth;
    private final int screenHeight;
    private final List<Integer> maps;
    private final short minSeats;
    private final short maxSeats;
    private final boolean needRegularAmmoForSpecialWeaponShot;
    private final short maxBulletsOnMap;
    private final boolean supportPlayerQuests;
    private final boolean supportLootBox;
    private final boolean supportPaidSpecialWeapon;
    private final boolean checkWeaponPrices;
    private final boolean battleGroundGame;
    private final boolean crashGame;
    private final boolean singleNodeRoomGame;

    public static final List<Integer> AMMO_VALUES = new ArrayList<>(Arrays.asList(100, 250, 500, 1000, 2000, 2500, 5000));
    public static final List<Integer> FRB_AMMO_VALUES = new ArrayList<>(Arrays.asList(1, 2, 5, 10, 20, 50, 100, 250, 500, 1000));

    public static final Map<Float, List<Integer>> DEFAULT_STAKE_AMMOS_MAP = new HashMap<>();

    static {
        DEFAULT_STAKE_AMMOS_MAP.put(Money.fromCents(1).toFloatCents(), new ArrayList<>(Arrays.asList(500, 1000, 2000, 2500, 5000)));
        DEFAULT_STAKE_AMMOS_MAP.put(Money.fromCents(2).toFloatCents(), new ArrayList<>(Arrays.asList(250, 500, 1000, 2000, 2500, 5000)));
        DEFAULT_STAKE_AMMOS_MAP.put(Money.fromCents(5).toFloatCents(), new ArrayList<>(Arrays.asList(100, 250, 500, 1000, 2000, 2500, 5000)));
        DEFAULT_STAKE_AMMOS_MAP.put(Money.fromCents(10).toFloatCents(), new ArrayList<>(Arrays.asList(50, 100, 250, 500, 1000, 2000, 2500, 5000)));
        DEFAULT_STAKE_AMMOS_MAP.put(Money.fromCents(25).toFloatCents(), new ArrayList<>(Arrays.asList(50, 100, 250, 500, 1000, 2000, 2500, 5000)));
    }

    GameType(long gameId, int screenWidth, int screenHeight, List<Integer> maps, short minSeats, short maxSeats,
             boolean needRegularAmmoForSpecialWeaponShot, short maxBulletsOnMap, boolean supportPlayerQuests,
             boolean supportLootBox, boolean supportPaidSpecialWeapon, boolean checkWeaponPrices, boolean battleGroundGame) {
        this.gameId = gameId;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.maps = maps;
        this.minSeats = minSeats;
        this.maxSeats = maxSeats;
        this.needRegularAmmoForSpecialWeaponShot = needRegularAmmoForSpecialWeaponShot;
        this.maxBulletsOnMap = maxBulletsOnMap;
        this.supportPlayerQuests = supportPlayerQuests;
        this.supportLootBox = supportLootBox;
        this.supportPaidSpecialWeapon = supportPaidSpecialWeapon;
        this.checkWeaponPrices = checkWeaponPrices;
        this.battleGroundGame = battleGroundGame;
        this.crashGame = false;
        this.singleNodeRoomGame = true;
    }

    GameType(long gameId, int screenWidth, int screenHeight, List<Integer> maps, short minSeats, short maxSeats,
             boolean needRegularAmmoForSpecialWeaponShot, short maxBulletsOnMap, boolean supportPlayerQuests,
             boolean supportLootBox, boolean supportPaidSpecialWeapon, boolean checkWeaponPrices,
             boolean battleGroundGame, boolean crashGame, boolean singleNodeRoomGame) {
        this.gameId = gameId;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.maps = maps;
        this.minSeats = minSeats;
        this.maxSeats = maxSeats;
        this.needRegularAmmoForSpecialWeaponShot = needRegularAmmoForSpecialWeaponShot;
        this.maxBulletsOnMap = maxBulletsOnMap;
        this.supportPlayerQuests = supportPlayerQuests;
        this.supportLootBox = supportLootBox;
        this.supportPaidSpecialWeapon = supportPaidSpecialWeapon;
        this.checkWeaponPrices = checkWeaponPrices;
        this.battleGroundGame = battleGroundGame;
        this.crashGame = crashGame;
        this.singleNodeRoomGame = singleNodeRoomGame;
    }

    public static GameType getByGameId(int gameId) {
        for (GameType gameType : values()) {
            if (gameType.gameId == gameId) {
                return gameType;
            }
        }
        return null;
    }

    public static GameType valueOf(int ordinal) {
        return values()[ordinal];
    }

    public long getGameId() {
        return gameId;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getStartMap() {
        return maps.get(0);
    }

    public List<Integer> getMaps() {
        return maps;
    }

    public short getMinSeats() {
        return minSeats;
    }

    public short getMaxSeats() {
        return maxSeats;
    }

    public short getMaxBulletsOnMap() {
        return maxBulletsOnMap;
    }

    public static List<Integer> getAmmoValues(MoneyType moneyType, Float stake) {
        if (moneyType == MoneyType.FRB) {
            return FRB_AMMO_VALUES;
        } else {
            List<Integer> ammos = DEFAULT_STAKE_AMMOS_MAP.get(stake);
            return ammos != null ? ammos : AMMO_VALUES;
        }
    }

    public boolean isBattleGroundGame() {
        return battleGroundGame;
    }

    public boolean isNeedRegularAmmoForSpecialWeaponShot() {
        return needRegularAmmoForSpecialWeaponShot;
    }

    public boolean isSupportPlayerQuests() {
        return supportPlayerQuests;
    }

    public static int getStakesReserve(GameType gameType) {
        if (CLASH_OF_THE_GODS.equals(gameType) || PIRATES_POV.equals(gameType)) {
            return IRoom.DEFAULT_STAKES_RESERVE * 3;
        } else return IRoom.DEFAULT_STAKES_RESERVE;
    }

    public static int getStakesLimit(GameType gameType) {
        if (CLASH_OF_THE_GODS.equals(gameType) || PIRATES_POV.equals(gameType)) {
            return IRoom.DEFAULT_STAKES_LIMIT * 3;
        } else return IRoom.DEFAULT_STAKES_LIMIT;
    }

    public static List<Long> getAllGameIds() {
        List<Long> res = new ArrayList<>();
        Arrays.stream(values()).forEach(gameType -> res.add(gameType.gameId));
        return res;
    }

    public boolean isSupportLootBox() {
        return supportLootBox;
    }

    public boolean isSupportPaidSpecialWeapon() {
        return supportPaidSpecialWeapon;
    }

    public boolean isCheckWeaponPrices() {
        return checkWeaponPrices;
    }

    public boolean isCrashGame() {
        return crashGame;
    }

    public boolean isSingleNodeRoomGame() {
        return singleNodeRoomGame;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameType [");
        sb.append("name=").append(name());
        sb.append(", gameId=").append(gameId);
        sb.append(", screenWidth=").append(screenWidth);
        sb.append(", screenHeight=").append(screenHeight);
        sb.append(", maxBulletsOnMap=").append(maxBulletsOnMap);
        sb.append(", supportLootBox=").append(supportLootBox);
        sb.append(", supportPlayerQuests=").append(supportPlayerQuests);
        sb.append(", supportPaidSpecialWeapon=").append(supportPaidSpecialWeapon);
        sb.append(", battleGroundGame=").append(battleGroundGame);
        sb.append(", crashGame=").append(crashGame);
        sb.append(", singleNodeRoomGame=").append(singleNodeRoomGame);
        sb.append(']');
        return sb.toString();
    }
}
