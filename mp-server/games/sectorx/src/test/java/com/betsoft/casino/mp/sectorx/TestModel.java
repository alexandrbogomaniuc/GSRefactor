package com.betsoft.casino.mp.sectorx;

import com.betsoft.casino.mp.common.AdditionalWeaponStat;
import com.betsoft.casino.mp.common.EnemyStat;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.WeaponStat;
import com.betsoft.casino.mp.common.math.TimeMeasure;
import com.betsoft.casino.mp.common.testmodel.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.sectorx.model.*;
import com.betsoft.casino.mp.sectorx.model.math.EnemyRange;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.betsoft.casino.mp.sectorx.model.math.MathData;
import com.betsoft.casino.mp.sectorx.model.math.config.*;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.LongIdGenerator;
import com.dgphoenix.casino.common.util.RNG;
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
import static com.betsoft.casino.mp.sectorx.model.math.EnemyRange.*;

@SuppressWarnings("rawtypes")
public class TestModel {
    private final ConcurrentHashMap<Long, SeatStat> seatStats = new ConcurrentHashMap<>();
    private long totalBet = 0;
    private long totalWin = 0;
    PrintWriter pwh;
    boolean fullDebug = true;
    public static GameType gameType = GameType.SECTOR_X;
    boolean roundIsFinished;
    boolean roundIsStarted;
    long minBosKills = 10000;
    long maxBosKills = 0;
    PlayerRoundInfo totalPlayerRoundsInfo = new PlayerRoundInfo(-1);
    List<Enemy> newEnemies = new ArrayList<>();
    StubRoomTemplate roomTemplate;

    boolean prioritiseEnemies = true;
    Integer enemyPriorityBoss = 10;
    Integer enemyPrioritySpecialItem = 10;
    Integer enemyPriorityHugePay = 30;
    Integer enemyPriorityOtherEnemies = 70;

    public static void main(String[] args) throws Exception {

        init();
        for (int i = 0; i < 100; i++) {
            TestModel testModel = new TestModel();
            boolean debug = true;
            int cntPlayers = 1;
            boolean needSaveToFile = false;
            String idEnemiesForShooting = "57,100"; /*1,2,3,4,5,6,7,8,9,10,11,12,13,15,15,16,57,100,32*/
            int numberOfEnemies = 1;
            int coinInCents = 2;
            int cntRealShotsRequired = 1000000;
            int betLevel = 1;
            for (String arg : args) {
                String[] params = arg.split("=");
                switch (params[0]) {
                    case "debug":
                        debug = params[1].equals("true");
                        break;
                    case "cntPlayers":
                        cntPlayers = Integer.parseInt(params[1]);
                        break;
                    case "betLevel":
                        betLevel = Integer.parseInt(params[1]);
                        break;
                    case "idEnemiesForShooting":
                        idEnemiesForShooting = params[1];
                        break;
                    case "numberOfEnemies":
                        numberOfEnemies = Integer.parseInt(params[1]);
                        break;
                    case "needSaveToFile":
                        needSaveToFile = params[1].equals("true");
                        break;
                    case "coinInCents":
                        coinInCents = Integer.parseInt(params[1]);
                        break;
                    case "cntRealShotsRequired":
                        cntRealShotsRequired = Integer.parseInt(params[1]);
                        break;
                    case "prioritiseEnemies":
                        testModel.prioritiseEnemies = params[1].equals("true");
                        break;
                    case "enemyPriorityBoss":
                        testModel.enemyPriorityBoss = Integer.parseInt(params[1]);
                        break;
                    case "enemyPrioritySpecialItem":
                        testModel.enemyPrioritySpecialItem = Integer.parseInt(params[1]);
                        break;
                    case "enemyPriorityHugePay":
                        testModel.enemyPriorityHugePay = Integer.parseInt(params[1]);
                        break;
                    case "enemyPriorityOtherEnemies":
                        testModel.enemyPriorityOtherEnemies = Integer.parseInt(params[1]);
                        break;
                    default:
                        break;
                }
            }
            Set<EnemyType> enemiesForSpecialWeapons = new HashSet<>();
            Set<Integer> enemiesForBaseShooting = new HashSet<>();
            if (idEnemiesForShooting != null && !idEnemiesForShooting.isEmpty()) {
                String[] split = idEnemiesForShooting.split(",");
                for (String s : split) {
                    EnemyType enemy = EnemyType.getById(Integer.parseInt(s));
                    enemiesForSpecialWeapons.add(enemy);
                    enemiesForBaseShooting.add(enemy.getId());
                }
            } else {
                for (EnemyType value : EnemyType.values()) {
                    if (!value.isBoss()) {
                        enemiesForSpecialWeapons.add(value);
                        enemiesForBaseShooting.add(value.getId());
                    }
                }
            }
            String fileStatsName = getDebugFileName(idEnemiesForShooting, coinInCents, cntRealShotsRequired, betLevel);

            testModel.doTestRound(debug,
                    enemiesForSpecialWeapons,
                    cntPlayers,
                    enemiesForBaseShooting,
                    needSaveToFile, idEnemiesForShooting, numberOfEnemies,
                    coinInCents, cntRealShotsRequired, betLevel, fileStatsName
            );
        }
    }

    public void doTestRound(boolean debug, Set<EnemyType> enemiesForSpecialWeapons, int cntPlayers,
                            Set<Integer> enemiesForShooting,
                            boolean needSaveToFile,
                            String idEnemiesForShooting, int numberOfEnemies,
                            int coinInCents,
                            int cntRealShotsRequired, int betLevel, String fileStatsName) throws Exception {
        StubRoomTemplate stubRoomTemplate = new StubRoomTemplate(1, 271, gameType,
                (short) 6, (short) 1, MoneyType.REAL, 100, 100, 1, 1,
                1, 1, "Test Room", 5);

        roomTemplate = stubRoomTemplate;


        System.out.println(fileStatsName);
        if (needSaveToFile)
            pwh = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileStatsName, true), "UTF-8"));
        fullDebug = debug;
        List<SpecialWeaponType> specialWeaponTypes = Arrays.stream(SpecialWeaponType.values()).filter(
                        specialWeaponType -> specialWeaponType.getAvailableGameIds().contains((int) gameType.getGameId())
                                && !specialWeaponType.equals(SpecialWeaponType.Landmines)
                )
                .collect(Collectors.toList());
        printLog("mode: needSaveToFile: " + needSaveToFile);
        printLog("mode: optimal numberOfEnemies: " + numberOfEnemies);
        printLog("mode: coinInCents: " + coinInCents);
        printLog("mode: cntRealShotsRequired: " + cntRealShotsRequired);
        printLog("mode: betLevel: " + betLevel);
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
        currentMap = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(1001));
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        GameRoom currentRoom = getCurrentRoom(config, roomInfo, seats, currentMap, spawnConfig);
        for (StubPlayerInfo player : players) {
            long accountId = LongIdGenerator.getInstance().getNext(Seat.class);
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
            Seat seat = new Seat(roomPlayerInfo, client, 1);
            StubPlayerQuests playerQuestsForStake = new StubPlayerQuests(new HashSet<>());
            seat.getPlayerInfo().setPlayerQuests(playerQuestsForStake);
            seats.add(seat);
            seatStats.put(seat.getId(), new SeatStat(seat.getId(), stubCurrency));
            countOfSpinsToMainBoss.put(accountId, 0);
        }
        currentRoom.setDefaultTimeMillis(1);
        currentRoom.getGame().setDebug(false);
        Map<String, Long> stakesBySeats = new HashMap<>();
        int mult = 0;
        for (Seat seat : seats) {
            seat.getSocketClient().setRoomId(currentRoom.getId());
            currentRoom.processSitIn(seat, new StubSitIn(System.currentTimeMillis(), 0, "en"));
            stakesBySeats.put(seat.getNickname(), (long) (1 + mult * 1000));
            mult++;
        }
        currentRoom.start();
        int limit = 500000;
        boolean needFinish = false;
        long lastRealShot = -1;
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
                gameState.setPauseTime(-1999);
                gameState.setManualGenerationEnemies(true);
                Seat seat = seats.get(RNG.nextInt(seats.size()));
                seat.setBetLevel(betLevel);


                //   System.out.println("-----------nanoTime 1: " + System.nanoTime());
                if (!gameState.isAllowSpawn()) {
                    currentMap.removeAllEnemies();
                    continue;
                }
                AtomicLong realShots = new AtomicLong(0);
                synchronized (seatStats) {
                    totalPlayerRoundsInfo.getHitMissStatByWeapons().forEach((wpId, aws) -> {
                        realShots.addAndGet(aws.getNumberOfRealShots() - aws.getNumberOfKilledMiss());
                    });
                    updateRealShots(config, stake, realShots, totalPlayerRoundsInfo);
                }
                PlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
                long currentBetsSW = (long) currentPlayerRoundInfo.getTotalBetsSpecialWeapons().toDoubleCents();
                long currentBets = (long) currentPlayerRoundInfo.getTotalBets().toDoubleCents();
                long currentWins = (long) currentPlayerRoundInfo.getTotalPayouts().toDoubleCents();

                Map<Integer, AdditionalWeaponStat> hitMissStatByWeapons;
                try {
                    hitMissStatByWeapons = currentPlayerRoundInfo.getHitMissStatByWeapons();
                    hitMissStatByWeapons.forEach((wpId, aws) -> {
                                realShots.addAndGet(aws.getNumberOfRealShots() - aws.getNumberOfKilledMiss());
                            }
                    );
                    updateRealShots(config, stake, realShots, currentPlayerRoundInfo);
                } catch (Exception e) {
                    printLog(e.getMessage());
                }
                if (realShots.get() % 1000 == 0 && !needFinish) {
                    long tBet = totalBet + currentBets + currentBetsSW;
                    long tWin = totalWin + currentWins;
                    if (tBet > 0 && tWin > 0 && realShots.get() > 0 && realShots.get() != lastRealShot) {
                        if (realShots.get() / 1000 < 2)
                            printLog("realShots,RTP,totalBet,totalWin,currentWins,currentBetsSW,currentBets");
                        lastRealShot = realShots.get();

                        BigDecimal RTP = new BigDecimal((double) tWin / tBet).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP);

                        String csvOutput = realShots + ","
                                + RTP + ","
                                + toMoney(totalBet) + ","
                                + toMoney(totalWin) + ","
                                + toMoney(currentWins) + ","
                                + toMoney(currentBetsSW) + ","
                                + toMoney(currentBets);
                        printLog(csvOutput);
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
                checkEnemiesInRoom(enemiesForShooting, numberOfEnemies, currentMap, gameState);
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
                            if (weapon.getShots() > 0) {
                                noWeapons.set(false);
                            }
                        }
                );
                Long idEnemy = -1L;
                Enemy itemById;

                if (allowedEnemies.isEmpty() || !gameState.isAllowSpawn()) {
                    cnt++;
                    continue;
                } else {
                    if (prioritiseEnemies) {
                        List<EnemyType> enemyTypesToSelectFrom = Collections.emptyList();

                        switch (RNG.getRandom(Arrays.asList(enemyPriorityBoss, enemyPrioritySpecialItem, enemyPriorityHugePay, enemyPriorityOtherEnemies))) {
                            case 0:
                                enemyTypesToSelectFrom = Collections.singletonList(EnemyType.BOSS);
                                break;
                            case 1:
                                enemyTypesToSelectFrom = SPECIAL_ITEMS.getEnemies().stream().filter(it -> currentMap.getPossibleItemsId().containsValue(it.getId())).collect(Collectors.toList());
                                break;
                            case 2:
                                enemyTypesToSelectFrom = HUGE_PAY_ENEMIES.getEnemies().stream().filter(it -> currentMap.getPossibleItemsId().containsValue(it.getId())).collect(Collectors.toList());
                                break;
                            case 3:
                                enemyTypesToSelectFrom = EnemyRange.getEnemiesFromRanges(LOW_PAY_ENEMIES, MID_PAY_ENEMIES, HIGH_PAY_ENEMIES).stream().filter(it -> currentMap.getPossibleItemsId().containsValue(it.getId())).collect(Collectors.toList());
                                break;
                        }

                        EnemyType targetEnemyType = enemyTypesToSelectFrom.get(RNG.nextInt(enemyTypesToSelectFrom.size()));
                        idEnemy = currentMap.getPossibleItemsId().entrySet().stream().filter(it -> it.getValue().equals(targetEnemyType.getId())).findFirst().get().getKey();

                        if (!allowedEnemies.contains(idEnemy)) {
                            throw new RuntimeException("Selected Enemy " + idEnemy + " does not exist in the allowedEnemies list");
                        }
                    } else {
                        idEnemy = allowedEnemies.get(RNG.nextInt(allowedEnemies.size()));
                    }
                    itemById = currentMap.getItemById(idEnemy);
                    if (SPECIAL_ITEMS.contains(itemById.getEnemyType())) {
                        itemById = gameState.generatePredefinedEnemies(true, itemById, itemById.getEnemyType());
                    }
                    if (itemById.getEnemyType().getId() == 100) {
                        int skinId = MathData.getRandomBossType(config).getSkinId();
                        BossParams bossParams = config.getBosses().get(skinId);
                        int defeatTresHold = RNG.nextInt(bossParams.getMinPay(), bossParams.getMaxPay());
                        itemById.setSkin(skinId);
                        itemById.setEnergy(defeatTresHold);
                    }
                }

                EnemyType enemyType = itemById.getEnemyClass().getEnemyType();

                seat.setWeapon(-1);
                try {
                    int currentWeaponId = seat.getCurrentWeaponId();
                    IShot shot = toFactoryService.createShot(System.currentTimeMillis(), 0, currentWeaponId, idEnemy, 0, 0, false);
                    gameState.processShot(System.currentTimeMillis(), seat, shot, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.print(e.getLocalizedMessage());
                }
                cnt++;
            }
            System.out.println("-----------roundIsFinished:  " + roundIsFinished + System.nanoTime());
            needFinish = true;// Added so that round can finish

            roundIsStarted = false;
            TimeMeasure.getInstance().addMeasure("One Room played", System.currentTimeMillis() - s1);
        }

        if (needSaveToFile) {
            pwh.close();
        }
    }

    public static String getDebugFileName(String idEnemiesForShooting, int coinInCents, int cntRealShotsRequired, int betLevel) {
        String name_ = TestModel.class.getPackage().getName();
        String gameName = name_.substring(name_.lastIndexOf(".") + 1);
        return gameName + "_" //+ System.currentTimeMillis() + "_"
                + idEnemiesForShooting + "_"
                + coinInCents + "_"
                + cntRealShotsRequired + "_"
                + betLevel + "_"
                + ".txt";
    }

    private void updateRealShots(GameConfig config, Money stake, AtomicLong realShots, PlayerRoundInfo currentPlayerRoundInfo) {
        Map<String, EnemyStat> statByEnemies = currentPlayerRoundInfo.getStatByEnemies();
        for (Map.Entry<String, EnemyStat> statEntry : statByEnemies.entrySet()) {
            int betLevelEnemy = Integer.parseInt(statEntry.getKey().split("_")[2]);
            Map<String, WeaponStat> specialWeaponsStats = statEntry.getValue().getSpecialWeaponsStats();
            for (Map.Entry<String, WeaponStat> weaponStatEntry : specialWeaponsStats.entrySet()) {
                Money payBets = weaponStatEntry.getValue().getPayBets();
                if (payBets.greaterThan(Money.ZERO)) {
                    SpecialWeaponType weaponType = SpecialWeaponType.getByTitle(weaponStatEntry.getKey());
                    Integer price = MathData.getPaidWeaponCost(config, weaponType.getId());
                    long cntBets = payBets.divideBy(stake) / betLevelEnemy / price;
                    realShots.addAndGet(cntBets);
                }
            }
        }
    }

    private void checkEnemiesInRoom(Set<Integer> enemiesForShooting, int numberOfEnemies,
                                    GameMap currentMap, PlayGameState gameState) {
        newEnemies.clear();
        Map<Long, Integer> itemsId = currentMap.getPossibleItemsId();
        GameMap map = gameState.getMap();
        for (Integer enemyTypeId : enemiesForShooting) {
            int cntLiveEnemies = 0;
            if (gameState.isAllowSpawn()) {
                for (Map.Entry<Long, Integer> entry : itemsId.entrySet()) {
                    if (entry.getValue().equals(enemyTypeId)) {
                        cntLiveEnemies++;
                    }
                    if (!enemiesForShooting.contains(entry.getValue())) {
                        currentMap.removeItem(entry.getKey());
                    }
                }
                EnemyType enemyType = EnemyType.getById(enemyTypeId);
                int realNumberOfEnemies = EnemyRange.SPECIAL_ITEMS.getEnemies().contains(enemyType) ? 1 : numberOfEnemies;
                while (cntLiveEnemies++ < realNumberOfEnemies) {
                    Enemy baseEnemy = map.createEnemy(enemyType, 1, getRandomTrajectory(),
                            4, null, -1);
                    baseEnemy.setEnergy(baseEnemy.getFullEnergy());
                    // System.out.println("Add Enemy " + baseEnemy.getEnemyClass().getEnemyType().getName());
                    newEnemies.add(baseEnemy);
                }
            } else {
                map.removeAllEnemies();
                gameState.setRemainingNumberOfBoss(0);
            }
        }
        if (!newEnemies.isEmpty()) {
            map.addEnemiesToMap(newEnemies);
            newEnemies.clear();
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

    private GameRoom getCurrentRoom(GameConfig config, ISingleNodeRoomInfo roomInfo, List<Seat> seats, GameMap currentMap, SpawnConfig spawnConfig) {
        TestApplicationContext ctx = TestApplicationContext.createContextWithStubBeans(new StubSocketService() {
            @Override
            public Mono<IAddWinResult> addWin(int serverId, String sessionId, long gameSessionId,
                                              Money winAmount, Money returnedBet, long roundId, long roomId,
                                              long accountId, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) {
                synchronized (seatStats) {
                    try {
                        if (fullDebug) {
                            System.out.println("playerBet.getData(): " + playerBet.getData());
                            printLog("playerBet bet: " + playerBet.getBet() + " win: " + playerBet.getWin()
                                    + " RTP: " + playerBet.getWin() / playerBet.getBet());
                        }
                        PlayerRoundInfo roundInfo = PlayerRoundInfo.getPlayerRoundInfoFromVBAData(playerBet.getData(),
                                playerBet.getBet(), playerBet.getWin());
                        long cntToBoss = roundInfo.getStatBoss().getCntToBoss();
                        if (cntToBoss > maxBosKills) maxBosKills = cntToBoss;
                        if (cntToBoss < minBosKills) minBosKills = cntToBoss;
                        totalPlayerRoundsInfo.addRoundInfo(roundInfo);
//                        System.out.println(totalPlayerRoundsInfo.getRTPStatData(838));
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
            public IAddWinResult addWinSync(int serverId, String sessionId, long gameSessionId, Money winAmount, Money returnedBet, long roundId, long roomId, long accountId, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) {
                IAddWinResult iAddWinResult = super.addWinSync(serverId, sessionId, gameSessionId, winAmount, returnedBet, roundId, roomId, accountId, playerBet, bgRoundInfo);

                return iAddWinResult;
            }

            @Override
            public Mono<IAddWinResult> addWinWithSitOut(int serverId, String sessionId, long gameSessionId, Money winAmount, Money returnedBet, long roundId, long roomId, long accountId, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo, long gameId, long bankId, boolean sitOut) {
                Mono<IAddWinResult> iAddWinResultMono = super.addWinWithSitOut(serverId, sessionId, gameSessionId, winAmount, returnedBet, roundId, roomId, accountId, playerBet, bgRoundInfo, gameId, bankId, sitOut);
                return iAddWinResultMono;
            }

            @Override
            public IFrbCloseResult closeFRBonusAndSession(int serverId, long accountId, String sessionId,
                                                          long gameSessionId, long gameId, long bonusId,
                                                          long winSum) {
                return null;
            }

            @Override
            public Boolean savePlayerBetForFRB(int serverId, String sessionId, long gameSessionId, long roundId,
                                               long accountId, IPlayerBet playerBet) {
                return null;
            }

            @Override
            public IActiveCashBonusSession saveCashBonusRoundResult(long gameId, ISeat seat,
                                                                    IActiveCashBonusSession bonus,
                                                                    IPlayerProfile profile, Set<IQuest> allQuests,
                                                                    Map<Long, Map<Integer, Integer>> weapons,
                                                                    IPlayerBet playerBet, long roundid) {
                return null;
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

            @Override
            public <T extends IStartNewRoundResult> List<T> startNewRoundForManyPlayers(List<ISeat> seats, long roomId, long roomRoundId, long roundStartDate, boolean battlegroundRoom, long stakeOrBuyInAmount) throws Exception {
                List<IStartNewRoundResult> iStartNewRoundResults = super.startNewRoundForManyPlayers(seats, roomId, roomRoundId, roundStartDate, battlegroundRoom, stakeOrBuyInAmount);
                roundIsStarted = true;
                return (List<T>) iStartNewRoundResults;
            }

            @Override
            public ITournamentSession saveTournamentRoundResult(long gameId, ISeat seat, ITournamentSession tournament, IPlayerProfile profile, Set<IQuest> allQuests, Map<Long, Map<Integer, Integer>> weapons, IPlayerBet playerBet, long roundId) {
                return null;
            }

            @Override
            public Set<ICrashGameSetting> getCrashGameSetting(Set<Long> bankIds, int gameId) {
                return null;
            }
        }, seats);
        return new GameRoom(ctx, new EmptyLogger(), roomInfo, currentMap,
                new StubPlayerStatsService(), new StubWeaponService(),
                null, null, new StubPlayerProfileService(),
                null, null, null, null,
                new StubGameConfigProvider(config), new StubSpawnConfigProvider(spawnConfig));
    }

    private static void init() {
        LongIdGenerator.getInstance().init(new StubSequencerPersister());
    }

    public void printLog(String message) {
        if (pwh != null) {
            pwh.println(message);
            pwh.flush();
        } else {
            System.out.println(message);
        }
    }

    public String toMoney(long cents) {
        return new BigDecimal(cents / 100.).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
