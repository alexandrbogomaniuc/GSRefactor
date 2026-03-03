package com.betsoft.casino.mp.clashofthegods;

import com.betsoft.casino.mp.clashofthegods.model.*;
import com.betsoft.casino.mp.clashofthegods.model.math.EnemyRange;
import com.betsoft.casino.mp.clashofthegods.model.math.EnemyType;
import com.betsoft.casino.mp.clashofthegods.model.math.MathData;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.common.math.TimeMeasure;
import com.betsoft.casino.mp.common.testmodel.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.abs.casino.common.util.LongIdGenerator;
import com.abs.casino.common.util.RNG;
import com.google.gson.Gson;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.common.testmodel.StubSocketService.getConnection;

@SuppressWarnings("rawtypes")
public class TestModel {
    private final ConcurrentHashMap<Long, SeatStat> seatStats = new ConcurrentHashMap<>();
    private long totalBet = 0;
    private long totalWin = 0;
    PrintWriter pwh;
    boolean fullDebug = false;
    public static GameType gameType = GameType.CLASH_OF_THE_GODS;
    boolean roundIsFinished;
    boolean roundIsStarted;
    long minBosKills = 10000;
    long maxBosKills = 0;
    PlayerRoundInfo totalPlayerRoundsInfo = new PlayerRoundInfo(-1);

    public static void main(String[] args) throws Exception {
        init();
        TestModel testModel = new TestModel();
        boolean debug = false;
        int cntPlayers = 1;
        double probBuySpecialWeapon = 0.3; // 0-1
        boolean allowSpecialWeaponsShoots = true;
        boolean shootFromSpecialWeaponsOnly = false;
        boolean needSaveToFile = false;
        String idEnemiesForShooting = null;
        int numberOfEnemies = 50;
        int enemyLevel = -1;
        int coinInCents = 2;
        int cntRealShotsRequired = 100000;
        int betLevel = 1;
        boolean needUsePaidWeapons = false;
        int paidWeaponId = -1;
        boolean needTestX2Mode = false;
        for (String arg : args) {
            String[] param_ = arg.split("=");
            switch (param_[0]) {
                case "debug":
                    debug = param_[1].equals("true");
                    break;
                case "needTestX2Mode":
                    needTestX2Mode = param_[1].equals("true");
                    break;
                case "cntPlayers":
                    cntPlayers = Integer.parseInt(param_[1]);
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
                case "probBuySpecialWeapon":
                    probBuySpecialWeapon = Double.parseDouble(param_[1]);
                    break;
                case "shootFromSpecialWeaponsOnly":
                    shootFromSpecialWeaponsOnly = param_[1].equals("true");
                    break;
                case "idEnemiesForShooting": {
                    idEnemiesForShooting = param_[1];
                    break;
                }
                case "numberOfEnemies": {
                    numberOfEnemies = Integer.parseInt(param_[1]);
                    break;
                }
                case "needSaveToFile":
                    needSaveToFile = param_[1].equals("true");
                    break;
                case "enemyLevel":
                    enemyLevel = Integer.parseInt(param_[1]);
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
        Set<EnemyType> enemiesForSpecialWeapons = new HashSet<>();
        Set<Integer> enemiesForBaseShooting = new HashSet<>();
        //   idEnemiesForShooting = "21";
        if (idEnemiesForShooting != null && !idEnemiesForShooting.isEmpty()) {
            String[] split = idEnemiesForShooting.split(",");
            for (String s : split) {
                EnemyType enemy = EnemyType.getById(Integer.parseInt(s));
                enemiesForSpecialWeapons.add(enemy);
                enemiesForBaseShooting.add(enemy.getId());
            }
        } else {
            for (EnemyType value : EnemyType.values()) {
                enemiesForSpecialWeapons.add(value);
                enemiesForBaseShooting.add(value.getId());
            }
        }
        testModel.doTestRound(debug,
                enemiesForSpecialWeapons,
                cntPlayers,
                probBuySpecialWeapon,
                allowSpecialWeaponsShoots,
                enemiesForBaseShooting,
                shootFromSpecialWeaponsOnly,
                needSaveToFile, idEnemiesForShooting, numberOfEnemies, needTestX2Mode,
                enemyLevel, coinInCents, cntRealShotsRequired, betLevel,
                needUsePaidWeapons,
                paidWeaponId
        );
    }

    public void doTestRound(boolean debug, Set<EnemyType> enemiesForSpecialWeapons, int cntPlayers,
                            double probBuySpecialWeapon,
                            boolean allowSpecialWeaponsShoots, Set<Integer> enemiesForShooting,
                            boolean shootFromSpecialWeaponsOnly, boolean needSaveToFile,
                            String idEnemiesForShooting, int numberOfEnemies,
                            boolean needTestX2Mode, int enemyLevel, int coinInCents,
                            int cntRealShotsRequired, int betLevel, boolean needUsePaidWeapons, int paidWeaponId
    ) throws Exception {
        StubRoomTemplate stubRoomTemplate = new StubRoomTemplate(1, 271, gameType,
                (short) 6, (short) 1, MoneyType.REAL, 100, 100, 1, 1,
                1, 1, "Test Room", 5);
        String name_ = this.getClass().getPackage().getName();
        String gameName = name_.substring(name_.lastIndexOf(".") + 1);
        String fileStatsName = gameName + "_" + System.currentTimeMillis() + "_"
                + allowSpecialWeaponsShoots + "_"
                + idEnemiesForShooting + "_"
                + coinInCents + "_"
                + cntRealShotsRequired + "_"
                + needUsePaidWeapons + "_"
                + paidWeaponId + "_"
                + betLevel + "_"
                + ".txt";
        System.out.println(fileStatsName);
        if (needSaveToFile)
            pwh = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileStatsName), "UTF-8"));
        fullDebug = debug;
        List<SpecialWeaponType> specialWeaponTypes = Arrays.stream(SpecialWeaponType.values()).filter(
                        specialWeaponType -> specialWeaponType.getAvailableGameIds().contains((int) gameType.getGameId())
                                && !specialWeaponType.equals(SpecialWeaponType.Landmines)
                )
                .collect(Collectors.toList());
        printLog("mode: probBuySpecialWeapon: " + probBuySpecialWeapon);
        printLog("mode: allowSpecialWeaponsShoots: " + allowSpecialWeaponsShoots);
        printLog("mode: shootFromSpecialWeaponsOnly: " + shootFromSpecialWeaponsOnly);
        printLog("mode: needSaveToFile: " + needSaveToFile);
        printLog("mode: optimal enemyLevel: " + enemyLevel);
        printLog("mode: optimal numberOfEnemies: " + numberOfEnemies);
        printLog("mode: coinInCents: " + coinInCents);
        printLog("mode: cntRealShotsRequired: " + cntRealShotsRequired);
        printLog("mode: needTestX2Mode: " + needTestX2Mode);
        printLog("mode: betLevel: " + betLevel);
        printLog("mode: needUsePaidWeapons: " + needUsePaidWeapons);
        printLog("mode: paidWeaponId: " + paidWeaponId);
        boolean shotToCustomEnemies = idEnemiesForShooting != null;
        if (shotToCustomEnemies) {
            printLog("mode: idEnemiesForShooting: ");
            for (EnemyType enemiesForSpecialWeapon : enemiesForSpecialWeapons) {
                printLog(enemiesForSpecialWeapon.getName());
            }
        } else {
            printLog("mode: shot to all enemies");
        }
        printLog("-------------  cntRealShotsRequired: " + cntRealShotsRequired);
        int cntStarts = 0;
        int stakeReserved = 300 * betLevel * 50;
        List<StubPlayerInfo> players = new ArrayList<>();
        String name = "Player ";
        for (long aid = 1; aid < cntPlayers + 1; aid++) {
            players.add(new StubPlayerInfo(271L, aid, name + aid, name + aid,
                    1000000L,
                    new StubCurrency("USD", "$"), false, false));
        }
        Money stake = Money.fromCents(coinInCents);
        StubCurrency stubCurrency = new StubCurrency("EUR", "EUR");
        StubRoomInfoService stubRoomInfoService = new StubRoomInfoService();
        ISingleNodeRoomInfo roomInfo = stubRoomInfoService.createForTemplate(
                stubRoomTemplate,
                271,
                stake, stubCurrency.getCode(),
                10,
                gameType,
                "TestRoom"
        );
        Map<Long, Integer> countOfSpinsToMainBoss = new HashMap<>();
        List<Seat> seats = new ArrayList<>();
        GameMap currentMap;
        GameMapStore gameMapStore = new GameMapStore();
        gameMapStore.init();
        currentMap = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(504));
        GameRoom currentRoom = getCurrentRoom(roomInfo, seats, currentMap);
        for (StubPlayerInfo player : players) {
            long accountId = LongIdGenerator.getInstance().getNext(Seat.class);
            StubRoomPlayerInfo roomPlayerInfo = new StubRoomPlayerInfo(accountId, 271,
                    currentRoom.getId(),
                    1,
                    "sid_123123", 11,
                    "testUser_" + accountId,
                    new StubAvatar(0, 1, 2),
                    System.currentTimeMillis(),
                    new com.abs.casino.common.cache.data.currency.ICurrency() {
                        private String code = "USD";
                        private String symbol = "$";

                        @Override
                        public String getCode() {
                            return code;
                        }

                        @Override
                        public void setCode(String code) {
                            this.code = code;
                        }

                        @Override
                        public String getSymbol() {
                            return symbol;
                        }

                        @Override
                        public void setSymbol(String symbol) {
                            this.symbol = symbol;
                        }

                        @Override
                        public boolean isDefault(long bankId) {
                            return false;
                        }
                    },
                    new StubPlayerStatsService().load(271, gameType.getGameId(), 1), false,
                    null,
                    null, stake.toCents(),
                    10,
                    0.02, MaxQuestWeaponMode.LOOT_BOX, false);
            StubGameSocketClient client = new StubGameSocketClient(accountId, 271L, "", getConnection(),
                    new StubGsonMessageSerializer(new Gson()), gameType);
            Seat seat_ = new Seat(roomPlayerInfo, client, 1);
            StubPlayerQuests playerQuestsForStake = new StubPlayerQuests(new HashSet<>());
            seat_.getPlayerInfo().setPlayerQuests(playerQuestsForStake);
            seats.add(seat_);
            seatStats.put(seat_.getId(), new SeatStat(seat_.getId(), stubCurrency));
            countOfSpinsToMainBoss.put(accountId, 0);
        }
        currentRoom.setDefaultTimeMillis(1);
        currentRoom.getGame().setDebug(false);
        currentRoom.start();
        Map<String, Long> stakesBySeats = new HashMap<>();
        int mult = 0;
        for (Seat seat_ : seats) {
            seat_.getSocketClient().setRoomId(currentRoom.getId());
            currentRoom.processSitIn(seat_, new StubSitIn(System.currentTimeMillis(), 0, "en"));
            stakesBySeats.put(seat_.getNickname(), (long) (1 + mult * 1000));
            mult++;
        }
        int limit = 500000;
        boolean needFinish = false;
        while (!needFinish) {
            while (!roundIsStarted) {
                Thread.sleep(1);
            }
            cntStarts++;
            roundIsFinished = false;
            PlayGameState gameState = (PlayGameState) currentRoom.getGameState();
            gameState.setManualGenerationEnemies(true);
            int cnt = 0;
            long s1 = System.currentTimeMillis();
            ITransportObjectsFactoryService toFactoryService = gameState.getRoom().getTOFactoryService();
            int lastPercent = -1;
            while (currentRoom.getGameState().getRoomState().equals(RoomState.PLAY)) {
                gameState.setPauseTime(-9999);
                Seat seat = seats.get(RNG.nextInt(seats.size()));
                seat.setBetLevel(betLevel);
                AtomicLong realShots = new AtomicLong(0);
                synchronized (seatStats) {
                    totalPlayerRoundsInfo.getHitMissStatByWeapons().forEach((wpId, aws) -> {
                        boolean isSWOnly = shootFromSpecialWeaponsOnly && wpId != -1;
                        boolean isPaidOnly = !shootFromSpecialWeaponsOnly && wpId == -1;
                        if (isSWOnly || isPaidOnly)
                            realShots.addAndGet(aws.getNumberOfRealShots() - aws.getNumberOfKilledMiss());
                    });
                    updateRealShots(stake, realShots, totalPlayerRoundsInfo);
                }
                PlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
                long currentBetsSW = (long) currentPlayerRoundInfo.getTotalBetsSpecialWeapons().toDoubleCents();
                long currentBets = (long) currentPlayerRoundInfo.getTotalBets().toDoubleCents();
                long currentWins = (long) currentPlayerRoundInfo.getTotalPayouts().toDoubleCents();
                Map<Integer, AdditionalWeaponStat> hitMissStatByWeapons;
                try {
                    hitMissStatByWeapons = currentPlayerRoundInfo.getHitMissStatByWeapons();
                    hitMissStatByWeapons.forEach((wpId, aws) -> {
                                boolean isSWOnly = shootFromSpecialWeaponsOnly && wpId != -1;
                                boolean isPaidOnly = !shootFromSpecialWeaponsOnly && wpId == -1;
                                if (isSWOnly || isPaidOnly)
                                    realShots.addAndGet(aws.getNumberOfRealShots() - aws.getNumberOfKilledMiss());
                            }
                    );
                    updateRealShots(stake, realShots, currentPlayerRoundInfo);
                } catch (Exception e) {
                }
                if (realShots.get() % 1000 == 0 && !needFinish) {
                    long tBet = totalBet + currentBets + currentBetsSW;
                    long tWin = totalWin + currentWins;
                    if (tBet > 0 && tWin > 0 && realShots.get() > 0) {
                        String message = "realShots: " + realShots
                                + " RTP: " + new BigDecimal((double) tWin / tBet).multiply(BigDecimal.valueOf(100))
                                .setScale(4, RoundingMode.HALF_UP)
                                + " totalBet: " + toMoney(totalBet)
                                + " totalWin: " + toMoney(totalWin)
                                + " currentWins: " + toMoney(currentWins)
                                + " currentBetsSW: " + toMoney(currentBetsSW)
                                + " currentBets: " + toMoney(currentBets);
                        printLog(message);
                    }
                }
                double percent = ((double) realShots.get() / cntRealShotsRequired) * 100;
                int iPercent = (int) percent;
                if (percent == iPercent && iPercent % 3 == 0 && iPercent != lastPercent) {
                    System.out.print(" " + percent + "%");
                    lastPercent = iPercent;
                }
                if (realShots.get() >= cntRealShotsRequired) {
                    needFinish = true;
                    continue;
                }
                if (seat.getAmmoAmount() < betLevel * 200 || seat.getAmmoAmountTotalInRound() == 0) {
                    // printLog("buy shots");
                    IActionRoomPlayerInfo playerInfo = seat.getPlayerInfo();
                    seat.setAmmoAmount(0);
                    seat.incrementAmmoAmount(stakeReserved);
                    seat.incrementTotalAmmoAmount(stakeReserved);
                    playerInfo.makeBuyIn(1, stake.multiply(stakeReserved).toCents());
                    playerInfo.setPendingOperation(false);
                    playerInfo.incrementBuyInCount();
                    seat.updatePlayerRoundInfo(1);
                    seat.setPlayerInfo(playerInfo);
                }
                if (cnt > limit) {
                    printLog("gameState.isRoundWasFinished(): " + gameState.isRoundWasFinished());
                    if (gameState.isRoundWasFinished()) {
                        gameState.setRoundWasFinished(false);
                    }
                    gameState.doFinishWithLock();
                    printLog("wrong end of round");
                    List<Enemy> items = gameState.getMap().getItems();
                    for (Enemy item : items) {
                        printLog(item.getEnemyClass().getEnemyType().getName());
                    }
                    break;
                }
                checkEnemiesInRoom(enemiesForShooting, numberOfEnemies, enemyLevel, currentMap, gameState, needTestX2Mode);
                Map<Long, Integer> itemsId = currentMap.getPossibleItemsId();
                List<Long> allowedEnemies = new ArrayList<>();
                for (Map.Entry<Long, Integer> entry : itemsId.entrySet()) {
                    Integer idType = entry.getValue();
                    Long idEnemy = entry.getKey();
                    if (enemiesForShooting.contains(idType)) {
                        allowedEnemies.add(idEnemy);
                    }
                }
                AtomicBoolean noWeapons = new AtomicBoolean(true);
                seat.getWeapons().forEach((specialWeaponType, weapon) -> {
                            if (weapon.getShots() > 0)
                                noWeapons.set(false);
                        }
                );
                Long idEnemy;
                Enemy itemById;
                if (allowedEnemies.isEmpty() || !gameState.isAllowSpawn()) {
                    cnt++;
                    continue;
                } else {
                    idEnemy = allowedEnemies.get(RNG.nextInt(allowedEnemies.size()));
                    itemById = currentMap.getItemById(idEnemy);
                    if (currentMap.getItemById(idEnemy) == null) {
                        cnt++;
                        continue;
                    }
                }
                EnemyType enemyType = itemById.getEnemyClass().getEnemyType();
                seat.setWeapon(-1);
                if (allowSpecialWeaponsShoots) {
                    if ((enemiesForSpecialWeapons.contains(enemyType) || enemiesForSpecialWeapons.isEmpty())
                            && seat.getSpecialWeaponId() == -1) {
                        choiceWeapon(seat);
                    }
                }
                if (shootFromSpecialWeaponsOnly && seat.getCurrentWeaponId() == -1)
                    continue;
                boolean isPaidShotIsPossible = !seat.isAnyWeaponShotAvailable();
                boolean isPaidSpecialShot = isPaidShotIsPossible && needUsePaidWeapons;
                try {
                    if (isPaidSpecialShot && noWeapons.get()) {
                        int wId = paidWeaponId == -1 ?
                                specialWeaponTypes.get(RNG.nextInt(specialWeaponTypes.size())).getId() : paidWeaponId;
                        seat.setWeapon(wId);
                    }
                    int currentWeaponId = seat.getCurrentWeaponId();
                    int itemsSize = currentMap.getItemsSize();
                    if (itemsSize > numberOfEnemies) {
                        IShot shot = toFactoryService.createShot(System.currentTimeMillis(), 0, currentWeaponId,
                                idEnemy, 0, 0, isPaidSpecialShot);
                        gameState.processShot(seat, shot, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.print(e.getLocalizedMessage());
                }
                cnt++;
            }
            while (true) {
                if (roundIsFinished)
                    break;
                Thread.sleep(2);
            }
            roundIsStarted = false;
            TimeMeasure.getInstance().addMeasure("One Room played", System.currentTimeMillis() - s1);
        }
        Thread.sleep(1000);
        int gameId = (int) gameType.getGameId();
        if (fullDebug) {
            printLog(totalPlayerRoundsInfo.toString());
            printLog("total roundInfo: \n" + totalPlayerRoundsInfo.getRTPStatData(gameId));
        }
        AtomicLong totalRealShots = new AtomicLong();
        totalPlayerRoundsInfo.getHitMissStatByWeapons().forEach((integer, additionalWeaponStat) -> {
            totalRealShots.addAndGet(additionalWeaponStat.getNumberOfRealShots());
        });
        printLog("number of rounds: " + cntStarts);
        printLog("---totalBet(main game + special weapons): " + toMoney(totalBet));
        printLog("---totalWin: " + toMoney(totalWin));
        printLog("---SW returned: " + toMoney((long) totalPlayerRoundsInfo.getWeaponSurplusMoney().toDoubleCents()));
        printLog("---RTP : " + new BigDecimal((double) totalWin / totalBet).multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP));
        Map<Integer, AdditionalWeaponStat> hitMissStatByWeapons = totalPlayerRoundsInfo.getHitMissStatByWeapons();
        hitMissStatByWeapons.forEach((wpId, additionalWeaponStat) -> {
            if (additionalWeaponStat != null) {
                String title = wpId == -1 ? "Pistol" : SpecialWeaponType.values()[wpId].getTitle();
                printLog(" Shots from " + title + " : " + additionalWeaponStat.getNumberOfRealShots());
            }
        });
        if (fullDebug) {
            printLog("maxBosKills: " + maxBosKills);
            printLog("minBosKills: " + minBosKills);
        }
        TimeMeasure.getInstance().printData();
        if (needSaveToFile)
            pwh.close();
    }

    private void updateRealShots(Money stake, AtomicLong realShots, PlayerRoundInfo currentPlayerRoundInfo) {
        Map<String, EnemyStat> statByEnemies = currentPlayerRoundInfo.getStatByEnemies();
        for (Map.Entry<String, EnemyStat> statEntry : statByEnemies.entrySet()) {
            int betLevelEnemy = Integer.parseInt(statEntry.getKey().split("_")[2]);
            Map<String, WeaponStat> specialWeaponsStats = statEntry.getValue().getSpecialWeaponsStats();
            for (Map.Entry<String, WeaponStat> weaponStatEntry : specialWeaponsStats.entrySet()) {
                Money payBets = weaponStatEntry.getValue().getPayBets();
                if (payBets.greaterThan(Money.ZERO)) {
                    SpecialWeaponType weaponType = SpecialWeaponType.getByTitle(weaponStatEntry.getKey());
                    Integer price = MathData.getPaidWeaponCost(weaponType.getId());
                    long cntBets = payBets.divideBy(stake) / betLevelEnemy / price;
                    realShots.addAndGet(cntBets);
                }
            }
        }
    }

    private void checkEnemiesInRoom(Set<Integer> enemiesForShooting, int numberOfEnemies,
                                    int enemyLevel, GameMap currentMap, PlayGameState gameState, boolean needTestX2Mode) {
        List<Enemy> newEnemies = new ArrayList<>();
        Map<Long, Integer> itemsId = currentMap.getPossibleItemsId();
        for (Integer enemyTypeId : enemiesForShooting) {
            int cntLiveEnemies = 0;
            if (gameState.isAllowSpawn()) {
                for (Map.Entry<Long, Integer> entry : itemsId.entrySet()) {
                    if (entry.getValue().equals(enemyTypeId))
                        cntLiveEnemies++;
                }
                EnemyType enemyType = EnemyType.getById(enemyTypeId);
                boolean isBoss = enemyType.isBoss();
                IMathEnemy mathEnemy;
                int realBossEnemyLevel = enemyLevel == -1 ? RNG.nextInt(3) : enemyLevel;
                if (!isBoss) {
                    mathEnemy = currentMap.createMathEnemyWithLevel(enemyType, enemyLevel);
                } else {
                    Integer bossHP = MathData.getBossParams().get(realBossEnemyLevel).second();
                    mathEnemy = new MathEnemy(0, "", 0, bossHP);
                }
                if (gameState.isAllowSpawn()) {
                    while (cntLiveEnemies++ < numberOfEnemies) {
                        Enemy enemy = gameState.getMap().createEnemy(enemyType, 1, getRandomTrajectory(),
                                4, mathEnemy, -1);
                        if (isBoss)
                            enemy.setSkin(realBossEnemyLevel + 1);
                        enemy.setEnergy(enemy.getFullEnergy());
                        if (needTestX2Mode && EnemyRange.BaseEnemiesWithoutSpirits.getEnemies().contains(enemyType))
                            enemy.setEnemyMode(EnemyMode.X_2);
                        newEnemies.add(enemy);
                    }
                }
                if (!newEnemies.isEmpty())
                    gameState.getMap().addEnemiesToMap(newEnemies);
            } else {
                gameState.getMap().removeAllEnemies();
                gameState.setRemainingNumberOfBoss(0);
            }
        }
    }

    private Trajectory getRandomTrajectory() {
        List<Point> points = new ArrayList<>();
        int xFirst = 40 - RNG.nextInt(15);
        int yFirst = 40 - RNG.nextInt(15);
        int xSecond = 44 + RNG.nextInt(15);
        int ySecond = 44 + RNG.nextInt(15);
        points.add(new Point(xFirst, yFirst, System.currentTimeMillis() - 10000));
        points.add(new Point(xSecond, ySecond, System.currentTimeMillis() + 80000));
        return new Trajectory(3, points);
    }

    private void choiceWeapon(Seat seat) {
        List<Integer> listPossibleSpecialIds = new LinkedList<>();
        for (Map.Entry<SpecialWeaponType, Weapon> wp : seat.getWeapons().entrySet()) {
            if (wp.getValue().getShots() > 0 && !wp.getKey().equals(SpecialWeaponType.Landmines)) {
                listPossibleSpecialIds.add(wp.getKey().getId());
            }
        }
        if (!listPossibleSpecialIds.isEmpty()) {
            seat.setWeapon(listPossibleSpecialIds.get(RNG.nextInt(listPossibleSpecialIds.size())));
        }
    }

    private GameRoom getCurrentRoom(ISingleNodeRoomInfo roomInfo, List<Seat> seats, GameMap currentMap) {
        TestApplicationContext ctx = TestApplicationContext.createContextWithStubBeans(new StubSocketService() {
            @Override
            public ISitInResult sitIn(String sessionId, long gameId, String mode, String lang, Long bonusId,
                                      long oldGameSessionId, long oldRoundId, long roomId, int betNumber,
                                      Long tournamentId, String nickname) {
                return null;
            }

            @Override
            public Mono<IAddWinResult> addWin(int serverId, String sessionId, long gameSessionId,
                                              Money winAmount, Money returnedBet, long roundId, long roomId,
                                              long accountId, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) {
                synchronized (seatStats) {
                    try {
                        if (fullDebug) {
                            System.out.println("playerBet.getData(): " + playerBet.getData());
                        }
                        PlayerRoundInfo roundInfo = PlayerRoundInfo.getPlayerRoundInfoFromVBAData(playerBet.getData(),
                                playerBet.getBet(), playerBet.getWin());
                        long cntToBoss = roundInfo.getStatBoss().getCntToBoss();
                        if (cntToBoss > maxBosKills) maxBosKills = cntToBoss;
                        if (cntToBoss < minBosKills) minBosKills = cntToBoss;
                        totalPlayerRoundsInfo.addRoundInfo(roundInfo);
                    } catch (Exception e) {
                        System.out.println("error addWin");
                        e.printStackTrace();
                    }
                }
                totalWin += (long) playerBet.getWin();
                totalBet += (long) playerBet.getBet();
                roundIsFinished = true;
                return Mono.just(new StubAddWinResult(false, 1000, true, 0, ""));
            }

            @Override
            public IStartNewRoundResult startNewRound(int serverId, long accountId, String sessionId,
                                                      long gameSessionId, long roomId, long roomRoundId,
                                                      long roundStartDate, boolean battlegroundRoom, long stakeOrBuyInAmount) {
                IStartNewRoundResult iStartNewRoundResult = super.startNewRound(serverId, accountId, sessionId,
                        gameSessionId, roomId, roomRoundId, roundStartDate, battlegroundRoom, stakeOrBuyInAmount);
                roundIsStarted = true;
                return iStartNewRoundResult;
            }
        }, seats);
        return new GameRoom(ctx, new EmptyLogger(), roomInfo, currentMap,
                new StubPlayerStatsService(), new StubWeaponService(),
                null, null, new StubPlayerProfileService(),
                null, null, null, null);
    }

    private static void init() {
        LongIdGenerator.getInstance().init(new StubSequencerPersister());
    }

    public void printLog(String message) {
        if (pwh != null) {
            pwh.println(message);
            pwh.flush();
        } else
            System.out.println(message);
    }

    public String toMoney(long cents) {
        return new BigDecimal(cents / 100.).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
