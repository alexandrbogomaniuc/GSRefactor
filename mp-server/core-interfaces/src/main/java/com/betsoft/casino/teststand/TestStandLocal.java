package com.betsoft.casino.teststand;

import com.betsoft.casino.mp.model.SpecialWeaponType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.SpecialWeaponType.*;
import static com.betsoft.casino.teststand.TeststandConst.*;

public class TestStandLocal {
    private static TestStandLocal ourInstance = new TestStandLocal();

    Map<String, TestStandFeature> featuresBySid = new HashMap<>();
    Map<Integer, TestStandFeature> mapOfPossibleFeatures;
    Map<Long, Integer> bossesForRoom = new HashMap<>();
    String[] amazonTreasures;
    Set<Long> minEnemiesModeForRoom = new HashSet<>();
    Map<Long, Integer> enemiesForRoom = new HashMap<>();
    Map<String, TestStandError> requestedSidForException = new HashMap<>();


    public static TestStandLocal getInstance() {
        return ourInstance;
    }


    private TestStandLocal() {
        amazonTreasures = new String[]{
                "Brass Scarab Sigil", "Cartouche of Gold Amulet", "Eagle of Ruby & Emerald", "Garnet Scarab Amulet",
                "Golden Statue of Tutenkhamun", "Vase of Osiris", "Golden Bust of Anubis", "Knight of Anubis Bust",
                "Lapis Cuff of the Queen", "Offering Bowl of Gems", "Onyx servent of Bastet", "Onyx Statue of Pharaoh",
                "Sacred Golden Pyramid", "Golden Bust of Nefertiti", "Jade Brooch"
        };

        List<Long> games = Arrays.asList(779L, 808L, 821L, 829L, 838L, 856L, 859L, 862L);
        List<Long> dragonstone = Arrays.asList(838L, 856L);
        List<Long> game843 = Collections.singletonList(843L);
        List<Long> gamesNewWeapons = Arrays.asList(843L, 848L, 826L);

        mapOfPossibleFeatures = new HashMap<>();
        registerFeature(5, "Kill current enemy and get HV enemy");
        registerFeature(6, "Kill base enemy");
        registerFeature(FEATURE_KILL_AND_GET_WEAPON, "Kill base enemy(not HV) and get Special weapon");
        registerFeature(8, "No special weapon after shot");
        registerFeature(9, "Kill base enemy from  weapon and get Boss");
        registerFeature(11, "Guaranteed Hit");

        registerFeature(30, "SW: Shotgun", gameIds(HolyArrows));
        registerFeature(31, "SW: Grenade", gameIds(Bomb));
        registerFeature(32, "SW: Machine Gun", gameIds(MachineGun));
        registerFeature(33, "SW: Laser", gameIds(Ricochet));
        registerFeature(34, "SW: Plasma Gun", gameIds(Plasma));
        registerFeature(35, "SW: Mine Launcher", gameIds(Landmines));
        registerFeature(36, "SW: Rail Gun", gameIds(Railgun));
        registerFeature(37, "SW: Artillery Strike", gameIds(ArtilleryStrike));
        registerFeature(38, "SW: Rocket Launcher", gameIds(RocketLauncher));
        registerFeature(39, "SW: Flamethrower", gameIds(Flamethrower));
        registerFeature(40, "SW: Cryogun", gameIds(Cryogun));
        registerFeature(41, "SW: Double Strength", gameIds(DoubleStrengthPowerUp));

        registerFeature(42, "No any prizes for shots");
        registerFeature(43, "Kill from base weapon and get free shots");

        registerFeature(44, "Get prize_9", games);
        registerFeature(45, "Get prize_10", games);
        registerFeature(46, "Get prize_11", games);
        registerFeature(47, "Get prize_12", games);

        registerFeature(48, "Bronze Key", games);
        registerFeature(49, "Silver Key", games);
        registerFeature(50, "Gold Key", games);

        registerFeature(51, "Set mode of minimal number of enemies for one round");

        registerFeature(FEATURE_GET_WHEEL, "Get Wheel", Arrays.asList(829L, 843L));
        registerFeature(53, "Double SW Shot + Kill Bonus", Arrays.asList(829L, 843L));

        registerFeature(FEATURE_SLOT, "Trigger MiniSlot", dragonstone);

        registerFeature(55, "Pistol No Win", Arrays.asList(829L, 843L));

        registerFeature(FEATURE_FRAGMENT, "Drop DragonStone fragment", dragonstone);

        Map<Integer, AtomicInteger> featuresCounterMap = new HashMap<>();
        featuresCounterMap.put(FEATURE_GET_WHEEL, new AtomicInteger(0));
        featuresCounterMap.put(FEATURE_RANDOM_WEAPON, new AtomicInteger(0));
        registerFeature(58, "SW: Lightning", gamesNewWeapons);
        registerFeature(59, "SW: Napalm", gamesNewWeapons);
        registerFeature(60, "SW: Nuke", gamesNewWeapons);
        registerFeature(61, "SW+MW", new HashMap<>(featuresCounterMap), game843);
        registerFeature(62, "2SW+2MW", featuresCounterMap, game843);
        registerFeature(65, "Need Phoenix", game843);
        registerFeature(68, "Need long X2", game843);

        registerFeature(FEATURE_NEED_CH, "Need CH multiplier",
                Collections.singletonMap(FEATURE_NEED_CH, new AtomicInteger(0)), Arrays.asList(843L, 848L, 826L, 859L, 862L));
        registerFeature(FEATURE_RANDOM_WEAPON, "Random possible weapon", game843);

        registerFeature(FEATURE_RAGE, "Trigger Rage", counter(FEATURE_RAGE), dragonstone);
        registerFeature(FEATURE_RAGE_WITH_IK, "Trigger Rage with IK", counter(FEATURE_RAGE), dragonstone);

        registerFeature(FEATURE_SLOT_WIN_1, "Slot win 1", dragonstone);
        registerFeature(FEATURE_SLOT_WIN_2, "Slot win 2", dragonstone);
        registerFeature(FEATURE_SLOT_WIN_3, "Slot win 3", dragonstone);
        registerFeature(FEATURE_SLOT_WIN_4, "Slot win 4", dragonstone);
        registerFeature(FEATURE_SLOT_WIN_5, "Slot win 5", dragonstone);

        registerFeature(FEATURE_DUAL_RAGE, "Dual Rage", counter(FEATURE_RAGE), dragonstone);
        registerFeature(FEATURE_RAGE_WITH_STONE, "Rage with Stone",
                counter(FEATURE_RAGE, FEATURE_FRAGMENT), dragonstone);
        registerFeature(FEATURE_SLOT_AND_WEAPON, "Slot and Weapon", counter(FEATURE_SLOT, FEATURE_RANDOM_WEAPON), dragonstone);
        registerFeature(FEATURE_DUAL_SLOT, "Dual Slot", counter(FEATURE_SLOT), dragonstone);
        registerFeature(FEATURE_DUAL_SLOT_AND_WEAPON, "Dual Slot and Weapon", counter(FEATURE_SLOT, FEATURE_RANDOM_WEAPON), dragonstone);
        registerFeature(FEATURE_EIGHT_STONES, "Drop 8 fragments", counter(FEATURE_FRAGMENT), dragonstone);
        registerFeature(FEATURE_DROP_TWO_WEAPONS_FROM_SPECTER, "Drop 2 weapons from Specter");

        registerFeature(FEATURE_DROP_LEVEL_UP_WEAPON, "Drop LevelUp weapon", Arrays.asList(856L, 862L, 867L));

        registerFeature(FEATURE_DROP_GEM, "Drop random gem", Arrays.asList(859L, 862L));

        registerFeature(FEATURE_BIG_WIN, "Payout: BIG WIN", Arrays.asList(859L, 862L));
        registerFeature(FEATURE_HUGE_WIN, "Payout: HUGE WIN", Arrays.asList(859L, 862L));
        registerFeature(FEATURE_MEGA_WIN, "Payout: MEGA WIN", Arrays.asList(859L, 862L));
        registerFeature(FEATURE_GET_WEAPON_WITHOUT_KILL, "Special weapon without kill", Arrays.asList(859L, 862L));

        int cnt = 200;
        for (String amazonTreasure : amazonTreasures) {
            registerFeature(cnt, "Treasure: " + amazonTreasure);
            cnt++;
        }
    }

    private Map<Integer, AtomicInteger> counter(Integer... features) {
        Map<Integer, AtomicInteger> result = new HashMap<>();
        for (Integer feature : features) {
            result.put(feature, new AtomicInteger(0));
        }
        return result;
    }

    private void registerFeature(int id, String name) {
        assertFeatureNotRegistered(id);
        mapOfPossibleFeatures.put(id, new TestStandFeature(id, name));
    }

    private void registerFeature(int id, String name, List<Long> games) {
        assertFeatureNotRegistered(id);
        mapOfPossibleFeatures.put(id, new TestStandFeature(id, name, Collections.emptyMap(), games));
    }

    private void registerFeature(int id, String name, Map<Integer, AtomicInteger> featuresAppeared, List<Long> games) {
        assertFeatureNotRegistered(id);
        mapOfPossibleFeatures.put(id, new TestStandFeature(id, name, featuresAppeared, games));
    }

    private void assertFeatureNotRegistered(int id) {
        if (mapOfPossibleFeatures.containsKey(id)) {
            throw new RuntimeException("Invalid TestStand configuration, found duplicated feature id: " + id);
        }
    }

    public void addFeature(String sid, TestStandFeature feature) {
        if (sid != null && feature != null) {
            featuresBySid.put(sid, feature);
        }
    }

    public TestStandFeature getFeatureBySid(String sid) {
        return featuresBySid.get(sid);
    }

    public void removeFeatureBySid(String sid) {
        if (sid != null) {
            featuresBySid.remove(sid);
        }
    }

    public TestStandFeature getPossibleFeatureById(int featureId) {
        return mapOfPossibleFeatures.get(featureId);
    }

    public Collection<TestStandFeature> getAllPossibleFeatures(long gameId) {
        return mapOfPossibleFeatures.values().stream()
                .filter(testStandFeature -> testStandFeature.gameIds.contains(gameId)).collect(Collectors.toList());
    }

    public Collection<TestStandFeature> getAllPossibleFeatures() {
        return mapOfPossibleFeatures.values();
    }

    public void addBossForRoom(Long roomId, Integer skinId) {
        bossesForRoom.put(roomId, skinId);
    }

    public Integer getBossForRoom(Long roomId) {
        Integer skinId = bossesForRoom.get(roomId);
        return skinId != null ? skinId : -1;
    }

    public void removeBossForRoom(Long roomId) {
        bossesForRoom.remove(roomId);
    }

    public void addEnemyIdForRoom(Long roomId, Integer enemyId) {
        enemiesForRoom.put(roomId, enemyId);
    }


    public Integer getEnemyIdForRoom(Long roomId) {
        Integer enemyId = enemiesForRoom.get(roomId);
        return enemyId != null ? enemyId : -1;
    }

    public void removeEnemyIdForRoom(Long roomId) {
        enemiesForRoom.remove(roomId);
    }

    public String[] getAmazonTreasures() {
        return amazonTreasures;
    }

    public boolean needMinimalEnemiesModeForRoom(long roomId) {
        return minEnemiesModeForRoom.contains(roomId);
    }

    public void addMinimalEnemiesModeForRoom(long roomId) {
        minEnemiesModeForRoom.add(roomId);
    }

    public void removeMinimalEnemiesModeForRoom(long roomId) {
        minEnemiesModeForRoom.remove(roomId);
    }

    public boolean featuresIsEmpty() {
        return featuresBySid.isEmpty();
    }

    private List<Long> gameIds(SpecialWeaponType weaponType) {
        return weaponType.getAvailableGameIds().stream().map(value -> (long) value).collect(Collectors.toList());
    }

    public boolean needTransportExceptionOnAddWin(String sessionId, TestStandError requestedError) {
        if (requestedSidForException == null || requestedSidForException.isEmpty()) {
            return false;
        }
        TestStandError testStandError = requestedSidForException.get(sessionId);
        return testStandError != null && testStandError.equals(requestedError);
    }

    public String addTransportError(String sessionId, String testStandErrorName) {
        Optional<TestStandError> first = Arrays.stream(TestStandError.values())
                .filter(testStandError -> testStandError.name().equals(testStandErrorName)).findFirst();
        first.ifPresent(standError -> requestedSidForException.put(sessionId, standError));
        return requestedSidForException.toString();
    }

    public void removeTransportExceptionForSid(String sessionId) {
        requestedSidForException.remove(sessionId);
    }

    public Map<String, TestStandError> getRequestedSidForException() {
        return requestedSidForException;
    }
}
