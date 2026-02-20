package com.betsoft.casino.bots;

import com.betsoft.casino.bots.handlers.*;
import com.betsoft.casino.bots.requests.*;
import com.betsoft.casino.bots.strategies.IRoomBotStrategy;
import com.betsoft.casino.mp.common.math.SWPaidCosts;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.web.IConfigurableWebSocketHandler;
import com.betsoft.casino.mp.web.IMessageSerializer;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class UnifiedBot extends AbstractBot implements IUnifiedBot {
    private String nickname;
    private final int gameId;
    private final IUnifiedBotStrategy botStrategy;
    private final AtomicBoolean scheduledDoSleepAction = new AtomicBoolean(false);
    private final AtomicInteger currentAstronautCounter = new AtomicInteger(1);
    private volatile BotState state = BotState.IDLE;
    private long lastStateChangeDate = System.currentTimeMillis();
    private ICrashGameInfo roomInfo;
    private Long minStake;
    private Long maxStake;
    private double currentMultiplier;
    private List<AstronautBetData> astronautsData;
    private long roomId = -1;
    private boolean specificRoom = false;
    private int roundsCount;

    public UnifiedBot(String nickname, String id, String url, int gameId, int bankId, int serverId, String sessionId,
                      IMessageSerializer serializer,
                      Function<Void, Integer> shutdownCallback, IUnifiedBotStrategy botStrategy, Function<Void, Integer> startCallback) {
        super(id, url, serverId, bankId, sessionId, serializer, shutdownCallback, startCallback);
        this.nickname = nickname;
        this.gameId = gameId;
        this.botStrategy = botStrategy;
        this.astronautsData = new ArrayList<>();
        roundsCount = 0;
    }

    @Override
    protected void registerServerMessageHandlers() {
        serverMessageHandlers.put(com.betsoft.casino.mp.transport.Stats.class, new StatsLobbyHandler(this));
        serverMessageHandlers.put(SitInResponse.class, new SitInHandler(this));
        serverMessageHandlers.put(SitOutResponse.class, new SitOutHandler(this));
        serverMessageHandlers.put(GameStateChanged.class, new GameStateChangedHandler(this));
        serverMessageHandlers.put(RoundResult.class, new RoundResultHandler(this));
        serverMessageHandlers.put(Error.class, new RoomErrorHandler(this));
        serverMessageHandlers.put(EnemyDestroyed.class, new EnemyDestroyedHandler(this));
        serverMessageHandlers.put(BalanceUpdated.class, new BalanceUpdatedHandler(this));
        serverMessageHandlers.put(Awards.class, new AwardsHandler(this));
        serverMessageHandlers.put(Weapons.class, new WeaponsHandler(this));
        serverMessageHandlers.put(WeaponSwitched.class, new WeaponSwitchedHandler(this));
        serverMessageHandlers.put(ChangeMap.class, new ChangeMapHandler(this));
        serverMessageHandlers.put(RoundFinishSoon.class, new RoundFinishSoonHandler(this));
        serverMessageHandlers.put(CrashStateInfo.class, new CrashStateInfoHandler(this));
        serverMessageHandlers.put(CrashCancelBetResponse.class, new CrashCancelBetHandler(this));
        serverMessageHandlers.put(CrashBetResponse.class, new CrashBetResponseHandler(this));
        serverMessageHandlers.put(CrashAllBetsResponse.class, new CrashAllBetsHandler(this));
        serverMessageHandlers.put(CrashAllBetsRejectedDetailedResponse.class, new CrashAllBetsRejectedDetailedHandler(this));

    }

    @Override
    protected void sendInitialRequest() {
        sleep(300).subscribe(t -> send(new EnterLobbyRequest(this, client, gameId, serverId, sessionId)));
    }

    @Override
    public void sendGetStartGameUrlRequest() {
        sendOpenRoomRequest();
    }

    private void sendOpenRoomRequest() {
        sleep(300).subscribe(t -> send(new OpenRoomRequest(this, client, roomId, sessionId, serverId, MoneyType.REAL, "en")));
    }

    @Override
    public void restart() {
        getLogger().debug("restart: start");
        stop();
        getLogger().debug("restart: client.isDisconnected()={}", client.isDisconnected());
        sleep(500).subscribe(t -> startWithOpenRoom());
        getLogger().debug("restart: end");
    }

    public void startWithOpenRoom() {
        getLogger().debug("startWithOpenRoom: Starting bot with params ws='{}', sid='{}', server={}", url, sessionId, serverId);
        started = true;
        if (stats == null) {
            stats = new Stats();
        }
        requests.clear();
        webSocketClient = new ReactorNettyWebSocketClient();

        IConfigurableWebSocketHandler webSocketHandler = new IConfigurableWebSocketHandler() {
            @Override
            public Mono<Void> handle(WebSocketSession session) {
                try {
                    // Raise WS frame size limit
                    changeDefaultConfig(session, false, LOG);
                } catch (Exception e) {
                    LOG.error("handle: Exception", e);
                }

                Mono<Void> outbound = session.send(
                        Flux.create((FluxSink<WebSocketMessage> sink) ->
                                        createClient(session, sink, getBankId()), FluxSink.OverflowStrategy.BUFFER
                                )
                                .doFinally(s ->
                                        closeConnection(session)
                                )
                );

                Mono<Void> inbound = session.receive()
                        .doOnNext(message ->
                                processMessage(session, message))
                        .then();

                return Mono.when(outbound, inbound);
            }
        };

        //noinspection NullableProblems
        webSocketClient.execute(URI.create(url), webSocketHandler).subscribe();
        startCallback.apply(null);
    }

    protected void createClientAndEnter(WebSocketSession session, FluxSink<WebSocketMessage> sink, int bankId) {
        client = new SocketClient(session, sink, serializer, bankId, LOG);
        getLogger().debug("createClient: new client created, sendInitialRequest");
        sleep(10).subscribe(t -> send(new EnterLobbyRequest(this, client, gameId, serverId, sessionId)));
    }

    @Override
    public void pickNickname(boolean retry, String enterLobbyNickname) {
        nickname = enterLobbyNickname;
    }

    @Override
    public void pickAvatar() {
    }

    @Override
    public void setStakes(List<Float> stakes) {
    }

    @Override
    public void setStakesLimit(int limit) {
    }

    @Override
    public void setWeaponPrices(List<SWPaidCosts> weaponPrices) {
    }

    @Override
    public Integer getWeaponPriceById(int id) {
        return null;
    }

    @Override
    public void setBalance(Long balance) {
    }

    @Override
    public long getBalance() {
        return 0;
    }

    @Override
    public IRoomBotStrategy getStrategy() {
        return botStrategy;
    }

    @Override
    public void clearAmmo() {
    }

    @Override
    public void addEnemy(IRoomEnemy enemy) {
    }

    @Override
    public void addEnemies(List<IRoomEnemy> enemies) {
    }

    @Override
    public void removeEnemy(long enemyId) {
    }

    @Override
    public void setRoomEnemies(List<RoomEnemy> enemies) {
    }

    @Override
    public void setRoomInfo(IGetRoomInfoResponse roomInfo) {
    }

    @Override
    public void setRoomInfo(ICrashGameInfo roomInfo) {
        this.roomInfo = roomInfo;
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public void setRoomState(RoomState state) {
        this.roomInfo.setState(state);
    }

    @Override
    public RoomState getRoomState() {
        return roomInfo == null ? null : roomInfo.getState();
    }

    @Override
    public void setState(BotState state, String reason) {
        getLogger().debug("setState: {}, reason={}", state, reason);
        if (this.state != state) {
            lastStateChangeDate = System.currentTimeMillis();
        }
        this.state = state;
    }

    @Override
    public BotState getState() {
        return state;
    }

    @Override
    public long getLastStateChangeDate() {
        return lastStateChangeDate;
    }

    @Override
    public void clearShotRequests() {
    }

    @Override
    public boolean shot() {
        return false;
    }

    @Override
    public void sendCloseRoomRequest() {
        send(new CloseRoomRequest(this, client, roomInfo.getRoomId()));
    }

    @Override
    public void sendSitOutRequest() {
    }

    @Override
    public void sendSitInRequest(int failedCount) {
        setState(BotState.WAITING_FOR_RESPONSE, "unified sendSitInRequest");
        send(new CrashSitInRequest(this, client, "en"));
    }

    @Override
    public void sendBuyInRequest(int failedCount) {
    }

    @Override
    public void setNickname(String nickname) {
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void activateWeapon(int weaponId) {
    }

    @Override
    public void addWeapon(int weaponId, int shots) {
    }

    @Override
    public void updateWeapon(int weaponId, int shots) {
    }

    @Override
    public double getRoomStake() {
        return 0;
    }

    @Override
    public void doActionWithSleep(String debugInfo) {
    }

    public boolean isSpecificRoom() {
        return specificRoom;
    }

    public void setSpecificRoom(boolean specificRoom) {
        this.specificRoom = specificRoom;
    }

    public void setRoomFromArgs(long roomId) {
        this.roomId = roomId;
    }

    public long getRoomId() {
        return roomId;
    }

    public int getRoundsCount() {
        return roundsCount;
    }

    public void setRoundsCount(int roundsCount) {
        this.roundsCount = roundsCount;
    }

    protected void doAction(String debugInfo) {
        RoomState roomState = roomInfo.getState();
        getLogger().debug("doAction: bot: {}, getSeatId(): {}, debugInfo={}, state: {}, roomState={}", getId(), getSeatId(), debugInfo,
                state, roomState);
        scheduledDoSleepAction.set(false);
        boolean needSleepAndRetry = true;
        long sleepPause = 3000L;
        switch (state) {
            case WAITING_FOR_RESPONSE:
                break;
            case IDLE:
/*                openRoom();
                needSleepAndRetry = false;
                break;*/
                break;
            case OBSERVING:
                if (roomState.equals(RoomState.WAIT)) {
                    int numberRoundBeforeRestart = ((IUnifiedBotStrategy) getStrategy()).getNumberRoundBeforeRestart();
                    if (roundsCount > numberRoundBeforeRestart) {
                        getLogger().debug("doActionWithSleep: roundsCount: {} more allowed {}, need restart debugInfo{}", roundsCount,
                                numberRoundBeforeRestart, debugInfo);
                        setState(BotState.IDLE, "Need restart for bot: " + getId());
                        send(new SitOutRequest(this, client));
                    } else {
                        sendSitInRequest(0);
                    }
                    needSleepAndRetry = false;
                }
                astronautsData.clear();
                currentAstronautCounter.set(0);
                break;
            case PLAYING:
                if (RoomState.WAIT.equals(roomState)) {
                    AstronautBetData newAstronautBetData = null;
                    if (currentAstronautCounter.get() == 0) {
                        roundsCount++;
                        newAstronautBetData = ((IUnifiedBotStrategy) getStrategy()).generateMultiplierForFirst(nickname);
                    } else if (currentAstronautCounter.get() == 1) {
                        newAstronautBetData = ((IUnifiedBotStrategy) getStrategy()).generateMultiplierForSecond(nickname);
                    } else if (currentAstronautCounter.get() == 2) {
                        newAstronautBetData = ((IUnifiedBotStrategy) getStrategy()).generateMultiplierForThird(nickname);
                    }
                    if (newAstronautBetData != null) {
                        setState(BotState.WAITING_FOR_RESPONSE, "betRequest");
                        astronautsData.add(newAstronautBetData);
                        getLogger().debug("new newAstronautBetData added {}, astronautsData size{}: ", newAstronautBetData, astronautsData.size());
                        double sentMultiplier = newAstronautBetData.isNeedAutoEject() ? newAstronautBetData.getMultiplierForCancelOrEject() : 0.0;
                        send(new CrashBetRequest(this, client, maxStake.intValue(), sentMultiplier, newAstronautBetData.getEjectBetId()));
                        sleepPause = 500L;
                    }
                } else if (RoomState.PLAY.equals(roomState)) {
                    for (AstronautBetData astronautsDatum : astronautsData) {
                        if (!astronautsDatum.isNeedAutoEject() && currentMultiplier >= astronautsDatum.getMultiplierForCancelOrEject()
                                && !astronautsDatum.isCancelled()) {
                            setState(BotState.WAITING_FOR_RESPONSE, "crashCancelBetRequest");
                            astronautsDatum.setCancelled(true);
                            send(new CrashCancelBetRequest(astronautsDatum.getEjectBetId(), false, this, client));
                            break;
                        }
                    }
                }
        }
        if (needSleepAndRetry) {
            sleep(sleepPause).subscribe(t -> doAction("from doAction()"));
        }
    }

    public void doActionWithSleep(long waitTime, String debugInfo) {
        doActionWithSleep(waitTime, debugInfo, true);
    }

    public void doActionWithSleep(long waitTime, String debugInfo, boolean logDebug) {
        lock.lock();
        try {
            if (scheduledDoSleepAction.get()) {
                getLogger().warn("doActionWithSleep: found already scheduled doAction: debugInfo={}", debugInfo);
            } else {
                sleep(waitTime).subscribe(t -> doAction(debugInfo));
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
    public void openNewRoom() {
        long roomId = -1;
        if (isSpecificRoom()) {
            roomId = this.roomId;
        }
        long finalRoomId = roomId;
        sleep(300).subscribe(t -> send(new OpenRoomRequest(this, client, finalRoomId, sessionId, serverId, MoneyType.REAL, "en")));
    }

    @Override
    public void setDefaultWeapon() {
    }

    @Override
    public int getCurrentBeLevel() {
        return 0;
    }

    @Override
    public void setCurrentBeLevel(int currentBeLevel) {
    }

    @Override
    public int getServerAmmo() {
        return 0;
    }

    @Override
    public void setServerAmmo(int serverAmmo) {
    }

    @Override
    public void addServerAmmo(int serverAmmo) {
    }

    @Override
    public int getSeatId() {
        return 0;
    }

    @Override
    public void setSeatId(int seatId) {
    }

    @Override
    public void setMinStake(Long minStake) {
        this.minStake = minStake;
    }

    @Override
    public void setMaxStake(Long maxStake) {
        this.maxStake = maxStake;
    }

    @Override
    public void setCurrentMultiplier(double currentMultiplier) {
        this.currentMultiplier = currentMultiplier;
    }

    @Override
    public void incrementAstronaut() {
        currentAstronautCounter.incrementAndGet();
    }

    @Override
    public int getAstronautsCount() {
        return currentAstronautCounter.get();
    }
}
