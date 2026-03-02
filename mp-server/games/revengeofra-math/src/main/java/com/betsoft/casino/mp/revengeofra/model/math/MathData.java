package com.betsoft.casino.mp.revengeofra.model.math;

import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.Triple;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MathData {

//    ENEMY_1 (Scarab Hatchling, id = 0)		Enemy 1
//    ENEMY_2 (Shadow Scarab, id = 1)			Enemy 2
//    ENEMY_3 (Shadow Scarab, id = 2)			Enemy 3
//    ENEMY_4 (Ruby Scarab, id = 3)			Enemy 4
//    ENEMY_5 (Bewjelled Scarab, id = 4)		Enemy 5
//    ENEMY_6 (Devouring Locust, id = 5)		Enemy 6
//    ENEMY_7 (Giant Scorpion, id = 6)		Enemy 7
//    ENEMY_8 (Wrapped Minion, id = 7)		Enemy 8
//    ENEMY_9 (Wrapped Shadowguard, id = 8)		Enemy 9
//    ENEMY_10 (Wrapped Spiritguard, id = 9)		Enemy 10
//    ENEMY_11 (Wrapped Spiritguard, id = 10)		Enemy 11
//    ENEMY_12 (Crimson Bataanta, id = 11)		Enemy 12
//    ENEMY_13 (Emerald Bataanta, id = 12)		Enemy 13
//    ENEMY_14 (Exploding Enemy, id = 13)		Bomber
//    ENEMY_15 (MummyWarrior Green, id = 14)		Enemy 15
//    ENEMY_16 (Horus, id = 15)			Enemy 16
//    ENEMY_17 (Teal Locust, id = 16)			Enemy 14
//    ENEMY_18 (Brawler Berserk, id = 17)		Enemy 17
//    WEAPON_CARRIER (Infernal Forgemaster, id = 20)	Weapon Carrier
//    Boss(Boss, id = 21)				Boss

    private static final Map<Integer, Double> rtpWeapons;
    private static final Map<Integer, Double> rtpPaidWeapons;
    private static final Map<Integer, Double> rtpWeaponsBoss;
    private static final Map<Integer, Double> rtpPaidWeaponsBoss;
    private static final Map<Integer, Double> rtpCompensateFreeSpecialWeapons;
    private static final Map<Integer, Double> rtpCompensatePaidSpecialWeapons;

    private static final Map<Integer, EnemyData> enemyDataMap;
    public static final double PAY_HIT_PERCENT = 1.5; //0.1 ----------->> CHANGED
    private static final Map<Integer, Map<Integer, Double>> damageByWeapons;
    private static final double BOS_MAIN_PROB = 1 / 1750.;
    private static final List<Triple<Integer, Integer, Double>> bossParams;
    public static final int PISTOL_DEFAULT_WEAPON_ID = -1;
    public static final int ALL_DEFAULT_WEAPON_ID = -2;
    public static final int BOMBER_HP_WIN;
    private static final Map<Integer, Double> bossPistolRtp;
    private static final Map<Integer, Pair<Integer, Integer>> bossLimits;
    private static final Set<Integer> possibleBetLevels;
    private static final Map<String, Integer> idxWeaponsMap;
    private static  final  Map<Integer, Double> wheelPayouts;
    private static final double wheelRTPPistol = 5./100;
    private static final double wheelRTPSW = 50./100;
    private static final double wheelAveragePayouts;
    private static final double wheelHitPistol;
    private static final double wheelHitSpecialWeapons;
    private static final Map<Integer, Integer> paidWeaponCosts;
    private static final List<Triple<Integer, Integer, Double>> droppedWeaponsTable;
    private static final double droppedWeaponPistolRTP = 0.20;
    private static final double droppedWeaponSWRTP = 0.25;
    private static final double droppedWeaponAvgDropPrice;
    private static final double droppedWeaponPistolFreq;
    private static final double droppedWeaponSWFreq;
    private static final double totalRTP = 95; //is not working, I changed it and keeps the same

    //---------------  EnemyTypeId, Map<WeaponId, Pair<AvgWeaponAward, WeaponsDropGainHP>>
    private static final Map<Integer, Map<Integer, Pair<Double, Double>>> weaponDropData;
    private static final Map<Integer, Map<Integer, List<Triple<Integer, Integer, Double>>>> additionalWeaponKilledTable;

    private static final Map<Integer, Map<Integer, Pair<Double, Double>>> weaponDropDataPaid;
    private static final Map<Integer, Map<Integer, List<Triple<Integer, Integer, Double>>>> additionalWeaponKilledTablePaid;

    private static final Map<Integer, Double> weaponAwerageNumberEnemiesForWeapons;

    static {

        wheelPayouts = getMapFromString("25=0.70|50=0.15|100=0.05|250=0.04|500=0.03|1000=0.02|2000=0.01");
        AtomicReference<Double> wpay = new AtomicReference<>((double) 0);
        wheelPayouts.forEach((integer, aDouble) -> wpay.updateAndGet(v -> v + integer * aDouble));
        wheelAveragePayouts = wpay.get();
        wheelHitPistol = wheelRTPPistol / wheelAveragePayouts;
        wheelHitSpecialWeapons = wheelRTPSW / wheelAveragePayouts;

        idxWeaponsMap = new HashMap<>();
        idxWeaponsMap.put("Mine Launcher", SpecialWeaponType.Landmines.getId());
        idxWeaponsMap.put("Flamethrower", SpecialWeaponType.Flamethrower.getId());
        idxWeaponsMap.put("Cryogun", SpecialWeaponType.Cryogun.getId());
        idxWeaponsMap.put("Plasma Gun", SpecialWeaponType.Plasma.getId());
        idxWeaponsMap.put("Artillery Strike", SpecialWeaponType.ArtilleryStrike.getId());

        paidWeaponCosts = new HashMap<>();
        paidWeaponCosts.put(SpecialWeaponType.Landmines.getId(), 15);
        paidWeaponCosts.put(SpecialWeaponType.Flamethrower.getId(), 35);
        paidWeaponCosts.put(SpecialWeaponType.Cryogun.getId(), 45);
        paidWeaponCosts.put(SpecialWeaponType.Plasma.getId(), 25);
        paidWeaponCosts.put(SpecialWeaponType.ArtilleryStrike.getId(), 50);

        possibleBetLevels = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(1, 2, 3, 5, 10)));

        weaponDropData = new HashMap<>();
        additionalWeaponKilledTable = new HashMap<>();

        weaponDropDataPaid = new HashMap<>();
        additionalWeaponKilledTablePaid = new HashMap<>();


        fillAdditionalWeaponData();

        bossPistolRtp = new HashMap<>();
        bossPistolRtp.put(1, 97.5);
        bossPistolRtp.put(2, 97.5);
        bossPistolRtp.put(3, 97.5);

        bossLimits = new HashMap<>();
        bossLimits.put(1, new Pair<>(1250, 500));
        bossLimits.put(2, new Pair<>(2500, 600));
        bossLimits.put(3, new Pair<>(4000, 700));

        bossParams = new LinkedList<>();
        bossParams.add(new Triple<>(0, 2500, 40.));
        bossParams.add(new Triple<>(1, 5000, 35.));
        bossParams.add(new Triple<>(2, 7500, 25.));

        BOMBER_HP_WIN = 5000;


        damageByWeapons = new HashMap<>();
        damageByWeapons.put(SpecialWeaponType.Landmines.getId(), getMapFromString("3=5|4=40|5=30|6=20|7=5"));
        damageByWeapons.put(SpecialWeaponType.Flamethrower.getId(), getMapFromString("5=30|6=50|7=10|8=10"));
        damageByWeapons.put(SpecialWeaponType.Cryogun.getId(), getMapFromString("9=5|10=40|11=40|12=15"));
        damageByWeapons.put(SpecialWeaponType.Plasma.getId(), getMapFromString("1=100"));
        damageByWeapons.put(SpecialWeaponType.ArtilleryStrike.getId(), getMapFromString("10=10|11=40|12=50"));

        weaponAwerageNumberEnemiesForWeapons = new HashMap<>();
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Landmines.getId(), 4.8);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Flamethrower.getId(), 6.0);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Cryogun.getId(), 10.65);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Plasma.getId(), 1.);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.ArtilleryStrike.getId(), 11.4);

        rtpWeaponsBoss = new HashMap<>();
        rtpPaidWeaponsBoss = new HashMap<>();
        rtpWeapons = new HashMap<>();
        rtpCompensateFreeSpecialWeapons = new HashMap<>();
        rtpCompensatePaidSpecialWeapons = new HashMap<>();

        rtpWeapons.put(PISTOL_DEFAULT_WEAPON_ID, 72.50);

        rtpPaidWeapons = new HashMap<>();

        for (Map.Entry<Integer, Double> weaponEntry : weaponAwerageNumberEnemiesForWeapons.entrySet()) {
            int wpId = weaponEntry.getKey();
            double averageNumberEnemies = weaponEntry.getValue();
            double avgPayKill = paidWeaponCosts.get(wpId) / averageNumberEnemies;
            rtpWeaponsBoss.put(wpId, avgPayKill * 100);
            rtpPaidWeaponsBoss.put(wpId, avgPayKill * totalRTP);

            double freeActualAvg = avgPayKill * averageNumberEnemies * 100;
            double paidActualAvg = avgPayKill * averageNumberEnemies * totalRTP;
            double paidRTP = (paidActualAvg - (droppedWeaponSWRTP * 100) - (wheelRTPSW * 100)) / averageNumberEnemies;
            double freeRTP = (freeActualAvg - (droppedWeaponSWRTP * 100) - (wheelRTPSW * 100)) / averageNumberEnemies;
            rtpWeapons.put(wpId, freeRTP);
            rtpPaidWeapons.put(wpId, paidRTP);
            rtpCompensateFreeSpecialWeapons.put(wpId, freeActualAvg / averageNumberEnemies);
            rtpCompensatePaidSpecialWeapons.put(wpId, paidActualAvg / averageNumberEnemies);
        }


        droppedWeaponsTable = getKilledTable("Mine Launcher\t5\t\t\t\t2\n" +
                "Mine Launcher\t6\t\t\t\t2\n" +
                "Mine Launcher\t7\t\t\t\t1\n" +
                "Mine Launcher\t8\t\t\t\t1\n" +
                "Flamethrower\t3\t\t\t\t2\n" +
                "Flamethrower\t4\t\t\t\t1\n" +
                "Flamethrower\t5\t\t\t\t1\n" +
                "Flamethrower\t6\t\t\t\t1\n" +
                "Cryogun\t3\t\t\t\t2\n" +
                "Cryogun\t4\t\t\t\t1\n" +
                "Cryogun\t5\t\t\t\t1\n" +
                "Cryogun\t6\t\t\t\t1\n" +
                "Plasma Gun\t4\t\t\t\t2\n" +
                "Plasma Gun\t5\t\t\t\t2\n" +
                "Plasma Gun\t6\t\t\t\t1\n" +
                "Plasma Gun\t7\t\t\t\t1\n" +
                "Artillery Strike\t3\t\t\t\t2\n" +
                "Artillery Strike\t4\t\t\t\t1\n" +
                "Artillery Strike\t5\t\t\t\t1\n" +
                "Artillery Strike\t6\t\t\t\t1\n");

        AtomicDouble sumWeights = new AtomicDouble();
        AtomicDouble temp = new AtomicDouble();
        droppedWeaponsTable.forEach(triple -> sumWeights.addAndGet(triple.third()));
        droppedWeaponsTable.forEach(triple -> {
            double prob = triple.third() / sumWeights.get();
            Integer shots = triple.second();
            Integer wpId = triple.first();
            Double damageByWeapon = weaponAwerageNumberEnemiesForWeapons.get(wpId);
            double shotsEV = damageByWeapon * shots * (paidWeaponCosts.get(wpId) / damageByWeapon);
            temp.addAndGet(shotsEV * prob);
        });

        droppedWeaponAvgDropPrice = temp.get();
        droppedWeaponPistolFreq = droppedWeaponPistolRTP / droppedWeaponAvgDropPrice;
        droppedWeaponSWFreq = droppedWeaponSWRTP / droppedWeaponAvgDropPrice;

        Map<Integer, WeaponData[]> freeWeaponData;
        Map<Integer, WeaponData[]> paidWeaponData;
        enemyDataMap = new HashMap<>();

        /////////////////////////////////////////// ENEMY 1 - ENEMY 5
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 4.0), 20.0, 0,
                                getHP("0\t\t10.00%\n" +
                                        "10\t\t65.00%\n" +
                                        "20\t\t25.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 4.0), 15.0, 0,
                                getHP("0\t\t10.00%\n" +
                                        "15\t\t70.00%\n" +
                                        "25\t\t20.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 4.0), 12.0, 0,
                                getHP("0\t\t10.00%\n" +
                                        "20\t\t80.00%\n" +
                                        "30\t\t10.00%\n"),
                                null)
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        30, 0,
                        getHP("0\t\t18.40%\n" +
                                "30\t\t65.00%\n" +
                                "40\t\t15.00%\n" +
                                "50\t\t1.00%\n" +
                                "60\t\t0.50%\n" +
                                "70\t\t0.10%\n"),
                        null)});

        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 0,
                        getHP("0\t\t0.00%\n" +
                                "30\t\t24.00%\n" +
                                "40\t\t35.00%\n" +
                                "50\t\t28.00%\n" +
                                "70\t\t7.00%\n" +
                                "80\t\t4.00%\n" +
                                "100\t\t2.00%\n"),
                        null)});

        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        20, 0,
                        getHP("0\t\t0.00%\n" +
                                "30\t\t59.00%\n" +
                                "40\t\t25.00%\n" +
                                "50\t\t10.00%\n" +
                                "70\t\t3.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t1.00%\n"),
                        null)});
        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 0,
                        getHP("0\t\t0.00%\n" +
                                "180\t\t29.00%\n" +
                                "200\t\t54.00%\n" +
                                "250\t\t12.00%\n" +
                                "300\t\t3.00%\n" +
                                "400\t\t1.50%\n" +
                                "500\t\t0.50%\n"),
                        null)});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        20, 0,
                        getHP("0\t\t0.90%\n" +
                                "30\t\t40.00%\n" +
                                "40\t\t41.00%\n" +
                                "50\t\t12.00%\n" +
                                "70\t\t4.00%\n" +
                                "100\t\t1.50%\n" +
                                "120\t\t0.50%\n" +
                                "150\t\t0.10%\n"),
                        null)});

        enemyDataMap.put(EnemyType.ENEMY_1.getId(), new EnemyData(new int[]{20, 25, 30}, freeWeaponData, paidWeaponData));
        enemyDataMap.put(EnemyType.ENEMY_2.getId(), new EnemyData(new int[]{20, 25, 30}, freeWeaponData, paidWeaponData));
        enemyDataMap.put(EnemyType.ENEMY_3.getId(), new EnemyData(new int[]{20, 25, 30}, freeWeaponData, paidWeaponData));
        enemyDataMap.put(EnemyType.ENEMY_4.getId(), new EnemyData(new int[]{20, 25, 30}, freeWeaponData, paidWeaponData));
        enemyDataMap.put(EnemyType.ENEMY_5.getId(), new EnemyData(new int[]{20, 25, 30}, freeWeaponData, paidWeaponData));


        //----------------------------------- Enemy 6,14  ENEMY6, ENEMY17-----------------------------------------------
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 3.0), 18.0, 0,
                                getHP("0\t\t5.00%\n" +
                                        "20\t\t80.00%\n" +
                                        "30\t\t15.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 3.5), 15.0, 0,
                                getHP("0\t\t0.00%\n" +
                                        "20\t\t70.00%\n" +
                                        "30\t\t25.00%\n" +
                                        "40\t\t5.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 4.0), 12.0, 0,
                                getHP("0\t\t5.00%\n" +
                                        "20\t\t70.00%\n" +
                                        "35\t\t20.00%\n" +
                                        "45\t\t5.00%\n"),
                                null)
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        30, 20,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t65.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t3.00%\n" +
                                "60\t\t1.00%\n"),
                        getHP("0\t\t52.00%\n" +
                                "70.0\t\t48.00%\n"))});
        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 20,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t22.00%\n" +
                                "40\t\t35.00%\n" +
                                "50\t\t28.00%\n" +
                                "70\t\t7.00%\n" +
                                "80\t\t4.00%\n" +
                                "100\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t34.00%\n" +
                                "70.0\t\t66.00%\n"))});

        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        25, 20,
                        getHP("0\t\t6.00%\n" +
                                "30\t\t57.00%\n" +
                                "40\t\t20.00%\n" +
                                "50\t\t12.00%\n" +
                                "70\t\t2.00%\n" +
                                "80\t\t1.50%\n" +
                                "100\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        getHP("0\t\t29.10%\n" +
                                "50.0\t\t70.90%\n"))});

        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 20,
                        getHP("0\t\t0.00%\n" +
                                "180\t\t39.00%\n" +
                                "200\t\t40.00%\n" +
                                "250\t\t15.00%\n" +
                                "300\t\t3.00%\n" +
                                "400\t\t1.50%\n" +
                                "500\t\t1.00%\n" +
                                "750\t\t0.50%\n"),
                        getHP("0\t\t3.89%\n" +
                                "220.0\t\t96.11%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        30, 20,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t40.00%\n" +
                                "40\t\t40.00%\n" +
                                "50\t\t12.00%\n" +
                                "70\t\t4.00%\n" +
                                "100\t\t1.50%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        getHP("0\t\t46.33%\n" +
                                "75.0\t\t53.67%\n"))});
        enemyDataMap.put(EnemyType.ENEMY_6.getId(), new EnemyData(new int[]{30, 40, 50}, freeWeaponData, paidWeaponData));
        enemyDataMap.put(EnemyType.ENEMY_17.getId(), new EnemyData(new int[]{30, 40, 50}, freeWeaponData, paidWeaponData));
        //----------------------------------- End Enemy 6,14  ENEMY6, ENEMY17-----------------------------------------------//


        //-----------------------------------  Enemy 7, ENEMY 7-----------------------------------------------//
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 4.0), 20.0, 20,
                                getHP("0\t\t10.00%\n" +
                                        "15\t\t65.00%\n" +
                                        "20\t\t15.00%\n" +
                                        "30\t\t10.00%\n"),
                                getHP("0\t\t60.63%\n" +
                                        "40.0\t\t39.38%\n")),
                        new WeaponData(Collections.singletonMap(2, 4.0), 18.0, 25,
                                getHP("0\t\t10.00%\n" +
                                        "20\t\t65.00%\n" +
                                        "30\t\t15.00%\n" +
                                        "35\t\t10.00%\n"),
                                getHP("0\t\t58.00%\n" +
                                        "50.0\t\t42.00%\n")),
                        new WeaponData(Collections.singletonMap(2, 4.0), 15.0, 30,
                                getHP("0\t\t10.00%\n" +
                                        "20\t\t55.00%\n" +
                                        "30\t\t25.00%\n" +
                                        "40\t\t10.00%\n"),
                                getHP("0\t\t62.50%\n" +
                                        "60.0\t\t37.50%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        40, 25,
                        getHP("0\t\t0.75%\n" +
                                "30\t\t70.00%\n" +
                                "40\t\t23.00%\n" +
                                "50\t\t3.00%\n" +
                                "60\t\t1.50%\n" +
                                "70\t\t1.00%\n" +
                                "80\t\t0.75%\n"),
                        getHP("0\t\t62.33%\n" +
                                "90.0\t\t37.67%\n"))});
        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 25,
                        getHP("0\t\t0.00%\n" +
                                "30\t\t25.00%\n" +
                                "40\t\t35.00%\n" +
                                "50\t\t27.00%\n" +
                                "70\t\t8.00%\n" +
                                "80\t\t3.00%\n" +
                                "100\t\t1.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t39.33%\n" +
                                "75.0\t\t60.67%\n"))});

        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        35, 25,
                        getHP("0\t\t1.50%\n" +
                                "30\t\t56.00%\n" +
                                "40\t\t25.00%\n" +
                                "50\t\t12.00%\n" +
                                "70\t\t3.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        getHP("0\t\t50.07%\n" +
                                "75.0\t\t49.93%\n"))});

        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 50,
                        getHP("0\t\t3.40%\n" +
                                "200\t\t50.00%\n" +
                                "250\t\t30.00%\n" +
                                "300\t\t12.00%\n" +
                                "400\t\t3.00%\n" +
                                "500\t\t1.00%\n" +
                                "750\t\t0.50%\n" +
                                "1000\t\t0.10%\n"),
                        getHP("0\t\t22.42%\n" +
                                "300.0\t\t77.58%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        35, 25,
                        getHP("0\t\t1.50%\n" +
                                "30\t\t40.00%\n" +
                                "40\t\t38.00%\n" +
                                "50\t\t12.00%\n" +
                                "60\t\t5.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t1.00%\n" +
                                "120\t\t0.50%\n"),
                        getHP("0\t\t47.47%\n" +
                                "75.0\t\t52.53%\n"))});
        enemyDataMap.put(EnemyType.ENEMY_7.getId(), new EnemyData(new int[]{80, 100, 120}, freeWeaponData, paidWeaponData));
        //----------------------------------- End  Enemy 7, ENEMY 7-----------------------------------------------//

        //-----------------------------------  Enemy 8 - 11, ENEMY 8-11 -----------------------------------------------//
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 5.0), 35.0, 30,
                                getHP("0\t\t36.00%\n" +
                                        "15\t\t30.00%\n" +
                                        "40\t\t22.00%\n" +
                                        "60\t\t10.00%\n" +
                                        "100\t\t2.00%\n"),
                                getHP("0\t\t64.50%\n" +
                                        "60.0\t\t35.50%\n")),
                        new WeaponData(Collections.singletonMap(2, 5.0), 30.0, 35,
                                getHP("0\t\t15.00%\n" +
                                        "20\t\t30.00%\n" +
                                        "40\t\t30.00%\n" +
                                        "60\t\t16.00%\n" +
                                        "80\t\t8.00%\n" +
                                        "100\t\t1.00%\n"),
                                getHP("0\t\t50.00%\n" +
                                        "70.0\t\t50.00%\n")),
                        new WeaponData(Collections.singletonMap(2, 5.0), 30.0, 40,
                                getHP("0\t\t38.00%\n" +
                                        "20\t\t30.00%\n" +
                                        "60\t\t20.00%\n" +
                                        "80\t\t8.00%\n" +
                                        "100\t\t3.00%\n" +
                                        "120\t\t1.00%\n"),
                                getHP("0\t\t64.25%\n" +
                                        "80.0\t\t35.75%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        45, 30,
                        getHP("0\t\t4.00%\n" +
                                "30\t\t55.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t7.00%\n" +
                                "60\t\t3.00%\n" +
                                "80\t\t1.00%\n"),
                        getHP("0\t\t65.40%\n" +
                                "100.0\t\t34.60%\n"))});
        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 30,
                        getHP("0\t\t11.50%\n" +
                                "40\t\t35.00%\n" +
                                "50\t\t30.00%\n" +
                                "60\t\t15.00%\n" +
                                "70\t\t5.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        getHP("0\t\t55.15%\n" +
                                "100.0\t\t44.85%\n"))});

        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        35, 30,
                        getHP("0\t\t6.50%\n" +
                                "30\t\t60.00%\n" +
                                "50\t\t18.00%\n" +
                                "60\t\t8.00%\n" +
                                "70\t\t4.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t1.00%\n" +
                                "120\t\t0.50%\n"),
                        getHP("0\t\t62.20%\n" +
                                "100.0\t\t37.80%\n"))});

        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 70,
                        getHP("0\t\t7.40%\n" +
                                "150\t\t20.00%\n" +
                                "200\t\t50.00%\n" +
                                "300\t\t16.00%\n" +
                                "400\t\t5.00%\n" +
                                "500\t\t1.00%\n" +
                                "750\t\t0.50%\n" +
                                "1000\t\t0.10%\n"),
                        getHP("0\t\t40.64%\n" +
                                "350.0\t\t59.36%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        45, 30,
                        getHP("0\t\t3.50%\n" +
                                "30\t\t45.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t10.00%\n" +
                                "60\t\t7.00%\n" +
                                "80\t\t3.00%\n" +
                                "100\t\t1.00%\n" +
                                "120\t\t0.50%\n"),
                        getHP("0\t\t61.30%\n" +
                                "100.0\t\t38.70%\n"))});
        enemyDataMap.put(EnemyType.ENEMY_8.getId(), new EnemyData(new int[]{180, 200, 220}, freeWeaponData, paidWeaponData));
        enemyDataMap.put(EnemyType.ENEMY_9.getId(), new EnemyData(new int[]{180, 200, 220}, freeWeaponData, paidWeaponData));
        enemyDataMap.put(EnemyType.ENEMY_10.getId(), new EnemyData(new int[]{180, 200, 220}, freeWeaponData, paidWeaponData));
        enemyDataMap.put(EnemyType.ENEMY_11.getId(), new EnemyData(new int[]{180, 200, 220}, freeWeaponData, paidWeaponData));
        //----------------------------------- End Enemy 8 - 11, ENEMY 8-11-----------------------------------------------//


        //----------------------------------- Enemy 12,13 ENEMY 12,13 -----------------------------------------------//
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 7.0), 45.0, 40,
                                getHP("0\t\t11.00%\n" +
                                        "20\t\t40.00%\n" +
                                        "50\t\t30.00%\n" +
                                        "70\t\t12.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t1.00%\n" +
                                        "150\t\t1.00%\n"),
                                getHP("0\t\t60.90%\n" +
                                        "100.0\t\t39.10%\n")),
                        new WeaponData(Collections.singletonMap(2, 7.0), 40.0, 50,
                                getHP("0\t\t4.00%\n" +
                                        "20\t\t35.00%\n" +
                                        "50\t\t25.00%\n" +
                                        "70\t\t18.00%\n" +
                                        "80\t\t10.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "150\t\t2.00%\n" +
                                        "200\t\t1.00%\n"),
                                getHP("0\t\t54.45%\n" +
                                        "110.0\t\t45.55%\n")),
                        new WeaponData(Collections.singletonMap(2, 7.0), 35.0, 60,
                                getHP("0\t\t13.00%\n" +
                                        "20\t\t30.00%\n" +
                                        "60\t\t35.00%\n" +
                                        "80\t\t12.00%\n" +
                                        "100\t\t6.00%\n" +
                                        "150\t\t2.00%\n" +
                                        "200\t\t1.00%\n" +
                                        "220\t\t1.00%\n"),
                                getHP("0\t\t58.50%\n" +
                                        "120.0\t\t41.50%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        45, 30,
                        getHP("0\t\t13.00%\n" +
                                "30\t\t50.00%\n" +
                                "50\t\t25.00%\n" +
                                "70\t\t5.00%\n" +
                                "80\t\t3.00%\n" +
                                "100\t\t2.00%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t74.60%\n" +
                                "150.0\t\t25.40%\n"))});
        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 30,
                        getHP("0\t\t4.00%\n" +
                                "50\t\t30.00%\n" +
                                "60\t\t37.00%\n" +
                                "70\t\t20.00%\n" +
                                "80\t\t5.00%\n" +
                                "100\t\t2.00%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t57.21%\n" +
                                "140.0\t\t42.79%\n"))});

        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        40, 30,
                        getHP("0\t\t4.00%\n" +
                                "40\t\t50.00%\n" +
                                "50\t\t35.00%\n" +
                                "60\t\t5.00%\n" +
                                "80\t\t3.00%\n" +
                                "100\t\t1.50%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        getHP("0\t\t66.89%\n" +
                                "140.0\t\t33.11%\n"))});

        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 100,
                        getHP("0\t\t5.00%\n" +
                                "200\t\t55.00%\n" +
                                "250\t\t30.00%\n" +
                                "300\t\t5.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.50%\n" +
                                "1000\t\t1.00%\n" +
                                "1500\t\t0.50%\n"),
                        getHP("0\t\t46.94%\n" +
                                "450.0\t\t53.06%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        50, 30,
                        getHP("0\t\t5.00%\n" +
                                "40\t\t55.00%\n" +
                                "50\t\t25.00%\n" +
                                "60\t\t7.00%\n" +
                                "80\t\t5.00%\n" +
                                "100\t\t1.50%\n" +
                                "150\t\t1.00%\n" +
                                "250\t\t0.50%\n"),
                        getHP("0\t\t66.46%\n" +
                                "140.0\t\t33.54%\n"))});
        enemyDataMap.put(EnemyType.ENEMY_12.getId(), new EnemyData(new int[]{450, 475, 500}, freeWeaponData, paidWeaponData));
        enemyDataMap.put(EnemyType.ENEMY_13.getId(), new EnemyData(new int[]{450, 475, 500}, freeWeaponData, paidWeaponData));
        //----------------------------------- End Enemy 12,13 ENEMY 12,13 -----------------------------------------------//

        //----------------------------------- Enemy 15, ENEMY 15 -----------------------------------------------//
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 6.0), 40.0, 35,
                                getHP("0\t\t36.00%\n" +
                                        "15\t\t20.00%\n" +
                                        "50\t\t30.00%\n" +
                                        "70\t\t12.00%\n" +
                                        "100\t\t2.00%\n"),
                                getHP("0\t\t66.59%\n" +
                                        "85.0\t\t33.41%\n")),
                        new WeaponData(Collections.singletonMap(2, 6.0), 35.0, 40,
                                getHP("0\t\t22.00%\n" +
                                        "20\t\t25.00%\n" +
                                        "50\t\t20.00%\n" +
                                        "70\t\t20.00%\n" +
                                        "80\t\t10.00%\n" +
                                        "100\t\t2.00%\n" +
                                        "120\t\t1.00%\n"),
                                getHP("0\t\t55.33%\n" +
                                        "90.0\t\t44.67%\n")),
                        new WeaponData(Collections.singletonMap(2, 6.0), 35.0, 50,
                                getHP("0\t\t22.00%\n" +
                                        "20\t\t25.00%\n" +
                                        "60\t\t30.00%\n" +
                                        "80\t\t15.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t2.00%\n" +
                                        "150\t\t1.00%\n"),
                                getHP("0\t\t56.10%\n" +
                                        "100.0\t\t43.90%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        30, 30,
                        getHP("0\t\t3.00%\n" +
                                "30\t\t60.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t3.00%\n" +
                                "60\t\t2.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t0.50%\n" +
                                "120\t\t0.50%\n"),
                        getHP("0\t\t73.38%\n" +
                                "130.0\t\t26.62%\n"))});
        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 20,
                        getHP("0\t\t2.00%\n" +
                                "40\t\t35.00%\n" +
                                "50\t\t40.00%\n" +
                                "60\t\t15.00%\n" +
                                "70\t\t4.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t1.00%\n" +
                                "120\t\t1.00%\n"),
                        getHP("0\t\t56.87%\n" +
                                "115.0\t\t43.13%\n"))});

        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        40, 30,
                        getHP("0\t\t9.50%\n" +
                                "30\t\t60.00%\n" +
                                "50\t\t15.00%\n" +
                                "60\t\t8.00%\n" +
                                "70\t\t5.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t1.00%\n" +
                                "120\t\t0.50%\n"),
                        getHP("0\t\t71.04%\n" +
                                "125.0\t\t28.96%\n"))});

        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 80,
                        getHP("0\t\t0.50%\n" +
                                "150\t\t30.00%\n" +
                                "200\t\t45.00%\n" +
                                "250\t\t16.00%\n" +
                                "500\t\t5.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.00%\n" +
                                "1200\t\t0.50%\n"),
                        getHP("0\t\t37.57%\n" +
                                "370.0\t\t62.43%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        50, 20,
                        getHP("0\t\t5.00%\n" +
                                "30\t\t40.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t12.00%\n" +
                                "60\t\t7.00%\n" +
                                "80\t\t3.00%\n" +
                                "100\t\t2.00%\n" +
                                "120\t\t1.00%\n"),
                        getHP("0\t\t65.39%\n" +
                                "115.0\t\t34.61%\n"))});
        enemyDataMap.put(EnemyType.ENEMY_15.getId(), new EnemyData(new int[]{280, 300, 320}, freeWeaponData, paidWeaponData));
        //----------------------------------- End Enemy 15 , ENEMY 15-----------------------------------------------//

        //----------------------------------- Enemy 16, ENEMY 16 -----------------------------------------------//
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 100,
                                getHP("0\t\t13.00%\n" +
                                        "20\t\t45.00%\n" +
                                        "80\t\t22.00%\n" +
                                        "100\t\t12.00%\n" +
                                        "120\t\t5.00%\n" +
                                        "150\t\t1.50%\n" +
                                        "200\t\t1.00%\n" +
                                        "250\t\t0.50%\n"),
                                getHP("0\t\t79.96%\n" +
                                        "250.0\t\t20.04%\n")),
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 120,
                                getHP("0\t\t13.50%\n" +
                                        "20\t\t25.00%\n" +
                                        "80\t\t35.00%\n" +
                                        "100\t\t15.00%\n" +
                                        "150\t\t8.00%\n" +
                                        "170\t\t2.00%\n" +
                                        "200\t\t1.00%\n" +
                                        "250\t\t0.50%\n"),
                                getHP("0\t\t74.37%\n" +
                                        "260.0\t\t25.63%\n")),
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 140,
                                getHP("0\t\t8.00%\n" +
                                        "20\t\t25.00%\n" +
                                        "80\t\t35.00%\n" +
                                        "100\t\t22.00%\n" +
                                        "150\t\t5.00%\n" +
                                        "170\t\t3.00%\n" +
                                        "200\t\t1.00%\n" +
                                        "250\t\t1.00%\n"),
                                getHP("0\t\t73.30%\n" +
                                        "270.0\t\t26.70%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        50, 30,
                        getHP("0\t\t12.00%\n" +
                                "40\t\t40.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t10.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t74.55%\n" +
                                "200.0\t\t25.45%\n"))});
        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 40,
                        getHP("0\t\t14.00%\n" +
                                "80\t\t35.00%\n" +
                                "100\t\t25.00%\n" +
                                "120\t\t15.00%\n" +
                                "150\t\t5.00%\n" +
                                "170\t\t3.00%\n" +
                                "200\t\t2.00%\n" +
                                "220\t\t1.00%\n"),
                        getHP("0\t\t59.18%\n" +
                                "220.0\t\t40.82%\n"))});

        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        45, 30,
                        getHP("0\t\t5.00%\n" +
                                "40\t\t30.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t12.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t66.00%\n" +
                                "200.0\t\t34.00%\n"))});

        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 120,
                        getHP("0\t\t5.65%\n" +
                                "300\t\t60.00%\n" +
                                "400\t\t22.00%\n" +
                                "500\t\t10.00%\n" +
                                "750\t\t1.00%\n" +
                                "1500\t\t0.75%\n" +
                                "2500\t\t0.50%\n" +
                                "5000\t\t0.10%\n"),
                        getHP("0\t\t40.96%\n" +
                                "600.0\t\t59.04%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        50, 30,
                        getHP("0\t\t2.00%\n" +
                                "60\t\t25.00%\n" +
                                "80\t\t40.00%\n" +
                                "90\t\t17.00%\n" +
                                "100\t\t10.00%\n" +
                                "150\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "500\t\t1.00%\n"),
                        getHP("0\t\t56.60%\n" +
                                "200.0\t\t43.40%\n"))});
        enemyDataMap.put(EnemyType.ENEMY_16.getId(), new EnemyData(new int[]{1200, 1350, 1500}, freeWeaponData, paidWeaponData));
        //----------------------------------- End Enemy 16, ENEMY 16 -----------------------------------------------//

        //----------------------------------- Enemy 17, ENEMY 18 -----------------------------------------------//
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 8.0), 50.0, 50,
                                getHP("0\t\t24.50%\n" +
                                        "20\t\t35.00%\n" +
                                        "60\t\t25.00%\n" +
                                        "80\t\t12.00%\n" +
                                        "100\t\t1.50%\n" +
                                        "120\t\t1.00%\n" +
                                        "150\t\t0.50%\n" +
                                        "200\t\t0.50%\n"),
                                getHP("0\t\t75.97%\n" +
                                        "150.0\t\t24.03%\n")),
                        new WeaponData(Collections.singletonMap(2, 8.0), 45.0, 60,
                                getHP("0\t\t18.00%\n" +
                                        "20\t\t30.00%\n" +
                                        "60\t\t30.00%\n" +
                                        "80\t\t15.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t1.00%\n" +
                                        "150\t\t0.50%\n" +
                                        "200\t\t0.50%\n"),
                                getHP("0\t\t72.53%\n" +
                                        "160.0\t\t27.47%\n")),
                        new WeaponData(Collections.singletonMap(2, 8.0), 40.0, 70,
                                getHP("0\t\t5.00%\n" +
                                        "20\t\t25.00%\n" +
                                        "60\t\t40.00%\n" +
                                        "80\t\t20.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "150\t\t3.00%\n" +
                                        "200\t\t1.00%\n" +
                                        "220\t\t1.00%\n"),
                                getHP("0\t\t65.47%\n" +
                                        "170.0\t\t34.53%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        45, 30,
                        getHP("0\t\t8.00%\n" +
                                "40\t\t55.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t4.00%\n" +
                                "100\t\t1.50%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        getHP("0\t\t70.84%\n" +
                                "160.0\t\t29.16%\n"))});
        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 40,
                        getHP("0\t\t3.40%\n" +
                                "60\t\t50.00%\n" +
                                "70\t\t30.00%\n" +
                                "80\t\t12.00%\n" +
                                "100\t\t3.00%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t0.50%\n" +
                                "170\t\t0.10%\n"),
                        getHP("0\t\t58.93%\n" +
                                "160.0\t\t41.08%\n"))});

        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        45, 30,
                        getHP("0\t\t9.40%\n" +
                                "40\t\t50.00%\n" +
                                "60\t\t25.00%\n" +
                                "80\t\t12.00%\n" +
                                "100\t\t2.00%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t0.50%\n" +
                                "200\t\t0.10%\n"),
                        getHP("0\t\t69.53%\n" +
                                "160.0\t\t30.47%\n"))});

        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 120,
                        getHP("0\t\t3.50%\n" +
                                "200\t\t55.00%\n" +
                                "250\t\t25.00%\n" +
                                "300\t\t12.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n" +
                                "1000\t\t1.00%\n" +
                                "1500\t\t0.50%\n"),
                        getHP("0\t\t53.17%\n" +
                                "520.0\t\t46.83%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        50, 30,
                        getHP("0\t\t4.50%\n" +
                                "50\t\t45.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t12.00%\n" +
                                "100\t\t5.00%\n" +
                                "150\t\t2.00%\n" +
                                "250\t\t1.00%\n" +
                                "300\t\t0.50%\n"),
                        getHP("0\t\t59.94%\n" +
                                "155.0\t\t40.06%\n"))});
        enemyDataMap.put(EnemyType.ENEMY_18.getId(), new EnemyData(new int[]{700, 750, 800}, freeWeaponData, paidWeaponData));
        //----------------------------------- End Enemy 17, ENEMY 18 -----------------------------------------------//


        // ------------------------------------- Bomber
        enemyDataMap.put(EnemyType.ENEMY_14.getId(), new EnemyData(new int[]{20, 25, 30}, freeWeaponData, paidWeaponData));
        // ------------------------------------- End Bomber


        //----------------------------------- Weapon Carrier -----------------------------------------------//
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 4.0), 0., 70,
                                getHP("0\t\t74.00%\n" +
                                        "40\t\t1.00%\n" +
                                        "50\t\t5.00%\n" +
                                        "60\t\t20.00%\n"),
                                getHP("0\t\t98.94%\n" +
                                        "1400.0\t\t1.06%\n")),
                        new WeaponData(Collections.singletonMap(2, 4.0), 0., 70,
                                getHP("0\t\t74.00%\n" +
                                        "40\t\t1.00%\n" +
                                        "50\t\t5.00%\n" +
                                        "60\t\t20.00%\n"),
                                getHP("0\t\t98.94%\n" +
                                        "1400.0\t\t1.06%\n")),
                        new WeaponData(Collections.singletonMap(2, 4.0), 0., 70,
                                getHP("0\t\t74.00%\n" +
                                        "40\t\t1.00%\n" +
                                        "50\t\t5.00%\n" +
                                        "60\t\t20.00%\n"),
                                getHP("0\t\t98.94%\n" +
                                        "1400.0\t\t1.06%\n"))
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        Collections.singletonMap(2, 9.0),
                        0, 100,
                        getHP("0\t\t4.00%\n" +
                                "20\t\t30.00%\n" +
                                "30\t\t33.00%\n" +
                                "40\t\t17.00%\n" +
                                "50\t\t12.00%\n" +
                                "70\t\t3.00%\n" +
                                "100\t\t1.00%\n"),
                        getHP("0\t\t97.76%\n" +
                                "1420.0\t\t2.24%\n"))});
        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        Collections.singletonMap(2, 10.0),
                        0, 120,
                        getHP("0\t\t6.00%\n" +
                                "30\t\t25.00%\n" +
                                "40\t\t18.00%\n" +
                                "50\t\t13.00%\n" +
                                "60\t\t11.00%\n" +
                                "80\t\t10.00%\n" +
                                "100\t\t9.00%\n" +
                                "120\t\t8.00%\n"),
                        getHP("0\t\t96.25%\n" +
                                "1450.0\t\t3.75%\n"))});

        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        Collections.singletonMap(2, 10.0),
                        0, 120,
                        getHP("0\t\t4.00%\n" +
                                "30\t\t35.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t17.00%\n" +
                                "60\t\t10.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t1.00%\n" +
                                "120\t\t1.00%\n"),
                        getHP("0\t\t97.19%\n" +
                                "1450.0\t\t2.81%\n"))});

        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        Collections.singletonMap(2, 10.0),
                        0, 300,
                        getHP("0\t\t0.00%\n" +
                                "150\t\t22.00%\n" +
                                "200\t\t28.00%\n" +
                                "250\t\t25.00%\n" +
                                "300\t\t25.00%\n"),
                        getHP("0\t\t85.10%\n" +
                                "1520.0\t\t14.90%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        Collections.singletonMap(2, 8.0),
                        0, 100,
                        getHP("0\t\t7.00%\n" +
                                "30\t\t37.00%\n" +
                                "40\t\t25.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t12.00%\n" +
                                "100\t\t4.00%\n"),
                        getHP("0\t\t97.11%\n" +
                                "1420.0\t\t2.89%\n"))});
        enemyDataMap.put(EnemyType.WEAPON_CARRIER.getId(), new EnemyData(new int[]{400, 450, 500}, freeWeaponData, paidWeaponData));
        //----------------------------------- End Weapon Carrier -----------------------------------------------//



        /////////////////////////////////////////// Boss
        freeWeaponData = new HashMap<>();
        freeWeaponData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(getMapFromString("2=4.0"),
                                0, 1250, 500,
                                getHP("0\t\t15.00%\n" +
                                        "15\t\t40.00%\n" +
                                        "20\t\t25.00%\n" +
                                        "30\t\t15.00%\n" +
                                        "50\t\t5.00%\n"),
                                getHP("0\t\t79.48%\n" +
                                        "50\t\t8.00%\n" +
                                        "80\t\t6.00%\n" +
                                        "100\t\t2.00%\n" +
                                        "120\t\t2.00%\n" +
                                        "150\t\t1.00%\n" +
                                        "200\t\t1.00%\n" +
                                        "250\t\t0.52%\n"),
                                getHP("0\t\t97.60%\n" +
                                        "750.0\t\t2.40%\n")
                        ),
                        new WeaponData(getMapFromString("2=5.0"),
                                0, 2500, 600,
                                getHP("0\t\t10.00%\n" +
                                        "20\t\t40.00%\n" +
                                        "30\t\t25.00%\n" +
                                        "40\t\t15.00%\n" +
                                        "50\t\t5.00%\n" +
                                        "70\t\t5.00%\n"),
                                getHP("0\t\t79.81%\n" +
                                        "80\t\t8.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t2.00%\n" +
                                        "150\t\t1.50%\n" +
                                        "200\t\t1.50%\n" +
                                        "250\t\t1.00%\n" +
                                        "500\t\t1.19%\n"),
                                getHP("0\t\t96.94%\n" +
                                        "900.0\t\t3.06%\n")
                        ),
                        new WeaponData(getMapFromString("2=6.0"),
                                0, 4000, 700,
                                getHP("0\t\t12.00%\n" +
                                        "30\t\t45.00%\n" +
                                        "50\t\t25.00%\n" +
                                        "60\t\t10.00%\n" +
                                        "70\t\t5.00%\n" +
                                        "80\t\t2.00%\n" +
                                        "100\t\t1.00%\n"),
                                getHP("0\t\t75.08%\n" +
                                        "80\t\t8.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t3.00%\n" +
                                        "150\t\t3.00%\n" +
                                        "200\t\t2.00%\n" +
                                        "250\t\t2.00%\n" +
                                        "500\t\t1.92%\n"),
                                getHP("0\t\t97.46%\n" +
                                        "1500.0\t\t2.54%\n")
                        ),
                });
        freeWeaponData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        0, 500, 200,
                        getHP("0\t\t3.00%\n" +
                                "40\t\t46.00%\n" +
                                "60\t\t32.00%\n" +
                                "80\t\t8.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n"),
                        getHP("0\t\t25.32%\n" +
                                "60\t\t40.00%\n" +
                                "80\t\t23.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.20%\n" +
                                "250\t\t0.48%\n"),
                        getHP("0\t\t93.60%\n" +
                                "900.0\t\t6.40%\n"))});
        freeWeaponData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        0, 500, 200,
                        getHP("0\t\t14.00%\n" +
                                "80\t\t35.00%\n" +
                                "100\t\t25.00%\n" +
                                "120\t\t15.00%\n" +
                                "150\t\t5.00%\n" +
                                "170\t\t3.00%\n" +
                                "200\t\t2.00%\n" +
                                "220\t\t1.00%\n"),
                        getHP("0\t\t38.18%\n" +
                                "100\t\t20.00%\n" +
                                "120\t\t20.00%\n" +
                                "150\t\t10.00%\n" +
                                "200\t\t5.00%\n" +
                                "250\t\t3.00%\n" +
                                "300\t\t2.00%\n" +
                                "400\t\t1.83%\n"),
                        getHP("0\t\t91.02%\n" +
                                "1000.0\t\t8.98%\n"))});
        freeWeaponData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        0, 500, 200,
                        getHP("0\t\t5.00%\n" +
                                "40\t\t30.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t12.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t35.29%\n" +
                                "60\t\t20.00%\n" +
                                "80\t\t20.00%\n" +
                                "100\t\t9.00%\n" +
                                "150\t\t8.00%\n" +
                                "200\t\t4.00%\n" +
                                "250\t\t2.00%\n" +
                                "350\t\t1.71%\n"),
                        getHP("0\t\t94.33%\n" +
                                "1200.0\t\t5.67%\n"))});
        freeWeaponData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        0, 500, 200,
                        getHP("0\t\t2.90%\n" +
                                "200\t\t70.00%\n" +
                                "250\t\t20.00%\n" +
                                "500\t\t4.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.00%\n" +
                                "1500\t\t0.10%\n"),
                        getHP("0\t\t46.04%\n" +
                                "200\t\t15.00%\n" +
                                "300\t\t20.00%\n" +
                                "500\t\t12.00%\n" +
                                "750\t\t3.00%\n" +
                                "1000\t\t2.00%\n" +
                                "2000\t\t1.00%\n" +
                                "2500\t\t0.96%\n"),
                        getHP("0\t\t92.12%\n" +
                                "3000.0\t\t7.88%\n"))});
        freeWeaponData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        0, 500, 200,
                        getHP("0\t\t2.00%\n" +
                                "60\t\t37.00%\n" +
                                "80\t\t35.00%\n" +
                                "90\t\t12.00%\n" +
                                "100\t\t8.00%\n" +
                                "150\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "500\t\t1.00%\n"),
                        getHP("0\t\t36.73%\n" +
                                "80\t\t18.00%\n" +
                                "100\t\t18.00%\n" +
                                "120\t\t10.00%\n" +
                                "150\t\t10.00%\n" +
                                "200\t\t3.00%\n" +
                                "300\t\t2.50%\n" +
                                "600\t\t1.77%\n"),
                        getHP("0\t\t93.04%\n" +
                                "1200.0\t\t6.96%\n"))});
        enemyDataMap.put(EnemyType.Boss.getId(), new EnemyData(null, freeWeaponData, paidWeaponData)
        );


    }

    private static void fillAdditionalWeaponData() {

        Map<Integer, Pair<Double, Double>> tmpMap;
        HashMap<Integer, List<Triple<Integer, Integer, Double>>> map;

        //------------------------------Enemy 6,17 ----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(0.00, 0.00)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(25.00, 1700.00)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, Collections.EMPTY_LIST);
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t5\t\t\t1\n" +
                "Flamethrower\t5\t\t\t1\n" +
                "Cryogun\t5\t\t\t1\n" +
                "Plasma Gun\t5\t\t\t1\n" +
                "Artillery Strike\t5\t\t\t1\n"));

        weaponDropData.put(EnemyType.ENEMY_6.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_6.getId(), map);
        weaponDropData.put(EnemyType.ENEMY_17.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_17.getId(), map);


        //------------------------------Enemy 7 ----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(20.00, 1841.7)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(50.00, 2400.0)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t7\t\t\t2\n" +
                "Flamethrower\t7\t\t\t1\n" +
                "Cryogun\t5\t\t\t1\n" +
                "Plasma Gun\t7\t\t\t1\n" +
                "Artillery Strike\t5\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t8\t\t\t2\n" +
                "Flamethrower\t8\t\t\t1\n" +
                "Cryogun\t8\t\t\t1\n" +
                "Plasma Gun\t8\t\t\t2\n" +
                "Artillery Strike\t8\t\t\t1\n"));
        weaponDropData.put(EnemyType.ENEMY_7.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_7.getId(), map);

        //------------------------------Enemy 8-11 ----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(30.00, 3000.0)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(70.00, 4080.0)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t10\t\t\t2\n" +
                "Flamethrower\t10\t\t\t1\n" +
                "Cryogun\t10\t\t\t1\n" +
                "Plasma Gun\t10\t\t\t2\n" +
                "Artillery Strike\t10\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t12\t\t\t1\n" +
                "Flamethrower\t12\t\t\t1\n" +
                "Cryogun\t12\t\t\t1\n" +
                "Plasma Gun\t12\t\t\t1\n" +
                "Artillery Strike\t12\t\t\t1\n"));
        weaponDropData.put(EnemyType.ENEMY_8.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_9.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_10.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_11.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_8.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_9.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_10.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_11.getId(), map);


        //------------------------------Enemy 15 ----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(50.00, 4664.3)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(95.00, 5500.0)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t20\t\t\t2\n" +
                "Flamethrower\t15\t\t\t1\n" +
                "Cryogun\t12\t\t\t1\n" +
                "Plasma Gun\t20\t\t\t2\n" +
                "Artillery Strike\t12\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t20\t\t\t1\n" +
                "Flamethrower\t15\t\t\t1\n" +
                "Cryogun\t15\t\t\t1\n" +
                "Plasma Gun\t20\t\t\t1\n" +
                "Artillery Strike\t15\t\t\t1\n"));
        weaponDropData.put(EnemyType.ENEMY_15.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_15.getId(), map);


        //------------------------------Enemy 12,13 ----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(60.00, 5100.0)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(100.00, 5500.0)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t15\t\t\t1\n" +
                "Flamethrower\t15\t\t\t1\n" +
                "Cryogun\t15\t\t\t1\n" +
                "Plasma Gun\t15\t\t\t1\n" +
                "Artillery Strike\t15\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t20\t\t\t1\n" +
                "Flamethrower\t15\t\t\t1\n" +
                "Cryogun\t15\t\t\t1\n" +
                "Plasma Gun\t20\t\t\t1\n" +
                "Artillery Strike\t15\t\t\t1\n"));
        weaponDropData.put(EnemyType.ENEMY_12.getId(), tmpMap);
        weaponDropData.put(EnemyType.ENEMY_13.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_12.getId(), map);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_13.getId(), map);


        //------------------------------Enemy 17, ENEMY 18 ----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(100.00, 5860.0)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(120.00, 6380.0)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t25\t\t\t1\n" +
                "Flamethrower\t18\t\t\t1\n" +
                "Cryogun\t15\t\t\t1\n" +
                "Plasma Gun\t20\t\t\t1\n" +
                "Artillery Strike\t15\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t30\t\t\t1\n" +
                "Flamethrower\t18\t\t\t1\n" +
                "Cryogun\t18\t\t\t1\n" +
                "Plasma Gun\t22\t\t\t1\n" +
                "Artillery Strike\t15\t\t\t1\n"));
        weaponDropData.put(EnemyType.ENEMY_18.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_18.getId(), map);

        //------------------------------Enemy 16 ----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(110.00, 6057.1)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(150.00, 7450.0)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t35\t\t\t2\n" +
                "Flamethrower\t18\t\t\t1\n" +
                "Cryogun\t18\t\t\t1\n" +
                "Plasma Gun\t20\t\t\t2\n" +
                "Artillery Strike\t15\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t40\t\t\t1\n" +
                "Flamethrower\t20\t\t\t1\n" +
                "Cryogun\t20\t\t\t1\n" +
                "Plasma Gun\t25\t\t\t1\n" +
                "Artillery Strike\t18\t\t\t1\n"));
        weaponDropData.put(EnemyType.ENEMY_16.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.ENEMY_16.getId(), map);

        //------------------------------Weapon Carrier ----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(1314.3, 1314.3)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(1314.3, 1314.3)); // default data for enemy
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t5\t\t\t2\n" +
                "Flamethrower\t4\t\t\t1\n" +
                "Cryogun\t4\t\t\t1\n" +
                "Plasma Gun\t5\t\t\t2\n" +
                "Artillery Strike\t4\t\t\t1\n"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("Mine Launcher\t5\t\t\t2\n" +
                "Flamethrower\t4\t\t\t1\n" +
                "Cryogun\t4\t\t\t1\n" +
                "Plasma Gun\t5\t\t\t2\n" +
                "Artillery Strike\t4\t\t\t1\n"));
        weaponDropData.put(EnemyType.WEAPON_CARRIER.getId(), tmpMap);
        additionalWeaponKilledTable.put(EnemyType.WEAPON_CARRIER.getId(), map);
    }

    private static List<Triple<Integer, Integer, Double>> getKilledTable(String data) {
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
        String delim1 = hpMapString.contains("%") ? "\n" : "\\|";
        String delim2 = hpMapString.contains("%") ? "\t\t" : ",";

        String[] split = hpMapString.split(delim1);
        for (String value : split) {
            value = value.replace("%", "");
            String[] params = value.split(delim2);
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


    public static Double getRtpForWeapon(int weaponId, boolean paidMode) {
        return paidMode ? rtpPaidWeapons.get(weaponId) : rtpWeapons.get(weaponId);
    }

    public static Double getRtpForWeaponBoss(int weaponId, boolean paidMode) {
        return paidMode ? rtpPaidWeaponsBoss.get(weaponId) : rtpWeaponsBoss.get(weaponId);
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


    public static double getHitProbabilityForBoss(int weaponId, int bossId, boolean paidMode) {
        EnemyData enemyData = getEnemyData(EnemyType.Boss.getId());
        Double weaponRTP = getRtpForWeaponBoss(weaponId, paidMode);
        Double pistolRTP = bossPistolRtp.get(bossId);
        double rtpForWeapon = weaponId == PISTOL_DEFAULT_WEAPON_ID ? pistolRTP :  weaponRTP;
        Double swAvgPayouts = enemyData.getSwAvgPayouts(weaponId, bossId - 1, PAY_HIT_PERCENT, paidMode);
        return rtpForWeapon / swAvgPayouts;
    }


    public static int getBomberHPWin() {
        return BOMBER_HP_WIN;
    }

    public static Map<Integer, Pair<Integer, Integer>> getBossLimits() {
        return bossLimits;
    }


    public static int[] getDistributionByEnemy(int totalDamage, int cntEnemies) {
        int averageDamage = totalDamage / cntEnemies;
        int sumDamage = 0;
        int[] res = new int[cntEnemies];
        for (int i = 0; i < res.length - 1; i++) {
            res[i] = averageDamage;
            sumDamage += averageDamage;
        }
        res[cntEnemies - 1] = totalDamage - sumDamage;
        return res;
    }

    public static int getRandomPayoutForWheel() {
        return  GameTools.getRandomNumberKeyFromMap(wheelPayouts);
    }

    public static Set<Integer> getPossibleBetLevels() {
        return possibleBetLevels;
    }

    public static double getWheelHitPistol() {
        return wheelHitPistol;
    }

    public static double getWheelHitSpecialWeapons() {
        return wheelHitSpecialWeapons;
    }

    public static List<Integer> getMoneyWheelPayouts(){
        ArrayList<Integer> pays = new ArrayList<>(wheelPayouts.keySet());
        Collections.sort(pays);
        return pays;
    }

    public static Integer getPaidWeaponCost(int wpId) {
        return paidWeaponCosts.get(wpId);
    }


    public static double getDroppedWeaponPistolRTP() {
        return droppedWeaponPistolRTP;
    }

    public static double getDroppedWeaponSWRTP() {
        return droppedWeaponSWRTP;
    }

    public static double getDroppedWeaponAvgDropPrice() {
        return droppedWeaponAvgDropPrice;
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

    public static Map<Integer, Double> getRtpWeapons() {
        return rtpWeapons;
    }

    public static Map<Integer, Double> getRtpPaidWeapons() {
        return rtpPaidWeapons;
    }

    public static Map<Integer, Double> getRtpWeaponsBoss() {
        return rtpWeaponsBoss;
    }

    public static Map<Integer, Double> getRtpPaidWeaponsBoss() {
        return rtpPaidWeaponsBoss;
    }

    public static Double getRtpCompensateSpecialWeapons(int weaponId, boolean paidMode) {
        return paidMode ? rtpCompensatePaidSpecialWeapons.get(weaponId) : rtpCompensateFreeSpecialWeapons.get(weaponId);
    }

}
