package com.betsoft.casino.mp.clashofthegods.model.math;

import com.betsoft.casino.mp.clashofthegods.model.math.enemies.*;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.Triple;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MathData {

    private static final Map<Integer, Double> rtpWeapons;
    private static final Map<Integer, Double> rtpWeaponsAll;
    private static final Map<Integer, IEnemyData> enemyDataMap;
    public static final double PAY_HIT_PERCENT = 0.1;
    private static final Map<Integer, Map<Integer, Double>> damageByWeapons;
    private static final double BOS_MAIN_PROB = 1 / 1750.;
    private static final List<Triple<Integer, Integer, Double>> bossParams;
    public static final int PISTOL_DEFAULT_WEAPON_ID = -1;
    public static final int ALL_DEFAULT_WEAPON_ID = -2;
    private static final Map<Integer, Double> bossPistolRtp;
    private static final Map<Integer, Pair<Integer, Integer>> bossLimits;
    private static final Set<Integer> possibleBetLevels;
    private static final Map<String, Integer> idxWeaponsMap;
    private static  final  Map<Integer, Double> wheelPayouts;
    private static final double wheelRTPPistol = 5.0/100;
    private static final double wheelRTPSW = 50./100;
    private static final double wheelAveragePayouts;
    private static final double wheelHitPistol;
    private static final double wheelHitSpecialWeapons;
    private static final Map<Integer, Integer> paidWeaponCosts;
    private static final double totalRTP = 97.5;
    private static final Map<Integer, Double> weaponAwerageNumberEnemiesForWeapons;
    private static final Map<Integer, Double> totalHPdamageForExplorer;
    public static double  AVG_HP_WIN_EXPLODER;
    private static final List<Triple<Integer, Integer, Double>> droppedWeaponsTable;
    private static final double droppedWeaponPistolRTP = 0.20;
    private static final double droppedWeaponSWRTP = 1.50;
    private static final double droppedWeaponAvgDropPrice;
    private static final double droppedWeaponPistolFreq;
    private static final double droppedWeaponSWFreq;


    static {

        wheelPayouts = getMapFromString("25=0.70|50=0.12|100=0.07|250=0.04|500=0.03|1000=0.02|2000=0.01|5000=0.01");
        AtomicReference<Double> wpay = new AtomicReference<>((double) 0);
        wheelPayouts.forEach((integer, aDouble) -> wpay.updateAndGet(v -> v + integer * aDouble));
        wheelAveragePayouts = wpay.get();
        wheelHitPistol = wheelRTPPistol / wheelAveragePayouts;
        wheelHitSpecialWeapons = wheelRTPSW / wheelAveragePayouts;

        idxWeaponsMap = new HashMap<>();
        idxWeaponsMap.put("Laser", SpecialWeaponType.Ricochet.getId());
        idxWeaponsMap.put("Lightning", SpecialWeaponType.Lightning.getId());
        idxWeaponsMap.put("Napalm", SpecialWeaponType.Napalm.getId());
        idxWeaponsMap.put("Nuke", SpecialWeaponType.Nuke.getId());
        idxWeaponsMap.put("Artillery", SpecialWeaponType.ArtilleryStrike.getId());

        paidWeaponCosts = new HashMap<>();
        paidWeaponCosts.put(SpecialWeaponType.Ricochet.getId(), 30);
        paidWeaponCosts.put(SpecialWeaponType.Lightning.getId(), 60);
        paidWeaponCosts.put(SpecialWeaponType.Napalm.getId(), 90);
        paidWeaponCosts.put(SpecialWeaponType.ArtilleryStrike.getId(), 120);
        paidWeaponCosts.put(SpecialWeaponType.Nuke.getId(), 150);

        possibleBetLevels = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(1, 2, 3, 5, 10)));

        bossPistolRtp = new HashMap<>();
        bossPistolRtp.put(1, 72.5);
        bossPistolRtp.put(2, 72.5);
        bossPistolRtp.put(3, 72.5);

        bossLimits = new HashMap<>();
        bossLimits.put(1, new Pair<>(1250, 500));
        bossLimits.put(2, new Pair<>(2500, 600));
        bossLimits.put(3, new Pair<>(4000, 700));

        bossParams = new LinkedList<>();
        bossParams.add(new Triple<>(0, 2500, 40.));
        bossParams.add(new Triple<>(1, 5000, 35.));
        bossParams.add(new Triple<>(2, 7500, 25.));

        totalHPdamageForExplorer = new HashMap<>();
        totalHPdamageForExplorer.put(100, 18.75 / 100);
        totalHPdamageForExplorer.put(150, 25. / 100);
        totalHPdamageForExplorer.put(200, 28. / 100);
        totalHPdamageForExplorer.put(250, 15. / 100);
        totalHPdamageForExplorer.put(300, 8. / 100);
        totalHPdamageForExplorer.put(500, 3. / 100);
        totalHPdamageForExplorer.put(1000, 1. / 100);
        totalHPdamageForExplorer.put(1500, 0.75 / 100);
        totalHPdamageForExplorer.put(2500, 0.50 / 100);

        AtomicReference<Double> sumMult = new AtomicReference<>((double) 0);
        totalHPdamageForExplorer.forEach((integer, aDouble) ->
                sumMult.updateAndGet(v -> (v + (integer * aDouble / 100))));
        AVG_HP_WIN_EXPLODER = sumMult.get() * 100;


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
            rtpWeapons.put(wpId, (actualActualAvg - (droppedWeaponSWRTP * 100) -  (wheelRTPSW * 100)) / averageNumberEnemies);
        }

        enemyDataMap = new HashMap<>();
        enemyDataMap.put(EnemyType.Phoenix.getId(), new Phoenix());
        enemyDataMap.put(EnemyType.Lantern.getId(), new Lantern());
        enemyDataMap.put(EnemyType.Golden_Dragon.getId(), new GoldenDragon());
        enemyDataMap.put(EnemyType.Silver_Dragon.getId(), new SilverDragon());
        enemyDataMap.put(EnemyType.Tiger.getId(), new Tiger());
        enemyDataMap.put(EnemyType.Lizard.getId(), new Lizard());
        enemyDataMap.put(EnemyType.Lizard_1.getId(), new LizardOne());
        enemyDataMap.put(EnemyType.Lizard_2.getId(), new LizardTwo());
        enemyDataMap.put(EnemyType.Snake.getId(), new Snake());
        enemyDataMap.put(EnemyType.Owl.getId(), new Owl());
        enemyDataMap.put(EnemyType.Evil_Spirit.getId(), new EvilSpirit());
        enemyDataMap.put(EnemyType.Evil_Spirit_1.getId(), new EvilSpiritOne());
        enemyDataMap.put(EnemyType.Evil_Spirit_2.getId(), new EvilSpiritTwo());
        enemyDataMap.put(EnemyType.Dragonfly_Red.getId(), new DragonFlyRed());
        enemyDataMap.put(EnemyType.Dragonfly_Green.getId(), new DragonFlyGreen());
        enemyDataMap.put(EnemyType.Beetle_1.getId(), new BeetleOne());
        enemyDataMap.put(EnemyType.Beetle_2.getId(), new BeetleTwo());
        enemyDataMap.put(EnemyType.Spirits_1_RED.getId(), new Spirit());
        enemyDataMap.put(EnemyType.Spirits_2_ORANGE.getId(), new Spirit());
        enemyDataMap.put(EnemyType.Spirits_3_GREEN.getId(), new Spirit());
        enemyDataMap.put(EnemyType.Spirits_4_BLUE.getId(), new Spirit());
        enemyDataMap.put(EnemyType.Spirits_5_VIOLETT.getId(), new Spirit());
        enemyDataMap.put(EnemyType.Boss.getId(), new Boss());


        droppedWeaponsTable = getKilledTable("Laser\t3\t\t\t\t1\n" +
                "Laser\t4\t\t\t\t2\n" +
                "Laser\t5\t\t\t\t2\n" +
                "Laser\t6\t\t\t\t1\n" +
                "Lightning\t2\t\t\t\t2\n" +
                "Lightning\t3\t\t\t\t2\n" +
                "Lightning\t4\t\t\t\t1\n" +
                "Lightning\t5\t\t\t\t1\n" +
                "Napalm\t2\t\t\t\t2\n" +
                "Napalm\t3\t\t\t\t1\n" +
                "Napalm\t4\t\t\t\t1\n" +
                "Artillery\t1\t\t\t\t2\n" +
                "Artillery\t2\t\t\t\t1\n" +
                "Artillery\t3\t\t\t\t1\n" +
                "Nuke\t1\t\t\t\t1\n" +
                "Nuke\t2\t\t\t\t1\n");

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

    public static IEnemyData getEnemyData(Integer enemyTypeId) {
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

        Map<Integer, List<Triple<Integer, Integer, Double>>> map = enemyDataMap.get(enemyTypeId).getAdditionalWeaponKilledTable();
        if(map == null)
            return Collections.emptyList();
        List<Triple<Integer, Integer, Double>> res = map.get(weaponTypeId);
        if (weaponTypeId == PISTOL_DEFAULT_WEAPON_ID) {
            return res != null ? res : new LinkedList<>();
        } else
            return res != null ? res : map.get(ALL_DEFAULT_WEAPON_ID);
    }

    public static Pair<Double, Double> getWeaponDropData(int enemyTypeId, int weaponTypeId) {
        Map<Integer, Pair<Double, Double>> enemyWeaponData = enemyDataMap.get(enemyTypeId).getWeaponDropData();
        if (enemyWeaponData == null) {
            return  null;
        }
        Pair<Double, Double> weaponData = enemyWeaponData.get(weaponTypeId);
        return enemyWeaponData.get(weaponTypeId) == null && weaponTypeId != PISTOL_DEFAULT_WEAPON_ID ?
                enemyWeaponData.get(ALL_DEFAULT_WEAPON_ID) : weaponData;
    }


    public static double getHitProbabilityForBoss(int weaponId, int bossId, boolean paidMode,Map<Long, Double> hitPointMapData) {
        IEnemyData enemyData = getEnemyData(EnemyType.Boss.getId());
        Double weaponRTP = getRtpForWeapon(weaponId);
        Double pistolRTP = bossPistolRtp.get(bossId);
        double rtpForWeapon = weaponId == PISTOL_DEFAULT_WEAPON_ID ? pistolRTP :  weaponRTP;
        Double swAvgPayouts = enemyData.getSwAvgPayouts(weaponId, bossId - 1, PAY_HIT_PERCENT, paidMode, hitPointMapData);
        return rtpForWeapon / swAvgPayouts;
    }



    public static Map<Integer, Pair<Integer, Integer>> getBossLimits() {
        return bossLimits;
    }

    public static double getAvgHpWinExploder() {
        return AVG_HP_WIN_EXPLODER;
    }

    public static Map<Integer, Double> getTotalHPdamageForExplorer() {
        return totalHPdamageForExplorer;
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
