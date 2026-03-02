package com.betsoft.casino.bots;

import com.betsoft.casino.bots.handlers.BattlegroundScoreBoardHandler;
import com.betsoft.casino.bots.handlers.CancelBattlegroundRoundHandler;
import com.betsoft.casino.bots.handlers.KingOfHillChangedHandler;
import com.betsoft.casino.bots.requests.*;
import com.betsoft.casino.bots.strategies.BaseMetricTimeKey;
import com.betsoft.casino.bots.strategies.IRoomBotStrategy;
import com.betsoft.casino.bots.strategies.IRoomNaturalBotStrategy;
import com.betsoft.casino.mp.common.Coords;
import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.model.IGetRoomInfoResponse;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.mp.model.MoneyType;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.model.battleground.IRoomBattlegroundInfo;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointD;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

public class BattleGroundRoomBot extends RoomBot {
    protected long startRoundDateTimeLocal;
    protected long endRoundDateTimeLocal;
    protected long stopShotDateTimeLocal;
    protected boolean battlegroundBuyInConfirmed;
    protected IRoomBattlegroundInfo battlegroundInfo;
    protected long lastTimeFullGameInfo;
    protected boolean sitInAppeared;
    protected boolean needReBuyInRoom;
    protected boolean openRoomAppeared;
    protected long lastSwitchWeaponRequestTime;
    static Map<Integer, GameMapShape> possibleMaps;
    protected transient Coords coords;
    long timeSitInRequest;
    long timeReBuyInRequest;
    boolean needRandomExitAfterSitInOrReBuyIn;

    public BattleGroundRoomBot(LobbyBot lobbyBot, String id, String url, int serverId, String sessionId, IMessageSerializer serializer,
                               long roomId, long balance, float stake, String nickName, IRoomBotStrategy strategy,
                               Function<Void, Integer> shutdownCallback, Function<Void, Integer> startCallback) {
        super(lobbyBot, id, url, serverId, sessionId, serializer, roomId, balance, stake, nickName, strategy, shutdownCallback, startCallback);
        battlegroundBuyInConfirmed = false;
        startRoundDateTimeLocal = 0;
        endRoundDateTimeLocal = 0;
        stopShotDateTimeLocal = 0;
        sitInAppeared = false;
        needReBuyInRoom = false;
        openRoomAppeared = false;
        lastSwitchWeaponRequestTime = 0L;
        if (possibleMaps == null) {
            possibleMaps = new HashMap<>();
        }
        getLogger().debug("BattleGroundRoomBot init url:  {}", getUrl());
    }

    @Override
    public void stop() {
        super.stop();
        battlegroundBuyInConfirmed = false;
        startRoundDateTimeLocal = 0;
        endRoundDateTimeLocal = 0;
        stopShotDateTimeLocal = 0;
        sitInAppeared = false;
        needReBuyInRoom = false;
        openRoomAppeared = false;
    }

    @Override
    protected void registerServerMessageHandlers() {
        super.registerServerMessageHandlers();
        serverMessageHandlers.put(CancelBattlegroundRound.class, new CancelBattlegroundRoundHandler(this));
        serverMessageHandlers.put(BattlegroundScoreBoard.class, new BattlegroundScoreBoardHandler(this));
        serverMessageHandlers.put(KingOfHillChanged.class, new KingOfHillChangedHandler(this));
    }

    @Override
    public void sendSitInRequest(int failedCount) {
        setState(BotState.WAITING_FOR_RESPONSE, "battle sendSitInRequest");
        send(new SitInRequest(this, client, 0, battlegroundInfo.getBuyIn(), "en", failedCount));
        timeSitInRequest = System.currentTimeMillis();
    }


    @Override
    public void sendReBuyInRequest(int failedCount) {
        setState(BotState.WAITING_FOR_RESPONSE, "battle sendReBuyInRequest");
        send(new ReBuyInRequest(this, client, failedCount));
        timeReBuyInRequest = System.currentTimeMillis();
    }

    public boolean isNeedRandomExitAfterSitInOrReBuyIn() {
        return needRandomExitAfterSitInOrReBuyIn;
    }

    public void setNeedRandomExitAfterSitInOrReBuyIn(boolean needRandomExitAfterSitInOrReBuyIn) {
        this.needRandomExitAfterSitInOrReBuyIn = needRandomExitAfterSitInOrReBuyIn;
    }

    @Override
    public void setRoomInfo(IGetRoomInfoResponse roomInfo) {
        this.roomInfo = roomInfo;
        this.battlegroundInfo = roomInfo.getBattlegroundInfo();
    }

    public void updateRoundStartTime(long messageDateTime, long messageDateTimeToStart) {
        if(messageDateTimeToStart == 0) { // start round time is not defined
            startRoundDateTimeLocal = 0;
        } else {
            long deltaTimeStart = messageDateTimeToStart - messageDateTime;
            startRoundDateTimeLocal = System.currentTimeMillis() + deltaTimeStart;
        }

        getLogger().debug("updateRoundStartTime: messageDateTime: {}, messageDateTimeToStart(): {}, startRoundDateTimeLocal={}",
                toHumanReadableFormat(messageDateTime), toHumanReadableFormat(messageDateTimeToStart), toHumanReadableFormat(startRoundDateTimeLocal));
    }

    public void updateRoundEndTime(long messageDateTime, long messageDateTimeToEnd) {
        if(messageDateTimeToEnd == 0) { // end round time is not defined
            endRoundDateTimeLocal = 0;
            stopShotDateTimeLocal = 0;
        } else {
            long deltaTimeEnd = messageDateTimeToEnd - messageDateTime;
            endRoundDateTimeLocal = System.currentTimeMillis() + deltaTimeEnd;
            //stop shooting should happen before round ends
            stopShotDateTimeLocal = endRoundDateTimeLocal - RNG.nextInt(1000, 1500);
        }

        getLogger().debug("updateRoundEndTime: messageDateTime: {}, messageDateTimeToEnd(): {}, endRoundDateTimeLocal={}, stopShotDateTimeLocal={}",
                toHumanReadableFormat(messageDateTime), toHumanReadableFormat(messageDateTimeToEnd), toHumanReadableFormat(endRoundDateTimeLocal), toHumanReadableFormat(stopShotDateTimeLocal));
    }

    public void sendConfirmBattlegroundBuyIn() {
        setState(BotState.WAITING_FOR_RESPONSE, "battle confirmBattlegroundBuyIn");
        send(new ConfirmBattlegroundBuyInRequest(this, client));
    }

    public void sendGetFullGameInfoRequest() {
        send(new GetFullGameInfoRequest(this, client));
    }

    @Override
    public void sendSitOutRequest() {
        setState(BotState.WAITING_FOR_RESPONSE, "battle sendSitOutRequest");
        battlegroundBuyInConfirmed = false;
        send(new SitOutRequest(this, client));
    }

    public boolean isSitInAppeared() {
        return sitInAppeared;
    }

    public void setSitInAppeared(boolean sitInAppeared) {
        this.sitInAppeared = sitInAppeared;
    }

    public boolean isNeedReBuyInRoom() {
        return needReBuyInRoom;
    }

    public void setNeedReBuyInRoom(boolean needReBuyInRoom) {
        this.needReBuyInRoom = needReBuyInRoom;
    }

    @Override
    protected void openRoom() {
        if (getRoomId() > 0) {
            long currentTimeMillis = System.currentTimeMillis();
            if (lastReceivedServerTime != 0 && lastReceivedServerTime < currentTimeMillis) {
                long diff = currentTimeMillis - lastReceivedServerTime;
                long sleepTime = 300 - diff;
                getLogger().debug("lastReceivedServerTime: {}, diff: {}, sleepTime: {}", lastReceivedServerTime, diff, sleepTime);
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        getLogger().warn("OpenRoom sleep error");
                    }
                }
            }
            setState(BotState.WAITING_FOR_RESPONSE, "BattleGroundRoomBot openRoom");
            send(new OpenRoomRequest(this, client, getRoomId(), sessionId, serverId, MoneyType.REAL, "en"));
        } else {
            //this may be normal, roomId set by LobbyBot after cal GetStartGameUrl
            getLogger().warn("Cannot send OpenRoom request, roomId=0");
        }

    }

    public boolean isOpenRoomAppeared() {
        return openRoomAppeared;
    }

    public void setOpenRoomAppeared(boolean openRoomAppeared) {
        this.openRoomAppeared = openRoomAppeared;
    }

    @Override
    protected void doAction(String debugInfo) {

        long currentTimeMillis = System.currentTimeMillis();
        long lastTimeFullGameInfo = getLastTimeFullGameInfo();
        long startRoundTimeLocal = getStartRoundDateTimeLocal();
        BotState botState = this.getState();

        switch (botState) {

            case WAITING_FOR_RESPONSE:
                break;

            case WAIT_BATTLE_PLAYERS: {
                if(isNeedRandomExitAfterSitInOrReBuyIn()){
                    boolean needExitAfterSinIn = timeSitInRequest > 0 && currentTimeMillis > timeSitInRequest + 1000 + RNG.nextInt(2000);
                    boolean needExitAfterBuyIn = timeReBuyInRequest > 0 && currentTimeMillis > timeReBuyInRequest + 1000 + RNG.nextInt(2000);
                    if(needExitAfterSinIn || needExitAfterBuyIn){
                        getLogger().debug("doAction: need SitOut bot: {}, getSessionId(): {}, needExitAfterSinIn={}, needExitAfterBuyIn: {} state: {}",
                                getId(), getSessionId(), needExitAfterSinIn, needExitAfterBuyIn, botState);
                        sendSitOutRequest();
                    }
                }
                break;
            }

            case IDLE:
                if (!isOpenRoomAppeared()) {
                    openRoom();
                } else {
                    setState(BotState.OBSERVING, "Open room not need make");
                }
                break;

            case NEED_SIT_OUT:
                sendSitOutRequest();
                break;

            case OBSERVING: {
                if (startRoundTimeLocal == 0 || startRoundTimeLocal < currentTimeMillis) {
                    if (currentTimeMillis > lastTimeFullGameInfo + 500 || lastTimeFullGameInfo == 0) {
                        setLastTimeFullGameInfo(currentTimeMillis);
                        getLogger().debug("---------------------OBSERVING Usual BattleGroundRoomBot doAction: bot: {},  endRoundTimeLocal: {}, " +
                                        "currentTimeMillis: {}, lastTimeFullGameInfo: {}, confirmBattlegroundBuyIn: {}, needReBuyInRoom: {}",
                                getId(), startRoundTimeLocal, currentTimeMillis, lastTimeFullGameInfo, battlegroundBuyInConfirmed, needReBuyInRoom);
                        sendGetFullGameInfoRequest();
                    }
                } else {
                    RoomState roomState = roomInfo.getState();
                    if (!isSitInAppeared()) {
                        if (!battlegroundBuyInConfirmed) {
                            sendConfirmBattlegroundBuyIn();
                        } else {
                            if (roomState.equals(RoomState.WAIT)) {
                                sendSitInRequest(0);
                                setSitInAppeared(true);
                            }
                            getLogger().debug("doAction: wait other players bot: {}, getSeatId(): {}, debugInfo={}, state: {}",
                                    getId(), getSeatId(), debugInfo, botState);
                        }
                    } else if (isNeedReBuyInRoom()) {
                        sendReBuyInRequest(0);
                        setNeedReBuyInRoom(false);
                    }
                }
                break;
            }

            case PLAYING:
                RoomState roomState = roomInfo.getState();
                boolean shotIsAllowed = this.shotIsAllowed();

                if (roomState == RoomState.PLAY && shotIsAllowed) {
                    IRoomNaturalBotStrategy roomNaturalBotStrategy = (IRoomNaturalBotStrategy) getStrategy();
                    long minResponceByType = roomNaturalBotStrategy.getTimesMinResponseByType(BaseMetricTimeKey.PLAY_STARTED.name());
                    long startTimePlay = roomNaturalBotStrategy.getTimesShootLastResponseByType(BaseMetricTimeKey.PLAY_STARTED.name());
                    boolean allowShotInBattle = startTimePlay != 0 && currentTimeMillis > startTimePlay + minResponceByType;

                    getLogger().debug("doAction: currentBeLevel: {}, strategy.requestedBetLevel(): {}, " +
                                    "serverAmmo: {}, allowShotInBattle: {}, currentTimeMillis: {}, startTimePlay: {}, minResponceByType: {}",
                            currentBeLevel, strategy.requestedBetLevel(), getServerAmmo(), allowShotInBattle, currentTimeMillis,
                            startTimePlay, minResponceByType);

                    if (!strategy.botHasSpecialWeapons() && currentBeLevel != strategy.requestedBetLevel()) {
                        send(new BetLevelRequest(this, client));
                    } else if (strategy.shouldSwitchWeapon()) {
                        int weaponId = strategy.getWeaponId();
                        long diff = System.currentTimeMillis() - lastSwitchWeaponRequestTime;
                        setState(BotState.WAITING_FOR_RESPONSE, "switchWeapon, weaponId:  " + weaponId);
                        long sleepTime = 300 - diff;
                        getLogger().debug("need wait next switch weapon, diff: {}, lastSwitchWeaponRequestTime: {}, sleepTime: {} ",
                                diff, lastSwitchWeaponRequestTime, sleepTime);
                        if (diff > 0 && diff < 300) {
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        lastSwitchWeaponRequestTime = System.currentTimeMillis();
                        send(new SwitchWeaponRequest(this, client, weaponId));
                    } else if (allowShotInBattle && strategy.shouldShoot(getId())) {
                        shot();
                    }
                }
                break;
        }
        scheduledDoSleepAction.set(false);
        if (started) {
            doActionWithSleep(strategy.getWaitTime(), "BattleGroundRoomBot from doAction()", false);
        }
    }

    public long getStartRoundDateTimeLocal() {
        return startRoundDateTimeLocal;
    }

    public void setStartRoundDateTimeLocal(long startRoundDateTimeLocal) {
        this.startRoundDateTimeLocal = startRoundDateTimeLocal;
    }

    public long getEndRoundDateTimeLocal() {
        return endRoundDateTimeLocal;
    }

    public void setEndRoundDateTimeLocal(long endRoundDateTimeLocal) {
        this.endRoundDateTimeLocal = endRoundDateTimeLocal;
    }

    public long getStopShotDateTimeLocal() {
        return stopShotDateTimeLocal;
    }

    public void setStopShotDateTimeLocal(long stopShotDateTimeLocal) {
        this.stopShotDateTimeLocal = stopShotDateTimeLocal;
    }

    public boolean isStopShotBeforeRoundEnds() {
        if(stopShotDateTimeLocal == 0) { //not defined
            return false;
        }
        long now = System.currentTimeMillis();
        return now > stopShotDateTimeLocal; //consider round time up 1 second before round ends
    }

    public long getLastTimeFullGameInfo() {
        return lastTimeFullGameInfo;
    }

    public void setLastTimeFullGameInfo(long lastTimeFullGameInfo) {
        this.lastTimeFullGameInfo = lastTimeFullGameInfo;
    }

    public boolean isBattlegroundBuyInConfirmed() {
        return battlegroundBuyInConfirmed;
    }

    public void setBattlegroundBuyInConfirmed(boolean battlegroundBuyInConfirmed) {
        this.battlegroundBuyInConfirmed = battlegroundBuyInConfirmed;
    }

    public IRoomBattlegroundInfo getBattlegroundInfo() {
        return battlegroundInfo;
    }

    public void setBattlegroundInfo(IRoomBattlegroundInfo battlegroundInfo) {
        this.battlegroundInfo = battlegroundInfo;
    }

    @Override
    public boolean isBattleBot() {
        return true;
    }

    @Override
    public boolean activeWeaponIsPaid() {
        return false;
    }

    @Override
    public long getWaitTimeAfterSwitchWeapon() {
        return 200;
    }

    @Override
    public boolean isUsualActionBot() {
        return false;
    }


    public Coords getCoords(GameMapShape gameMapShape) {
        if (coords == null) {
            coords = new Coords(SCREEN_WIDTH, SCREEN_HEIGHT, gameMapShape.getWidth(), gameMapShape.getHeight());
        }
        return coords;
    }

    private Point getMapCoordsForEnemy(RoomEnemy enemy, GameMapShape currentMap, long serverTime) {
        if (currentMap != null) {
            PointD location = getLocation(enemy.getTrajectory().getPoints(), serverTime);
            Coords coords = getCoords(currentMap);
            double mapCurrentX = coords.toX(location.x + 0.5, location.y + 0.5);
            double mapCurrentY = coords.toY(location.x + 0.5, location.y + 0.5);
            return new Point(mapCurrentX, mapCurrentY, serverTime);
        } else {
            return null;
        }
    }

    public int getKey(String filename) {
        return Integer.parseInt(filename.substring(0, filename.indexOf('.')));
    }

    private GameMapShape getCurrentMap(int currentMapId) {
        GameMapShape gameMapShape = possibleMaps.get(currentMapId);
        if (gameMapShape == null && currentMapId != 0) {
            try {
                ClassLoader classLoader = GameMapStore.class.getClassLoader();
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
                for (Resource resource : resolver.getResources("classpath:maps/**/*.map")) {
                    String filename = resource.getFilename();
                    if (filename != null) {
                        try {
                            possibleMaps.put(getKey(filename), new GameMapShape(resource.getFilename(),
                                    new DataInputStream(resource.getInputStream())));
                        } catch (Exception e) {
                            LOG.debug("getCurrentMap: Failed to load map " + filename);
                        }
                    }
                }
                gameMapShape = possibleMaps.get(currentMapId);
            } catch (IOException e) {
                LOG.debug("getCurrentMap: not found map");
            }
        }
        return gameMapShape;
    }

    @Override
    public boolean isAllowedForShot(IRoomEnemy iRoomEnemy) {
        boolean isAllowForShot = true;

        if(iRoomEnemy != null && iRoomEnemy instanceof RoomEnemy) {

            long serverTime = System.currentTimeMillis() + getDiffLocalAndServerTime();
            int currentMapId = getCurrentMapId();
            GameMapShape currentMap = getCurrentMap(currentMapId);

            RoomEnemy roomEnemy = (RoomEnemy)iRoomEnemy;
            Point mapCoordsForEnemy = getMapCoordsForEnemy(roomEnemy, currentMap, serverTime);

            isAllowForShot  = getStrategy().isLocationOnMapAllowedForShot(
                    roomEnemy.getDetailPoints(),
                    serverTime,
                    currentMapId,
                    (int) roomEnemy.getTypeId(),
                    mapCoordsForEnemy,
                    currentMap
            );

            if(!isAllowForShot) {
                return false;
            }

            Point locationOnScreen = getStrategy().getLocationOnScreen(roomEnemy, serverTime);

            if(locationOnScreen == null) {
                return true;
            }

            isAllowForShot = isLocationAllowedForShot(roomEnemy.getId(), locationOnScreen);
        }

        return isAllowForShot;
    }

    @Override
    public IRoomEnemy getRandomEnemyWithRequestedType() {

        RoomEnemy roomEnemy = (RoomEnemy) super.getRandomEnemyWithRequestedType();
        if(roomEnemy == null) {
            return null;
        }

        return this.isAllowedForShot(roomEnemy) ? roomEnemy : null;
    }

    public PointD getLocation(List<Point> points, long time) {
        if (points.get(0).getTime() > time) {
            return new PointD(points.get(0).getX(), points.get(0).getY());
        }

        Point lastPoint = points.get(points.size() - 1);
        if (time >= lastPoint.getTime()) {
            return new PointD(lastPoint.getX(), lastPoint.getY());
        }

        int i = 1;
        while (i < points.size() && time > points.get(i).getTime()) {
            i++;
        }

        Point a = points.get(i - 1);
        Point b = points.get(i);
        double percent = ((double) (time - a.getTime())) / (b.getTime() - a.getTime());

        return new PointD(a.getX() + (b.getX() - a.getX()) * percent, a.getY() + (b.getY() - a.getY()) * percent);
    }

    public boolean shotIsAllowed() {

        boolean isRoundResultReceived = isRoundResultReceived();
        boolean isQualifyStateReceived = isQualifyStateReceived();

        //if boss is present do not allow to Stop to Shot Before Round Ends
        boolean isStopShotBeforeRoundEnds = false;
        boolean bossIsNotPresent = getEnemies().stream().noneMatch(RoomEnemy::isBoss);

        if(bossIsNotPresent) { //if boss is not present check if it is required to Stop to Shot Before Round Ends
            isStopShotBeforeRoundEnds = isStopShotBeforeRoundEnds();
        }

        if (isRoundResultReceived || isQualifyStateReceived || isStopShotBeforeRoundEnds) {
            LOG.debug("shotIsAllowed: botId={}, bossIsNotPresent={}, return false because isRoundResultReceived={}, isQualifyStateReceived={}, " +
                    "isStopShotBeforeRoundEnds={}", getId(), bossIsNotPresent, isRoundResultReceived, isQualifyStateReceived, isStopShotBeforeRoundEnds);
            return false;
        }

        boolean allowShotAfterRoundFinishSoon = getStrategy().allowShotAfterRoundFinishSoon();
        boolean isRoundFinishSoonReceived = isRoundFinishSoonReceived();
        boolean shotIsAllowed = allowShotAfterRoundFinishSoon || !isRoundFinishSoonReceived;

        if(!shotIsAllowed) {
            LOG.debug("shotIsAllowed: botId={}, bossIsNotPresent={}, return false because allowShotAfterRoundFinishSoon={}, isRoundFinishSoonReceived={}, " +
                    "shotIsAllowed={}", getId(), bossIsNotPresent, allowShotAfterRoundFinishSoon, isRoundFinishSoonReceived, shotIsAllowed);
        }

        return shotIsAllowed;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BattleGroundRoomBot{");
        sb.append("startRoundDateTimeLocal=").append(toHumanReadableFormat(startRoundDateTimeLocal));
        sb.append("endRoundDateTimeLocal=").append(toHumanReadableFormat(endRoundDateTimeLocal));
        sb.append("stopShotDateTimeLocal=").append(toHumanReadableFormat(stopShotDateTimeLocal));
        sb.append(", confirmBattlegroundBuyIn=").append(battlegroundBuyInConfirmed);
        sb.append(", lastTimeFullGameInfo=").append(lastTimeFullGameInfo);
        sb.append(", openRoomAppeared=").append(openRoomAppeared);
        sb.append(", battlegroundInfo=").append(battlegroundInfo);
        sb.append(", needReBuyInRoom=").append(needReBuyInRoom);
        sb.append(", state=").append(state);
        sb.append(", roomInfo=").append(roomInfo);
        sb.append(", strategy=").append(strategy);
        sb.append(", url='").append(url).append('\'');
        sb.append(", serverId=").append(serverId);
        sb.append(", bankId=").append(bankId);
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", stats=").append(stats);
        sb.append(", started=").append(started);
        sb.append(", needRandomExitAfterSitInOrReBuyIn=").append(needRandomExitAfterSitInOrReBuyIn);
        sb.append(", timeSitInRequest=").append(timeSitInRequest);
        sb.append(", timeReBuyInRequest=").append(timeReBuyInRequest);
        sb.append('}');
        return sb.toString();
    }
}
