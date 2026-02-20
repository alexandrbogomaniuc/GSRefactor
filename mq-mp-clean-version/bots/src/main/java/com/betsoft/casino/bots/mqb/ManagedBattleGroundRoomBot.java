package com.betsoft.casino.bots.mqb;

import com.betsoft.casino.bots.BattleGroundRoomBot;
import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.LobbyBot;
import com.betsoft.casino.bots.model.RicochetBullet;
import com.betsoft.casino.bots.model.Turret;
import com.betsoft.casino.bots.model.TurretPositions;
import com.betsoft.casino.bots.requests.BetLevelRequest;
import com.betsoft.casino.bots.requests.MinesRequest;
import com.betsoft.casino.bots.requests.ShotRequest;
import com.betsoft.casino.bots.requests.SwitchWeaponRequest;
import com.betsoft.casino.bots.strategies.BaseMetricTimeKey;
import com.betsoft.casino.bots.strategies.IRoomBotStrategy;
import com.betsoft.casino.bots.strategies.IRoomNaturalBotStrategy;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.transport.Observer;
import com.betsoft.casino.mp.transport.RoomEnemy;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.betsoft.casino.bots.model.Turret.DEFAULT_WEAPON_ID;
import static com.betsoft.casino.bots.strategies.NaturalBattleGroundDragonStoneStrategy.ENEMY_DRAGON_TYPE_ID;
import static com.betsoft.casino.bots.utils.GeometryUtils.getDistance;
import static com.betsoft.casino.bots.utils.GeometryUtils.divideSegment;
import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

/**
 * User: flsh
 * Date: 13.07.2022.
 */
public class ManagedBattleGroundRoomBot extends BattleGroundRoomBot {

    private boolean allowNextRoundPlay = false;
    private List<Observer> observers;
    private long confirmBuyInTime;
    private long expiresAt;
    private boolean fullGameInfoOnPlayRequested = false;
    private Long prevFocusedEnemyId = null;
    private PointD prevFocusedEnemyPosition = null;
    private List<Point> transitionPositionsList = null;

    private static final int DEFAULT_MAX_DELTA_CONFIRM_BUY_IN_TIME_MILLIS = 15_000;
    private static final int COUNTDOWN_BEFORE_CONFIRM_BUY_IN_TIME_MILLIS = 3_000;

    private final Map<String, RicochetBullet> ricochetBulletsMap = new ConcurrentHashMap<>();
    private final AtomicLong lastDoActionTickTimeMs = new AtomicLong(System.currentTimeMillis());


    public ManagedBattleGroundRoomBot(LobbyBot lobbyBot, String id, String url, int serverId, String sessionId, IMessageSerializer serializer,
                                      long roomId, long balance, float stake, String nickName, IRoomBotStrategy strategy,
                                      Function<Void, Integer> shutdownCallback, Function<Void, Integer> startCallback, long expiresAt) {
        super(lobbyBot, id, url, serverId, sessionId, serializer, roomId, balance, stake, nickName, strategy, shutdownCallback, startCallback);
        this.expiresAt = expiresAt;
    }

    public void putRicochetBullet(RicochetBullet ricochetBullet) {
        if(ricochetBullet != null && !StringUtils.isTrimmedEmpty(ricochetBullet.getBulletId())) {
            LOG.info("putRicochetBullet: botId={}, bulletId={}, ricochetBullet={}",
                    getId(), ricochetBullet.getBulletId(), ricochetBullet);
            ricochetBulletsMap.put(ricochetBullet.getBulletId(), ricochetBullet);
        }
    }

    public RicochetBullet getRicochetBullet(String ricochetBulletId) {
        if(!StringUtils.isTrimmedEmpty(ricochetBulletId)) {
            return ricochetBulletsMap.get(ricochetBulletId);
        }
        return null;
    }

    public RicochetBullet getRicochetBulletByRid(int rid) {

        for(RicochetBullet ricochetBullet : ricochetBulletsMap.values()) {
            if(ricochetBullet.getRid() == rid) {
                return ricochetBullet;
            }
        }

        return null;
    }

    public Collection<RicochetBullet> getRicochetBullets() {
        return ricochetBulletsMap.values();
    }

    public void removeRicochetBullet(String ricochetBulletId) {
        LOG.info("removeRicochetBullet: botId={}, bulletId={}", getId(), ricochetBulletId);
        if(!StringUtils.isTrimmedEmpty(ricochetBulletId)) {
            ricochetBulletsMap.remove(ricochetBulletId);
        }
    }

    public void removeRicochetBulletByRid(int  rid) {
        RicochetBullet ricochetBullet = getRicochetBulletByRid(rid);
        LOG.info("removeRicochetBulletByRid: botId={}, rid={}, ricochetBullet={}", getId(), rid, ricochetBullet);
        if(ricochetBullet != null) {
            removeRicochetBullet(ricochetBullet.getBulletId());
        }
    }

    public void removeRicochetBullet(RicochetBullet ricochetBullet) {
        if(ricochetBullet != null) {
            removeRicochetBullet(ricochetBullet.getBulletId());
        }
    }

    public void clearRicochetBulletsMap() {
        LOG.info("clearRicochetBulletsMap: botId={}", getId());
        ricochetBulletsMap.clear();
    }

    public int sizeOfRicochetBulletsMap() {
        Collection<RicochetBullet> ricochetBullets = getRicochetBullets();
        return ricochetBullets == null ? 0 : ricochetBullets.size();
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isAllowNextRoundPlay() {
        return allowNextRoundPlay;
    }

    public void setAllowNextRoundPlay(boolean allowNextRoundPlay) {
        this.allowNextRoundPlay = allowNextRoundPlay;
    }

    protected boolean shouldStop() {
        boolean isExpired = this.expiresAt <= System.currentTimeMillis();
        getLogger().debug("ManagedBattleGroundRoomBot shouldStop, botId={}, isExpired={}, expiresAt={}",
                id, isExpired, toHumanReadableFormat(this.expiresAt));
        return isExpired;
    }

    public void markExpiredAndStop() {

        long now = System.currentTimeMillis();
        setExpiresAt(now);
        LobbyBot lobbyBot = getLobbyBot();
        if (lobbyBot instanceof ManagedLobbyBot) {
            ManagedLobbyBot managedLobbyBot = ((ManagedLobbyBot) lobbyBot);
            managedLobbyBot.setExpiresAt(now);
        }
        stop();
    }

    @Override
    public void stop() {
        if(shouldStop()) {
            super.stop();
            allowNextRoundPlay = false;
            observers = null;
            confirmBuyInTime = 0;
            expiresAt = 0;
            fullGameInfoOnPlayRequested = false;
        } else {
            setState(BotState.OBSERVING, "Should not stop, bot is not expired");
        }
    }

    public List<Observer> getObservers() {
        return observers;
    }

    public void setObservers(List<Observer> observers) {
        this.observers = observers;
    }

    protected boolean otherObserversExist() {
        String botNickName = getNickname();
        if(StringUtils.isTrimmedEmpty(botNickName)) {
            return false;
        }
        boolean result = false;
        List<Observer> observers = getObservers();
        if(observers != null && !observers.isEmpty()) {
            result = observers.stream()
                    .anyMatch( observer -> !botNickName.equals(observer.getNickname()));
        }
        return result;
    }

    public long getConfirmBuyInTime() {
        return confirmBuyInTime;
    }

    public void setConfirmBuyInTime(long confirmBuyInTime) {
        this.confirmBuyInTime = confirmBuyInTime;
    }

    public void generateConfirmBuyInTime() {

        long now = System.currentTimeMillis();
        setConfirmBuyInTime(now); // if deltaTime is 0, let bot keep waiting for next round start, as current will be too late to join

        long currentTimeMillisShifted = now + 1_000;
        long startRoundTimeLocal = getStartRoundDateTimeLocal();

        int deltaTime = startRoundTimeLocal == 0 ?
                0 :
                //use +1 sec (1_000ms) before countdown starts
                (int)(startRoundTimeLocal - (currentTimeMillisShifted + COUNTDOWN_BEFORE_CONFIRM_BUY_IN_TIME_MILLIS + 1_000));

        getLogger().debug("generateConfirmBuyInTime: bot: {}, currentTimeMillisShifted={}, startRoundTimeLocal={}, deltaTime={}",
                getId(), toHumanReadableFormat(currentTimeMillisShifted), toHumanReadableFormat(startRoundTimeLocal), deltaTime);

        if(deltaTime > 0) {
            //shift 1 sec (1_000ms) from current time to future
            long confirmBuyIn = currentTimeMillisShifted + RNG.nextInt(deltaTime);
            getLogger().debug("generateConfirmBuyInTime: bot: {}, deltaTime={}, generated confirmBuyIn={}", getId(),
                    deltaTime, toHumanReadableFormat(confirmBuyIn));
            setConfirmBuyInTime(confirmBuyIn);
        } else {
            getLogger().debug("generateConfirmBuyInTime: bot: {}, deltaTime={} is negative skip setConfirmBuyInTime " +
                            "generation", getId(), deltaTime);
        }

        getLogger().debug("generateConfirmBuyInTime: bot: {}, deltaTime={}, final confirmBuyIn={}", getId(),
                deltaTime, toHumanReadableFormat(getConfirmBuyInTime()));
    }

    public boolean isFullGameInfoOnPlayRequested() {
        return fullGameInfoOnPlayRequested;
    }

    public void setFullGameInfoOnPlayRequested(boolean fullGameInfoOnPlayRequested) {
        this.fullGameInfoOnPlayRequested = fullGameInfoOnPlayRequested;
    }

    private void switchWeapon(int oldWeaponId, GameType gameType) {
        int weaponId = strategy.getWeaponId();

        long preDelay = 150L;

        if(oldWeaponId == DEFAULT_WEAPON_ID) {

            if (gameType == GameType.BG_SECTOR_X) {
                preDelay = 500L;
            }

            int sizeOfRicochetBulletsMap = sizeOfRicochetBulletsMap();
            if (sizeOfRicochetBulletsMap > 0) {
                long k = RNG.nextInt(250, 300);
                long bulletsWaitMs = k * sizeOfRicochetBulletsMap;
                getLogger().debug("switchWeapon: sizeOfRicochetBulletsMap={} is not 0 " +
                                "wait more {} ms, where k={}, switch oldWeaponId={} to weaponId: {}",
                        sizeOfRicochetBulletsMap, bulletsWaitMs, k, oldWeaponId, weaponId);
                preDelay += bulletsWaitMs;
            }
        }

        if(strategy instanceof IRoomNaturalBotStrategy) {
            long minWaitTimeForSwitchWeapon =
                    ((IRoomNaturalBotStrategy)strategy).getWaitTimeForSwitchWeapon(getId());
            preDelay = Math.max(preDelay, minWaitTimeForSwitchWeapon);
        }

        try {
            Thread.sleep(preDelay);
        } catch (InterruptedException e) {
            getLogger().debug("switchWeapon: InterruptedException 1 in switch oldWeaponId={} to weaponId: {}",
                    oldWeaponId, weaponId);
        }

        setState(BotState.WAITING_FOR_RESPONSE, "switchWeapon, weaponId:  " + weaponId);
        send(new SwitchWeaponRequest(this, client, weaponId));

        if(oldWeaponId == DEFAULT_WEAPON_ID && (gameType == GameType.BG_MISSION_AMAZON || gameType == GameType.BG_DRAGONSTONE)) {

            try {
                getLogger().debug("switchWeapon: botId: {}, GameId={}, Thread.sleep(500)", getId(), this.getLobbyBot().getGameId());
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                getLogger().debug("switchWeapon: InterruptedException 2 switch oldWeaponId={} to weaponId: {}",
                        oldWeaponId, weaponId);
            }
        }
    }

    @Override
    protected void doAction(String debugInfo) {

        long lastDoActionTickTimeMs = this.lastDoActionTickTimeMs.get();
        long currentTimeMillis = System.currentTimeMillis();
        long deltaTimeMs = currentTimeMillis - lastDoActionTickTimeMs;

        //getLogger().debug("doAction: botId:{}, lastDoActionTickTimeMs={}, currentTimeMillis={}, deltaTimeMs={}",
        //        getId(), toHumanReadableFormat(lastDoActionTickTimeMs), toHumanReadableFormat(currentTimeMillis), deltaTimeMs);

        this.lastDoActionTickTimeMs.set(System.currentTimeMillis());

        long lastTimeFullGameInfo = getLastTimeFullGameInfo();
        long startRoundTimeLocal = getStartRoundDateTimeLocal();
        boolean isFullGameInfoOnPlayRequested = isFullGameInfoOnPlayRequested();
        RoomState roomState;

        switch (getState()) {
            case WAITING_FOR_RESPONSE:
            case WAIT_BATTLE_PLAYERS:
                break;
            case IDLE:
                openRoom();
                break;
            case NEED_SIT_OUT:
                sendSitOutRequest();
                break;
            case OBSERVING:

                if(isFullGameInfoOnPlayRequested) {
                    setFullGameInfoOnPlayRequested(false);

                    getLogger().debug("OBSERVING doAction: getting vars values botId:{}, seatId={}, " +
                                    "activeWeaponId={}, strategy.getWeaponId()={}, serverAmmo={}, roundResultReceived={}, " +
                                    "roundFinishSoonReceived={}, qualifyStateReceived={}, pendingOperation={}, battlegroundBuyInConfirmed={}, " +
                                    "startRoundDateTimeLocal={}, endRoundDateTimeLocal={}, stopShotDateTimeLocal={}, sitInAppeared={}, " +
                                    "needReBuyInRoom={}, RicochetBullets={}, prevFocusedEnemyId={}, prevFocusedEnemyPosition={}, " +
                                    "transitionPositionsList={}",
                            getId(), seatId, activeWeaponId, strategy.getWeaponId(), serverAmmo, roundResultReceived,
                            roundFinishSoonReceived, qualifyStateReceived, pendingOperation, battlegroundBuyInConfirmed,
                            startRoundDateTimeLocal, endRoundDateTimeLocal, stopShotDateTimeLocal, sitInAppeared, needReBuyInRoom,
                            getRicochetBullets(), prevFocusedEnemyId, prevFocusedEnemyPosition, transitionPositionsList);

                    //RoomBot variables
                    seatId = -1;
                    activeWeaponId = DEFAULT_WEAPON_ID;
                    if(strategy != null) {
                        strategy.resetWeapons();
                    }
                    serverAmmo = 0;
                    roundResultReceived = false;
                    roundFinishSoonReceived = false;
                    qualifyStateReceived = false;
                    pendingOperation = false;

                    //BattleGroundRoomBot variables
                    battlegroundBuyInConfirmed = false;
                    startRoundDateTimeLocal = 0;
                    endRoundDateTimeLocal = 0;
                    stopShotDateTimeLocal = 0;
                    sitInAppeared = false;
                    needReBuyInRoom = false;

                    //ManagedBattleGroundRoomBot variables
                    clearRicochetBulletsMap();
                    prevFocusedEnemyId = null;
                    prevFocusedEnemyPosition = null;
                    transitionPositionsList = null;

                    getLogger().debug("OBSERVING doAction: setting vars to default values " +
                                    "botId:{}, seatId={}, activeWeaponId={}, strategy.getWeaponId()={}, serverAmmo={}, roundResultReceived={}, " +
                                    "roundFinishSoonReceived={}, qualifyStateReceived={}, pendingOperation={}, battlegroundBuyInConfirmed={}, " +
                                    "startRoundDateTimeLocal={}, endRoundDateTimeLocal={}, stopShotDateTimeLocal={}, sitInAppeared={}, " +
                                    "needReBuyInRoom={}, RicochetBullets={}, prevFocusedEnemyId={}, prevFocusedEnemyPosition={}, " +
                                    "transitionPositionsList={}",
                            getId(), seatId, activeWeaponId, strategy.getWeaponId(), serverAmmo, roundResultReceived,
                            roundFinishSoonReceived, qualifyStateReceived, pendingOperation, battlegroundBuyInConfirmed,
                            startRoundDateTimeLocal, endRoundDateTimeLocal, stopShotDateTimeLocal, sitInAppeared, needReBuyInRoom,
                            getRicochetBullets(), prevFocusedEnemyId, prevFocusedEnemyPosition, transitionPositionsList);
                }

                if (startRoundTimeLocal == 0 || startRoundTimeLocal < currentTimeMillis) {

                    getLogger().debug("OBSERVING sendGetFullGameInfoRequest doAction: " +
                                    "botId:{}, currentTimeMillis={}, startRoundTimeLocal={}, lastTimeFullGameInfo={}",
                            getId(), toHumanReadableFormat(currentTimeMillis),
                            toHumanReadableFormat(startRoundTimeLocal), toHumanReadableFormat(lastTimeFullGameInfo));

                    if (currentTimeMillis > lastTimeFullGameInfo + 1000 || lastTimeFullGameInfo == 0) {

                        getLogger().debug("OBSERVING sendGetFullGameInfoRequest doAction: " +
                                "botId:{}, getSeatId():{}, debugInfo={}, state:{} getSessionId():{}",
                                getId(), getSeatId(), debugInfo, getState(), getSessionId());

                        sendGetFullGameInfoRequest();
                        setLastTimeFullGameInfo(currentTimeMillis);
                    }
                } else {
                    roomState = roomInfo.getState();
                    if (!roomState.equals(RoomState.WAIT)) {
                        getLogger().debug("OBSERVING sitIn doAction: bot: {}, roomState={}, " +
                                "bot state: {}", getId(), roomState, getState());
                    } else {

                        if (!isBattlegroundBuyInConfirmed()) {
                            long confirmBuyInTime = getConfirmBuyInTime();
                            //getLogger().debug("OBSERVING sendConfirmBattlegroundBuyIn doAction: bot: {}, getSeatId(): {}, " +
                            //                "debugInfo={}, state: {} getSessionId(): {}, confirmBuyInTime={}, currentTimeMillis={}",
                            //        getId(), getSeatId(), debugInfo, getState(), getSessionId(),
                            //        toHumanReadableFormat(confirmBuyInTime), toHumanReadableFormat(currentTimeMillis));

                            if (/*otherObserversExist() && */currentTimeMillis > confirmBuyInTime) {
                                sendConfirmBattlegroundBuyIn();
                            }
                        } else {

                            getLogger().debug("OBSERVING sitIn doAction: bot: {}, getSeatId(): {}, " +
                                            "debugInfo={}, state: {} getSessionId(): {}", getId(), getSeatId(), debugInfo,
                                    getState(), getSessionId());
                            sendSitInRequest(0);
                            setSitInAppeared(true);
                        }
                    }
                }
                break;

            case PLAYING:

                if(!isFullGameInfoOnPlayRequested) {
                    getLogger().debug("PLAYING sendGetFullGameInfoRequest doAction: " +
                                    "botId:{}, getSeatId():{}, debugInfo={}, state:{} getSessionId():{}",
                            getId(), getSeatId(), debugInfo, getState(), getSessionId());

                    sendGetFullGameInfoRequest();
                    setLastTimeFullGameInfo(currentTimeMillis);

                    setFullGameInfoOnPlayRequested(true);
                }

                //move ricochet bullets on step of deltaTimeMs
                //getLogger().debug("doAction: botId:{}, ricochetBulletsMap.values()={}", getId(), ricochetBulletsMap.values());
                for(RicochetBullet ricochetBullet: getRicochetBullets()) {
                    ricochetBullet.tick(deltaTimeMs);
                }

                boolean shotIsAllowed = shotIsAllowed();
                boolean roomStateIsPlay = roomInfo.getState().equals(RoomState.PLAY);
                GameType gameType = GameType.getByGameId(this.getLobbyBot().getGameId());

                if(!roomStateIsPlay || !shotIsAllowed) {
                    getLogger().debug("PLAYING doAction: botId:{}, skip shot because roomStateIsPlay={}, shotIsAllowed={}",
                            getId(), roomStateIsPlay, shotIsAllowed);
                } else {

                    IRoomNaturalBotStrategy roomNaturalBotStrategy = (IRoomNaturalBotStrategy) getStrategy();
                    long minResponseByType = roomNaturalBotStrategy.getTimesMinResponseByType(BaseMetricTimeKey.PLAY_STARTED.name());
                    long startTimePlay = roomNaturalBotStrategy.getTimesShootLastResponseByType(BaseMetricTimeKey.PLAY_STARTED.name());
                    boolean allowShotInBattle = startTimePlay != 0 && currentTimeMillis > startTimePlay + minResponseByType;

                    getLogger().debug("PLAYING doAction: bot:{}, getSeatId():{}, debugInfo={}, state:{}, getSessionId():{}, " +
                                    "currentBeLevel:{}, strategy.requestedBetLevel():{}, " +
                                    "serverAmmo:{}, allowShotInBattle:{}, currentTimeMillis:{}, startTimePlay:{}, minResponseByType:{}",
                            getId(), getSeatId(), debugInfo, getState(), getSessionId(), currentBeLevel, strategy.requestedBetLevel(),
                            getServerAmmo(), allowShotInBattle,
                            toHumanReadableFormat(currentTimeMillis), toHumanReadableFormat(startTimePlay), minResponseByType);

                    int oldWeaponId = strategy.getWeaponId();

                    if (!strategy.botHasSpecialWeapons() && currentBeLevel != strategy.requestedBetLevel()) {

                        send(new BetLevelRequest(this, client));

                    } else if (strategy.shouldSwitchWeapon()) {

                        switchWeapon(oldWeaponId, gameType);

                    } else if (allowShotInBattle) {

                        long serverTime = System.currentTimeMillis() + getDiffLocalAndServerTime();
                        RoomEnemy roomEnemy = null;
                        RicochetBullet ricochetBullet = null;
                        String bulletId = null;

                        for (RicochetBullet ricochetBulletCurr : getRicochetBullets()) {

                            if (ricochetBulletCurr != null && ricochetBulletCurr.isEnabled()) {

                                roomEnemy = ricochetBulletCurr.collide(getEnemies(), getStrategy(), serverTime);

                                if (roomEnemy != null) {
                                    ricochetBullet = ricochetBulletCurr;
                                    bulletId = ricochetBulletCurr.getBulletId();
                                    break;
                                }
                            }
                        }

                        //shoot if there is a bullet collided to enemy
                        if (ricochetBullet != null && roomEnemy != null && !StringUtils.isTrimmedEmpty(bulletId)) {

                            boolean shouldShootWithDelay = strategy.shouldShoot(getId());
                            if(shouldShootWithDelay) {
                                shotFromCollide(roomEnemy, ricochetBullet, bulletId);
                            }

                        //if no bullets collided to enemy, send a bullet or shot direct to the enemy
                        } else {

                            boolean bulletSubmitted = true;

                            boolean chanceToSubmitBullet = true;
                            if(getLobbyBot() instanceof ManagedLobbyBot) {
                                double bulletsRate = ((ManagedLobbyBot)getLobbyBot()).getBulletsRate();
                                double random = RNG.rand();
                                chanceToSubmitBullet = random < bulletsRate;
                                // getLogger().debug("PLAYING doAction: botId: {}, random={}, bulletsRate={}, chanceToSubmitBullet={}",
                                //        getId(), random, bulletsRate, chanceToSubmitBullet);
                            }

                            int weaponId = strategy.getWeaponId();
                            int bulletsOnMap = this.sizeOfRicochetBulletsMap();

                            boolean isBulletTime = false;
                            boolean isRicochetWeapon = false;

                            if(strategy instanceof IRoomNaturalBotStrategy) {
                                isBulletTime = ((IRoomNaturalBotStrategy) strategy).isBulletTime(getId());
                                isRicochetWeapon = ((IRoomNaturalBotStrategy) strategy).isRicochetWeapon();
                            }

                            boolean useBullet = (bulletsOnMap < maxBulletsOnMap) && isBulletTime && isRicochetWeapon;

                            //send bullet if bullet time is earlier than shot time and the weapon is ricochet weapon
                            if (useBullet) {

                                boolean shouldSendBulletWithDelay = strategy.shouldSendBullet(getId());
                                if (shouldSendBulletWithDelay && chanceToSubmitBullet) {
                                    bulletSubmitted = sendBullet();
                                }
                            } else {
                                bulletSubmitted = false;
                            }

                            //getLogger().debug("PLAYING doAction: botId: {}, isBulletTime={}, isRicochetWeapon={}, " +
                            //                "chanceToSubmitBullet={}, bulletsOnMap={}, bulletSubmitted={}, weaponId={}",
                            //        getId(), isBulletTime, isRicochetWeapon, chanceToSubmitBullet, bulletsOnMap,
                            //        bulletSubmitted, weaponId);

                            if (!bulletSubmitted & !isRicochetWeapon) {
                                //check to shot per defined shot rate
                                boolean chanceToShoot = true;
                                if (getLobbyBot() instanceof ManagedLobbyBot) {
                                    double shootsRate = ((ManagedLobbyBot) getLobbyBot()).getShootsRate();
                                    double random = RNG.rand();
                                    chanceToShoot = random < shootsRate;
                                    //getLogger().debug("PLAYING doAction: botId: {}, random={}, shootsRate={}, chanceToShoot={}",
                                    //getId(), random, shootsRate, chanceToShoot);
                                }

                                boolean shouldShootWithDelay = strategy.shouldShoot(getId());
                                if (shouldShootWithDelay && chanceToShoot) {
                                    shot();
                                }
                            }
                        }
                    }
                }
                break;
        }

        scheduledDoSleepAction.set(false);

        if (started) {
            doActionWithSleep(strategy.getWaitTime(), "ManagedBattleGroundRoomBot from doAction()", false);
        }
    }

    @Override
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

            TurretPositions turret = TurretPositions.getTurretPositionBySeatID(getSeatId());

            Double x = null;
            Double y = null;

            if(focusedEnemy == null) {

                x = turret.getCentreCoordinateX();
                y = turret.getCentreCoordinateY();
            } else {
                PointD point = getEnemyPosition(focusedEnemy);
                if(point != null) {
                    x = point.x;
                    y = point.y;
                }
            }

            List<RoomEnemy> enemiesPriority1 = enemies.stream()
                    .filter(enemy -> {
                        PointD point = getEnemyPosition(enemy);
                        if (point != null) {
                            //TOP turret to select enemies located lower 1/4 by Y screen axis
                            return (turret.getDirect() == -1 && point.y > (double) SCREEN_HEIGHT / 4)
                                    //BOTTOM turret to select enemies located upper 3/4 by Y screen axis
                                    || (turret.getDirect() == 1 && point.y < (double) 3 * SCREEN_HEIGHT / 4);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            // Create a copy of main enemies to avoid modifying the original list
            List<RoomEnemy> enemiesPriority2 = new ArrayList<>(enemies);
            enemiesPriority2.removeAll(enemiesPriority1);

            RoomEnemy roomEnemy = null;
            if(x != null && y != null) {
                Long enemyId = getNearestEnemyIdAllowedForShot(enemiesPriority1, x, y);

                if(enemyId == null) {
                    enemyId = getNearestEnemyIdAllowedForShot(enemiesPriority2, x, y);
                }

                roomEnemy = getEnemyById(enemyId);
            }

            return roomEnemy;

        } finally {
            enemiesLock.unlock();
        }
    }

    @Override
    protected RoomEnemy getBossEnemy() {
        for (RoomEnemy roomEnemy : enemies) {
            if(roomEnemy.isBoss()){
                //ensure there is at least 3 sec after Boss message arrived
                Trajectory trajectory = roomEnemy.getTrajectory();
                if(trajectory != null) {
                    List<Point> trajectoryPoints = trajectory.getPoints();
                    if(trajectoryPoints != null && !trajectoryPoints.isEmpty()) {
                        Point firstPoint = trajectoryPoints.get(0);
                        if(firstPoint != null) {
                            long now = System.currentTimeMillis();
                            long spawnTime = firstPoint.getTime();
                            if(now - spawnTime > 3000) {
                                return roomEnemy;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void findUpdateFocusedEnemy() {
        if (focusedEnemy != null) {
            if (isAllowedForShot(focusedEnemy)) {
                prevFocusedEnemyId = focusedEnemy.getId();
                prevFocusedEnemyPosition = getEnemyPosition(focusedEnemy);
            } else {
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

        //make a pause when swapping enemy
        Long focusedEnemyId = focusedEnemy != null ? focusedEnemy.getId() : null;
        PointD focusedEnemyPosition =  focusedEnemy != null ? getEnemyPosition(focusedEnemy) : null;

        if (
                transitionPositionsList == null //if transition coordinates list is empty
                && focusedEnemyId != null
                && !focusedEnemyId.equals(prevFocusedEnemyId)
                && prevFocusedEnemyPosition != null
                && focusedEnemyPosition != null) {

            //count intermediate points between prevFocusedEnemyPosition and focusedEnemyPosition

            double distance =
                    getDistance(
                            prevFocusedEnemyPosition.x, prevFocusedEnemyPosition.y,
                            focusedEnemyPosition.x, focusedEnemyPosition.y);

            int shootingStep = 50; //shooting step in pixels when intermediate shot should happen
            int subSegmentsCount = ((int)(distance) / shootingStep);

            transitionPositionsList = divideSegment(
                    prevFocusedEnemyPosition.x, prevFocusedEnemyPosition.y,
                    focusedEnemyPosition.x, focusedEnemyPosition.y,
                    subSegmentsCount);

            if(transitionPositionsList.isEmpty()) {
                transitionPositionsList = null;
            }

            getLogger().debug("findUpdateFocusedEnemy: botId={}, distance={}, shootingStep={}, " +
                            "focusedEnemyPosition={}, prevFocusedEnemyPosition={}, transitionPositionsList={}",
                    getId(), distance, shootingStep, focusedEnemyPosition, prevFocusedEnemyPosition, transitionPositionsList);
        }

        /*if(focusedEnemyId != null && !focusedEnemyId.equals(prevFocusedEnemyId)) {
            try {
                GameType gameType = GameType.getByGameId(this.getLobbyBot().getGameId());
                if(gameType == GameType.BG_SECTOR_X) {
                    int delay = 50;
                    Double distance = null;
                    PointD focusedEnemyPosition = getEnemyPosition(focusedEnemy);
                    if (prevFocusedEnemyPosition != null && focusedEnemyPosition != null) {
                        distance = getDistance(focusedEnemyPosition.x, focusedEnemyPosition.y,
                                prevFocusedEnemyPosition.x, prevFocusedEnemyPosition.y);
                        if (distance != null) {
                            delay += distance.intValue();
                        }
                    }

                    getLogger().debug("findUpdateFocusedEnemy: botId={}, distance={}, delay={}, " +
                                    "focusedEnemyPosition={}, prevFocusedEnemyPosition={}",
                            getId(), distance, delay, focusedEnemyPosition, prevFocusedEnemyPosition);

                    if (delay > 1000) {
                        delay = RNG.nextInt(500, 1000);
                        getLogger().debug("findUpdateFocusedEnemy: botId={}, adjust delay={}, ", getId(), delay);
                    }

                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                getLogger().debug("findUpdateFocusedEnemy: InterruptedException in switch focusedEnemy: {}", focusedEnemyId);
            }
        }*/
        getLogger().debug("findUpdateFocusedEnemy: botId={}, focusedEnemyId={}, prevFocusedEnemyId={}",
                getId(), focusedEnemyId, prevFocusedEnemyId);
    }

    private void shotFromCollide(RoomEnemy roomEnemy, RicochetBullet ricochetBullet, String bulletId) {
        int weaponId = ricochetBullet.getWeaponId();
        boolean isPaidShot = ricochetBullet.isPaidShot();
        int weaponPrice = ricochetBullet.getWeaponPrice();

        float currentX = 0;
        float currentY = 0;

        if (roomEnemy.getTrajectory() != null && !roomEnemy.getTrajectory().getPoints().isEmpty()) {
            PointD currentPoint = getEnemyPosition(roomEnemy);
            if (currentPoint != null) {
                currentX = (float) currentPoint.x;
                currentY = (float) currentPoint.y;
            }
        }

        String metric = weaponId == DEFAULT_WEAPON_ID ? "PISTOL" : SpecialWeaponType.values()[weaponId].name();

        ShotRequest shotRequest = new ShotRequest(this, client, weaponId,
                roomEnemy.getId(), isPaidShot, bulletId, weaponPrice,
                currentX, currentY, metric);

        int rid = send(shotRequest);

        ricochetBullet.setEnabled(false);
        ricochetBullet.setRid(rid);
    }

    public boolean sendBullet() {
        boolean bulletSent = false;
        lock.lock();
        try {
            getLogger().debug("sendBullet: enemies: {}", enemies.size());
            removeDeadEnemies();

            if (!enemies.isEmpty()) {

                findUpdateFocusedEnemy();

                if (focusedEnemy != null) {
                    boolean isPaidShot = activeWeaponIsPaid();

                    String metric = activeWeaponId == DEFAULT_WEAPON_ID ?
                            "PISTOL" :
                            SpecialWeaponType.values()[activeWeaponId].name();

                    getLogger().debug("sendBullet: botId={}, nickname={}, isPaidShot: {}, activeWeaponId: {}, weaponType={}",
                            getId(), getNickname(), isPaidShot, activeWeaponId, metric);

                    if (!isPaidShot) {
                        strategy.consumeAmmo(activeWeaponId);
                    }

                    if (activeWeaponId == SpecialWeaponType.Landmines.getId()) {
                        send(new MinesRequest(this, client, isPaidShot));
                    } else {
                        String currentBulletId = getBulletId();
                        Integer weaponPrice = 1;

                        float currentX = 0;
                        float currentY = 0;

                        if(transitionPositionsList != null) {
                            if(!transitionPositionsList.isEmpty()) {
                                Point transitionPosition = transitionPositionsList.get(0);
                                currentX = (float)transitionPosition.getX();
                                currentY = (float)transitionPosition.getY();
                                transitionPositionsList.remove(0);
                            }

                            if(transitionPositionsList.isEmpty()) {
                                transitionPositionsList = null;
                            }
                        } else if (focusedEnemy.getTrajectory() != null && !focusedEnemy.getTrajectory().getPoints().isEmpty()) {
                            PointD currentPoint = getEnemyPosition(focusedEnemy);
                            if (currentPoint != null) {
                                currentX = (float) currentPoint.x;
                                currentY = (float) currentPoint.y;

                                GameType gameType = GameType.getByGameId(this.getLobbyBot().getGameId());
                                if(gameType == GameType.BG_DRAGONSTONE && focusedEnemy.getTypeId() == ENEMY_DRAGON_TYPE_ID) {
                                    int deltaX = RNG.nextInt(-50, 50);
                                    currentX += deltaX;
                                }
                            }
                        }

                        if (activeWeaponId == DEFAULT_WEAPON_ID || activeWeaponId == SpecialWeaponType.LevelUp.getId()) {

                            setState(BotState.WAITING_FOR_RESPONSE, "sendBullet");
                            sendBullet(currentBulletId, currentX, currentY, isPaidShot, weaponPrice, this, metric);
                            bulletSent = true;
                        }
                    }
                } else {
                    getLogger().debug("sendBullet: Not found enemy with allow location, enemies size: {} ", enemies.size());
                }
            } else {
                getLogger().debug("sendBullet: No enemies");
            }

        } catch (Exception e) {
            getLogger().error("sendBullet: Unexpected error on shot, focusedEnemy={}", focusedEnemy, e);
        } finally {
            lock.unlock();
        }
        return bulletSent;
    }

    @Override
    //true if shot sent
    public boolean shot() {
        boolean shotSent = false;
        lock.lock();
        try {
            getLogger().debug("shot: enemies: {}", enemies.size());
            removeDeadEnemies();

            if (!enemies.isEmpty()) {

                findUpdateFocusedEnemy();

                if (focusedEnemy != null) {
                    boolean isPaidShot = activeWeaponIsPaid();

                    String metric = activeWeaponId == DEFAULT_WEAPON_ID ?
                            "PISTOL" :
                            SpecialWeaponType.values()[activeWeaponId].name();

                    getLogger().debug("shot: botId={}, nickname={}, isPaidShot: {}, activeWeaponId: {}, weaponType={}",
                            getId(), getNickname(), isPaidShot, activeWeaponId, metric);

                    if (!isPaidShot) {
                        strategy.consumeAmmo(activeWeaponId);
                    }

                    if (activeWeaponId == SpecialWeaponType.Landmines.getId()) {
                        send(new MinesRequest(this, client, isPaidShot));
                    } else {

                        Integer weaponPrice = 1;

                        float currentX = 0;
                        float currentY = 0;

                        if(transitionPositionsList != null) {
                            if(!transitionPositionsList.isEmpty()) {
                                Point transitionPosition = transitionPositionsList.get(0);
                                currentX = (float)transitionPosition.getX();
                                currentY = (float)transitionPosition.getY();
                                transitionPositionsList.remove(0);
                            }

                            if(transitionPositionsList.isEmpty()) {
                                transitionPositionsList = null;
                            }
                        } else if (focusedEnemy.getTrajectory() != null && !focusedEnemy.getTrajectory().getPoints().isEmpty()) {
                            PointD currentPoint = getEnemyPosition(focusedEnemy);
                            if (currentPoint != null) {
                                currentX = (float) currentPoint.x;
                                currentY = (float) currentPoint.y;

                                GameType gameType = GameType.getByGameId(this.getLobbyBot().getGameId());
                                if(gameType == GameType.BG_DRAGONSTONE && focusedEnemy.getTypeId() == ENEMY_DRAGON_TYPE_ID) {
                                    int deltaX = RNG.nextInt(-50, 50);
                                    currentX += deltaX;
                                }
                            }
                        }

                        setState(BotState.WAITING_FOR_RESPONSE, "shot");
                        sendShot(currentX, currentY, weaponPrice, isPaidShot, metric);
                        shotSent = true;
                    }
                } else {
                    getLogger().debug("Not found enemy with allow location, enemies size: {} ", enemies.size());
                }
            } else {
                getLogger().debug("No enemies");
            }

        } catch (Exception e) {
            getLogger().error("Unexpected error on shot, focusedEnemy={}", focusedEnemy, e);
        } finally {
            lock.unlock();
        }
        return shotSent;
    }

    @Override
    public boolean isMqbBattleBot() {
        return true;
    }

    @Override
    public void restart() {
        getLogger().debug("MQB bot not need restart");
    }
}
