package com.betsoft.casino.bots;

import com.betsoft.casino.bots.handlers.*;
import com.betsoft.casino.bots.model.RicochetBullet;
import com.betsoft.casino.bots.model.Turret;
import com.betsoft.casino.bots.model.TurretPositions;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.bots.requests.*;
import com.betsoft.casino.bots.strategies.IRoomBotStrategy;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.util.Pair;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static com.betsoft.casino.bots.utils.GeometryUtils.getDistance;

public class RoomBot extends AbstractBot implements IRoomBot {

    public static final int SCREEN_WIDTH = 960;
    public static final int SCREEN_HEIGHT = 540;

    //double CROSSHAIR_POSITION_ACCURACY = 0.01;
    public static final int SCREEN_PADDING_LEFT = 19;
    public static final int SCREEN_PADDING_RIGHT = 20;
    public static final int SCREEN_PADDING_BOTTOM = 2 * 35;
    public static final int SCREEN_PADDING_TOP = 2 * 29;

    public static final int SCREEN_ADJUSTED_LEFT = 0 + SCREEN_PADDING_LEFT;
    public static final int SCREEN_ADJUSTED_RIGHT = SCREEN_WIDTH - SCREEN_PADDING_RIGHT;
    public static final int SCREEN_ADJUSTED_TOP = 0 + SCREEN_PADDING_TOP;
    public static final int SCREEN_ADJUSTED_BOTTOM = SCREEN_HEIGHT - SCREEN_PADDING_BOTTOM;


    protected LobbyBot lobbyBot;
    private String nickname;
    private long balance;
    private float stake;
    protected int activeWeaponId;
    protected int currentBeLevel;

    protected IRoomEnemy focusedEnemy = null;
    protected volatile BotState state = BotState.IDLE;
    private long lastStateChangeDate = System.currentTimeMillis();

    private long roomId;
    protected IGetRoomInfoResponse roomInfo;
    protected List<RoomEnemy> enemies = new ArrayList<>();
    protected IRoomBotStrategy strategy;
    protected ReentrantLock enemiesLock = new ReentrantLock();
    protected int serverAmmo;
    protected int seatId = -1;
    protected AtomicBoolean scheduledDoSleepAction = new AtomicBoolean(false);
    protected int maxBulletsOnMap = 0;
    private final AtomicInteger bulletId = new AtomicInteger();
    protected long lastReceivedServerTime;
    protected long diffLocalAndServerTime;
    protected boolean roundFinishSoonReceived;
    protected boolean roundResultReceived;
    protected boolean qualifyStateReceived;

    private int currentMapId;
    private String currentSubround;
    protected boolean pendingOperation;

    public RoomBot(LobbyBot lobbyBot, String id, String url, int serverId, String sessionId, IMessageSerializer serializer,
                   long roomId, long balance, float stake, String nickName, IRoomBotStrategy strategy,
                   Function<Void, Integer> shutdownCallback, Function<Void, Integer> startCallback) {
        super(id, url, serverId, lobbyBot.getBankId(), sessionId, serializer, shutdownCallback, startCallback);
        this.lobbyBot = lobbyBot;
        this.roomId = roomId;
        this.balance = balance;
        this.stake = stake;
        this.nickname = nickName;
        this.strategy = strategy;
        this.activeWeaponId = -1;
        this.currentBeLevel = 1;
        if (roomInfo != null && roomInfo.getRoomEnemies() != null) {
            enemies = roomInfo.getRoomEnemies();
        }
        this.serverAmmo = 0;
        GameType gameType = GameType.getByGameId(lobbyBot.getGameId());
        this.maxBulletsOnMap = gameType == null ? 0 : gameType.getMaxBulletsOnMap();
        roundResultReceived = false;
        roundFinishSoonReceived = false;
        qualifyStateReceived = false;
        currentMapId = 0;
        currentSubround = "";
        pendingOperation = false;
    }

    @Override
    protected void registerServerMessageHandlers() {
        serverMessageHandlers.put(SitInResponse.class, new SitInHandler(this));
        serverMessageHandlers.put(SitOutResponse.class, new SitOutHandler(this));
        serverMessageHandlers.put(GameStateChanged.class, new GameStateChangedHandler(this));
        serverMessageHandlers.put(NewEnemy.class, new NewEnemyHandler(this));
        serverMessageHandlers.put(NewEnemies.class, new NewEnemiesHandler(this));
        serverMessageHandlers.put(EnemiesMoved.class, new EnemiesMovedHandler(this));
        serverMessageHandlers.put(RoundResult.class, new RoundResultHandler(this));
        serverMessageHandlers.put(Error.class, new RoomErrorHandler(this));
        serverMessageHandlers.put(EnemyDestroyed.class, new EnemyDestroyedHandler(this));
        serverMessageHandlers.put(UpdateTrajectories.class, new UpdateTrajectoriesHandler(this));
        serverMessageHandlers.put(BalanceUpdated.class, new BalanceUpdatedHandler(this));
        serverMessageHandlers.put(Awards.class, new AwardsHandler(this));
        serverMessageHandlers.put(Weapons.class, new WeaponsHandler(this));
        serverMessageHandlers.put(WeaponSwitched.class, new WeaponSwitchedHandler(this));
        serverMessageHandlers.put(Hit.class, new HitHandler(this));
        serverMessageHandlers.put(Miss.class, new MissHandler(this));
        serverMessageHandlers.put(ChangeMap.class, new ChangeMapHandler(this));
        serverMessageHandlers.put(LevelUp.class, new LevelUpHandler(this));
        serverMessageHandlers.put(NewQuest.class, new NewQuestHandler(this));
        serverMessageHandlers.put(UpdateQuest.class, new UpdateQuestHandler(this));
        serverMessageHandlers.put(RemoveQuest.class, new RemoveQuestHandler(this));
        serverMessageHandlers.put(RoundFinishSoon.class, new RoundFinishSoonHandler(this));
        serverMessageHandlers.put(NewTreasure.class, new NewTreasureHandler(this));
        serverMessageHandlers.put(MinePlace.class, new MinePlaceHandler(this));
        serverMessageHandlers.put(SeatWinForQuest.class, new SeatWinForQuestHandler(this));
        serverMessageHandlers.put(BulletResponse.class, new BulletResponseHandler(this));
        serverMessageHandlers.put(ChangeEnemyMode.class, new ChangeEnemyModeHandler(this));
        serverMessageHandlers.put(BulletClearResponse.class, new BulletClearResponseHandler(this));
        serverMessageHandlers.put(BetLevelResponse.class, new BetLevelResponseHandler(this));
        serverMessageHandlers.put(PendingOperationStatus.class, new PendingOperationStatusHandler(this));
        serverMessageHandlers.put(CrashAllBetsRejectedDetailedResponse.class, new CrashAllBetsRejectedDetailedHandler(this));
        serverMessageHandlers.put(FullGameInfo.class, new FullGameInfoHandler(this));

    }

    public void resetRoundStatesOnPlay() {
        roundResultReceived = false;
        roundFinishSoonReceived = false;
        qualifyStateReceived = false;
    }

    @Override
    public void stop() {
        super.stop();
        this.seatId = -1;
        this.activeWeaponId = -1;
        this.serverAmmo = 0;
        this.roomId = 0;
        roundResultReceived = false;
        roundFinishSoonReceived = false;
        qualifyStateReceived = false;
        pendingOperation = false;
    }

    @Override
    public void restart() {
        roundResultReceived = false;
        roundFinishSoonReceived = false;
        qualifyStateReceived = false;
        getLogger().debug("restart: start");
        sleep(strategy.getWaitTime()).subscribe(t -> stop());
        //call start() not required, call start from lobbyBot.connectToRoom() after process GetStartGameUrl
        getLogger().debug("restart: end");
    }

    protected List<RoomEnemy> getEnemies() {
        return new ArrayList<>(enemies);
    }

    public RoomEnemy getEnemy(long enemyId) {
        if(enemies == null || enemies.isEmpty()) {
            return null;
        }

        return enemies.stream()
                .filter( e-> e.getId() == enemyId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public long getBalance() {
        return balance;
    }

    @Override
    public IRoomBotStrategy getStrategy() {
        return strategy;
    }

    @Override
    public void setState(BotState state, String reason) {
        lock.lock();
        try {
            getLogger().debug("bot: {}, setState: {}, reason={}", getId(), state, reason);
            if (this.state != state) {
                lastStateChangeDate = System.currentTimeMillis();
            }
            this.state = state;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long getLastStateChangeDate() {
        return lastStateChangeDate;
    }

    @Override
    public BotState getState() {
        return state;
    }

    @Override
    protected void sendInitialRequest() {
        setState(BotState.IDLE, "sendInitialRequest");
        doAction("sendInitialRequest");
    }

    public boolean isPendingOperation() {
        return pendingOperation;
    }

    public void setPendingOperation(boolean pendingOperation) {
        this.pendingOperation = pendingOperation;
    }

    protected void doAction(String debugInfo) {
        getLogger().debug("doAction: bot: {}, getSeatId(): {}, debugInfo={}, state: {}", getId(), getSeatId(), debugInfo,
                getState());
        scheduledDoSleepAction.set(false);
        boolean needSleepAndRetry = true;
        switch (getState()) {
            case WAITING_FOR_RESPONSE:
                break;
            case IDLE:
                openRoom();
                needSleepAndRetry = false;
                break;
            case OBSERVING:
                RoomState roomState = getRoomState();
                // TODO: Add check for boss round
                if (RoomState.WAIT.equals(roomState) || (RoomState.PLAY.equals(roomState))) {
                    sendSitInRequest(0);
                    needSleepAndRetry = false;
                }
                break;
            case PLAYING:
                getLogger().debug("doAction: currentBeLevel: {}, strategy.requestedBetLevel(): {}, serverAmmo: {}",
                        currentBeLevel, strategy.requestedBetLevel(), getServerAmmo());

                if (!strategy.botHasSpecialWeapons() && currentBeLevel != strategy.requestedBetLevel()) {
                    send(new BetLevelRequest(this, client));
                    needSleepAndRetry = false;
                } else if (strategy.shouldPurchaseWeaponLootBox()) {
                    setState(BotState.WAITING_FOR_RESPONSE, "purchaseLootBox");
                    send(new PurchaseWeaponLootBoxRequest(this, client));
                    needSleepAndRetry = false;
                } else if (strategy.shouldSwitchWeapon()) {
                    int weaponId = strategy.getWeaponId();
                    setState(BotState.WAITING_FOR_RESPONSE, "switchWeapon, weaponId:  " + weaponId);
                    send(new SwitchWeaponRequest(this, client, weaponId));
                    needSleepAndRetry = false;
                } else if (strategy.shouldPurchaseBullets() && getState() == BotState.PLAYING && RoomState.PLAY.equals(getRoomState())) {
                    int weaponId = strategy.getWeaponId();
                    int shotsForWeapon = strategy.getShotsForWeapon(-1);
                    int shots = strategy.getShots();
                    getLogger().debug("doAction: weaponId: {}, shotsForWeapon: {}, shots: {}", weaponId,
                            shotsForWeapon, shots);
                    sendBuyInRequest(0);
                    needSleepAndRetry = false;
                } else if (strategy.shouldShoot(getId()) && RoomState.PLAY.equals(getRoomState())) {
                    boolean shotSended = shot();
                    if (shotSended) {
                        needSleepAndRetry = false;
                    } else {
                        getLogger().debug("doAction: shot is not sended, schedule next");
                    }
                }
                break;
        }
        if (needSleepAndRetry && started) {
            sleep(strategy.getWaitTime()).subscribe(t -> doAction("from doAction()"));
        }
    }

    public void doActionWithSleep(String debugInfo) {
        doActionWithSleep(strategy.getWaitTime(), debugInfo);
    }

    public void doActionWithSleep(long waitTime, String debugInfo) {
        doActionWithSleep(waitTime, debugInfo, true);
    }

    public void doActionWithSleep(long waitTime, String debugInfo, boolean logDebug) {
        lock.lock();
        try {
            if (scheduledDoSleepAction.get()) {
                getLogger().warn("doActionWithSleep: found already scheduled doAction: debugInfo={}", debugInfo);
            } else if (started) {

                sleep(waitTime)
                        .subscribe(t ->
                                doAction(debugInfo)
                        );

                scheduledDoSleepAction.set(true);
                if (logDebug) {
                    getLogger().debug("doActionWithSleep: scheduled doAction with sleep={}, debugInfo={}", waitTime, debugInfo);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setDefaultWeapon() {
        getLogger().debug("setDefaultWeapon");
        send(new SwitchWeaponRequest(this, client, -1));
    }

    protected void openRoom() {
        if (roomId > 0) {
            setState(BotState.WAITING_FOR_RESPONSE, "openRoom");
            send(new OpenRoomRequest(this, client, roomId, sessionId, serverId, MoneyType.REAL, "en"));
        } else {
            //this may be normal, roomId set by LobbyBot after cal GetStartGameUrl
            getLogger().warn("Cannot send OpenRoom request, roomId=0");
        }
    }

    @Override
    public void sendSitInRequest(int failedCount) {
        int ammo = strategy.getBuyInAmmoAmount(balance, stake, (int) (roomInfo.getMinBuyIn() / stake));
        if (ammo >= 0) {
            setState(BotState.WAITING_FOR_RESPONSE, "sendSitInRequest");
            send(new SitInRequest(this, client, ammo, (long) stake, "en", failedCount));
        } else {
            getLogger().error("Wrong ammo count: {}", ammo);
        }
    }

    @Override
    public void sendBuyInRequest(int failedCount) {
        setState(BotState.WAITING_FOR_RESPONSE, "sendBuyInRequest");
        send(new BuyInRequest(this, client, 0, failedCount));
    }

    @Override
    protected void logResponse(TObject response) {
        // Too much spam in logs from this message
        if (!"EnemiesMoved".equals(response.getClassName())) {
            getLogger().debug("Response: {}", response);
        }
    }

    @Override
    public void setBalance(Long balance) {
        this.balance = balance;
    }

    @Override
    public void clearAmmo() {
        strategy.resetShots();
    }

    @Override
    public void addEnemy(IRoomEnemy enemy) {
        enemiesLock.lock();
        try {
            enemies.removeIf(roomEnemy -> roomEnemy.getId() == enemy.getId());
            enemies.add((RoomEnemy) enemy);
        } finally {
            enemiesLock.unlock();
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addEnemies(List<IRoomEnemy> newEnemies) {
        enemiesLock.lock();
        try {
            for (IRoomEnemy enemy : newEnemies) {
                enemies.removeIf(roomEnemy -> roomEnemy.getId() == enemy.getId());
                enemies.add((RoomEnemy) enemy);
            }
        } finally {
            enemiesLock.unlock();
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void removeEnemy(long enemyId) {
        enemiesLock.lock();
        try {
            enemies.remove(getRoomEnemy(enemyId));
        } finally {
            enemiesLock.unlock();
        }
    }

    @SuppressWarnings("rawtypes")
    private IRoomEnemy getRoomEnemy(long enemyId) {
        for (IRoomEnemy enemy : enemies) {
            if (enemy.getId() == enemyId) {
                return enemy;
            }
        }
        return null;
    }

    @Override
    public void setRoomEnemies(List<RoomEnemy> enemies) {
        enemiesLock.lock();
        try {
            this.enemies = enemies;
        } finally {
            enemiesLock.unlock();
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setRoomInfo(IGetRoomInfoResponse roomInfo) {
        this.roomInfo = roomInfo;
    }

    @Override
    public void setRoomInfo(ICrashGameInfo roomInfo) {
        throw new UnsupportedOperationException("ICrashGameInfo roomInfo unsupported for RoomBoot");
    }

    @Override
    public void sendCloseRoomRequest() {
        try {
            Thread.sleep(300);// exclude Too many requests
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        send(new CloseRoomRequest(this, client, roomInfo.getRoomId()));
    }

    @Override
    public void sendSitOutRequest() {
        send(new SitOutRequest(this, client));
    }

    @Override
    public void clearShotRequests() {
        requests.entrySet().removeIf(request -> request.getValue().getClass().equals(ShotRequest.class));
    }

    private void sendShotWithBullet(String currentBulletId, Integer weaponPrice, boolean isPaidShot, String metric) {


        if (maxBulletsOnMap > 0 && activeWeaponId < 0) {

            ShotRequest shotRequest = new ShotRequest(this, client, activeWeaponId,
                    focusedEnemy.getId(), isPaidShot, currentBulletId, weaponPrice,
                    0, 0, "");

            BulletRequest bulletRequest = new BulletRequest(this, client, currentBulletId,
                    shotRequest, activeWeaponId, metric);
            send(bulletRequest);
        }
    }

    protected void sendShot(float currentX, float currentY, Integer weaponPrice, boolean isPaidShot, String metric) {
        long enemyId = focusedEnemy == null ? 0 : focusedEnemy.getId();
        ShotRequest request = new ShotRequest(this, client, activeWeaponId, enemyId,
                isPaidShot, null, weaponPrice, currentX, currentY, metric);
        send(request);
    }

    protected void sendBullet(String currentBulletId, float currentX, float currentY, boolean isPaidShot, int weaponPrice,
                            ManagedBattleGroundRoomBot managedBattleGroundRoomBot, String metric) {
        Point endPoint = new Point(currentX, currentY, System.currentTimeMillis());
        Point startPoint = Turret.getMuzzleTipPoint(seatId, endPoint, activeWeaponId, getCurrentBeLevel());


        RicochetBullet ricochetBullet = new RicochetBullet(currentBulletId, startPoint, endPoint, activeWeaponId, isPaidShot, weaponPrice);
        getLogger().debug("sendBullet: botId:{}, ricochetBullet={}", getId(), ricochetBullet);

        BulletRequest bulletRequest = new BulletRequest(this, client, currentBulletId,
                (int)Math.round(startPoint.getX()), (int)Math.round(startPoint.getY()),
                (int)Math.round(endPoint.getX()), (int)Math.round(endPoint.getY()),
                (float)ricochetBullet.getBulletAngle(), null, activeWeaponId, metric);

        int rid = send(bulletRequest);
        ricochetBullet.setRid(rid);

        managedBattleGroundRoomBot.putRicochetBullet(ricochetBullet);
        getLogger().debug("sendBullet: botId:{}, ricochetBulletsMap.values()={},{}", getId(),
                managedBattleGroundRoomBot.getRicochetBullets().size(), managedBattleGroundRoomBot.getRicochetBullets());
    }

    protected void findUpdateFocusedEnemy() {
        if (focusedEnemy != null) {
            if (!isAllowedForShot(focusedEnemy)) {
                focusedEnemy = null;
            }
        }

        if (strategy.getRequestedEnemiesIds() != null && strategy.getRequestedEnemiesIds().length != 0) {
            IRoomEnemy enemyWithRequestedType = getRandomEnemyWithRequestedType();
            if (enemyWithRequestedType != null) {
                getLogger().debug("findUpdateFocusedEnemy: botId={}, found enemyWithRequestedType or it's child, enemyId={}, enemyTypeId={} ",
                        getId(), enemyWithRequestedType.getId(), enemyWithRequestedType.getTypeId());
                focusedEnemy = enemyWithRequestedType;
            }
        }

        if (focusedEnemy == null || !enemies.contains(focusedEnemy)) {
            focusedEnemy = getRandomEnemy();
        }

        getLogger().debug("findUpdateFocusedEnemy: botId={}, focusedEnemyId={} ", getId(), focusedEnemy != null ? focusedEnemy.getId() : null);
    }

    @Override
    //true if shot sent
    public boolean shot() {
        boolean shotSent = false;
        lock.lock();
        try {
            if (RoomState.PLAY.equals(getRoomState())) {
                getLogger().debug("shot: enemies: {}", enemies.size());
                removeDeadEnemies();
                if (!enemies.isEmpty()) {

                    findUpdateFocusedEnemy();

                    if (focusedEnemy != null) {
                        boolean isPaidShot = activeWeaponIsPaid();

                        String metric = "";
                        if (isBattleBot()) {
                            metric = activeWeaponId == Turret.DEFAULT_WEAPON_ID ? "PISTOL" : SpecialWeaponType.values()[activeWeaponId].name();
                        }

                        getLogger().debug("shot: botId={}, nickname={}, isPaidShot: {}, activeWeaponId: {}, weaponType={}",
                                getId(), getNickname(), isPaidShot, activeWeaponId, metric);

                        if (!isPaidShot) {
                            strategy.consumeAmmo(activeWeaponId);
                        }

                        if (activeWeaponId == SpecialWeaponType.Landmines.getId()) {
                            send(new MinesRequest(this, client, isPaidShot));
                        } else {
                            String currentBulletId = getBulletId();
                            setState(BotState.WAITING_FOR_RESPONSE, "shot");
                            Integer weaponPrice = 1;

                            if (!isBattleBot()) {

                                Integer weaponPriceById = lobbyBot.getWeaponPriceById(activeWeaponId);
                                if (weaponPriceById != null) {
                                    weaponPrice = weaponPriceById;
                                }

                                sendShotWithBullet(currentBulletId, weaponPrice, isPaidShot, metric);

                            } else {

                                float currentX = 0;
                                float currentY = 0;

                                if (focusedEnemy.getTrajectory() != null && !focusedEnemy.getTrajectory().getPoints().isEmpty()) {
                                    PointD currentPoint = getEnemyPosition(focusedEnemy);
                                    if (currentPoint != null) {
                                        currentX = (float) currentPoint.x;
                                        currentY = (float) currentPoint.y;
                                    }
                                }

                                if(this instanceof ManagedBattleGroundRoomBot
                                        && (activeWeaponId == Turret.DEFAULT_WEAPON_ID || activeWeaponId == SpecialWeaponType.LevelUp.getId())
                                ) {

                                    ManagedBattleGroundRoomBot managedBattleGroundRoomBot = (ManagedBattleGroundRoomBot)this;
                                    int bulletsOnMap = managedBattleGroundRoomBot.sizeOfRicochetBulletsMap();

                                    if(bulletsOnMap < maxBulletsOnMap) {

                                        sendBullet(currentBulletId, currentX, currentY, isPaidShot, weaponPrice, managedBattleGroundRoomBot, metric);

                                    } else {
                                        setState(BotState.PLAYING, "Skip bullets creation");
                                        getLogger().debug("shot: botId={}, Skip bullets creation, bulletsOnMap={}", getId(), bulletsOnMap);
                                        //sendShot(currentX, currentY, weaponPrice, isPaidShot, metric);
                                    }

                                } else {

                                    sendShot(currentX, currentY, weaponPrice, isPaidShot, metric);
                                }
                            }
                        }

                        shotSent = true;

                    } else {
                        getLogger().debug("Not found enemy with allow location, enemies size: {} ", enemies.size());
                    }
                } else {
                    getLogger().debug("No enemies");
                }
            }
        } catch (Exception e) {
            getLogger().error("Unexpected error on shot, focusedEnemy={}", focusedEnemy, e);
        } finally {
            lock.unlock();
        }
        return shotSent;
    }

    public boolean activeWeaponIsPaid() {
        return activeWeaponId >= 0 && strategy.getShotsForWeapon(activeWeaponId) == 0;
    }

    protected String getBulletId() {
        return "" + seatId + "_" + bulletId.getAndIncrement();
    }

    @SuppressWarnings("rawtypes")
    protected void removeDeadEnemies() {
        enemiesLock.lock();
        try {
            long time = System.currentTimeMillis() - 100;
            List<IRoomEnemy> deadEnemies = new ArrayList<>();
            for (IRoomEnemy enemy : enemies) {
                if (enemy.getTrajectory().getLeaveTime() < time) {
                    deadEnemies.add(enemy);
                }
            }
            enemies.removeAll(deadEnemies);

        } finally {
            enemiesLock.unlock();
        }
    }

    protected RoomEnemy getEnemyById(Long enemyId) {
        if(enemies == null || enemyId == null)  {
            return null;
        }

        return enemies.stream()
                .filter( e -> enemyId.equals(e.getId()))
                .findFirst()
                .orElse(null);
    }

    protected RoomEnemy getBossEnemy() {
        for (RoomEnemy roomEnemy : enemies) {
            if (roomEnemy.isBoss()) {
                return roomEnemy;
            }
        }
        return null;
    }

    protected PointD getEnemyPosition(IRoomEnemy enemy) {
        if(enemy == null) {
            return null;
        }

        long serverTime = System.currentTimeMillis() + getDiffLocalAndServerTime();
        Point locationOnScreen = getStrategy().getLocationOnScreen((RoomEnemy)enemy, serverTime);

        if(locationOnScreen != null) {
            return new PointD(locationOnScreen.getX(), locationOnScreen.getY());
        }

        return null;
    }

    protected Long getNearestEnemyIdAllowedForShot(List<RoomEnemy> enemies, double x, double y) {

        List<Pair<Long, Double>> distances = new ArrayList<>();

        for (int idx = 0; idx < enemies.size(); idx++) {
            RoomEnemy roomEnemy = enemies.get(idx);
            PointD point = getEnemyPosition(roomEnemy);
            if (point != null && isLocationAllowedForShot(roomEnemy.getId(), point)) {
                double dist = getDistance(x, y, point.x, point.y);
                distances.add(new Pair<>(roomEnemy.getId(), dist));
            }
        }

        return distances.stream().
                sorted(Comparator.comparing(Pair::getValue))
                .map(Pair::getKey)
                .findFirst()
                .orElse(null);
    }

    public IRoomEnemy getRandomEnemy() {

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            getLogger().debug("InterruptedException in getRandomEnemy");
        }

        enemiesLock.lock();

        try {

            RoomEnemy boss = getBossEnemy();
            if(boss != null && isAllowedForShot(boss)) {
                return boss;
            }

            Double x = null;
            Double y = null;

            if(focusedEnemy == null) {
                TurretPositions turret = TurretPositions.getTurretPositionBySeatID(getSeatId());
                x = turret.getCentreCoordinateX();
                y = turret.getCentreCoordinateY();
            } else {
                PointD point = getEnemyPosition(focusedEnemy);
                if(point != null) {
                    x = point.x;
                    y = point.y;
                }
            }

            RoomEnemy roomEnemy = null;
            if(x != null && y != null) {

                Long enemyId = getNearestEnemyIdAllowedForShot(enemies, x, y);
                roomEnemy = getEnemyById(enemyId);
            }

            return roomEnemy;

        } finally {
            enemiesLock.unlock();
        }
    }

    public boolean isLocationAllowedForShot(long enemyId, Point point) {
        return isLocationAllowedForShot(enemyId, new PointD(point.getX(), point.getY()));
    }

    public boolean isLocationAllowedForShot(long enemyId, PointD point) {

        if(point != null) {

            boolean isLocationAllowForShot =
                    point.x >= SCREEN_ADJUSTED_LEFT && point.x <= SCREEN_ADJUSTED_RIGHT
                    && point.y >= SCREEN_ADJUSTED_TOP && point.y <= SCREEN_ADJUSTED_BOTTOM;

            if(isLocationAllowForShot) {
                getLogger().debug("isLocationAllowForShot: botId={}, enemyId={}, point={}", getId(), enemyId, point);
            }

            return isLocationAllowForShot;

        }

        return  false;
    }

    @Override
    public boolean isAllowedForShot(IRoomEnemy roomEnemy) {

        if(roomEnemy == null) {
            return false;
        }

        PointD point = getEnemyPosition(roomEnemy);

        if(point == null) {
            return false;
        }

        return isLocationAllowedForShot(roomEnemy.getId(), point);
    }

    public IRoomEnemy getRandomEnemyWithRequestedType() {
        enemiesLock.lock();
        try {
            return strategy.getEnemyToShoot(enemies);
        } finally {
            enemiesLock.unlock();
        }
    }

    @Override
    public long getLastReceivedServerTime() {
        return lastReceivedServerTime;
    }

    @Override
    public void setLastReceivedServerTime(long lastReceivedServerTime) {
        this.lastReceivedServerTime = lastReceivedServerTime;
        this.diffLocalAndServerTime = lastReceivedServerTime - System.currentTimeMillis();
    }

    public long getDiffLocalAndServerTime() {
        return diffLocalAndServerTime;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public void setRoomState(RoomState state) {
        roomInfo.setState(state);
    }

    @Override
    public RoomState getRoomState() {
        return roomInfo == null ? null : roomInfo.getState();
    }

    public long getRoomId() {
        return roomId;
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }


    @Override
    public void activateWeapon(int weaponId) {
        this.activeWeaponId = weaponId;
        strategy.activateWeapon(weaponId);
    }

    @Override
    public void updateWeapon(int weaponId, int shots) {
        strategy.updateWeapon(weaponId, shots);
        getLogger().debug("{}  update weapon weaponId: {} shots: {}", getNickname(), weaponId, shots);
    }

    @Override
    public void addWeapon(int weaponId, int shots) {
        strategy.addWeapon(weaponId, shots);
    }

    @Override
    public double getRoomStake() {
        return roomInfo.getStake();
    }

    @Override
    public int getCurrentBeLevel() {
        return currentBeLevel;
    }

    @Override
    public void setCurrentBeLevel(int currentBeLevel) {
        this.currentBeLevel = currentBeLevel;
    }

    @Override
    public int getServerAmmo() {
        return serverAmmo;
    }

    @Override
    public void setServerAmmo(int serverAmmo) {
        this.serverAmmo = serverAmmo;
        this.getStrategy().updateWeapon(-1, serverAmmo);
        getLogger().debug("setServerAmmo: {}", serverAmmo);
    }

    @Override
    public void addServerAmmo(int serverAmmo) {
        this.serverAmmo += serverAmmo;
        this.getStrategy().updateWeapon(-1, this.serverAmmo);
        getLogger().debug("addServerAmmo: {}, serverAmmo: {}", this.serverAmmo, serverAmmo);
    }

    @Override
    public int getSeatId() {
        return seatId;
    }

    @Override
    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    @Override
    public void resetFocusedEnemy() {
        this.focusedEnemy = null;
    }


    public boolean isRoundFinishSoonReceived() {
        return roundFinishSoonReceived;
    }

    public void setRoundFinishSoonReceived(boolean roundFinishSoonReceived) {
        this.roundFinishSoonReceived = roundFinishSoonReceived;
    }

    public boolean isRoundResultReceived() {
        return roundResultReceived;
    }

    public void setRoundResultReceived(boolean roundResultReceived) {
        this.roundResultReceived = roundResultReceived;
    }

    public boolean isQualifyStateReceived() {
        return qualifyStateReceived;
    }

    public void setQualifyStateReceived(boolean qualifyStateReceived) {
        this.qualifyStateReceived = qualifyStateReceived;
    }

    public LobbyBot getLobbyBot() {
        return lobbyBot;
    }

    public int getCurrentMapId() {
        return currentMapId;
    }

    public void setCurrentMapId(int currentMapId) {
        this.currentMapId = currentMapId;
    }

    public String getCurrentSubround() {
        return currentSubround;
    }

    public void setCurrentSubround(String currentSubround) {
        this.currentSubround = currentSubround;
    }

    @Override
    public void sentCheckPendingStatus(int failedCount) {
        setState(BotState.WAITING_FOR_RESPONSE, "sentCheckPendingStatus");
        sleep(500).subscribe(t -> send(new CheckPendingOperationStatusRequest(this, client, failedCount)));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoomBot{");
        sb.append("nickname='").append(nickname).append('\'');
        sb.append(", balance=").append(balance);
        sb.append(", stake=").append(stake);
        sb.append(", activeWeaponId=").append(activeWeaponId);
        sb.append(", currentBeLevel=").append(currentBeLevel);
        sb.append(", focusedEnemy=").append(focusedEnemy);
        sb.append(", state=").append(state);
        sb.append(", roomId=").append(roomId);
        //sb.append(", roomInfo=").append(roomInfo);
        //sb.append(", enemies=").append(enemies);
        sb.append(", strategy=").append(strategy);
        sb.append(", enemiesLock=").append(enemiesLock);
        sb.append(", serverAmmo=").append(serverAmmo);
        sb.append(", currentMapId=").append(currentMapId);
        sb.append(", currentSubround=").append(currentSubround);
        sb.append(", pendingOperation=").append(pendingOperation);
        sb.append(", seatId=").append(seatId);
        sb.append('}');
        return sb.toString();
    }
}
