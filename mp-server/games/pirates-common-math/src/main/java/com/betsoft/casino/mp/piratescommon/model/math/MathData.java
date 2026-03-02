package com.betsoft.casino.mp.piratescommon.model.math;

import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.piratescommon.model.math.data.*;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.Triple;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.*;

import static com.betsoft.casino.mp.model.SpecialWeaponType.*;
import static com.betsoft.casino.mp.piratescommon.model.math.EnemyType.*;

public class MathData {
    public static final int TURRET_WEAPON_ID = -1;
    private static final Map<Integer, Double> rtpWeapons;
    private static final Map<Integer, Double> rtpWeaponsAll;
    private static final Map<Integer, EnemyData> enemyDataMap;
    public static final double PAY_HIT_PERCENT = 0.1;
    private static final Map<Integer, Map<Integer, Double>> damageByWeapons;
    private static final double BOS_MAIN_PROB = 1 / 1750.;
    private static final List<Triple<Integer, Integer, Double>> bossParams;
    public static final int PISTOL_DEFAULT_WEAPON_ID = -1;
    public static final int ALL_DEFAULT_WEAPON_ID = -2;
    private static final Map<Integer, Map<Integer, List<Triple<Integer, Integer, Double>>>> additionalWeaponKilledTable;
    private static final Map<Integer, Double> bossPistolRtp;
    private static final Map<Integer, Pair<Integer, Integer>> bossLimits;
    private static final Set<Integer> possibleBetLevels;
    private static final Map<String, Integer> idxWeaponsMap;
    private static final Map<Integer, Integer> paidWeaponCosts;
    private static final double totalRTP = 97.5;
    private static final List<Triple<Integer, Integer, Double>> droppedWeaponsTable;
    private static final double droppedWeaponPistolRTP = 0.15;
    private static final double droppedWeaponSWRTP = 1.50;
    private static final double droppedWeaponAvgDropPrice;
    private static final double droppedWeaponPistolFreq;
    private static final double droppedWeaponSWFreq;
    private static final double questTurretRTP = 0.10;
    private static final double questWeaponRTP = 0.50;



    //---------------  EnemyTypeId, Map<WeaponId, Pair<AvgWeaponAward, WeaponsDropGainHP>>
    private static final Map<Integer, Map<Integer, Pair<Double, Double>>> weaponDropData;

    private static final Map<Integer, Double> weaponAwerageNumberEnemiesForWeapons;

    static {
        weaponDropData = new HashMap<>();
        additionalWeaponKilledTable = new HashMap<>();

        idxWeaponsMap = new HashMap<>();
        idxWeaponsMap.put("Laser", SpecialWeaponType.Ricochet.getId());
        idxWeaponsMap.put("Lightning", SpecialWeaponType.Lightning.getId());
        idxWeaponsMap.put("Napalm", SpecialWeaponType.Napalm.getId());
        idxWeaponsMap.put("Nuke", SpecialWeaponType.Nuke.getId());
        idxWeaponsMap.put("Artillery", SpecialWeaponType.ArtilleryStrike.getId());


        fillAdditionalWeaponData();

        bossPistolRtp = new HashMap<>();
        bossPistolRtp.put(1, 72.5);
        bossPistolRtp.put(2, 72.5);
        bossPistolRtp.put(3, 72.5);

        paidWeaponCosts = new HashMap<>();
        paidWeaponCosts.put(SpecialWeaponType.Ricochet.getId(), 30);
        paidWeaponCosts.put(SpecialWeaponType.Lightning.getId(), 60);
        paidWeaponCosts.put(SpecialWeaponType.Napalm.getId(), 90);
        paidWeaponCosts.put(SpecialWeaponType.ArtilleryStrike.getId(), 120);
        paidWeaponCosts.put(SpecialWeaponType.Nuke.getId(), 150);

        possibleBetLevels = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(1, 2, 3, 5, 10)));


        bossLimits = new HashMap<>();
        bossLimits.put(1, new Pair<>(1250, 500));
        bossLimits.put(2, new Pair<>(2500, 600));
        bossLimits.put(3, new Pair<>(4000, 700));

        bossParams = new LinkedList<>();
        bossParams.add(new Triple<>(0, 2500, 45.));
        bossParams.add(new Triple<>(1, 5000, 35.));
        bossParams.add(new Triple<>(2, 7500, 20.));

        damageByWeapons = new HashMap<>();
        damageByWeapons.put(SpecialWeaponType.Ricochet.getId(), getMapFromString("6=10|7=30|8=40|9=15|10=5"));
        damageByWeapons.put(SpecialWeaponType.Lightning.getId(), getMapFromString("10=5|11=30|12=40|15=20|18=5"));
        damageByWeapons.put(SpecialWeaponType.Napalm.getId(), getMapFromString("15=10|18=30|20=40|22=15|25=5"));
        damageByWeapons.put(SpecialWeaponType.ArtilleryStrike.getId(), getMapFromString("20=5|22=15|25=40|30=30|40=10"));
        damageByWeapons.put(SpecialWeaponType.Nuke.getId(), getMapFromString("25=10|30=20|40=50|50=20"));

        weaponAwerageNumberEnemiesForWeapons = new HashMap<>();
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Ricochet.getId(), 7.75);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Lightning.getId(), 12.5);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Napalm.getId(), 19.45);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.ArtilleryStrike.getId(), 27.3);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Nuke.getId(), 38.5);

        rtpWeaponsAll = new HashMap<>();
        rtpWeapons = new HashMap<>();
        rtpWeapons.put(PISTOL_DEFAULT_WEAPON_ID, 72.5);
        rtpWeaponsAll.put(PISTOL_DEFAULT_WEAPON_ID, 72.5);

        for (Map.Entry<Integer, Double> weaponEntry : weaponAwerageNumberEnemiesForWeapons.entrySet()) {
            int wpId = weaponEntry.getKey();
            double averageNumberEnemies = weaponEntry.getValue();
            double avgPayKill = paidWeaponCosts.get(wpId) / averageNumberEnemies;
            rtpWeaponsAll.put(wpId, avgPayKill * totalRTP);
            double actualActualAvg = avgPayKill * averageNumberEnemies * totalRTP;
            rtpWeapons.put(wpId, (actualActualAvg - (droppedWeaponSWRTP * 100) -  (questWeaponRTP * 100)) / averageNumberEnemies);
        }

        enemyDataMap = new HashMap<>();
        enemyDataMap.put(ENEMY_1.getId(), createEnemyData(new RatData()));
        enemyDataMap.put(ENEMY_2.getId(), createEnemyData(new RatData()));
        enemyDataMap.put(ENEMY_3.getId(), createEnemyData(new RatData()));
        enemyDataMap.put(ENEMY_4.getId(), createEnemyData(new CrabData()));
        enemyDataMap.put(ENEMY_5.getId(), createEnemyData(new CrabData()));
        enemyDataMap.put(ENEMY_6.getId(), createEnemyData(new CrabData()));
        enemyDataMap.put(ENEMY_7.getId(), createEnemyData(new CrabData()));
        enemyDataMap.put(ENEMY_8.getId(), createEnemyData(new CrabData()));
        enemyDataMap.put(ENEMY_9.getId(), createEnemyData(new BirdData()));
        enemyDataMap.put(ENEMY_10.getId(), createEnemyData(new BirdData()));
        enemyDataMap.put(ENEMY_11.getId(), createEnemyData(new DeckhandData()));
        enemyDataMap.put(ENEMY_12.getId(), createEnemyData(new DeckhandData()));
        enemyDataMap.put(ENEMY_13.getId(), createEnemyData(new NeckbeardData()));
        enemyDataMap.put(ENEMY_14.getId(), createEnemyData(new NeckbeardData()));
        enemyDataMap.put(ENEMY_15.getId(), createEnemyData(new CaptainData()));
        enemyDataMap.put(ENEMY_16.getId(), createEnemyData(new CaptainData()));
        enemyDataMap.put(ENEMY_17.getId(), createEnemyData(new RunnerData()));
        enemyDataMap.put(ENEMY_18.getId(), createEnemyData(new RunnerData()));
        enemyDataMap.put(ENEMY_19.getId(), createEnemyData(new TrollData()));
        enemyDataMap.put(ENEMY_20.getId(), createEnemyData(new TrollData()));
        enemyDataMap.put(WEAPON_CARRIER.getId(), createEnemyData(new WeaponCarrierData()));
        enemyDataMap.put(Boss.getId(), createEnemyData(new BossData()));

        droppedWeaponsTable = getKilledTable("Laser\t3\t\t\t\t10\n" +
                "Laser\t4\t\t\t\t20\n" +
                "Laser\t5\t\t\t\t20\n" +
                "Laser\t6\t\t\t\t10\n" +
                "Lightning\t2\t\t\t\t20\n" +
                "Lightning\t3\t\t\t\t20\n" +
                "Lightning\t4\t\t\t\t10\n" +
                "Lightning\t5\t\t\t\t10\n" +
                "Napalm\t2\t\t\t\t20\n" +
                "Napalm\t3\t\t\t\t10\n" +
                "Napalm\t4\t\t\t\t10\n" +
                "Napalm\t5\t\t\t\t1\n" +
                "Artillery\t1\t\t\t\t20\n" +
                "Artillery\t2\t\t\t\t10\n" +
                "Artillery\t3\t\t\t\t10\n" +
                "Artillery\t4\t\t\t\t1\n" +
                "Nuke\t1\t\t\t\t10\n" +
                "Nuke\t2\t\t\t\t10\n" +
                "Nuke\t3\t\t\t\t1\n" +
                "Nuke\t4\t\t\t\t1\n");

        AtomicDouble sumWeights = new AtomicDouble();
        AtomicDouble temp = new AtomicDouble();
        droppedWeaponsTable.forEach(triple -> sumWeights.addAndGet(triple.third()));
        droppedWeaponsTable.forEach(triple -> {
            double prob = triple.third() / sumWeights.get();
            Integer shots = triple.second();
            Integer wpId = triple.first();
            Double damageByWeapon = weaponAwerageNumberEnemiesForWeapons.get(wpId);
            double shotsEV = damageByWeapon * shots * ((paidWeaponCosts.get(wpId) / damageByWeapon) * (totalRTP/100));
            temp.addAndGet(shotsEV * prob);
        });

        droppedWeaponAvgDropPrice = temp.get();
        droppedWeaponPistolFreq = droppedWeaponPistolRTP / droppedWeaponAvgDropPrice;
        droppedWeaponSWFreq = droppedWeaponSWRTP / droppedWeaponAvgDropPrice;

    }

    private static EnemyData createEnemyData(IEnemyData data) {
        Map<Integer, WeaponData[]> weaponData = new HashMap<>();
        weaponData.put(TURRET_WEAPON_ID, data.getTurretData());
        weaponData.put(Ricochet.getId(), data.getLaserData());
        weaponData.put(Lightning.getId(), data.getLightningData());
        weaponData.put(Napalm.getId(), data.getNapalmData());
        weaponData.put(ArtilleryStrike.getId(), data.getArtilleryData());
        weaponData.put(Nuke.getId(), data.getNukeData());
        return new EnemyData(data.getLevels(), weaponData);
    }

    private static void fillAdditionalWeaponData() {
        Map<Integer, Pair<Double, Double>> tmpMap;
        HashMap<Integer, List<Triple<Integer, Integer, Double>>> map;

        // Enemy 19, 20
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(150.00, 1060.3)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(200.00, 1267.5)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t3\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t0\n" +
                "Lightning\t1\t\t\t0\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t4\n" +
                "Nuke\t1\t\t\t4\n"));
        weaponDropData.put(EnemyType.ENEMY_19.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_20.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_19.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_20.getId(), map);


        // Enemy 17, 18
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(140.00, 1002.9)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(220.00, 1096.9)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t2\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t4\n" +
                "Nuke\t1\t\t\t4\n"));
        weaponDropData.put(EnemyType.ENEMY_17.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_18.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_17.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_18.getId(), map);


        // Enemy 15, 16
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(120.00, 877.5)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(180.00, 1002.9)); // default data for enemy
        weaponDropData.put(EnemyType.ENEMY_15.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_16.getId(), tmpMap);
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t1\n" +
                "Nuke\t1\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t2\n"));
        additionalWeaponKilledTable.put(EnemyType.ENEMY_15.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_16.getId(), map);


        // Enemy 13, 14
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(80., 828.8)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(120.00, 942.5)); // default data for enemy
        weaponDropData.put(EnemyType.ENEMY_13.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_14.getId(), tmpMap);
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t1\n" +
                "Nuke\t1\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t2\n"));
        additionalWeaponKilledTable.put(EnemyType.ENEMY_13.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_14.getId(), map);


        // Enemy 11, 12
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(40., 767.8)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(80.00, 919.3)); // default data for enemy
        weaponDropData.put(EnemyType.ENEMY_11.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_12.getId(), tmpMap);
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t2\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t1\n" +
                "Nuke\t1\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t1\n" +
                "Lightning\t1\t\t\t1\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t2\n" +
                "Nuke\t1\t\t\t1\n"));
        additionalWeaponKilledTable.put(EnemyType.ENEMY_11.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_12.getId(), map);


        // Enemy 9, 10
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(10., 487.5)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(40.00, 767.8)); // default data for enemy
        weaponDropData.put(EnemyType.ENEMY_9.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_10.getId(), tmpMap);
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t3\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t0\n" +
                "Nuke\t1\t\t\t0\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t2\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t2\n" +
                "Artillery\t1\t\t\t1\n" +
                "Nuke\t1\t\t\t1\n"));
        additionalWeaponKilledTable.put(EnemyType.ENEMY_9.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_10.getId(), map);

        //------------------------------WEAPON CARRIER + pistol----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(752.1, 752.1)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(752.1, 752.1)); // default data for enemy
        weaponDropData.put(EnemyType.WEAPON_CARRIER.getId(), tmpMap);

        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t2\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t1\n" +
                "Nuke\t1\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Laser\t1\t\t\t2\n" +
                "Lightning\t1\t\t\t2\n" +
                "Napalm\t1\t\t\t1\n" +
                "Artillery\t1\t\t\t1\n" +
                "Nuke\t1\t\t\t1\n"));
        additionalWeaponKilledTable.put(EnemyType.WEAPON_CARRIER.getId(), map);

    }

    public static List<Triple<Integer, Integer, Double>> getKilledTable(String data) {
        String[] weaponData = data.split("\\n");
        List<Triple<Integer, Integer, Double>> tmpWeaponKilledTable = new ArrayList<>();
        for (int i = 0; i < weaponData.length; i++) {
            String[] wData = Arrays.stream(weaponData[i].split("\\t")).filter(s -> !s.isEmpty()).toArray(String[]::new);
            tmpWeaponKilledTable.add(new Triple<>(idxWeaponsMap.get(wData[0]), Integer.parseInt(wData[1]),
                    Double.parseDouble(wData[2])));
        }
        return tmpWeaponKilledTable;
    }

    public static Map<Long, Double> getHP(String hpMapString) {
        Map<Long, Double> hitMap = new HashMap<>();
        String[] split = hpMapString.split("\n");
        for (String value : split) {
            value = value.replace("%", "");
            value = value.replace(",", ".");
            String[] params = value.split("\t\t");
            long key = (long) Double.parseDouble(params[0]);
            double additionalValue = Double.parseDouble(params[1]) / 100;
            hitMap.merge(key, additionalValue, Double::sum);
        }
        return hitMap;
    }

    public static Map<Integer, Double> getMapFromString(String conf) {
        Map<Integer, Double> tempMap = new HashMap<>();
        String delim1 = conf.contains("%") ? "\n" : "\\|";
        String delim2 = conf.contains("%") ? "\t\t" : "=";
        String[] values = conf.split(delim1);
        for (String value : values) {
            value = value.replace("%", "");
            value = value.replace(",", ".");
            String[] params = value.split(delim2);
            tempMap.put((int) Double.parseDouble(params[0]), Double.parseDouble(params[1]));
        }
        return tempMap;
    }

    public static Map<Integer, String> getMapFromStringComplex(String conf) {
        Map<Integer, String> tempMap = new HashMap<>();
        String[] values = conf.split("\\|");
        for (String value : values) {
            String[] params = value.split("=");
            tempMap.put(Integer.parseInt(params[0]), params[1]);
        }
        return tempMap;
    }


    public static Integer getRandomDamageForWeapon(Integer weaponId) {
        return weaponId == PISTOL_DEFAULT_WEAPON_ID ? 1 : GameTools.getRandomNumberKeyFromMapWithNorm(damageByWeapons.get(weaponId));
    }

    public static Double getAverageDamageForWeapon(Integer weaponId) {
        return weaponId == PISTOL_DEFAULT_WEAPON_ID ? 1. : weaponAwerageNumberEnemiesForWeapons.get(weaponId);
    }

    public static EnemyData getEnemyData(Integer enemyTypeId) {
        return enemyDataMap.get(enemyTypeId);
    }


    public static Double getRtpForWeapon(int weaponId) {
        return rtpWeapons.get(weaponId);
    }

    public static Double getFullRtpForWeapon(int weaponId) {
        return rtpWeaponsAll.get(weaponId);
    }

    public static double getPayHitPercent() {
        return PAY_HIT_PERCENT;
    }

    public static List<Triple<Integer, Integer, Double>> getBossParams() {
        return bossParams;
    }

    public static double getBosMainProb() {
        return BOS_MAIN_PROB;
    }

    public static List<Triple<Integer, Integer, Double>> getWeaponKilledTable(int enemyTypeId, int weaponTypeId) {

        Map<Integer, List<Triple<Integer, Integer, Double>>> map = additionalWeaponKilledTable.get(enemyTypeId);
        if(map == null)
            return Collections.emptyList();
        List<Triple<Integer, Integer, Double>> res = map.get(weaponTypeId);
        if (weaponTypeId == PISTOL_DEFAULT_WEAPON_ID) {
            return res != null ? res : new LinkedList<>();
        } else
            return res != null ? res : map.get(ALL_DEFAULT_WEAPON_ID);
    }

    public static Pair<Double, Double> getWeaponDropData(int enemyTypeId, int weaponTypeId) {
        Map<Integer, Pair<Double, Double>> enemyWeaponData = weaponDropData.get(enemyTypeId);
        if (enemyWeaponData == null) {
            return  null;
        }
        Pair<Double, Double> weaponData = enemyWeaponData.get(weaponTypeId);
        return enemyWeaponData.get(weaponTypeId) == null && weaponTypeId != PISTOL_DEFAULT_WEAPON_ID ?
                enemyWeaponData.get(ALL_DEFAULT_WEAPON_ID) : weaponData;
    }


    public static double getHitProbabilityForBoss(int weaponId, int bossId, Map<Long, Double> hitPointMapData) {
        EnemyData enemyData = getEnemyData(EnemyType.Boss.getId());
        Double weaponRTP = getRtpForWeapon(weaponId);
        Double pistolRTP = bossPistolRtp.get(bossId);
        double rtpForWeapon = weaponId == PISTOL_DEFAULT_WEAPON_ID ? pistolRTP :  weaponRTP;
        Double swAvgPayouts = enemyData.getSwAvgPayouts(weaponId, bossId - 1, PAY_HIT_PERCENT, hitPointMapData);
        return rtpForWeapon / swAvgPayouts;
    }


    public static Map<Integer, Pair<Integer, Integer>> getBossLimits() {
        return bossLimits;
    }

    public static Set<Integer> getPossibleBetLevels() {
        return possibleBetLevels;
    }

    public static Integer getPaidWeaponCost(int wpId) {
        return paidWeaponCosts.get(wpId);
    }

    public static Map<Integer, Double> getRtpWeapons() {
        return rtpWeapons;
    }

    public static Map<Integer, Double> getRtpWeaponsAll() {
        return rtpWeaponsAll;
    }

    public static double getDroppedWeaponPistolFreq() {
        return droppedWeaponPistolFreq;
    }

    public static double getDroppedWeaponSWFreq() {
        return droppedWeaponSWFreq;
    }

    public static List<Triple<Integer, Integer, Double>> getDroppedWeaponsTable() {
        return droppedWeaponsTable;
    }

}
