package com.betsoft.casino.mp.missionamazon.model;

import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.ShootResult;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.common.WeaponSource;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.common.testmodel.*;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyRange;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyType;
import com.betsoft.casino.mp.missionamazon.model.math.MathData;
import com.betsoft.casino.mp.missionamazon.model.math.config.GameConfig;
import com.betsoft.casino.mp.missionamazon.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.model.*;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.google.gson.Gson;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.common.testmodel.StubSocketService.getConnection;

public class TestMathModel {
    GameType gameType = GameType.MISSION_AMAZON;
    GameMap currentMap;
    GameMapStore gameMapStore = new GameMapStore();
    StubRoomPlayerInfo roomPlayerInfo;
    StubGameSocketClient client;
    Seat seat;
    long accountId;
    Money stake;
    EnemyGame enemyGame;
    StubTransportObjectsFactoryService factoryService;
    Map<Integer, AtomicInteger> weaponsDrop = new HashMap<>();
    Map<Integer, AtomicInteger> weaponsKillAward = new HashMap<>();
    PrintWriter pwh;


    public static void main(String[] args) throws CommonException, FileNotFoundException, UnsupportedEncodingException {
        TestMathModel testMathModel = new TestMathModel();


        boolean debug = false;
        boolean allowSpecialWeaponsShoots = true;
        boolean needSaveToFile = false;
        int idEnemiesForShooting = 5;
        int coinInCents = 20;
        int cntRealShotsRequired = 1000000;
        int betLevel = 1;
        boolean needUsePaidWeapons = true;
        int paidWeaponId = SpecialWeaponType.ArtilleryStrike.getId();

        for (String arg : args) {
            String[] param_ = arg.split("=");
            switch (param_[0]) {
                case "debug":
                    debug = param_[1].equals("true");
                    break;
                case "allowSpecialWeaponsShoots":
                    allowSpecialWeaponsShoots = param_[1].equals("true");
                    break;
                case "needUsePaidWeapons":
                    needUsePaidWeapons = param_[1].equals("true");
                    break;
                case "betLevel":
                    betLevel = Integer.parseInt(param_[1]);
                    break;
                case "idEnemiesForShooting": {
                    idEnemiesForShooting = Integer.parseInt(param_[1]);
                    break;
                }
                case "needSaveToFile":
                    needSaveToFile = param_[1].equals("true");
                    break;
                case "coinInCents":
                    coinInCents = Integer.parseInt(param_[1]);
                    break;
                case "cntRealShotsRequired":
                    cntRealShotsRequired = Integer.parseInt(param_[1]);
                    break;
                case "paidWeaponId":
                    paidWeaponId = Integer.parseInt(param_[1]);
                    break;

                default:
                    break;
            }
        }

        EnemyType enemyType = EnemyType.getById(idEnemiesForShooting);
        System.out.println("idEnemiesForShooting: " + idEnemiesForShooting + " enemyType: " + enemyType);

        testMathModel.doTest(needUsePaidWeapons, allowSpecialWeaponsShoots, cntRealShotsRequired,
                enemyType, coinInCents, needSaveToFile, paidWeaponId, betLevel);
    }

    public TestMathModel() {

    }

    void doTest(boolean needPaidShots, boolean allowSpecialWeaponsShoots, int totalNumberShots, EnemyType enemyType,
                int coinInCents, boolean needSaveToFile, int paidWeaponId, int betLevel)
            throws CommonException, FileNotFoundException, UnsupportedEncodingException {

        long start = System.currentTimeMillis();
        double tBet = 0;
        double baseWin = 0;
        double moneyWheel = 0;
        double killAward = 0;

        double weaponCompensateKillAward = 0;
        double weaponCompensateDrop = 0;

        double weaponBaseWinsKA = 0;
        double weaponKillAwardKA = 0;
        double weaponBaseWinsDrop = 0;
        double weaponKillAwardDrop = 0;

        double weaponBaseWinsPaid = 0;
        double weaponKillAwardPaid = 0;

        double paidBet = 0;

        double bomberWin = 0;
        Weapon weapon;
        boolean isPaidShot;
        StubShot stubShot;
        int cntMoneyWheel = 0;
        int cntKillAward = 0;
        int cntEnemyKilled = 0;
        boolean isFreeShot;
        boolean isSpecialWeaponShot;
        int lastPercent = 0;
        double allWin = 0;
        double allBet = 0;

        String weaponName = "Turret";
        if (needPaidShots)
            weaponName = paidWeaponId == -1 ? "RandPaidWeapon" : SpecialWeaponType.values()[paidWeaponId].name();

        String name_ = this.getClass().getPackage().getName();
        String gameName = name_.substring(name_.lastIndexOf(".") + 1);
        String fileStatsName = gameName + "_" + System.currentTimeMillis() + "_"
                + allowSpecialWeaponsShoots + "_"
                + enemyType.getName() + "_"
                + enemyType.getId() + "_"
                + coinInCents + "_"
                + totalNumberShots + "_"
                + needPaidShots + "_"
                + weaponName + "_"
                + betLevel + "_"
                + ".txt";
        if (needSaveToFile) {
            pwh = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileStatsName), StandardCharsets.UTF_8));
        }

        printLog("enemy: " + enemyType.getName());
        printLog("coinInCents: " + coinInCents);
        printLog("totalNumberShots: " + totalNumberShots);
        printLog("weaponName: " + weaponName);
        printLog("needPaidShots: " + needPaidShots);
        printLog("betLevel: " + betLevel);

        stake = Money.fromCents(coinInCents);

        gameMapStore.init();
        currentMap = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(504));
        accountId = 11;

        roomPlayerInfo = new StubRoomPlayerInfo(accountId, 271,
                1111,
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

        client = new StubGameSocketClient(accountId, 271L, "", getConnection(),
                new StubGsonMessageSerializer(new Gson()), gameType);
        seat = new Seat(roomPlayerInfo, client, 1);
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        enemyGame = new EnemyGame(new EmptyLogger(), null, new StubGameConfigProvider(config), null);
        factoryService = new StubTransportObjectsFactoryService();

        seat.setBetLevel(betLevel);

        ArrayList<SpecialWeaponType> weaponTypes = Arrays.stream(SpecialWeaponType.values())
                .filter(specialWeaponType -> specialWeaponType.getAvailableGameIds().contains((int) gameType.getGameId()))
                .collect(Collectors.toCollection(ArrayList::new));

        Enemy enemy = createNewEnemy(enemyType);

        for (int idx = 0; idx < totalNumberShots; idx++) {
            double percent = ((double) idx / totalNumberShots) * 100;
            int iPercent = (int) percent;
            if (percent == iPercent && iPercent % 3 == 0 && iPercent != lastPercent) {
                System.out.print(" " + percent + "%");
                lastPercent = iPercent;
            }

            Pair<Integer, Integer> randomWeaponId = getRandomWeapon(allowSpecialWeaponsShoots);
            isFreeShot = randomWeaponId.getKey() != -1;

            isPaidShot = false;
            isSpecialWeaponShot = isFreeShot;
            if (!isFreeShot && needPaidShots) {
                isPaidShot = true;
                isSpecialWeaponShot = true;

                SpecialWeaponType specialWeaponType;
                if (paidWeaponId == -1)
                    specialWeaponType = weaponTypes.get(RNG.nextInt(weaponTypes.size()));
                else {
                    specialWeaponType = SpecialWeaponType.values()[paidWeaponId];
                }
                randomWeaponId = new Pair<>(specialWeaponType.getId(), -2);
            }


            int cntShots = 1;
            if ((allowSpecialWeaponsShoots || needPaidShots) && randomWeaponId.getKey() != -1) {
                stubShot = new StubShot(0, 0, randomWeaponId.getKey(), 22, 2, 2, isPaidShot);
                cntShots = MathData.getRandomDamageForWeapon(config, randomWeaponId.getKey(), false);
                weapon = new Weapon(1, SpecialWeaponType.values()[randomWeaponId.getKey()]);
            } else {
                stubShot = new StubShot(0, 0, -1, 22, 2, 2, isPaidShot);
                weapon = null;
            }
            seat.setActualShot(stubShot);

            if (!isFreeShot) {
                if (isPaidShot) {
                    paidBet += betLevel * stake.getWithMultiplier(MathData.getPaidWeaponCost(config, weapon.getType().getId())).toDoubleCents() / 100;
                    allBet += betLevel * stake.getWithMultiplier(MathData.getPaidWeaponCost(config, weapon.getType().getId())).toDoubleCents();
                } else {
                    tBet += betLevel * stake.toDoubleCents() / 100;
                    allBet += betLevel * stake.toDoubleCents();
                }
            }

            int size = cntShots;
            //cntShots = 1;
            size = 1;
            for (int cnt = 0; cnt < size; cnt++) {
                ShootResult shootResult = enemyGame.shootBaseEnemy(seat, weapon, stake, enemy, false, factoryService, true, false);

                allWin += shootResult.getWin().getWithMultiplier(cntShots).toDoubleCents();
                allWin += shootResult.getKillAwardWin().getWithMultiplier(cntShots).toDoubleCents();
                allWin += shootResult.getMoneyWheelWin().getWithMultiplier(cntShots).toDoubleCents();

                if (shootResult.getNeedExplodeHP() > 0) {
                    double doubleCents = stake.getWithMultiplier(shootResult.getNeedExplodeHP() *
                            seat.getBetLevel() * cntShots).toDoubleCents();
                    bomberWin += doubleCents / 100;
                    allWin += doubleCents;
                }
                double win = shootResult.getWin().getWithMultiplier(cntShots).toDoubleCents() / 100;
                if (isSpecialWeaponShot) {
                    if (randomWeaponId.getValue() == WeaponSource.KILL_AWARD.ordinal()) {
                        weaponBaseWinsKA += win;
                    } else if (randomWeaponId.getValue() == WeaponSource.DROP_ON_SHOOT.ordinal()) {
                        weaponBaseWinsDrop += win;
                    } else if (randomWeaponId.getValue() == -2) {
                        weaponBaseWinsPaid += win;
                    }
                } else {
                    baseWin += win;
                }

                if (shootResult.getKillAwardWin().greaterThan(Money.ZERO)) {
                    win = shootResult.getKillAwardWin().getWithMultiplier(cntShots).toDoubleCents() / 100;
                    if (isSpecialWeaponShot) {
                        if (randomWeaponId.getValue() == WeaponSource.KILL_AWARD.ordinal()) {
                            weaponKillAwardKA += win;
                        } else if (randomWeaponId.getValue() == WeaponSource.DROP_ON_SHOOT.ordinal()) {
                            weaponKillAwardDrop += win;
                        } else if (randomWeaponId.getValue() == -2) {
                            weaponKillAwardPaid += win;
                        }
                    } else {
                        killAward += win;
                    }
                    cntKillAward++;
                }
                if (shootResult.getMoneyWheelWin().greaterThan(Money.ZERO)) {
                    moneyWheel += (shootResult.getMoneyWheelWin().getWithMultiplier(cntShots).toDoubleCents() / 100);
                    cntMoneyWheel++;
                }

                for (ITransportWeapon awardedWeapon : shootResult.getAwardedWeapons()) {
                    Map<Integer, AtomicInteger> weapons = awardedWeapon.getSourceId() == WeaponSource.KILL_AWARD.ordinal() ? weaponsKillAward : weaponsDrop;
                    AtomicInteger weaponsOrDefault = weapons.getOrDefault(awardedWeapon.getId(), new AtomicInteger(0));
                    weaponsOrDefault.addAndGet(awardedWeapon.getShots() * cntShots);
                    weapons.put(awardedWeapon.getId(), weaponsOrDefault);
                }

                if (shootResult.isDestroyed()) {
                    enemy = createNewEnemy(enemyType);
                    cntEnemyKilled++;
                }
            }
        }

        printLog("Kill award weapons: ");
        for (Map.Entry<Integer, AtomicInteger> specialWeapon : weaponsKillAward.entrySet()) {
            printLog(SpecialWeaponType.values()[specialWeapon.getKey()]
                    + " - " + specialWeapon.getValue().get());
            weaponCompensateKillAward += getCompensationForWeapon(config, specialWeapon.getKey(), specialWeapon.getValue().get());
        }

        printLog("Drop weapons: ");
        for (Map.Entry<Integer, AtomicInteger> specialWeapon : weaponsDrop.entrySet()) {
            printLog(SpecialWeaponType.values()[specialWeapon.getKey()]
                    + " - " + specialWeapon.getValue().get());
            weaponCompensateDrop += getCompensationForWeapon(config, specialWeapon.getKey(), specialWeapon.getValue().get());
        }

        double totalBet = tBet + paidBet;
        printLog("Total bet: " + tBet);
        printLog("Total paid bet: " + paidBet);

        printLog("Base win: " + baseWin + " RTP: " + baseWin / totalBet);
        printLog("killAward: " + killAward + " RTP: " + killAward / totalBet + "  cnt: " + cntKillAward);
        printLog("moneyWheel: " + moneyWheel + " RTP: " + moneyWheel / totalBet + "  cnt: " + cntMoneyWheel);
        printLog("weaponCompensateKillAward: " + weaponCompensateKillAward + " RTP: " + weaponCompensateKillAward / totalBet);
        printLog("weaponCompensateDrop: " + weaponCompensateDrop + " RTP: " + weaponCompensateDrop / totalBet);

        printLog("Base win ( + SW KA + killAward): " + (baseWin + weaponBaseWinsKA + weaponKillAwardKA + killAward)
                + " RTP: " + (baseWin + weaponBaseWinsKA + weaponKillAwardKA + killAward) / totalBet);

        printLog("SW Drop: Base win: " + weaponBaseWinsDrop + " RTP: " + weaponBaseWinsDrop / totalBet);
        printLog("SW Drop: killAward: " + weaponKillAwardDrop + " RTP: " + weaponKillAwardDrop / totalBet + "  cnt: " + cntKillAward);

        printLog("SW KA: Base win: " + weaponBaseWinsKA + " RTP: " + weaponBaseWinsKA / totalBet);
        printLog("SW KA: killAward: " + weaponKillAwardKA + " RTP: " + weaponKillAwardKA / totalBet + "  cnt: " + cntKillAward);


        printLog("SW Paid: Base win: " + weaponBaseWinsPaid + " RTP: " + weaponBaseWinsPaid / totalBet);
        printLog("SW Paid: killAward: " + weaponKillAwardPaid + " RTP: " + weaponKillAwardPaid / totalBet);

        printLog("bomberWin: " + bomberWin + " RTP: " + bomberWin / totalBet);
        double baseWins = moneyWheel + baseWin + killAward + weaponCompensateDrop + weaponCompensateKillAward;
        double weaponWins = weaponBaseWinsDrop + weaponKillAwardDrop + weaponBaseWinsKA + weaponKillAwardKA;
        double weaponPaidWins = weaponBaseWinsPaid + weaponKillAwardPaid;

        printLog("Total RTP: " + (baseWins + weaponWins + weaponPaidWins + bomberWin) / totalBet);
        printLog("cnt enemy killed: " + cntEnemyKilled);


        printLog("allBet: " + allBet);
        printLog("allWin: " + allWin);
        printLog("totalWin/totalBet (RTP): " + allWin / allBet);

        double duration = (System.currentTimeMillis() - start) / 1000. / 60;
        printLog("duration: " + duration);

        if (needSaveToFile) {
            pwh.flush();
            pwh.close();
        }
    }

    double getCompensationForWeapon(GameConfig config, int weaponId, int shots) {
        Double rtpForWeapon = MathData.getFullRtpForWeapon(config, weaponId, false) / 100;
        Money newCompensation = Money.ZERO;
        double multiplier = new BigDecimal(shots, MathContext.DECIMAL32)
                .multiply(new BigDecimal(rtpForWeapon, MathContext.DECIMAL32))
                .multiply(new BigDecimal(MathData.getAverageDamageForWeapon(enemyGame.getConfig(seat), weaponId), MathContext.DECIMAL32))
                .multiply(new BigDecimal(seat.getBetLevel(), MathContext.DECIMAL32))
                .doubleValue();
        newCompensation = newCompensation.add(stake.getWithMultiplier(multiplier));
        return newCompensation.toDoubleCents() / 100;
    }


    Pair<Integer, Integer> getRandomWeapon(boolean allowSpecialWeaponsShoots) {
        if (allowSpecialWeaponsShoots) {
            for (Map.Entry<Integer, AtomicInteger> weapon : weaponsDrop.entrySet()) {
                if (weapon.getValue().get() > 0) {
                    weapon.getValue().decrementAndGet();
                    weaponsDrop.put(weapon.getKey(), weapon.getValue());
                    return new Pair<>(weapon.getKey(), WeaponSource.DROP_ON_SHOOT.ordinal());
                }
            }

            for (Map.Entry<Integer, AtomicInteger> weapon : weaponsKillAward.entrySet()) {
                if (weapon.getValue().get() > 0) {
                    weapon.getValue().decrementAndGet();
                    weaponsKillAward.put(weapon.getKey(), weapon.getValue());
                    return new Pair<>(weapon.getKey(), WeaponSource.KILL_AWARD.ordinal());
                }
            }
        }
        return new Pair<>(-1, -1);
    }

    Enemy createNewEnemy(EnemyType enemyType) {
        IMathEnemy mathEnemy;
        int bossId = RNG.nextInt(3);
        if (enemyType.isBoss()) {
            mathEnemy = new MathEnemy(0, "", 0, 100);
        } else {
            mathEnemy = createMathEnemyWithLevel(enemyType);
        }

        Enemy enemy = currentMap.createEnemy(enemyType, 1, null, 4, mathEnemy, -1);
        enemy.setEnergy(enemy.getFullEnergy());
        if (enemyType.isBoss()) {
            enemy.setSkin(bossId + 1);
        }
        enemy.setEnemyMode(EnemyMode.X_1);
        return enemy;
    }

    static public IMathEnemy createMathEnemyWithLevel(EnemyType enemyType) {
        return new MathEnemy(0, "", 0, 100);
    }

    public void printLog(String message) {
        if (pwh != null) {
            pwh.println(message);
            pwh.flush();
        } else
            System.out.println(message);
    }
}

