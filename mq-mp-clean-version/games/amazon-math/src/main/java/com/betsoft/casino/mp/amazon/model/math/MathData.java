package com.betsoft.casino.mp.amazon.model.math;

import com.betsoft.casino.mp.model.Gem;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.Triple;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MathData {
    private static final Map<Integer, Double> rtpWeapons;
    private static final Map<Integer, EnemyData> enemyDataMap;
    public static final double PAY_HIT_PERCENT = 0.1;
    private static final Map<Integer, Map<Integer, Double>> damageByWeapons;
    private static final double BOS_MAIN_PROB = 1 / 2000.;
    private static final List<Triple<Integer, Integer, Double>> bossParams;
    private static final double PROB_TREASURE_FOR_QUEST = 0.005714285714286;
    private static final Map<Treasure, Double> treasures;
    private static final Map<Integer, Map<Integer, List<Triple<Integer, Integer, Double>>>> additionalWeaponKilledTable;
    private static final Map<Integer, Double> totalHPdamageForExplorer;
    public static int PISTOL_DEFAULT_WEAPON_ID = -1;
    public static int ALL_DEFAULT_WEAPON_ID = -2;
    public static double  AVG_HP_WIN_EXPLODER;
    private static final Map<Integer, Double> bossPistolRtp;
    private static final Map<Integer, Pair<Integer, Integer>> bossLimits;
    public static final List<Integer> WEAPON_LOOT_BOX_PRICES = Arrays.asList(150, 300, 450);


    private static final int[] idxWeaponsMapping = new int[]{
            SpecialWeaponType.DoubleStrengthPowerUp.getId(),
            SpecialWeaponType.HolyArrows.getId(),
            SpecialWeaponType.Bomb.getId(),
            SpecialWeaponType.Landmines.getId(),
            SpecialWeaponType.RocketLauncher.getId(),
            SpecialWeaponType.MachineGun.getId(),
            SpecialWeaponType.Ricochet.getId(),
            SpecialWeaponType.Flamethrower.getId(),
            SpecialWeaponType.Cryogun.getId(),
            SpecialWeaponType.Plasma.getId(),
            SpecialWeaponType.Railgun.getId(),
            SpecialWeaponType.ArtilleryStrike.getId()
    };


    //---------------  EnemyTypeId, Map<WeaponId, Pair<AvgWeaponAward, WeaponsDropGainHP>>
    private static final Map<Integer, Map<Integer, Pair<Double, Double>>> weaponDropData;


    private static final Map<Integer, Double> randomMultiplierForMultiplier;
    private static final Map<Integer, Map<Integer, List<Pair<Integer, Double>>>> gemPayoutsByBoss;
    private static final Map<Integer, Double> weaponAwerageNumberEnemiesForWeapons;

//0 - ShotGun (AP Pistol Ammo)
//11 - ChainGun (Shotgun)
//1 - Grenade (Grenade)
//5 - Mine Launcher (Mine Launcher)
//8 - Rocket launcher (Rocket Launcher)
//2 - Machine Gun (Machine Gun)
//3 - Laser Gun (Laser)
//9 - Flamethrower (Flamethrower)
//10 - Cryogun (Cryogun)
//4 - Plasma Gun (Plasma Rifle)
//6 - Railgun (Railgun)
//7 - Artillery Strike (Artillery Strike)

    static {

        weaponDropData = new HashMap<>();
        additionalWeaponKilledTable = new HashMap<>();

        fillAdditionalWeaponData();

        bossPistolRtp = new HashMap<>();
        bossPistolRtp.put(1, 80.);
        bossPistolRtp.put(2, 77.);
        bossPistolRtp.put(3, 74.);

        bossLimits = new HashMap<>();
        bossLimits.put(1, new Pair<>(500, 200));
        bossLimits.put(2, new Pair<>(1250, 400));
        bossLimits.put(3, new Pair<>(2500, 1000));


        gemPayoutsByBoss = new HashMap<>();

        Map<Integer, List<Pair<Integer, Double>>> tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 5.), new Pair<>(30, 6.5), new Pair<>(40, 7.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 5.), new Pair<>(25, 5.5), new Pair<>(30, 6.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 4.), new Pair<>(18, 4.3), new Pair<>(20, 5.3)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 3.5), new Pair<>(12, 4.20), new Pair<>(15, 5.20)));
        gemPayoutsByBoss.put(PISTOL_DEFAULT_WEAPON_ID, tmBossGem);

        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 9.), new Pair<>(30, 8.), new Pair<>(40, 12.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 8.), new Pair<>(25, 8.), new Pair<>(30, 8.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 6.), new Pair<>(18, 7.), new Pair<>(20, 7.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 7.0), new Pair<>(12, 7.), new Pair<>(15, 3.)));
        gemPayoutsByBoss.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(), tmBossGem);


        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 10.), new Pair<>(30, 10.), new Pair<>(40, 20.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 10.), new Pair<>(25, 12.), new Pair<>(30, 10.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 10.), new Pair<>(18, 13.), new Pair<>(20, 10.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 20.0), new Pair<>(12, 15.), new Pair<>(15, 10.)));
        gemPayoutsByBoss.put(SpecialWeaponType.HolyArrows.getId(), tmBossGem);

        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 8.), new Pair<>(30, 8.), new Pair<>(40, 8.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 7.), new Pair<>(25, 7.), new Pair<>(30, 6.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 6.), new Pair<>(18, 6.), new Pair<>(20, 6.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 4.0), new Pair<>(12, 4.), new Pair<>(15, 5.)));
        gemPayoutsByBoss.put(SpecialWeaponType.Bomb.getId(), tmBossGem);

        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 8.), new Pair<>(30, 8.), new Pair<>(40, 8.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 7.), new Pair<>(25, 7.), new Pair<>(30, 6.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 6.), new Pair<>(18, 6.), new Pair<>(20, 6.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 4.0), new Pair<>(12, 4.), new Pair<>(15, 5.)));
        gemPayoutsByBoss.put(SpecialWeaponType.Landmines.getId(), tmBossGem);


        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 10.), new Pair<>(30, 12.), new Pair<>(40, 50.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 20.), new Pair<>(25, 22.), new Pair<>(30, 15.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 20.), new Pair<>(18, 22.), new Pair<>(20, 10.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 50.0), new Pair<>(12, 44.), new Pair<>(15, 25.)));
        gemPayoutsByBoss.put(SpecialWeaponType.RocketLauncher.getId(), tmBossGem);

        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 10.), new Pair<>(20, 10.), new Pair<>(40, 30.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 20.), new Pair<>(25, 20.), new Pair<>(30, 15.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 20.), new Pair<>(18, 20.), new Pair<>(20, 10.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 25.0), new Pair<>(12, 25.), new Pair<>(15, 20.)));
        gemPayoutsByBoss.put(SpecialWeaponType.MachineGun.getId(), tmBossGem);


        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 10.), new Pair<>(20, 15.), new Pair<>(40, 30.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 15.), new Pair<>(25, 15.), new Pair<>(30, 15.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 20.), new Pair<>(18, 15.), new Pair<>(20, 10.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 15.0), new Pair<>(12, 15.), new Pair<>(15, 5.)));
        gemPayoutsByBoss.put(SpecialWeaponType.Ricochet.getId(), tmBossGem);

        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 10.), new Pair<>(20, 10.), new Pair<>(40, 10.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 10.), new Pair<>(25, 10.), new Pair<>(30, 10.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 10.), new Pair<>(18, 10.), new Pair<>(20, 10.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 10.0), new Pair<>(12, 10.), new Pair<>(15, 10.)));
        gemPayoutsByBoss.put(SpecialWeaponType.Flamethrower.getId(), tmBossGem);

        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 10.), new Pair<>(20, 10.), new Pair<>(40, 10.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 10.), new Pair<>(25, 10.), new Pair<>(30, 10.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 10.), new Pair<>(18, 10.), new Pair<>(20, 10.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 10.0), new Pair<>(12, 10.), new Pair<>(15, 10.)));
        gemPayoutsByBoss.put(SpecialWeaponType.Cryogun.getId(), tmBossGem);


        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 50.), new Pair<>(20, 70.), new Pair<>(40, 100.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 50.), new Pair<>(25, 60.), new Pair<>(30, 40.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 50.), new Pair<>(18, 40.), new Pair<>(20, 30.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 50.0), new Pair<>(12, 30.), new Pair<>(15, 30.)));
        gemPayoutsByBoss.put(SpecialWeaponType.Plasma.getId(), tmBossGem);


        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 40.), new Pair<>(20, 30.), new Pair<>(40, 30.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 25.), new Pair<>(25, 30.), new Pair<>(30, 33.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 20.), new Pair<>(18, 30.), new Pair<>(20, 23.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 15.0), new Pair<>(12, 10.), new Pair<>(15, 14.)));
        gemPayoutsByBoss.put(SpecialWeaponType.Railgun.getId(), tmBossGem);


        tmBossGem = new HashMap<>();
        tmBossGem.put(Gem.DIAMOND.ordinal(), Arrays.asList(new Pair<>(25, 9.), new Pair<>(20, 9.), new Pair<>(40, 9.)));
        tmBossGem.put(Gem.RUBY.ordinal(), Arrays.asList(new Pair<>(20, 8.), new Pair<>(25, 8.), new Pair<>(30, 8.)));
        tmBossGem.put(Gem.EMERALD.ordinal(), Arrays.asList(new Pair<>(15, 7.), new Pair<>(18, 7.), new Pair<>(20, 7.)));
        tmBossGem.put(Gem.SAPPHIRE.ordinal(), Arrays.asList(new Pair<>(10, 6.0), new Pair<>(12, 6.), new Pair<>(15, 6.)));
        gemPayoutsByBoss.put(SpecialWeaponType.ArtilleryStrike.getId(), tmBossGem);


        bossParams = new LinkedList<>();
        bossParams.add(new Triple<>(0, 1000, 55.));
        bossParams.add(new Triple<>(1, 2500, 30.));
        bossParams.add(new Triple<>(2, 5000, 15.));

        totalHPdamageForExplorer = new HashMap<>();
        totalHPdamageForExplorer.put(200, 46.5 / 100);
        totalHPdamageForExplorer.put(250, 35. / 100);
        totalHPdamageForExplorer.put(300, 11. / 100);
        totalHPdamageForExplorer.put(500, 5. / 100);
        totalHPdamageForExplorer.put(1000, 1. / 100);
        totalHPdamageForExplorer.put(1500, 1. / 100);
        totalHPdamageForExplorer.put(2500, 0.5 / 100);

        AtomicReference<Double> sumMult = new AtomicReference<>((double) 0);
        totalHPdamageForExplorer.forEach((integer, aDouble) ->
                sumMult.updateAndGet(v -> (double) (v + (integer * aDouble / 100))));
        AVG_HP_WIN_EXPLODER = sumMult.get() * 100;


        randomMultiplierForMultiplier = new HashMap<>();
        randomMultiplierForMultiplier.put(15, 25. / 100);
        randomMultiplierForMultiplier.put(20, 27. / 100);
        randomMultiplierForMultiplier.put(25, 25. / 100);
        randomMultiplierForMultiplier.put(30, 18. / 100);
        randomMultiplierForMultiplier.put(40, 5. / 100);


        treasures = new HashMap<>();
        treasures.put(Treasure.Brass_Scarab_Sigil, 1.);
        treasures.put(Treasure.Cartouche_of_Gold_Amulet, 1.);
        treasures.put(Treasure.Eagle_of_Ruby_Emerald, 1.);
        treasures.put(Treasure.Garnet_Scarab_Amulet, 1.);
        treasures.put(Treasure.Golden_Statue_of_Tutenkhamun, 1.);
        treasures.put(Treasure.Vase_of_Osiris, 2.);
        treasures.put(Treasure.Golden_Bust_of_Anubis, 2.);
        treasures.put(Treasure.Knight_of_Anubis_Bust, 2.);
        treasures.put(Treasure.Lapis_Cuff_of_the_Queen, 2.);
        treasures.put(Treasure.Offering_Bowl_of_Gems, 2.);
        treasures.put(Treasure.Onyx_servent_of_Bastet, 3.);
        treasures.put(Treasure.Onyx_Statue_of_Pharaoh, 3.);
        treasures.put(Treasure.Sacred_Golden_Pyramid, 3.);
        treasures.put(Treasure.Golden_Bust_of_Nefertiti, 3.);
        treasures.put(Treasure.Jade_Brooch, 3.);

        rtpWeapons = new HashMap<>();
        rtpWeapons.put(PISTOL_DEFAULT_WEAPON_ID, 86.55);
        rtpWeapons.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(), 500.);
        rtpWeapons.put(SpecialWeaponType.HolyArrows.getId(), 700.);
        rtpWeapons.put(SpecialWeaponType.Bomb.getId(), 291.26);
        rtpWeapons.put(SpecialWeaponType.Landmines.getId(), 375.);
        rtpWeapons.put(SpecialWeaponType.RocketLauncher.getId(), 1200.);
        rtpWeapons.put(SpecialWeaponType.MachineGun.getId(), 1000.);
        rtpWeapons.put(SpecialWeaponType.Ricochet.getId(), 757.58);
        rtpWeapons.put(SpecialWeaponType.Flamethrower.getId(), 583.33);
        rtpWeapons.put(SpecialWeaponType.Cryogun.getId(), 422.54);
        rtpWeapons.put(SpecialWeaponType.Plasma.getId(), 2500.);
        rtpWeapons.put(SpecialWeaponType.Railgun.getId(), 1250.);
        rtpWeapons.put(SpecialWeaponType.ArtilleryStrike.getId(), 438.6);

        damageByWeapons = new HashMap<>();
        damageByWeapons.put(SpecialWeaponType.HolyArrows.getId(), getMapFromString("1=100"));
        damageByWeapons.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(), getMapFromString("1=100"));
        damageByWeapons.put(SpecialWeaponType.Bomb.getId(), getMapFromString("4=20|5=50|6=25|7=5"));
        damageByWeapons.put(SpecialWeaponType.Landmines.getId(), getMapFromString("3=5|4=40|5=30|6=20|7=5"));
        damageByWeapons.put(SpecialWeaponType.RocketLauncher.getId(), getMapFromString("1=100"));
        damageByWeapons.put(SpecialWeaponType.MachineGun.getId(), getMapFromString("1=100"));
        damageByWeapons.put(SpecialWeaponType.Ricochet.getId(), getMapFromString("2=10|3=50|4=40"));
        damageByWeapons.put(SpecialWeaponType.Flamethrower.getId(), getMapFromString("5=30|6=50|7=10|8=10"));
        damageByWeapons.put(SpecialWeaponType.Cryogun.getId(), getMapFromString("9=5|10=40|11=40|12=15"));
        damageByWeapons.put(SpecialWeaponType.Plasma.getId(), getMapFromString("1=100"));
        damageByWeapons.put(SpecialWeaponType.Railgun.getId(), getMapFromString("1=30|2=40|3=30"));
        damageByWeapons.put(SpecialWeaponType.ArtilleryStrike.getId(), getMapFromString("10=10|11=40|12=50"));


        weaponAwerageNumberEnemiesForWeapons = new HashMap<>();
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.HolyArrows.getId(), 1.);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(), 1.);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Bomb.getId(), 5.15);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Landmines.getId(), 4.8);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.RocketLauncher.getId(), 1.);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.MachineGun.getId(), 1.);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Ricochet.getId(), 3.3);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Flamethrower.getId(), 6.0);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Cryogun.getId(), 10.65);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Plasma.getId(), 1.);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.Railgun.getId(), 2.);
        weaponAwerageNumberEnemiesForWeapons.put(SpecialWeaponType.ArtilleryStrike.getId(), 11.4);

        Map<Integer, WeaponData[]> wData;
        enemyDataMap = new HashMap<>();

        /////////////////////////////////////////// SKULL BREAKER
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID, // default weapon - Pistol
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 100,
                                getHP("0,21.00|20,25.00|80,30.00|100,15.00|120,5.00|150,2.00|200,1.00|250,1.00"),
                                getHP("0,77.00|250.0,23.00")),
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 120,
                                getHP("0,19.00|20,15.00|80,25.00|100,25.00|150,12.00|170,2.00|200,1.00|250,1.00"),
                                getHP("0,71.58|260.0,28.42")),
                        new WeaponData(Collections.singletonMap(2, 9.0), 50.0, 140,
                                getHP("0,9.00|20,15.00|80,30.00|100,27.00|150,10.00|170,6.00|200,2.00|250,1.00"),
                                getHP("0,68.26|270.0,31.74"))
                });
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{
                        new WeaponData(
                                getMapFromString("2=5.0|3=3.0|5=2.00"),
                                40, 50,
                                getHP("0,2.00|70,25.00|80,40.00|100,15.00|150,6.00|200,5.00|250,4.00|500,3.00"),
                                getHP("0,56.60|250.0,43.40"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(), // shotgun
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=4.00|3=3.00|5=2.00|7=1.00|10=1.00"),
                        45, 60,
                        getHP("0,6.00|80,22.00|100,30.00|150,25.00|200,10.00|250,4.00|300,2.00|500,1.00"),
                        getHP("0,57.97|300.0,42.03"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=1.00|3=0.50|5=0.50"),
                        45, 25,
                        getHP("0,3.00|50,30.00|60,35.00|80,20.00|100,5.00|120,4.00|150,2.00|200,1.00"),
                        getHP("0,66.60|200.0,33.40"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=1.00|3=0.50|5=0.50|7=0.25|10=0.01"),
                        50, 30,
                        getHP("0,3.00|40,40.00|60,30.00|80,10.00|100,8.00|120,4.00|150,3.00|200,2.00"),
                        getHP("0,68.35|200.0,31.65"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=3.00|3=2.50|5=0.75|7=0.50|10=0.25"),
                        50, 100,
                        getHP("0,4.00|100,40.00|150,32.00|250,15.00|500,5.00|750,2.00|1000,1.00|1250,1.00"),
                        getHP("0,37.33|300.0,62.67"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=3.00|3=1.50|5=1.00|7=0.50|10=0.25"),
                        45, 80,
                        getHP("0,6.00|80,35.00|100,30.00|150,15.00|250,8.00|500,3.00|750,2.00|1000,1.00"),
                        getHP("0,49.82|280.0,50.18"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=2.00|3=1.00|5=1.00"),
                        50, 40,
                        getHP("0,7.00|80,15.00|100,40.00|150,17.00|200,10.00|250,8.00|500,2.00|750,1.00"),
                        getHP("0,46.00|250.0,54.00"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=5.00|3=3.00|5=2.00"),
                        50, 40,
                        getHP("0,14.00|80,35.00|100,25.00|120,15.00|150,5.00|170,3.00|200,2.00|220,1.00"),
                        getHP("0,59.18|220.0,40.82"))});
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=2.50|3=1.00|5=0.75|7=0.50|10=0.25"),
                        45, 30,
                        getHP("0,5.00|40,30.00|60,30.00|80,15.00|100,12.00|150,5.00|200,2.00|250,1.00"),
                        getHP("0,66.00|200.0,34.00"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=2.50|3=1.50|5=1.00|7=0.50|10=0.25"),
                        50, 120,
                        getHP("0,2.00|300,20.00|400,50.00|500,15.00|750,6.00|1500,4.00|2500,2.00|5000,1.00"),
                        getHP("0,10.00|600.0,90.00"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=3.50|3=2.00|5=1.00"),
                        50, 100,
                        getHP("0,8.00|150,32.00|250,35.00|500,20.00|750,2.00|1000,1.00|1500,1.00|2500,1.00"),
                        getHP("0,24.88|400.0,75.13"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=1.00|3=0.75|5=0.50|7=0.25|10=0.10"),
                        50, 30,
                        getHP("0,2.00|60,25.00|80,40.00|90,17.00|100,10.00|150,3.00|250,2.00|500,1.00"),
                        getHP("0,56.60|200.0,43.40"))});
        enemyDataMap.put(EnemyType.SKULL_BREAKER.getId(), new EnemyData(new int[]{1200, 1350, 1500}, wData));


        /////////////////////////////////////////// JAGUAR
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 8.0), 50, 50,
                                getHP("0\t\t21.00%\n" +
                                        "20\t\t25.00%\n" +
                                        "60\t\t30.00%\n" +
                                        "80\t\t15.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t2.00%\n" +
                                        "150\t\t1.00%\n" +
                                        "200\t\t1.00%\n"),
                                getHP("0\t\t69.40%\n" +
                                        "150.0\t\t30.60%\n")),
                        new WeaponData(Collections.singletonMap(2, 8.0), 45, 60,
                                getHP("0\t\t14.00%\n" +
                                        "20\t\t15.00%\n" +
                                        "60\t\t25.00%\n" +
                                        "80\t\t25.00%\n" +
                                        "100\t\t15.00%\n" +
                                        "120\t\t3.00%\n" +
                                        "150\t\t2.00%\n" +
                                        "200\t\t1.00%\n"),
                                getHP("0\t\t61.50%\n" +
                                        "160.0\t\t38.50%\n")),
                        new WeaponData(Collections.singletonMap(2, 8.0), 40, 70,
                                getHP("0\t\t5.00%\n" +
                                        "20\t\t15.00%\n" +
                                        "60\t\t35.00%\n" +
                                        "80\t\t27.00%\n" +
                                        "100\t\t10.00%\n" +
                                        "150\t\t5.00%\n" +
                                        "200\t\t2.00%\n" +
                                        "220\t\t1.00%\n"),
                                getHP("0\t\t59.24%\n" +
                                        "170.0\t\t40.76%\n"))
                });
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        40, 40,
                        getHP("0\t\t2.00%\n" +
                                "70\t\t25.00%\n" +
                                "80\t\t40.00%\n" +
                                "100\t\t15.00%\n" +
                                "120\t\t6.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t4.00%\n" +
                                "250\t\t3.00%\n"),
                        getHP("0\t\t56.95%\n" +
                                "220.0\t\t43.05%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(), // shotgun
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t4.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n" +
                                "7\t\t1.00%\n" +
                                "10\t\t1.00%\n"),
                        45, 50,
                        getHP("0\t\t7.00%\n" +
                                "80\t\t27.00%\n" +
                                "100\t\t33.00%\n" +
                                "150\t\t20.00%\n" +
                                "170\t\t7.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "350\t\t1.00%\n"),
                        getHP("0\t\t55.60%\n" +
                                "250.0\t\t44.40%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n"),
                        40, 25,
                        getHP("0\t\t6.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t30.00%\n" +
                                "60\t\t20.00%\n" +
                                "80\t\t5.00%\n" +
                                "100\t\t4.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n"),
                        getHP("0\t\t66.50%\n" +
                                "160.0\t\t33.50%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        45, 30,
                        getHP("0\t\t10.00%\n" +
                                "40\t\t40.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t10.00%\n" +
                                "100\t\t4.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "170\t\t1.00%\n"),
                        getHP("0\t\t66.06%\n" +
                                "160.0\t\t33.94%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t2.50%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 80,
                        getHP("0\t\t4.00%\n" +
                                "100\t\t40.00%\n" +
                                "120\t\t32.00%\n" +
                                "150\t\t12.00%\n" +
                                "250\t\t6.00%\n" +
                                "500\t\t3.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.00%\n"),
                        getHP("0\t\t43.93%\n" +
                                "270.0\t\t56.07%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        45, 70,
                        getHP("0\t\t6.00%\n" +
                                "80\t\t35.00%\n" +
                                "100\t\t30.00%\n" +
                                "150\t\t15.00%\n" +
                                "200\t\t8.00%\n" +
                                "250\t\t3.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n"),
                        getHP("0\t\t51.40%\n" +
                                "250.0\t\t48.60%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.00%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 40,
                        getHP("0\t\t6.00%\n" +
                                "60\t\t15.00%\n" +
                                "80\t\t40.00%\n" +
                                "100\t\t17.00%\n" +
                                "150\t\t12.00%\n" +
                                "250\t\t7.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n"),
                        getHP("0\t\t38.33%\n" +
                                "180.0\t\t61.67%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 40,
                        getHP("0\t\t6.00%\n" +
                                "60\t\t25.00%\n" +
                                "70\t\t35.00%\n" +
                                "80\t\t20.00%\n" +
                                "100\t\t8.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n"),
                        getHP("0\t\t54.94%\n" +
                                "160.0\t\t45.06%\n"))});
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        45, 30,
                        getHP("0\t\t5.00%\n" +
                                "40\t\t35.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t7.00%\n" +
                                "120\t\t5.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n"),
                        getHP("0\t\t61.25%\n" +
                                "160.0\t\t38.75%\n"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 120,
                        getHP("0\t\t4.00%\n" +
                                "300\t\t20.00%\n" +
                                "400\t\t50.00%\n" +
                                "500\t\t15.00%\n" +
                                "750\t\t5.00%\n" +
                                "1500\t\t3.00%\n" +
                                "2000\t\t2.00%\n" +
                                "2500\t\t1.00%\n"),
                        getHP("0\t\t7.21%\n" +
                                "520.0\t\t92.79%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 100,
                        getHP("0\t\t1.00%\n" +
                                "150\t\t32.00%\n" +
                                "200\t\t35.00%\n" +
                                "250\t\t25.00%\n" +
                                "500\t\t3.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.00%\n" +
                                "1500\t\t1.00%\n"),
                        getHP("0\t\t21.50%\n" +
                                "300.0\t\t78.50%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        50, 30,
                        getHP("0\t\t2.00%\n" +
                                "50\t\t25.00%\n" +
                                "60\t\t40.00%\n" +
                                "80\t\t17.00%\n" +
                                "100\t\t10.00%\n" +
                                "150\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "300\t\t1.00%\n"),
                        getHP("0\t\t53.16%\n" +
                                "155.0\t\t46.84%\n"))});
        enemyDataMap.put(EnemyType.JAGUAR.getId(), new EnemyData(new int[]{700, 750, 800}, wData));

        /////////////////////////////////////////// JUMPER
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 7.0), 45, 40,
                                getHP("0\t\t25.00%\n" +
                                        "20\t\t20.00%\n" +
                                        "50\t\t30.00%\n" +
                                        "70\t\t15.00%\n" +
                                        "100\t\t7.00%\n" +
                                        "120\t\t2.00%\n" +
                                        "150\t\t1.00%\n"),
                                getHP("0\t\t59.60%\n" +
                                        "100.0\t\t40.40%\n")),
                        new WeaponData(Collections.singletonMap(2, 7.0), 40, 50,
                                getHP("0\t\t22.00%\n" +
                                        "20\t\t10.00%\n" +
                                        "50\t\t25.00%\n" +
                                        "70\t\t20.00%\n" +
                                        "80\t\t15.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "150\t\t2.00%\n" +
                                        "200\t\t1.00%\n"),
                                getHP("0\t\t54.09%\n" +
                                        "110.0\t\t45.91%\n")),
                        new WeaponData(Collections.singletonMap(2, 7.0), 35, 60,
                                getHP("0\t\t21.00%\n" +
                                        "20\t\t15.00%\n" +
                                        "60\t\t35.00%\n" +
                                        "80\t\t15.00%\n" +
                                        "100\t\t8.00%\n" +
                                        "150\t\t3.00%\n" +
                                        "200\t\t2.00%\n" +
                                        "220\t\t1.00%\n"),
                                getHP("0\t\t54.42%\n" +
                                        "120.0\t\t45.58%\n"))
                });
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"), 40, 40,
                        getHP("0\t\t2.00%\n" +
                                "50\t\t25.00%\n" +
                                "70\t\t40.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t6.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t4.00%\n" +
                                "250\t\t3.00%\n"),
                        getHP("0\t\t59.25%\n" +
                                "200.0\t\t40.75%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(), // shotgun
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t4.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n" +
                                "7\t\t1.00%\n" +
                                "10\t\t1.00%\n"),
                        45, 50,
                        getHP("0\t\t3.00%\n" +
                                "60\t\t25.00%\n" +
                                "80\t\t33.00%\n" +
                                "100\t\t22.00%\n" +
                                "150\t\t9.00%\n" +
                                "200\t\t5.00%\n" +
                                "250\t\t2.00%\n" +
                                "350\t\t1.00%\n"),
                        getHP("0\t\t56.64%\n" +
                                "220.0\t\t43.36%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n"),
                        40, 25,
                        getHP("0\t\t6.00%\n" +
                                "30\t\t30.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t20.00%\n" +
                                "70\t\t5.00%\n" +
                                "100\t\t4.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n"),
                        getHP("0\t\t69.93%\n" +
                                "150.0\t\t30.07%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        45, 30,
                        getHP("0\t\t6.00%\n" +
                                "30\t\t46.00%\n" +
                                "50\t\t30.00%\n" +
                                "70\t\t8.00%\n" +
                                "80\t\t4.00%\n" +
                                "100\t\t3.00%\n" +
                                "120\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t70.33%\n" +
                                "150.0\t\t29.67%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t2.50%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 70,
                        getHP("0\t\t2.00%\n" +
                                "100\t\t45.00%\n" +
                                "120\t\t32.00%\n" +
                                "150\t\t12.00%\n" +
                                "250\t\t5.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n" +
                                "1000\t\t1.00%\n"),
                        getHP("0\t\t29.30%\n" +
                                "200.0\t\t70.70%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        45, 60,
                        getHP("0\t\t4.00%\n" +
                                "80\t\t40.00%\n" +
                                "100\t\t30.00%\n" +
                                "150\t\t15.00%\n" +
                                "200\t\t6.00%\n" +
                                "250\t\t3.00%\n" +
                                "500\t\t1.00%\n" +
                                "750\t\t1.00%\n"),
                        getHP("0\t\t35.28%\n" +
                                "180.0\t\t64.72%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.00%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 30,
                        getHP("0\t\t2.00%\n" +
                                "60\t\t15.00%\n" +
                                "80\t\t45.00%\n" +
                                "100\t\t15.00%\n" +
                                "120\t\t12.00%\n" +
                                "150\t\t7.00%\n" +
                                "250\t\t3.00%\n" +
                                "500\t\t1.00%\n"),
                        getHP("0\t\t30.43%\n" +
                                "140.0\t\t69.57%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 30,
                        getHP("0\t\t3.00%\n" +
                                "50\t\t25.00%\n" +
                                "60\t\t35.00%\n" +
                                "70\t\t23.00%\n" +
                                "80\t\t8.00%\n" +
                                "100\t\t3.00%\n" +
                                "120\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t55.07%\n" +
                                "140.0\t\t44.93%\n"))});
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        40, 30,
                        getHP("0\t\t5.00%\n" +
                                "40\t\t35.00%\n" +
                                "50\t\t30.00%\n" +
                                "60\t\t15.00%\n" +
                                "80\t\t7.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t62.50%\n" +
                                "140.0\t\t37.50%\n"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 100,
                        getHP("0\t\t6.00%\n" +
                                "250\t\t20.00%\n" +
                                "300\t\t50.00%\n" +
                                "400\t\t15.00%\n" +
                                "500\t\t5.00%\n" +
                                "1000\t\t2.00%\n" +
                                "1500\t\t1.00%\n" +
                                "2000\t\t1.00%\n"),
                        getHP("0\t\t24.44%\n" +
                                "450.0\t\t75.56%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 80,
                        getHP("0\t\t3.00%\n" +
                                "150\t\t35.00%\n" +
                                "170\t\t35.00%\n" +
                                "200\t\t20.00%\n" +
                                "250\t\t3.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n" +
                                "1000\t\t1.00%\n"),
                        getHP("0\t\t25.20%\n" +
                                "250.0\t\t74.80%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        50, 30,
                        getHP("0\t\t2.00%\n" +
                                "40\t\t25.00%\n" +
                                "50\t\t40.00%\n" +
                                "60\t\t17.00%\n" +
                                "80\t\t10.00%\n" +
                                "100\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t59.50%\n" +
                                "140.0\t\t40.50%\n"))});
        enemyDataMap.put(EnemyType.JUMPER.getId(), new EnemyData(new int[]{450, 475, 500}, wData));


        ///////////////////////////////////////////RUNNER
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 6.0), 40, 35,
                                getHP("0\t\t42.00%\n" +
                                        "15\t\t10.00%\n" +
                                        "50\t\t30.00%\n" +
                                        "70\t\t15.00%\n" +
                                        "100\t\t3.00%\n"),
                                getHP("0\t\t64.71%\n" +
                                        "85.0\t\t35.29%\n")),
                        new WeaponData(Collections.singletonMap(2, 6.0), 35, 40,
                                getHP("0\t\t37.00%\n" +
                                        "20\t\t10.00%\n" +
                                        "50\t\t20.00%\n" +
                                        "70\t\t20.00%\n" +
                                        "80\t\t10.00%\n" +
                                        "100\t\t2.00%\n" +
                                        "120\t\t1.00%\n"),
                                getHP("0\t\t58.67%\n" +
                                        "90.0\t\t41.33%\n")),
                        new WeaponData(Collections.singletonMap(2, 6.0), 35, 50,
                                getHP("0\t\t32.00%\n" +
                                        "20\t\t15.00%\n" +
                                        "60\t\t30.00%\n" +
                                        "80\t\t15.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t2.00%\n" +
                                        "150\t\t1.00%\n"),
                                getHP("0\t\t58.10%\n" +
                                        "100.0\t\t41.90%\n"))
                });
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        40, 35,
                        getHP("0\t\t3.00%\n" +
                                "50\t\t28.00%\n" +
                                "70\t\t40.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t4.00%\n" +
                                "150\t\t3.00%\n" +
                                "200\t\t2.00%\n"),
                        getHP("0\t\t48.36%\n" +
                                "140.0\t\t51.64%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t4.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n" +
                                "7\t\t1.00%\n" +
                                "10\t\t1.00%\n"),
                        45, 40,
                        getHP("0\t\t4.00%\n" +
                                "60\t\t25.00%\n" +
                                "80\t\t33.00%\n" +
                                "100\t\t22.00%\n" +
                                "120\t\t8.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t42.00%\n" +
                                "150.0\t\t58.00%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n"),
                        40, 25,
                        getHP("0\t\t5.00%\n" +
                                "30\t\t30.00%\n" +
                                "40\t\t40.00%\n" +
                                "50\t\t12.00%\n" +
                                "60\t\t5.00%\n" +
                                "70\t\t3.00%\n" +
                                "100\t\t3.00%\n" +
                                "120\t\t2.00%\n"),
                        getHP("0\t\t68.08%\n" +
                                "130.0\t\t31.92%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        45, 30,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t46.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t10.00%\n" +
                                "70\t\t5.00%\n" +
                                "80\t\t3.00%\n" +
                                "100\t\t3.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t68.31%\n" +
                                "130.0\t\t31.69%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t2.50%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 50,
                        getHP("0\t\t2.00%\n" +
                                "100\t\t47.00%\n" +
                                "120\t\t32.00%\n" +
                                "150\t\t10.00%\n" +
                                "200\t\t5.00%\n" +
                                "250\t\t2.00%\n" +
                                "500\t\t1.00%\n" +
                                "700\t\t1.00%\n"),
                        getHP("0\t\t15.07%\n" +
                                "150.0\t\t84.93%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        45, 40,
                        getHP("0\t\t2.00%\n" +
                                "80\t\t33.00%\n" +
                                "100\t\t35.00%\n" +
                                "120\t\t15.00%\n" +
                                "150\t\t8.00%\n" +
                                "200\t\t4.00%\n" +
                                "250\t\t2.00%\n" +
                                "300\t\t1.00%\n"),
                        getHP("0\t\t20.44%\n" +
                                "135.0\t\t79.56%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.00%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 30,
                        getHP("0\t\t0.00%\n" +
                                "60\t\t23.00%\n" +
                                "80\t\t50.00%\n" +
                                "100\t\t12.00%\n" +
                                "120\t\t6.00%\n" +
                                "150\t\t4.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n"),
                        getHP("0\t\t28.00%\n" +
                                "125.0\t\t72.00%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 20,
                        getHP("0\t\t2.00%\n" +
                                "40\t\t25.00%\n" +
                                "50\t\t35.00%\n" +
                                "60\t\t23.00%\n" +
                                "70\t\t6.00%\n" +
                                "80\t\t4.00%\n" +
                                "100\t\t3.00%\n" +
                                "150\t\t2.00%\n"),
                        getHP("0\t\t52.43%\n" +
                                "115.0\t\t47.57%\n"))});
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        40, 30,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t50.00%\n" +
                                "50\t\t20.00%\n" +
                                "60\t\t10.00%\n" +
                                "70\t\t8.00%\n" +
                                "80\t\t5.00%\n" +
                                "100\t\t3.00%\n" +
                                "150\t\t2.00%\n"),
                        getHP("0\t\t62.72%\n" +
                                "125.0\t\t37.28%\n"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 80,
                        getHP("0\t\t1.00%\n" +
                                "200\t\t20.00%\n" +
                                "250\t\t50.00%\n" +
                                "300\t\t16.00%\n" +
                                "500\t\t7.00%\n" +
                                "750\t\t3.00%\n" +
                                "1000\t\t2.00%\n" +
                                "1500\t\t1.00%\n"),
                        getHP("0\t\t17.43%\n" +
                                "370.0\t\t82.57%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 50,
                        getHP("0\t\t1.00%\n" +
                                "100\t\t35.00%\n" +
                                "120\t\t40.00%\n" +
                                "150\t\t15.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t3.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n"),
                        getHP("0\t\t24.17%\n" +
                                "180.0\t\t75.83%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        50, 20,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t25.00%\n" +
                                "40\t\t40.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t10.00%\n" +
                                "100\t\t5.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n"),
                        getHP("0\t\t58.26%\n" +
                                "115.0\t\t41.74%\n"))});
        enemyDataMap.put(EnemyType.RUNNER.getId(), new EnemyData(new int[]{280, 300, 320}, wData));

        /////////////////////////////////////////// SHAMAN
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 5.0), 35, 30,
                                getHP("0\t\t42.00%\n" +
                                        "15\t\t10.00%\n" +
                                        "40\t\t30.00%\n" +
                                        "60\t\t15.00%\n" +
                                        "100\t\t3.00%\n"),
                                getHP("0\t\t57.50%\n" +
                                        "60.0\t\t42.50%\n")),
                        new WeaponData(Collections.singletonMap(2, 5.0), 30, 35,
                                getHP("0\t\t38.00%\n" +
                                        "20\t\t10.00%\n" +
                                        "40\t\t20.00%\n" +
                                        "60\t\t20.00%\n" +
                                        "80\t\t10.00%\n" +
                                        "100\t\t2.00%\n"),
                                getHP("0\t\t54.29%\n" +
                                        "70.0\t\t45.71%\n")),
                        new WeaponData(Collections.singletonMap(2, 5.0), 30, 40,
                                getHP("0\t\t34.00%\n" +
                                        "20\t\t15.00%\n" +
                                        "60\t\t30.00%\n" +
                                        "80\t\t15.00%\n" +
                                        "100\t\t5.00%\n" +
                                        "120\t\t1.00%\n"),
                                getHP("0\t\t51.00%\n" +
                                        "80.0\t\t49.00%\n"))
                });
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        40, 35,
                        getHP("0\t\t4.00%\n" +
                                "50\t\t30.00%\n" +
                                "70\t\t40.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n"),
                        getHP("0\t\t37.64%\n" +
                                "110.0\t\t62.36%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t4.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n" +
                                "7\t\t1.00%\n" +
                                "10\t\t1.00%\n"),
                        45, 40,
                        getHP("0\t\t10.00%\n" +
                                "60\t\t25.00%\n" +
                                "80\t\t33.00%\n" +
                                "100\t\t20.00%\n" +
                                "120\t\t8.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t34.58%\n" +
                                "120.0\t\t65.42%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n"),
                        40, 30,
                        getHP("0\t\t12.00%\n" +
                                "30\t\t30.00%\n" +
                                "40\t\t40.00%\n" +
                                "50\t\t10.00%\n" +
                                "60\t\t4.00%\n" +
                                "70\t\t2.00%\n" +
                                "100\t\t1.00%\n" +
                                "120\t\t1.00%\n"),
                        getHP("0\t\t64.00%\n" +
                                "100.0\t\t36.00%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        45, 30,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t50.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t10.00%\n" +
                                "70\t\t5.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t1.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t60.40%\n" +
                                "100.0\t\t39.60%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t2.50%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 60,
                        getHP("0\t\t3.00%\n" +
                                "100\t\t45.00%\n" +
                                "120\t\t34.00%\n" +
                                "150\t\t10.00%\n" +
                                "200\t\t4.00%\n" +
                                "250\t\t2.00%\n" +
                                "300\t\t1.00%\n" +
                                "400\t\t1.00%\n"),
                        getHP("0\t\t13.71%\n" +
                                "140.0\t\t86.29%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        45, 50,
                        getHP("0\t\t4.00%\n" +
                                "80\t\t38.00%\n" +
                                "100\t\t35.00%\n" +
                                "120\t\t12.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "300\t\t1.00%\n"),
                        getHP("0\t\t15.58%\n" +
                                "120.0\t\t84.42%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.00%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 40,
                        getHP("0\t\t1.00%\n" +
                                "60\t\t25.00%\n" +
                                "80\t\t50.00%\n" +
                                "100\t\t12.00%\n" +
                                "120\t\t5.00%\n" +
                                "150\t\t4.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t22.27%\n" +
                                "110.0\t\t77.73%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 30,
                        getHP("0\t\t7.00%\n" +
                                "40\t\t25.00%\n" +
                                "50\t\t35.00%\n" +
                                "60\t\t22.00%\n" +
                                "70\t\t5.00%\n" +
                                "80\t\t3.00%\n" +
                                "100\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t49.90%\n" +
                                "100.0\t\t50.10%\n"))});
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        35, 30,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t55.00%\n" +
                                "50\t\t21.00%\n" +
                                "60\t\t10.00%\n" +
                                "70\t\t6.00%\n" +
                                "80\t\t3.00%\n" +
                                "100\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t56.90%\n" +
                                "100.0\t\t43.10%\n"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 70,
                        getHP("0\t\t1.00%\n" +
                                "200\t\t20.00%\n" +
                                "250\t\t50.00%\n" +
                                "300\t\t16.00%\n" +
                                "400\t\t7.00%\n" +
                                "500\t\t3.00%\n" +
                                "1000\t\t2.00%\n" +
                                "1200\t\t1.00%\n"),
                        getHP("0\t\t17.71%\n" +
                                "350.0\t\t82.29%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 40,
                        getHP("0\t\t1.00%\n" +
                                "100\t\t35.00%\n" +
                                "120\t\t40.00%\n" +
                                "150\t\t15.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t3.00%\n" +
                                "300\t\t2.00%\n" +
                                "500\t\t1.00%\n"),
                        getHP("0\t\t18.75%\n" +
                                "160.0\t\t81.25%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        45, 30,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t28.00%\n" +
                                "40\t\t40.00%\n" +
                                "50\t\t12.00%\n" +
                                "70\t\t10.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t53.70%\n" +
                                "100.0\t\t46.30%\n"))});
        enemyDataMap.put(EnemyType.SHAMAN.getId(), new EnemyData(new int[]{180, 200, 220}, wData));

        /////////////////////////////////////////// SNAKE
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 4.0), 20, 20,
                                getHP("0\t\t20.00%\n" +
                                        "15\t\t40.00%\n" +
                                        "20\t\t25.00%\n" +
                                        "30\t\t15.00%\n"),
                                getHP("0\t\t61.25%\n" +
                                        "40.0\t\t38.75%\n")),
                        new WeaponData(Collections.singletonMap(2, 4.0), 18, 25,
                                getHP("0\t\t15.00%\n" +
                                        "20\t\t40.00%\n" +
                                        "30\t\t25.00%\n" +
                                        "35\t\t20.00%\n"),
                                getHP("0\t\t55.00%\n" +
                                        "50.0\t\t45.00%\n")),
                        new WeaponData(Collections.singletonMap(2, 4.0), 15, 30,
                                getHP("0\t\t10.00%\n" +
                                        "20\t\t40.00%\n" +
                                        "30\t\t30.00%\n" +
                                        "40\t\t20.00%\n"),
                                getHP("0\t\t58.33%\n" +
                                        "60.0\t\t41.67%\n"))
                });
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        35, 25,
                        getHP("0\t\t2.00%\n" +
                                "40\t\t48.00%\n" +
                                "50\t\t41.00%\n" +
                                "75\t\t5.00%\n" +
                                "100\t\t2.00%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t35.80%\n" +
                                "75.0\t\t64.20%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t4.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n" +
                                "7\t\t1.00%\n" +
                                "10\t\t1.00%\n"),
                        45, 25,
                        getHP("0\t\t2.00%\n" +
                                "40\t\t22.00%\n" +
                                "50\t\t35.00%\n" +
                                "60\t\t22.00%\n" +
                                "80\t\t12.00%\n" +
                                "100\t\t4.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n"),
                        getHP("0\t\t35.44%\n" +
                                "90.0\t\t64.56%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n"),
                        30, 25,
                        getHP("0\t\t2.00%\n" +
                                "20\t\t25.00%\n" +
                                "30\t\t53.00%\n" +
                                "40\t\t10.00%\n" +
                                "50\t\t4.00%\n" +
                                "70\t\t3.00%\n" +
                                "100\t\t2.00%\n" +
                                "120\t\t1.00%\n"),
                        getHP("0\t\t57.07%\n" +
                                "75.0\t\t42.93%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        40, 25,
                        getHP("0\t\t3.00%\n" +
                                "30\t\t55.00%\n" +
                                "40\t\t32.00%\n" +
                                "50\t\t5.00%\n" +
                                "70\t\t2.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t1.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t59.44%\n" +
                                "90.0\t\t40.56%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t2.50%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 50,
                        getHP("0\t\t1.00%\n" +
                                "80\t\t27.00%\n" +
                                "100\t\t40.00%\n" +
                                "120\t\t20.00%\n" +
                                "150\t\t6.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "300\t\t1.00%\n"),
                        getHP("0\t\t16.46%\n" +
                                "130.0\t\t83.54%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        40, 40,
                        getHP("0\t\t0.50%\n" +
                                "70\t\t20.00%\n" +
                                "80\t\t35.50%\n" +
                                "100\t\t25.00%\n" +
                                "120\t\t12.00%\n" +
                                "150\t\t4.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t18.00%\n" +
                                "115.0\t\t82.00%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.00%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 30,
                        getHP("0\t\t2.00%\n" +
                                "50\t\t25.00%\n" +
                                "75\t\t50.00%\n" +
                                "100\t\t12.00%\n" +
                                "120\t\t5.00%\n" +
                                "150\t\t3.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t12.22%\n" +
                                "90.0\t\t87.78%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        50, 25,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t20.00%\n" +
                                "40\t\t35.00%\n" +
                                "50\t\t27.00%\n" +
                                "70\t\t10.00%\n" +
                                "80\t\t4.00%\n" +
                                "100\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t37.07%\n" +
                                "75.0\t\t62.93%\n"))});
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        35, 25,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t53.00%\n" +
                                "40\t\t20.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t4.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t2.00%\n" +
                                "150\t\t2.00%\n"),
                        getHP("0\t\t45.60%\n" +
                                "75.0\t\t54.40%\n"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(getMapFromString("2\t\t2.50%\n" +
                        "3\t\t1.50%\n" +
                        "5\t\t1.00%\n" +
                        "7\t\t0.50%\n" +
                        "10\t\t0.25%\n"),
                        50, 50,
                        getHP("0\t\t1.00%\n" +
                                "200\t\t20.00%\n" +
                                "250\t\t50.00%\n" +
                                "300\t\t18.00%\n" +
                                "400\t\t7.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n" +
                                "1000\t\t1.00%\n"),
                        getHP("0\t\t8.50%\n" +
                                "300.0\t\t91.50%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        40, 35,
                        getHP("0\t\t4.50%\n" +
                                "100\t\t35.00%\n" +
                                "120\t\t44.00%\n" +
                                "150\t\t10.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "300\t\t1.00%\n" +
                                "500\t\t0.50%\n"),
                        getHP("0\t\t20.47%\n" +
                                "150.0\t\t79.53%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        35, 25,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t30.00%\n" +
                                "40\t\t40.00%\n" +
                                "50\t\t14.00%\n" +
                                "70\t\t8.00%\n" +
                                "100\t\t4.00%\n" +
                                "120\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t39.33%\n" +
                                "75.0\t\t60.67%\n"))});
        enemyDataMap.put(EnemyType.SNAKE.getId(), new EnemyData(new int[]{80, 100, 120}, wData));


        /////////////////////////////////////////// WASP
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 3.0), 18, 0,
                                getHP("0\t\t5.00%\n" +
                                        "20\t\t55.00%\n" +
                                        "30\t\t40.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 3.5), 15, 0,
                                getHP("0\t\t5.00%\n" +
                                        "20\t\t45.00%\n" +
                                        "30\t\t40.00%\n" +
                                        "40\t\t10.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 4.0), 12, 0,
                                getHP("0\t\t5.00%\n" +
                                        "20\t\t50.00%\n" +
                                        "35\t\t40.00%\n" +
                                        "45\t\t5.00%\n"),
                                null)
                });
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        25, 20,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t50.00%\n" +
                                "50\t\t40.00%\n" +
                                "70\t\t5.00%\n" +
                                "100\t\t3.00%\n" +
                                "120\t\t1.00%\n"),
                        getHP("0\t\t14.60%\n" +
                                "50.0\t\t85.40%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t4.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n" +
                                "7\t\t1.00%\n" +
                                "10\t\t1.00%\n"),
                        30, 20,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t26.00%\n" +
                                "50\t\t35.00%\n" +
                                "60\t\t24.00%\n" +
                                "80\t\t8.00%\n" +
                                "100\t\t4.00%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t12.00%\n" +
                                "60.0\t\t88.00%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n"),
                        20, 20,
                        getHP("0\t\t1.50%\n" +
                                "15\t\t25.00%\n" +
                                "30\t\t60.00%\n" +
                                "40\t\t10.00%\n" +
                                "50\t\t2.00%\n" +
                                "70\t\t1.00%\n" +
                                "100\t\t0.50%\n"),
                        getHP("0\t\t44.10%\n" +
                                "50.0\t\t55.90%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        30, 20,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t57.00%\n" +
                                "40\t\t32.00%\n" +
                                "50\t\t5.00%\n" +
                                "70\t\t3.00%\n" +
                                "80\t\t1.00%\n" +
                                "100\t\t1.00%\n"),
                        getHP("0\t\t48.14%\n" +
                                "70.0\t\t51.86%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t2.50%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 20,
                        getHP("0\t\t0.00%\n" +
                                "80\t\t30.00%\n" +
                                "100\t\t42.00%\n" +
                                "120\t\t18.00%\n" +
                                "150\t\t6.00%\n" +
                                "200\t\t3.00%\n" +
                                "250\t\t1.00%\n"),
                        getHP("0\t\t12.42%\n" +
                                "120.0\t\t87.58%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        40, 20,
                        getHP("0\t\t1.00%\n" +
                                "60\t\t21.00%\n" +
                                "80\t\t35.50%\n" +
                                "100\t\t25.00%\n" +
                                "120\t\t12.00%\n" +
                                "150\t\t4.00%\n" +
                                "200\t\t1.00%\n" +
                                "250\t\t0.50%\n"),
                        getHP("0\t\t18.50%\n" +
                                "110.0\t\t81.50%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.00%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 20,
                        getHP("0\t\t1.00%\n" +
                                "50\t\t26.00%\n" +
                                "70\t\t47.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n"),
                        getHP("0\t\t10.63%\n" +
                                "80.0\t\t89.38%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
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
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        25, 20,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t57.00%\n" +
                                "40\t\t20.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t3.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t1.00%\n" +
                                "150\t\t1.00%\n"),
                        getHP("0\t\t22.40%\n" +
                                "50.0\t\t77.60%\n"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 20,
                        getHP("0\t\t3.00%\n" +
                                "180\t\t20.00%\n" +
                                "200\t\t50.00%\n" +
                                "250\t\t18.00%\n" +
                                "300\t\t5.00%\n" +
                                "400\t\t2.00%\n" +
                                "500\t\t1.00%\n" +
                                "750\t\t1.00%\n"),
                        getHP("0\t\t1.59%\n" +
                                "220.0\t\t98.41%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        40, 20,
                        getHP("0\t\t1.00%\n" +
                                "80\t\t30.00%\n" +
                                "100\t\t42.00%\n" +
                                "120\t\t16.00%\n" +
                                "150\t\t5.00%\n" +
                                "200\t\t3.00%\n" +
                                "300\t\t2.00%\n" +
                                "500\t\t1.00%\n"),
                        getHP("0\t\t4.61%\n" +
                                "115.0\t\t95.39%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        30, 20,
                        getHP("0\t\t0.50%\n" +
                                "30\t\t32.00%\n" +
                                "40\t\t40.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t7.00%\n" +
                                "100\t\t4.00%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        getHP("0\t\t41.40%\n" +
                                "75.0\t\t58.60%\n"))});
        enemyDataMap.put(EnemyType.WASP.getId(), new EnemyData(new int[]{30, 40, 50}, wData));

        /////////////////////////////////////////// ANT
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(Collections.singletonMap(2, 4.0), 20, 0,
                                getHP("0\t\t20.00%\n" +
                                        "10\t\t40.00%\n" +
                                        "20\t\t40.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 4.0), 15, 0,
                                getHP("0\t\t20.00%\n" +
                                        "15\t\t40.00%\n" +
                                        "25\t\t40.00%\n"),
                                null),
                        new WeaponData(Collections.singletonMap(2, 4.0), 12, 0,
                                getHP("0\t\t10.00%\n" +
                                        "20\t\t50.00%\n" +
                                        "30\t\t40.00%\n"),
                                null),
                });
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        30, 0,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t51.00%\n" +
                                "50\t\t40.00%\n" +
                                "70\t\t5.00%\n" +
                                "100\t\t2.00%\n"),
                        null)});
        wData.put(SpecialWeaponType.HolyArrows.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t4.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n" +
                                "7\t\t1.00%\n" +
                                "10\t\t1.00%\n"),
                        30, 0,
                        getHP("0\t\t0.50%\n" +
                                "30\t\t30.00%\n" +
                                "50\t\t32.50%\n" +
                                "60\t\t24.00%\n" +
                                "80\t\t8.00%\n" +
                                "100\t\t4.00%\n" +
                                "120\t\t1.00%\n"),
                        null)});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n"),
                        15, 0,
                        getHP("0\t\t1.00%\n" +
                                "15\t\t30.00%\n" +
                                "30\t\t50.00%\n" +
                                "40\t\t15.00%\n" +
                                "50\t\t3.00%\n" +
                                "70\t\t1.00%\n"),
                        null)});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        30, 0,
                        getHP("0\t\t4.00%\n" +
                                "30\t\t57.00%\n" +
                                "40\t\t32.00%\n" +
                                "50\t\t4.00%\n" +
                                "70\t\t2.00%\n" +
                                "80\t\t1.00%\n"),
                        null)});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t2.50%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 0,
                        getHP("0\t\t0.00%\n" +
                                "80\t\t32.00%\n" +
                                "100\t\t43.00%\n" +
                                "120\t\t18.00%\n" +
                                "150\t\t4.00%\n" +
                                "200\t\t2.00%\n" +
                                "250\t\t1.00%\n"),
                        null)});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t4.00%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        40, 0,
                        getHP("0\t\t0.50%\n" +
                                "60\t\t25.00%\n" +
                                "80\t\t35.50%\n" +
                                "100\t\t25.00%\n" +
                                "120\t\t10.00%\n" +
                                "150\t\t3.00%\n" +
                                "200\t\t1.00%\n"),
                        null)});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.00%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t1.00%\n"),
                        50, 0,
                        getHP("0\t\t4.00%\n" +
                                "50\t\t25.00%\n" +
                                "70\t\t47.00%\n" +
                                "80\t\t17.00%\n" +
                                "100\t\t4.00%\n" +
                                "120\t\t2.00%\n" +
                                "150\t\t1.00%\n"),
                        null)});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
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
        wData.put(SpecialWeaponType.Cryogun.getId(),
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
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        50, 0,
                        getHP("0\t\t0.00%\n" +
                                "180\t\t21.00%\n" +
                                "200\t\t55.00%\n" +
                                "250\t\t17.00%\n" +
                                "300\t\t4.00%\n" +
                                "400\t\t2.00%\n" +
                                "500\t\t1.00%\n"),
                        null)});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        40, 0,
                        getHP("0\t\t1.00%\n" +
                                "80\t\t32.00%\n" +
                                "100\t\t40.00%\n" +
                                "120\t\t15.00%\n" +
                                "150\t\t6.00%\n" +
                                "200\t\t3.00%\n" +
                                "300\t\t2.00%\n" +
                                "500\t\t1.00%\n"),
                        null)});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        20, 0,
                        getHP("0\t\t0.50%\n" +
                                "30\t\t31.00%\n" +
                                "40\t\t42.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t7.00%\n" +
                                "100\t\t3.00%\n" +
                                "120\t\t1.00%\n" +
                                "150\t\t0.50%\n"),
                        null)});
        enemyDataMap.put(EnemyType.ANT.getId(), new EnemyData(new int[]{20, 25, 30}, wData));

        /////////////////////////////////////////// WEAPON CARRIER
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=4.0"), 0, 70,
                        getHP("0\t\t78.00%\n" +
                                "50\t\t1.00%\n" +
                                "60\t\t1.00%\n" +
                                "70\t\t20.00%\n"),
                        getHP("0\t\t98.79%\n" +
                                "1250.0\t\t1.21%\n"))});
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=6.0"), 0, 120,
                        getHP("0\t\t15.00%\n" +
                                "40\t\t25.00%\n" +
                                "50\t\t25.00%\n" +
                                "70\t\t20.00%\n" +
                                "80\t\t8.00%\n" +
                                "100\t\t7.00%\n"),
                        getHP("0\t\t96.16%\n" +
                                "1300.0\t\t3.84%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=9.0"), 0, 130,
                        getHP("0\t\t8.00%\n" +
                                "50\t\t30.00%\n" +
                                "70\t\t25.00%\n" +
                                "80\t\t18.00%\n" +
                                "100\t\t12.00%\n" +
                                "120\t\t7.00%\n"),
                        getHP("0\t\t94.82%\n" +
                                "1300.0\t\t5.18%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=8.0"),  0, 90,
                        getHP("0\t\t9.00%\n" +
                                "20\t\t40.00%\n" +
                                "30\t\t25.00%\n" +
                                "40\t\t12.00%\n" +
                                "50\t\t7.00%\n" +
                                "60\t\t5.00%\n" +
                                "80\t\t2.00%\n"),
                        getHP("0\t\t97.82%\n" +
                                "1300.0\t\t2.18%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=9.0"),  0, 100,
                        getHP("0\t\t3.00%\n" +
                                "20\t\t25.00%\n" +
                                "30\t\t28.00%\n" +
                                "40\t\t20.00%\n" +
                                "50\t\t17.00%\n" +
                                "70\t\t5.00%\n" +
                                "100\t\t2.00%\n"),
                        getHP("0\t\t97.28%\n" +
                                "1300.0\t\t2.72%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=12.0"), 0, 200,
                        getHP("0\t\t4.00%\n" +
                                "60\t\t12.00%\n" +
                                "80\t\t20.00%\n" +
                                "100\t\t22.00%\n" +
                                "120\t\t17.00%\n" +
                                "150\t\t15.00%\n" +
                                "200\t\t10.00%\n"),
                        getHP("0\t\t92.11%\n" +
                                "1370.0\t\t7.89%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=9.0"),   0, 150,
                        getHP("0\t\t2.00%\n" +
                                "50\t\t12.00%\n" +
                                "70\t\t25.00%\n" +
                                "100\t\t30.00%\n" +
                                "120\t\t21.00%\n" +
                                "150\t\t10.00%\n"),
                        getHP("0\t\t93.06%\n" +
                                "1350.0\t\t6.94%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=8.0"),  0, 150,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t10.00%\n" +
                                "40\t\t18.00%\n" +
                                "50\t\t20.00%\n" +
                                "70\t\t20.00%\n" +
                                "100\t\t18.00%\n" +
                                "150\t\t12.00%\n"),
                        getHP("0\t\t94.80%\n" +
                                "1350.0\t\t5.20%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=10.0"), 0, 120,
                        getHP("0\t\t6.00%\n" +
                                "30\t\t25.00%\n" +
                                "40\t\t18.00%\n" +
                                "50\t\t13.00%\n" +
                                "60\t\t11.00%\n" +
                                "80\t\t10.00%\n" +
                                "100\t\t9.00%\n" +
                                "120\t\t8.00%\n"),
                        getHP("0\t\t95.82%\n" +
                                "1300.0\t\t4.18%\n"))});
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=10.0"), 0, 120,
                        getHP("0\t\t4.00%\n" +
                                "30\t\t35.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t17.00%\n" +
                                "60\t\t10.00%\n" +
                                "80\t\t2.00%\n" +
                                "100\t\t1.00%\n" +
                                "120\t\t1.00%\n"),
                        getHP("0\t\t96.86%\n" +
                                "1300.0\t\t3.14%\n"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=10.0"), 0, 300,
                        getHP("0\t\t0.00%\n" +
                                "150\t\t22.00%\n" +
                                "200\t\t25.00%\n" +
                                "250\t\t23.00%\n" +
                                "300\t\t30.00%\n"),
                        getHP("0\t\t84.63%\n" +
                                "1500.0\t\t15.37%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=8.0"), 0, 150,
                        getHP("0\t\t3.00%\n" +
                                "100\t\t40.00%\n" +
                                "125\t\t32.00%\n" +
                                "150\t\t25.00%\n"),
                        getHP("0\t\t91.30%\n" +
                                "1350.0\t\t8.70%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=8.0"), 0, 100,
                        getHP("0\t\t7.00%\n" +
                                "30\t\t37.00%\n" +
                                "40\t\t25.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t12.00%\n" +
                                "100\t\t4.00%\n"),
                        getHP("0\t\t96.85%\n" +
                                "1300.0\t\t3.15%\n"))});
        enemyDataMap.put(EnemyType.WEAPON_CARRIER.getId(), new EnemyData(
                new int[]{400, 450, 500}, wData)
        );

        /////////////////////////////////////////// EXPLODER
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=5.0"), 0, 50,
                        getHP("0\t\t15.00%\n" +
                                "10\t\t20.00%\n" +
                                "20\t\t30.00%\n" +
                                "30\t\t25.00%\n" +
                                "50\t\t10.00%\n"),
                        getHP("0\t\t94.14%\n" +
                                "350.0\t\t5.86%\n"))});
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=6.0"), 0, 100,
                        getHP("0\t\t2.00%\n" +
                                "40\t\t45.00%\n" +
                                "50\t\t27.00%\n" +
                                "70\t\t16.00%\n" +
                                "80\t\t7.00%\n" +
                                "100\t\t3.00%\n"),
                        getHP("0\t\t87.18%\n" +
                                "400.0\t\t12.83%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=7.0"), 0, 120,
                        getHP("0\t\t1.00%\n" +
                                "40\t\t27.00%\n" +
                                "50\t\t20.00%\n" +
                                "70\t\t20.00%\n" +
                                "100\t\t17.00%\n" +
                                "120\t\t15.00%\n"),
                        getHP("0\t\t82.55%\n" +
                                "400.0\t\t17.45%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=5.0"), 0, 50,
                        getHP("0\t\t0.00%\n" +
                                "15\t\t10.00%\n" +
                                "20\t\t15.00%\n" +
                                "25\t\t25.00%\n" +
                                "30\t\t25.00%\n" +
                                "40\t\t15.00%\n" +
                                "50\t\t10.00%\n"),
                        getHP("0\t\t91.64%\n" +
                                "350.0\t\t8.36%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=10.0"), 0, 100,
                        getHP("0\t\t1.00%\n" +
                                "20\t\t35.00%\n" +
                                "30\t\t23.00%\n" +
                                "40\t\t20.00%\n" +
                                "50\t\t10.00%\n" +
                                "60\t\t6.00%\n" +
                                "80\t\t3.00%\n" +
                                "100\t\t2.00%\n"),
                        getHP("0\t\t91.28%\n" +
                                "400.0\t\t8.73%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=20.0"), 0, 150,
                        getHP("0\t\t0.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t18.00%\n" +
                                "100\t\t25.00%\n" +
                                "120\t\t22.00%\n" +
                                "150\t\t20.00%\n"),
                        getHP("0\t\t77.44%\n" +
                                "450.0\t\t22.56%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=15.0"), 0, 120,
                        getHP("0\t\t0.00%\n" +
                                "50\t\t18.00%\n" +
                                "70\t\t23.00%\n" +
                                "100\t\t37.00%\n" +
                                "120\t\t22.00%\n"),
                        getHP("0\t\t77.88%\n" +
                                "400.0\t\t22.13%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=12.0"), 0, 100,
                        getHP("0\t\t0.00%\n" +
                                "30\t\t12.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t35.00%\n" +
                                "80\t\t28.00%\n" +
                                "100\t\t10.00%\n"),
                        getHP("0\t\t83.00%\n" +
                                "400.0\t\t17.00%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=10.0"), 0, 80,
                        getHP("0\t\t0.00%\n" +
                                "30\t\t15.00%\n" +
                                "40\t\t22.00%\n" +
                                "50\t\t25.00%\n" +
                                "70\t\t23.00%\n" +
                                "80\t\t15.00%\n"),
                        getHP("0\t\t85.43%\n" +
                                "370.0\t\t14.57%\n"))});
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=10.0"), 0, 70,
                        getHP("0\t\t3.00%\n" +
                                "30\t\t40.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t16.00%\n" +
                                "60\t\t8.00%\n" +
                                "70\t\t3.00%\n"),
                        getHP("0\t\t89.49%\n" +
                                "370.0\t\t10.51%\n"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=25.0"), 0, 220,
                        getHP("0\t\t0.00%\n" +
                                "100\t\t5.00%\n" +
                                "150\t\t5.00%\n" +
                                "170\t\t5.00%\n" +
                                "200\t\t35.00%\n" +
                                "220\t\t50.00%\n"),
                        getHP("0\t\t59.80%\n" +
                                "500.0\t\t40.20%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=20.0"), 0, 150,
                        getHP("0\t\t0.00%\n" +
                                "70\t\t13.00%\n" +
                                "80\t\t20.00%\n" +
                                "100\t\t25.00%\n" +
                                "120\t\t22.00%\n" +
                                "150\t\t20.00%\n"),
                        getHP("0\t\t76.33%\n" +
                                "450.0\t\t23.67%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=12.0"), 0, 70,
                        getHP("0\t\t0.00%\n" +
                                "30\t\t40.00%\n" +
                                "40\t\t30.00%\n" +
                                "50\t\t20.00%\n" +
                                "70\t\t10.00%\n"),
                        getHP("0\t\t88.29%\n" +
                                "350.0\t\t11.71%\n"))});
        enemyDataMap.put(EnemyType.EXPLODER.getId(), new EnemyData(
                new int[]{350, 450, 550}, wData)
        );

        /////////////////////////////////////////// MULTIPLIER
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=2.0"), 0, 40,
                        getHP("0\t\t68.00%\n" +
                                "25\t\t15.00%\n" +
                                "30\t\t10.00%\n" +
                                "40\t\t7.00%\n"),
                        getHP("0\t\t98.95%\n" +
                                "912.0\t\t1.05%\n"))});
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=3.0"), 0, 60,
                        getHP("0\t\t0.00%\n" +
                                "30\t\t10.00%\n" +
                                "40\t\t13.00%\n" +
                                "50\t\t40.00%\n" +
                                "60\t\t37.00%\n"),
                        getHP("0\t\t96.32%\n" +
                                "1368.0\t\t3.68%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=5.0"), 0, 80,
                        getHP("0\t\t2.00%\n" +
                                "30\t\t6.00%\n" +
                                "50\t\t15.00%\n" +
                                "70\t\t45.00%\n" +
                                "80\t\t32.00%\n"),
                        getHP("0\t\t96.36%\n" +
                                "1824.0\t\t3.64%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=7.0"), 0, 40,
                        getHP("0\t\t1.00%\n" +
                                "20\t\t36.00%\n" +
                                "30\t\t38.00%\n" +
                                "40\t\t25.00%\n"),
                        getHP("0\t\t96.86%\n" +
                                "912.0\t\t3.14%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=8.0"), 0, 50,
                        getHP("0\t\t3.00%\n" +
                                "20\t\t17.00%\n" +
                                "30\t\t25.00%\n" +
                                "40\t\t33.00%\n" +
                                "50\t\t22.00%\n"),
                        getHP("0\t\t96.92%\n" +
                                "1140.0\t\t3.08%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=10.0"), 0, 140,
                        getHP("0\t\t0.00%\n" +
                                "50\t\t7.00%\n" +
                                "70\t\t15.00%\n" +
                                "100\t\t15.00%\n" +
                                "120\t\t30.00%\n" +
                                "140\t\t33.00%\n"),
                        getHP("0\t\t96.52%\n" +
                                "3192.0\t\t3.48%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=8.0"),0, 120,
                        getHP("0\t\t1.00%\n" +
                                "70\t\t19.00%\n" +
                                "80\t\t22.00%\n" +
                                "100\t\t33.00%\n" +
                                "120\t\t25.00%\n"),
                        getHP("0\t\t96.57%\n" +
                                "2736.0\t\t3.43%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=10.0"), 0, 100,
                        getHP("0\t\t4.00%\n" +
                                "50\t\t15.00%\n" +
                                "60\t\t19.00%\n" +
                                "70\t\t25.00%\n" +
                                "80\t\t22.00%\n" +
                                "100\t\t15.00%\n"),
                        getHP("0\t\t96.97%\n" +
                                "2280.0\t\t3.03%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=6.0"), 0, 70,
                        getHP("0\t\t1.00%\n" +
                                "30\t\t2.00%\n" +
                                "40\t\t12.00%\n" +
                                "50\t\t12.00%\n" +
                                "60\t\t53.00%\n" +
                                "70\t\t20.00%\n"),
                        getHP("0\t\t96.42%\n" +
                                "1596.0\t\t3.58%\n"))});
        wData.put(SpecialWeaponType.Cryogun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=8.0"), 0, 60,
                        getHP("0\t\t2.00%\n" +
                                "20\t\t12.00%\n" +
                                "30\t\t18.00%\n" +
                                "40\t\t32.00%\n" +
                                "50\t\t23.00%\n" +
                                "60\t\t13.00%\n"),
                        getHP("0\t\t97.08%\n" +
                                "1368.0\t\t2.92%\n"))});
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=20.0"), 0, 300,
                        getHP("0\t\t0.00%\n" +
                                "150\t\t25.00%\n" +
                                "200\t\t33.00%\n" +
                                "250\t\t30.00%\n" +
                                "300\t\t12.00%\n"),
                        getHP("0\t\t96.86%\n" +
                                "6840.0\t\t3.14%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=15.0"), 0, 140,
                        getHP("0\t\t0.00%\n" +
                                "80\t\t17.00%\n" +
                                "100\t\t25.00%\n" +
                                "120\t\t33.00%\n" +
                                "140\t\t25.00%\n"),
                        getHP("0\t\t96.45%\n" +
                                "3192.0\t\t3.55%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2=10.0"), 0, 60,
                        getHP("0\t\t0.00%\n" +
                                "30\t\t25.00%\n" +
                                "40\t\t40.00%\n" +
                                "50\t\t20.00%\n" +
                                "60\t\t15.00%\n"),
                        getHP("0\t\t96.89%\n" +
                                "1368.0\t\t3.11%\n"))});
        enemyDataMap.put(EnemyType.MULTIPLIER.getId(), new EnemyData(
                new int[]{400, 450, 500}, wData)
        );


        /////////////////////////////////////////// Boss
        wData = new HashMap<>();
        wData.put(PISTOL_DEFAULT_WEAPON_ID,
                new WeaponData[]{
                        new WeaponData(getMapFromString("2=4.0"),
                                0, 500, 200,
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
                                getHP("0\t\t96.40%\n" +
                                        "500.0\t\t3.60%\n")
                        ),
                        new WeaponData(getMapFromString("2=5.0"),
                                0, 1250, 400,
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
                                getHP("0\t\t96.33%\n" +
                                        "750.0\t\t3.67%\n")
                        ),
                        new WeaponData(getMapFromString("2=6.0"),
                                0, 2500, 1000,
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
        wData.put(SpecialWeaponType.DoubleStrengthPowerUp.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t5.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n"),
                        0, 500, 200,
                        getHP("0\t\t2.00%\n" +
                                "70\t\t25.00%\n" +
                                "80\t\t40.00%\n" +
                                "100\t\t15.00%\n" +
                                "150\t\t6.00%\n" +
                                "200\t\t5.00%\n" +
                                "250\t\t4.00%\n" +
                                "500\t\t3.00%\n"),
                        getHP("0\t\t22.58%\n" +
                                "100\t\t40.00%\n" +
                                "150\t\t25.00%\n" +
                                "170\t\t8.00%\n" +
                                "200\t\t1.50%\n" +
                                "250\t\t1.00%\n" +
                                "500\t\t1.00%\n" +
                                "750\t\t0.92%\n"),
                        getHP("0\t\t89.15%\n" +
                                "1000.0\t\t10.85%\n"))});
        wData.put(SpecialWeaponType.HolyArrows.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t4.00%\n" +
                                "3\t\t3.00%\n" +
                                "5\t\t2.00%\n" +
                                "7\t\t1.00%\n" +
                                "10\t\t1.00%\n"),
                        0, 500, 200,
                        getHP("0\t\t6.00%\n" +
                                "80\t\t22.00%\n" +
                                "100\t\t30.00%\n" +
                                "150\t\t25.00%\n" +
                                "200\t\t10.00%\n" +
                                "250\t\t4.00%\n" +
                                "300\t\t2.00%\n" +
                                "500\t\t1.00%\n"),
                        getHP("0\t\t28.28%\n" +
                                "120\t\t32.00%\n" +
                                "150\t\t23.00%\n" +
                                "200\t\t8.00%\n" +
                                "250\t\t5.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n" +
                                "1000\t\t0.72%\n"),
                        getHP("0\t\t89.49%\n" +
                                "1200.0\t\t10.51%\n"))});
        wData.put(SpecialWeaponType.Bomb.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n"),
                        0, 500, 200,
                        getHP("0\t\t3.00%\n" +
                                "50\t\t30.00%\n" +
                                "60\t\t35.00%\n" +
                                "80\t\t20.00%\n" +
                                "100\t\t5.00%\n" +
                                "120\t\t4.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n"),
                        getHP("0\t\t18.12%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t35.00%\n" +
                                "100\t\t10.00%\n" +
                                "120\t\t3.00%\n" +
                                "150\t\t2.00%\n" +
                                "200\t\t1.00%\n" +
                                "250\t\t0.88%\n"),
                        getHP("0\t\t91.09%\n" +
                                "750.0\t\t8.91%\n"))});
        wData.put(SpecialWeaponType.Landmines.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.50%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.01%\n"),
                        0, 500, 200,
                        getHP("0\t\t3.00%\n" +
                                "40\t\t40.00%\n" +
                                "60\t\t30.00%\n" +
                                "80\t\t10.00%\n" +
                                "100\t\t8.00%\n" +
                                "120\t\t4.00%\n" +
                                "150\t\t3.00%\n" +
                                "200\t\t2.00%\n"),
                        getHP("0\t\t22.76%\n" +
                                "60\t\t35.00%\n" +
                                "80\t\t25.00%\n" +
                                "100\t\t8.00%\n" +
                                "120\t\t4.00%\n" +
                                "150\t\t3.00%\n" +
                                "200\t\t1.20%\n" +
                                "250\t\t1.04%\n"),
                        getHP("0\t\t92.97%\n" +
                                "900.0\t\t7.03%\n"))});
        wData.put(SpecialWeaponType.RocketLauncher.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t2.50%\n" +
                                "5\t\t0.75%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        0, 500, 200,
                        getHP("0\t\t4.00%\n" +
                                "100\t\t40.00%\n" +
                                "150\t\t32.00%\n" +
                                "250\t\t15.00%\n" +
                                "500\t\t5.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.00%\n" +
                                "1250\t\t1.00%\n"),
                        getHP("0\t\t45.05%\n" +
                                "150\t\t20.00%\n" +
                                "250\t\t20.00%\n" +
                                "500\t\t8.00%\n" +
                                "750\t\t3.00%\n" +
                                "1000\t\t2.00%\n" +
                                "1250\t\t1.50%\n" +
                                "1500\t\t0.45%\n"),
                        getHP("0\t\t87.47%\n" +
                                "1500.0\t\t12.53%\n"))});
        wData.put(SpecialWeaponType.MachineGun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.00%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        0, 500, 200,
                        getHP("0\t\t6.00%\n" +
                                "80\t\t35.00%\n" +
                                "100\t\t30.00%\n" +
                                "150\t\t15.00%\n" +
                                "250\t\t8.00%\n" +
                                "500\t\t3.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.00%\n"),
                        getHP("0\t\t43.50%\n" +
                                "100\t\t18.00%\n" +
                                "150\t\t18.00%\n" +
                                "200\t\t9.00%\n" +
                                "500\t\t7.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.50%\n" +
                                "1250\t\t1.00%\n"),
                        getHP("0\t\t89.19%\n" +
                                "1300.0\t\t10.81%\n"))});
        wData.put(SpecialWeaponType.Ricochet.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.00%\n" +
                                "3\t\t1.00%\n" +
                                "5\t\t1.00%\n"),
                        0, 500, 200,
                        getHP("0\t\t7.00%\n" +
                                "80\t\t15.00%\n" +
                                "100\t\t40.00%\n" +
                                "150\t\t17.00%\n" +
                                "200\t\t10.00%\n" +
                                "250\t\t8.00%\n" +
                                "500\t\t2.00%\n" +
                                "750\t\t1.00%\n"),
                        getHP("0\t\t41.95%\n" +
                                "100\t\t13.00%\n" +
                                "150\t\t15.00%\n" +
                                "200\t\t12.00%\n" +
                                "250\t\t9.00%\n" +
                                "500\t\t7.00%\n" +
                                "750\t\t1.00%\n" +
                                "1000\t\t1.05%\n"),
                        getHP("0\t\t88.75%\n" +
                                "1200.0\t\t11.25%\n"))});
        wData.put(SpecialWeaponType.Flamethrower.getId(),
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
        wData.put(SpecialWeaponType.Cryogun.getId(),
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
        wData.put(SpecialWeaponType.Plasma.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t2.50%\n" +
                                "3\t\t1.50%\n" +
                                "5\t\t1.00%\n" +
                                "7\t\t0.50%\n" +
                                "10\t\t0.25%\n"),
                        0, 500, 200,
                        getHP("0\t\t2.00%\n" +
                                "300\t\t20.00%\n" +
                                "400\t\t50.00%\n" +
                                "500\t\t15.00%\n" +
                                "750\t\t6.00%\n" +
                                "1500\t\t4.00%\n" +
                                "2500\t\t2.00%\n" +
                                "5000\t\t1.00%\n"),
                        getHP("0\t\t36.88%\n" +
                                "400\t\t15.00%\n" +
                                "500\t\t20.00%\n" +
                                "750\t\t15.00%\n" +
                                "1000\t\t6.00%\n" +
                                "2000\t\t4.00%\n" +
                                "3000\t\t2.00%\n" +
                                "6000\t\t1.13%\n"),
                        getHP("0\t\t82.00%\n" +
                                "3000.0\t\t18.00%\n"))});
        wData.put(SpecialWeaponType.Railgun.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t3.50%\n" +
                                "3\t\t2.00%\n" +
                                "5\t\t1.00%\n"),
                        0, 500, 200,
                        getHP("0\t\t8.00%\n" +
                                "150\t\t32.00%\n" +
                                "250\t\t35.00%\n" +
                                "500\t\t20.00%\n" +
                                "750\t\t2.00%\n" +
                                "1000\t\t1.00%\n" +
                                "1500\t\t1.00%\n" +
                                "2500\t\t1.00%\n"),
                        getHP("0\t\t31.87%\n" +
                                "200\t\t22.00%\n" +
                                "300\t\t25.00%\n" +
                                "500\t\t15.00%\n" +
                                "1000\t\t2.00%\n" +
                                "1500\t\t1.50%\n" +
                                "2000\t\t1.50%\n" +
                                "3000\t\t1.13%\n"),
                        getHP("0\t\t87.98%\n" +
                                "2500.0\t\t12.02%\n"))});
        wData.put(SpecialWeaponType.ArtilleryStrike.getId(),
                new WeaponData[]{new WeaponData(
                        getMapFromString("2\t\t1.00%\n" +
                                "3\t\t0.75%\n" +
                                "5\t\t0.50%\n" +
                                "7\t\t0.25%\n" +
                                "10\t\t0.10%\n"),
                        0, 500, 200,
                        getHP("0\t\t2.00%\n" +
                                "60\t\t25.00%\n" +
                                "80\t\t40.00%\n" +
                                "90\t\t17.00%\n" +
                                "100\t\t10.00%\n" +
                                "150\t\t3.00%\n" +
                                "250\t\t2.00%\n" +
                                "500\t\t1.00%\n"),
                        getHP("0\t\t35.27%\n" +
                                "80\t\t18.00%\n" +
                                "100\t\t18.00%\n" +
                                "120\t\t10.00%\n" +
                                "150\t\t10.00%\n" +
                                "200\t\t4.00%\n" +
                                "300\t\t3.00%\n" +
                                "600\t\t1.73%\n"),
                        getHP("0\t\t92.77%\n" +
                                "1200.0\t\t7.23%\n"))});
        enemyDataMap.put(EnemyType.Boss.getId(), new EnemyData(null, wData)
        );


    }

    private static void fillAdditionalWeaponData() {

        Map<Integer, Pair<Double, Double>> tmpMap;
        HashMap<Integer, List<Triple<Integer, Integer, Double>>> map;

        //------------------------------SKULL_BREAKER + pistol----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(110.00, 4041.7)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(150.00, 5189.6)); // default data for enemy
        weaponDropData.put(EnemyType.SKULL_BREAKER.getId(), tmpMap);

        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("0,0|0,0|0,0|0,0|0,0|0,0|15,1|10,1|10,1|15,1|15,1|10,1"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("100,1|70,1|30,1|30,1|40,1|50,1|20,3|15,3|12,3|20,3|20,3|12,3"));
        additionalWeaponKilledTable.put(EnemyType.SKULL_BREAKER.getId(), map);
        //------------------------------SKULL_BREAKER + pistol-----END----------------------------------------------//

        //------------------------------JAGUAR + pistol----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(100.00, 3750.00)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(120.00, 3397.2)); // default data for enemy
        weaponDropData.put(EnemyType.JAGUAR.getId(), tmpMap);
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("60,1|40,1|20,1|20,1|30,1|30,1|15,2|10,2|10,2|15,2|15,2|10,2"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("50,1|40,1|15,1|15,1|20,1|25,1|15,2|10,2|10,2|15,2|10,2|10,2"));

        additionalWeaponKilledTable.put(EnemyType.JAGUAR.getId(), map);
        //------------------------------JAGUAR + pistol-----END----------------------------------------------//

        //------------------------------JUMPER + pistol----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(60.00, 2755.6)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(100.00, 3228.1)); // default data for enemy
        weaponDropData.put(EnemyType.JUMPER.getId(), tmpMap);
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("40,1|35,1|15,1|15,1|20,1|20,1|10,2|8,2|8,2|10,2|10,2|8,2"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("50,1|40,1|15,1|15,1|20,1|25,1|15,2|10,2|10,1|15,2|10,2|10,1"));

        additionalWeaponKilledTable.put(EnemyType.JUMPER.getId(), map);
        //------------------------------JUMPER + pistol-----END----------------------------------------------//


        //------------------------------RUNNER + pistol----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(50., 1916.10)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(95.00, 2552.8)); // default data for enemy
        weaponDropData.put(EnemyType.RUNNER.getId(), tmpMap);

        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("30,1|25,1|10,1|10,1|12,1|15,1|8,2|5,2|5,2|8,2|8,2|5,2"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("30,1|25,1|12,1|10,1|15,1|15,1|10,2|8,2|8,2|10,2|10,2|8,2"));
        additionalWeaponKilledTable.put(EnemyType.RUNNER.getId(), map);
        //------------------------------RUNNER + pistol-----END----------------------------------------------//

        //------------------------------SHAMAN + pistol----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(30., 1302.1)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(70.00, 1983.3)); // default data for enemy
        weaponDropData.put(EnemyType.SHAMAN.getId(), tmpMap);
        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("25,1|20,1|10,2|8,2|10,1|10,1|5,2|0,0|0,0|5,2|5,2|0,0"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("30,1|25,1|12,1|10,1|15,1|15,1|8,2|5,3|5,3|8,2|8,2|5,3"));
        additionalWeaponKilledTable.put(EnemyType.SHAMAN.getId(), map);
        //------------------------------SHAMAN + pistol-----END----------------------------------------------//

        //------------------------------SNAKE + pistol----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(20., 1062.5)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(50.00, 1384.4)); // default data for enemy
        weaponDropData.put(EnemyType.SNAKE.getId(), tmpMap);

        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("20,1|15,1|0,0|0,0|10,1|10,1|0,0|0,0|0,0|0,0|0,0|0,0"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("25,3|20,2|10,2|8,2|10,3|12,3|5,3|5,1|5,1|5,3|5,3|5,1"));
        additionalWeaponKilledTable.put(EnemyType.SNAKE.getId(), map);
        //------------------------------SHAMAN + pistol-----END----------------------------------------------//


        //------------------------------WASP + pistol----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(0.0, 0.00)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(25.00, 1018.8)); // default data for enemy
        weaponDropData.put(EnemyType.WASP.getId(), tmpMap);

        map = new HashMap<>();
//        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable(""));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("20,1|15,1|7,2|5,2|10,1|10,1|0,0|0,0|0,0|0,0|0,0|0,0"));
        additionalWeaponKilledTable.put(EnemyType.WASP.getId(), map);
        //------------------------------WASP + pistol-----END----------------------------------------------//

//        //------------------------------ANT + pistol----------------------------------------------//
//        tmpMap = new HashMap<>();
//        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(0.0, 0.0)); // pistol
//        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(0.00, 0.0)); // default data for enemy
//        weaponDropData.put(EnemyType.ANT.getId(), tmpMap);

//        map = new HashMap<>();
//        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("0,0|0,0|0,0|0,0|0,0|0,0|15,1|10,1|10,1|15,1|15,1|10,1"));
//        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("0,0|0,0|0,0|0,0|0,0|0,0|15,1|10,1|10,1|15,1|15,1|10,1"));
//        additionalWeaponKilledTable.put(EnemyType.ANT.getId(), map);
        //------------------------------WASP + pistol-----END----------------------------------------------//


        //------------------------------WEAPON CARRIER + pistol----------------------------------------------//
        tmpMap = new HashMap<>();
        tmpMap.put(PISTOL_DEFAULT_WEAPON_ID, new Pair<>(1162.5, 1162.5)); // pistol
        tmpMap.put(ALL_DEFAULT_WEAPON_ID, new Pair<>(1162.5, 1162.5)); // default data for enemy
        weaponDropData.put(EnemyType.WEAPON_CARRIER.getId(), tmpMap);

        map = new HashMap<>();
        map.put(PISTOL_DEFAULT_WEAPON_ID, getKilledTable("15,1|12,1|7,1|7,1|10,1|12,1|5,1|3,1|3,1|5,1|5,1|3,1"));
        map.put(ALL_DEFAULT_WEAPON_ID, getKilledTable("15,1|12,1|7,1|7,1|10,1|12,1|5,1|3,1|3,1|5,1|5,1|3,1"));
        additionalWeaponKilledTable.put(EnemyType.WEAPON_CARRIER.getId(), map);

    }

    private static List<Triple<Integer, Integer, Double>> getKilledTable(String data) {
        String[] weaponData = data.split("\\|");
        List<Triple<Integer, Integer, Double>> tmpWeaponKilledTable = new ArrayList<>();
        ;
        for (int i = 0; i < weaponData.length; i++) {
            String[] wData = weaponData[i].split(",");
            tmpWeaponKilledTable.add(new Triple<>(idxWeaponsMapping[i], Integer.parseInt(wData[0]),
                    Double.parseDouble(wData[1])));
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

    public static Treasure getRandomTreasure() {
        return GameTools.getRandomNumberKeyFromMapWithNorm(treasures);
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

    public static double getPayHitPercent() {
        return PAY_HIT_PERCENT;
    }

    public static List<Triple<Integer, Integer, Double>> getBossParams() {
        return bossParams;
    }

    public static double getBosMainProb() {
        return BOS_MAIN_PROB;
    }

    public static double getProbTreasureForQuest() {
        return PROB_TREASURE_FOR_QUEST;
    }

    public static List<Triple<Integer, Integer, Double>> getWeaponKilledTable(int enemyTypeId, int weaponTypeId) {
        Map<Integer, List<Triple<Integer, Integer, Double>>> map = additionalWeaponKilledTable.get(enemyTypeId);
        List<Triple<Integer, Integer, Double>> res = map.get(weaponTypeId);
        if (weaponTypeId == PISTOL_DEFAULT_WEAPON_ID) {
            return res != null ? res : new LinkedList<>();
        } else
            return res != null ? res : map.get(ALL_DEFAULT_WEAPON_ID);
    }

    public static Map<Integer, Double> getTotalHPdamageForExplorer() {
        return totalHPdamageForExplorer;
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

    public static Map<Integer, Double> getRandomMultiplierForMultiplier() {
        return randomMultiplierForMultiplier;
    }

    public static Map<Integer, List<Integer>> getPayGemsForPayTable() {
        Map<Integer, List<Integer>> res = new HashMap<>();
        gemPayoutsByBoss.get(PISTOL_DEFAULT_WEAPON_ID).forEach((integer, pairs) ->
                res.put(integer, pairs.stream().map(Pair::getKey).collect(Collectors.toList())));
        return res;
    }

    public static Map<Integer, List<Pair<Integer, Double>>> getGemPayoutsByBoss(int weaponId) {
        return gemPayoutsByBoss.get(weaponId);
    }

    public static double getBossGemRTPForWeapon(int weaponId, int bossSkinId) {
        AtomicReference<Double> res = new AtomicReference<>((double) 0);
        Map<Integer, List<Pair<Integer, Double>>> weaponData = gemPayoutsByBoss.get(weaponId);
        weaponData.forEach((integer, pairs) -> {
            res.updateAndGet(v -> (double) (v + pairs.get(bossSkinId-1).getValue()));
        });
        return res.get();
    }


    public static double getHitProbabilityForBoss(int weaponId, int bossId) {
        EnemyData enemyData = getEnemyData(EnemyType.Boss.getId());
        double bossGemRTPForWeapon = getBossGemRTPForWeapon(weaponId, bossId );
        Double weaponRTP = getRtpForWeapon(weaponId);
        Double pistolRTP = bossPistolRtp.get(bossId);
        double rtpForWeapon = weaponId == PISTOL_DEFAULT_WEAPON_ID ? pistolRTP :  weaponRTP - bossGemRTPForWeapon;
        Double swAvgPayouts = enemyData.getSwAvgPayouts(weaponId, bossId - 1, PAY_HIT_PERCENT);
        return rtpForWeapon / swAvgPayouts;
    }


    public static double getAvgHpWinExploder() {
        return AVG_HP_WIN_EXPLODER;
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
}
