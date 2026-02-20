package com.betsoft.casino.mp.pirates;

import com.betsoft.casino.mp.common.AdditionalWeaponStat;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.common.math.TimeMeasure;
import com.betsoft.casino.mp.common.testmodel.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.quests.ITreasureProgress;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.pirates.model.*;
import com.betsoft.casino.mp.pirates.model.math.EnemyRange;
import com.betsoft.casino.mp.pirates.model.math.EnemyType;
import com.betsoft.casino.mp.pirates.model.math.MathData;
import com.betsoft.casino.mp.pirates.model.math.Treasure;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.dgphoenix.casino.common.util.LongIdGenerator;
import com.dgphoenix.casino.common.util.RNG;
import com.google.gson.Gson;
import reactor.core.Disposable;
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
import java.util.concurrent.locks.ReentrantLock;

import static com.betsoft.casino.mp.common.testmodel.StubSocketService.getConnection;

@SuppressWarnings("rawtypes")
public class TestModel {
    final ConcurrentHashMap<Long, SeatStat> seatStats = new ConcurrentHashMap<>();
    private long totalBet = 0;
    private long totalWin = 0;
    private long totalBetsSpecialWeapons = 0;
    private long totalBetsSpecialWeaponsCount = 0;
    PrintWriter pwh;
    boolean fullDebug = false;
    public static GameType gameType = GameType.PIRATES;
    boolean roundIsFinished;
    boolean roundIsStarted;
    long lastRealShotChangeTime;
    long lastRealShot = -1;
    long totalFinishedRooms = 0;
    long lastLogTime;
    long minBosKills = 10000;
    long maxBosKills = 0;
    PlayerRoundInfo totalPlayerRoundsInfo = new PlayerRoundInfo(-1);

    public static void main(String[] args) throws Exception {
        init();
        TestModel testModel = new TestModel();
        boolean debug = true;
        int cntPlayers = 1;
        boolean allowBuyInSpecialWeapons = false;
        String lootBox = "gold";
        double probBuySpecialWeapon = 0.3; // 0-1
        boolean allowSpecialWeaponsShoots = true;
        boolean shootFromSpecialWeaponsOnly = false;
        boolean needSaveToFile = false;
        String idEnemiesForShooting = null;
        int numberOfBoss = 0;
        int numberOfEnemies = 50;
        int numberOfBuyWeaponsOnStartRound = RNG.nextInt(300) + 50;
        int enemyLevel = -1;
        int coinInCents = 2;
        int cntRealShotsRequired = 100000;
        boolean fullDebug = false;
        for (String arg : args) {
            String[] param_ = arg.split("=");
            switch (param_[0]) {
                case "debug":
                    debug = param_[1].equals("true");
                    break;
                case "cntPlayers":
                    cntPlayers = Integer.parseInt(param_[1]);
                    break;
                case "lootBox":
                    lootBox = param_[1];
                    break;
                case "allowBuyInSpecialWeapons":
                    allowBuyInSpecialWeapons = param_[1].equals("true");
                    break;
                case "allowSpecialWeaponsShoots":
                    allowSpecialWeaponsShoots = param_[1].equals("true");
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
                case "numberOfBoss": {
                    numberOfBoss = Integer.parseInt(param_[1]);
                    break;
                }
                case "numberOfEnemies": {
                    numberOfEnemies = Integer.parseInt(param_[1]);
                    break;
                }
                case "numberOfBuyWeaponsOnStartRound": {
                    numberOfBuyWeaponsOnStartRound = Integer.parseInt(param_[1]);
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
                case "fullDebug":
                    fullDebug = param_[1].equals("true");
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
                allowBuyInSpecialWeapons,
                probBuySpecialWeapon,
                lootBox,
                allowSpecialWeaponsShoots,
                enemiesForBaseShooting,
                shootFromSpecialWeaponsOnly,
                needSaveToFile, idEnemiesForShooting, numberOfBoss,
                numberOfEnemies, numberOfBuyWeaponsOnStartRound,
                enemyLevel, coinInCents, cntRealShotsRequired, fullDebug
        );
    }

    public void doTestRound(boolean debug, Set<EnemyType> enemiesForSpecialWeapons, int cntPlayers,
                            boolean allowBuyInSpecialWeapons, double probBuySpecialWeapon, String lootBox,
                            boolean allowSpecialWeaponsShoots, Set<Integer> enemiesForShooting,
                            boolean shootFromSpecialWeaponsOnly, boolean needSaveToFile,
                            String idEnemiesForShooting, int numberOfBoss, int numberOfEnemies,
                            int numberOfBuyWeaponsOnStartRound, int enemyLevel, int coinInCents,
                            int cntRealShotsRequired, boolean fullDebug_
    ) throws Exception {
        StubRoomTemplate stubRoomTemplate = new StubRoomTemplate(1, 271, gameType,
                (short) 6, (short) 1, MoneyType.REAL, 100, 100, 1, 1,
                1, 1, "Test Room", 5);
        String name_ = this.getClass().getPackage().getName();
        String gameName = name_.substring(name_.lastIndexOf(".") + 1);
        String fileStatsName = gameName + "_" + System.currentTimeMillis() + "_"
                + allowBuyInSpecialWeapons + "_"
                + allowSpecialWeaponsShoots + "_"
                + idEnemiesForShooting + "_"
                + lootBox + "_"
                + coinInCents + "_"
                + cntRealShotsRequired + "_"
                + ".txt";
        System.out.println(fileStatsName);
        fullDebug = fullDebug_;
        if (needSaveToFile)
            pwh = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileStatsName), "UTF-8"));
        printLog("mode: allowBuyInSpecialWeapons: " + allowBuyInSpecialWeapons);
        printLog("mode: probBuySpecialWeapon: " + probBuySpecialWeapon);
        printLog("mode: lootBox: " + lootBox);
        printLog("mode: allowSpecialWeaponsShoots: " + allowSpecialWeaponsShoots);
        printLog("mode: shootFromSpecialWeaponsOnly: " + shootFromSpecialWeaponsOnly);
        printLog("mode: needSaveToFile: " + needSaveToFile);
        printLog("mode: optimal enemyLevel: " + enemyLevel);
        printLog("mode: optimal numberOfEnemies: " + numberOfEnemies);
        printLog("mode: optimal numberOfBoss: " + numberOfBoss);
        printLog("mode: coinInCents: " + coinInCents);
        printLog("mode: cntRealShotsRequired: " + cntRealShotsRequired);
        printLog("mode: numberOfBuyWeaponsOnStartRound: " + numberOfBuyWeaponsOnStartRound);
        int boxId = 0;
        if (!lootBox.isEmpty() && lootBox.equals("gold")) boxId = 2;
        else if (!lootBox.isEmpty() && lootBox.equals("silver")) boxId = 1;
        else if (!lootBox.isEmpty() && lootBox.equals("bronze")) boxId = 0;
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
        int stakeReserved = 300;
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
        currentMap = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(201));
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
                    new StubCurrency("USD", "$"),
                    new StubPlayerStatsService().load(271, gameType.getGameId(), 1), false,
                    null,
                    null, stake.toCents(),
                    10,
                    0.02, MaxQuestWeaponMode.LOOT_BOX, false);
            StubGameSocketClient client = new StubGameSocketClient(accountId, 271L, "", getConnection(),
                    new StubGsonMessageSerializer(new Gson()), gameType);
            Seat seat_ = new Seat(roomPlayerInfo, client, 1);
            StubPlayerQuests playerQuestsForStake = new StubPlayerQuests(new HashSet<>());
            int idxQuestForCoin = 0;
            int[] wins = {20, 50, 150};
            for (int idx = 1; idx <= 3; idx++) {
                ArrayList<ITreasureProgress> treasureProgresses = new ArrayList<>();
                treasureProgresses.add(new StubTreasureProgress(idx, 0, 3));
                StubQuestPrize questPrize = new StubQuestPrize(new StubQuestAmount(wins[idx - 1], wins[idx - 1]), -1);
                name_ = Treasure.getById(idx).name();
                StubQuest newQuest = new StubQuest(idxQuestForCoin++, 1, stake.toCents(), false, 0,
                        new StubQuestProgress(treasureProgresses), questPrize, name_);
                playerQuestsForStake.getQuests().add(newQuest);
            }
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
            int cntBuyForRound = 500;
            long s1 = System.currentTimeMillis();
            boolean seatIBuyWeaponOnStartRound = false;
            ITransportObjectsFactoryService toFactoryService = gameState.getRoom().getTOFactoryService();
            while (currentRoom.getGameState().getRoomState().equals(RoomState.PLAY)) {
                gameState.setPauseTime(-9999);
                Seat seat = seats.get(RNG.nextInt(seats.size()));
                if (isLongTimeWithoutActivity()) {
                    System.out.println(" -----------------------------------------------------------------------------");
                    System.out.println("currentRoom: " + currentRoom);
                    System.out.println("gameState: " + gameState);
                    System.out.println("seat: " + seat);
                }
                AtomicLong realShots = new AtomicLong(0);
                synchronized (seatStats) {
                    totalPlayerRoundsInfo.getHitMissStatByWeapons().forEach((wpId, aws) -> {
                        boolean isSWOnly = shootFromSpecialWeaponsOnly && wpId != -1;
                        boolean isPaidOnly = !shootFromSpecialWeaponsOnly && wpId == -1;
                        if (isSWOnly || isPaidOnly)
                            realShots.addAndGet(aws.getNumberOfRealShots() - aws.getNumberOfKilledMiss());
                    });
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
                    } catch (Exception e) {
                    }
                    long currentRealShots = realShots.get();
                    if (lastRealShot != currentRealShots) {
                        lastRealShot = currentRealShots;
                        lastRealShotChangeTime = System.currentTimeMillis();
                    }
                    if (currentRealShots % 10000 == 0 && !needFinish) {
                        long tBet = totalBet + currentBets + currentBetsSW;
                        long tWin = totalWin + currentWins;
                        long timeOfLogs = System.currentTimeMillis() - lastLogTime;
                        lastLogTime = System.currentTimeMillis();
                        if (tBet > 0 && tWin > 0 && currentRealShots > 0) {
                            String message = fullDebug ? (timeOfLogs / 1000 + " ") : "";
                            message = message + "realShots: " + realShots
                                    + " RTP: " + new BigDecimal((double) tWin / tBet).multiply(BigDecimal.valueOf(100))
                                    .setScale(4, RoundingMode.HALF_UP)
                                    + " totalBet: " + toMoney(totalBet)
                                    + " totalWin: " + toMoney(totalWin)
                                    + " currentWins: " + toMoney(currentWins)
                                    + " currentBetsSW: " + toMoney(currentBetsSW)
                                    + " currentBets: " + toMoney(currentBets);
                            printLog(message);
                            double percent = ((double) currentRealShots / cntRealShotsRequired) * 100;
                            int iPercent = (int) percent;
                            if (percent == iPercent && iPercent % 3 == 0) {
                                System.out.print(" " + percent + "%");
                            }
                        }
                    }
                }
                if (isLongTimeWithoutActivity()) {
                    System.out.println("realShots.get(): " + realShots.get()
                            + " cntRealShotsRequired: " + cntRealShotsRequired
                            + " needFinish: " + needFinish
                            + " roundIsFinished: " + roundIsFinished
                            + " roundIsStarted: " + roundIsStarted
                    );
                }
                if (realShots.get() >= cntRealShotsRequired) {
                    needFinish = true;
                    continue;
                }
                if (seat.getAmmoAmount() == 0 || seat.getAmmoAmountTotalInRound() == 0) {
                    // printLog("buy shots");
                    IActionRoomPlayerInfo playerInfo = seat.getPlayerInfo();
                    seat.setAmmoAmount(0);
                    seat.incrementAmmoAmount(stakeReserved);
                    seat.incrementTotalAmmoAmount(stakeReserved);
                    playerInfo.makeBuyIn(1, stake.multiply(stakeReserved).toCents());
                    playerInfo.setPendingOperation(false);
                    playerInfo.incrementBuyInCount();
                    //seat.setBalance(100000000000L);
                    seat.updatePlayerRoundInfo(1);
                    seat.setPlayerInfo(playerInfo);
                }
                if (!seatIBuyWeaponOnStartRound) {
                    for (int i = 0; i < numberOfBuyWeaponsOnStartRound; i++) {
                        buyWeapon(boxId, currentRoom, seat);
                    }
                    seatIBuyWeaponOnStartRound = true;
                }
                if (cnt > limit) {
                    if (fullDebug)
                        printLog("gameState.isRoundWasFinished(): " + gameState.isRoundWasFinished());
                    if (gameState.isRoundWasFinished()) {
                        gameState.setRoundWasFinished(false);
                    }
                    Disposable updateTimer = currentRoom.getUpdateTimer();
                    if (updateTimer != null) {
                        if (fullDebug)
                            System.out.println("getUpdateTimer room " + updateTimer + " updateTimer.isDisposed(): "
                                    + updateTimer.isDisposed()
                            );
                    }
                    gameState.doFinishWithLock();
                    if (fullDebug)
                        System.out.println("finish room " + new Date(System.currentTimeMillis())
                                + " totalFinishedRooms: " + totalFinishedRooms++);
                    break;
                }
                checkEnemiesInRoom(enemiesForShooting, numberOfBoss, numberOfEnemies, enemyLevel, currentMap, gameState);
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
                if (((allowBuyInSpecialWeapons || shootFromSpecialWeaponsOnly) && RNG.rand() < probBuySpecialWeapon)) {
                    if (noWeapons.get()) {
                        if (cntBuyForRound > 0) {
                            buyWeapon(boxId, currentRoom, seat);
                            cntBuyForRound--;
                        }
                    }
                }
                Long idEnemy;
                Enemy itemById;
                if (allowedEnemies.isEmpty()) {
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
                if (shootFromSpecialWeaponsOnly && seat.getSpecialWeaponId() == -1) {
                    if (noWeapons.get()) {
                        if (cntBuyForRound > 0) {
                            buyWeapon(boxId, currentRoom, seat);
                            cntBuyForRound--;
                        }
                        choiceWeapon(seat);
                    }
                }
                if (shootFromSpecialWeaponsOnly && seat.getCurrentWeaponId() == -1)
                    continue;
                int notUsedMineShoots = seat.getWeapons().get(SpecialWeaponType.Landmines).getShots();
                try {
                    if (notUsedMineShoots > 0 && seat.getSeatMines().isEmpty() && allowSpecialWeaponsShoots) {
                        seat.setWeapon(SpecialWeaponType.Landmines.getId());
                        checkEnemiesInRoom(enemiesForShooting, numberOfBoss, numberOfEnemies, enemyLevel, currentMap, gameState);
                        float x = (float) ((RNG.rand() * 200) + 250);
                        float y = (float) ((RNG.rand() * 200) + 150);
                        try {
                            gameState.placeMineToMap(seat, new StubMineCoordinates(System.currentTimeMillis(), cnt, x, y,
                                    false));
                        } catch (Exception e) {
                        }
                        Thread.sleep(2);
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(" error place mine,  " + e.getLocalizedMessage());
                }
                try {
                    IShot shot = toFactoryService.createShot(System.currentTimeMillis(), 0, seat.getCurrentWeaponId(),
                            idEnemy, 0, 0, false);
                    if (isLongTimeWithoutActivity()) {
                        ReentrantLock lockShots = gameState.getLockShots();
                        System.out.println("try make shot: " + shot + " lockShots.isLocked(): " + lockShots.isLocked()
                                + " getTimeFromLastShotTime:{} " + gameState.getTimeFromLastShotTime());
                        if (lockShots.isLocked()) {
                            lockShots.unlock();
                        }
                    }
                    gameState.processShot(seat, shot, false);
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
        printLog("---totalBets of buying of SW: " + toMoney(totalBetsSpecialWeapons));
        printLog("---number of purchases of shots: " + totalBetsSpecialWeaponsCount);
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
        if (fullDebug)
            System.out.println("totalFinishedRooms: " + totalFinishedRooms);
        if (needSaveToFile)
            pwh.close();
    }

    private void checkEnemiesInRoom(Set<Integer> enemiesForShooting, int numberOfBoss, int numberOfEnemies,
                                    int enemyLevel, GameMap currentMap, PlayGameState gameState) {
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
                int limitEnemy = isBoss ? numberOfBoss : numberOfEnemies;
                if (gameState.isAllowSpawn()) {
                    while (cntLiveEnemies++ < limitEnemy) {
                        Enemy enemy = gameState.getMap().createEnemy(enemyType, 1, getRandomTrajectory(),
                                4, mathEnemy, -1);
                        if (isBoss)
                            enemy.setSkin(realBossEnemyLevel + 1);
                        enemy.setEnergy(enemy.getFullEnergy());
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

    private void buyWeapon(int boxId, GameRoom currentRoom, Seat seat) {
        int size = boxId == -1 ? RNG.nextInt(3) : boxId;
        Money weaponLootBoxPrice = currentRoom.getWeaponLootBoxPrice(seat, size);
        currentRoom.getPlayerInfoService().lock(seat.getAccountId());
        try {
            if (currentRoom.getGameState().isBuyInAllowed(seat)) {
                IWeaponLootBox iWeaponLootBox = currentRoom.generateWeaponLootBox(seat, 0, size, 0, Money.ZERO);
                if (iWeaponLootBox != null) {
                    totalBetsSpecialWeaponsCount++;
                    long totalBetsSpecialWeapons = (long) weaponLootBoxPrice.toDoubleCents();
                    this.totalBetsSpecialWeapons += totalBetsSpecialWeapons;
                }
            }
        } finally {
            currentRoom.getPlayerInfoService().unlock(seat.getAccountId());
        }
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
            public Mono<IAddWinResult> addWin(int serverId, String sessionId, long gameSessionId,
                                              Money winAmount, Money returnedBet, long roundId, long roomId,
                                              long accountId, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) {
                synchronized (seatStats) {
                    SeatStat seatStat = seatStats.get(playerBet.getAccountId());
                    try {
                        //System.out.println(playerBet);
                        PlayerRoundInfo roundInfo = PlayerRoundInfo.getPlayerRoundInfoFromVBAData(playerBet.getData(),
                                playerBet.getBet(), playerBet.getWin());
//                        seatStat.updateStat(roundInfo);
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
                null, null,
                new StubPlayerProfileService(), null, null,
                null, null);
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

    boolean isLongTimeWithoutActivity() {
        if (fullDebug)
            return lastRealShotChangeTime != 0 && System.currentTimeMillis() - lastRealShotChangeTime > 300000;
        else
            return false;
    }
}
