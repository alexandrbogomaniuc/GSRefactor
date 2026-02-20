package com.betsoft.casino.bots.mqb;

import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.*;
import com.betsoft.casino.bots.handlers.*;
import com.betsoft.casino.bots.requests.*;
import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.betsoft.casino.bots.strategies.*;
import com.betsoft.casino.mp.common.math.SWPaidCosts;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.dgphoenix.casino.common.util.RNG;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.betsoft.casino.bots.BotState.*;
import static com.betsoft.casino.bots.utils.CrashBetKeyUtil.getCrashBetKeyFromTimeAndNickname;
import static com.betsoft.casino.bots.utils.CrashBetKeyUtil.getNicknameFromCrashBetKey;
import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

public class ManagedMaxBlastChampionsRoomBot extends AbstractBot implements IUnifiedBot, IManagedLobbyBot {

    private final int gameId;
    private final IRoomBotStrategy strategy;
    private final AtomicInteger currentAstronautCounter = new AtomicInteger(0);
    private final AtomicBoolean scheduledDoSleepAction = new AtomicBoolean(false);
    private long lastStateChangeDate = System.currentTimeMillis();
    private long lastRoomStateChangeDate = System.currentTimeMillis();
    private ICrashGameInfo roomInfo;
    private Long minStake;
    private Long maxStake;
    private long selectedBuyIn;
    private double currentMultiplier;
    private String ejectBetId = "";
    private double ejectMultiplier = Long.MAX_VALUE;
    private long ejectTimeIfBotIsLast = Long.MAX_VALUE;
    private String nickname;
    private int roomId;
    protected volatile BotState state = BotState.IDLE;
    private final String token;
    private final MQBBotServiceHandler mqbBotServiceHandler;
    private final Map<String, ManagedMaxBlastChampionsPlayer> playersCrashBets = new ConcurrentHashMap();
    private final Map<String, ManagedMaxBlastChampionsPlayer> playersCrashBetsPrevRound = new ConcurrentHashMap();
    private final Map<String, ManagedMaxBlastChampionsPlayer> playersCrashCancelBets = new ConcurrentHashMap();

    private long roundStartTime = Long.MAX_VALUE;
    private long crashBetRequestTime = Long.MAX_VALUE;
    private final int MIN_ROUNDS_TO_PLAY =20;
    private final int MAX_ROUNDS_TO_PLAY =200;
    private int roundsToPlay = 0;
    private AtomicInteger roundsCount = new AtomicInteger(0);
    private AtomicLong balance = new AtomicLong(0);

    private final long THRESHOLD_AGGRESSIVE = 4;
    private final long THRESHOLD_MEDIUM = 5;
    private final long THRESHOLD_ROCK = 6;
    private final String userName;
    private final String password;
    private final String externalId;

    public ManagedMaxBlastChampionsRoomBot(MQBBotServiceHandler mqbBotServiceHandler, String nickname, String userName,
                                           String password, String externalId, String id, String url,
                                           int serverId, int bankId, String sessionId, IMessageSerializer serializer,
                                           Function<Void, Integer> shutdownCallback, Function<Void, Integer> startCallback, int gameId,
                                           IRoomBotStrategy strategy, int roomId, String token) {

        super(id, url, serverId, bankId, sessionId, serializer, shutdownCallback, startCallback);

        this.gameId = gameId;
        this.strategy = strategy;
        this.nickname = nickname;
        this.roomId = roomId;
        this.token = token;
        this.mqbBotServiceHandler = mqbBotServiceHandler;
        this.setRoundsToPlay(RNG.nextInt(MIN_ROUNDS_TO_PLAY, MAX_ROUNDS_TO_PLAY + 1));
        this.setRoundsCount(0);
        this.userName = userName;
        this.password = password;
        this.externalId = externalId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getExternalId() {
        return externalId;
    }

    public void processCrashBet(String crashBetKey){
        RoomState roomState = this.getRoomInfo().getState();
        String nickname = getNicknameFromCrashBetKey(crashBetKey);
        //LOG.debug("processCrashBet: botId={}, nickname={}, roomState={}, crashBetKey={}, derivedNickname={}",
        //        getId(), getNickname(), roomState, crashBetKey, nickname);
        if (nickname != null && !nickname.isEmpty()) {
            if(roomState == RoomState.WAIT) {
                this.addPlayersCrashBet(nickname);
                //LOG.debug("processCrashBet: botId={}, nickname={}, derivedNickname={} added to playersCrashBets list: {}",
                //        getId(), getNickname(), nickname, playersCrashBets.keySet().toArray());
            }
        }
    }

    public void processCrashCancelBet(String crashBetId){
        RoomState roomState = this.getRoomInfo().getState();
        String nickname = getNicknameFromCrashBetKey(crashBetId);
        //LOG.debug("processCrashCancelBet: botId={}, nickname={}, roomState={}, crashBetId={}, derivedNickname={}",
        //        getId(), getNickname(), roomState, crashBetId, nickname);
        if (nickname != null && !nickname.isEmpty()) {
            if (roomState == RoomState.WAIT) {
                this.removePlayerCrashBet(nickname);
                //LOG.debug("processCrashCancelBet: botId={}, nickname={}, derivedNickname={} removed from playerCrashBets list: {}",
                //        getId(), getNickname(), nickname, playersCrashBets.keySet().toArray());
            } else if (roomState == RoomState.PLAY) {
                this.addPlayersCrashCancelBet(nickname);
                //LOG.debug("processCrashCancelBet: botId={}, nickname={}, derivedNickname={} added to playersCrashCancelBets list: {}",
                //        getId(), getNickname(), nickname, playersCrashCancelBets.keySet().toArray());
            }
        }
    }

    public int getRoundsToPlay() {
        return roundsToPlay;
    }

    public void setRoundsToPlay(int roundsToPlay) {
        this.roundsToPlay = roundsToPlay;
    }

    public void incrementRoundsCount() {
        roundsCount.incrementAndGet();
    }

    public void setRoundsCount(int roundsCount) {
        this.roundsCount.set(roundsCount);
    }

    public int getRoundsCount() {
        return roundsCount.get();
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public void setCrashBetRequestTime(long crashBetRequestTime) {
        this.crashBetRequestTime = crashBetRequestTime;
    }

    public void calcCrashBetRequestTime(long msgDateTime, long msgRoundStartTime) {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - msgDateTime;
        //LOG.debug("calcCrashBetRequestTime: currentTime={}, msgDateTime={} -> deltaTime={}",
        //        toHumanReadableFormat(currentTime), toHumanReadableFormat(msgDateTime), deltaTime);

        long roundStartTime = msgRoundStartTime + deltaTime;
        //LOG.debug("calcCrashBetRequestTime: deltaTime={}, msgRoundStartTime={} -> roundStartTime={}",
        //        deltaTime, toHumanReadableFormat(msgRoundStartTime), toHumanReadableFormat(roundStartTime));
        this.setRoundStartTime(roundStartTime);

        MaxBlastChampionsBotStrategy strategy = (MaxBlastChampionsBotStrategy) this.getStrategy();
        long crashBetRequestTime = strategy.getCrashBetRequestTime(currentTime, roundStartTime);
        this.setCrashBetRequestTime(crashBetRequestTime);

        //LOG.debug("calcCrashBetRequestTime: botId={}, nickname={}, roundsCount={}, roundsToPlay={}",
        //        getId(), getNickname(), getRoundsCount(), getRoundsToPlay());

        this.incrementRoundsCount();

        LOG.debug("calcCrashBetRequestTime: botId={}, nickname={}, crashBetRequestTime={}, roundStartTime={}, roundsCount={}, roundsToPlay={}",
                getId(), getNickname(), toHumanReadableFormat(crashBetRequestTime), toHumanReadableFormat(roundStartTime), getRoundsCount(), getRoundsToPlay());
    }

    public boolean isInBotsMap(String nickname) {
        return this.mqbBotServiceHandler.isInBotsMap(nickname);
    }

    public Map<String, ManagedMaxBlastChampionsPlayer> getPlayers() {
        return mqbBotServiceHandler.getPlayers(getRoomId());
    }

    public void addPlayer(ManagedMaxBlastChampionsPlayer player) {
        mqbBotServiceHandler.getPlayers(getRoomId()).put(player.getNickName(), player);
    }

    public void addPlayer(String nickname) {
        boolean isBot = isInBotsMap(nickname);
        ManagedMaxBlastChampionsPlayer player = new ManagedMaxBlastChampionsPlayer(nickname, isBot);
        addPlayer(player);
    }

    public void removePlayer(String nickname) {
        this.mqbBotServiceHandler.getPlayers(getRoomId()).remove(nickname);
        this.playersCrashBets.remove(nickname);
        this.playersCrashCancelBets.remove(nickname);
    }

    public void addPlayersCrashBet(ManagedMaxBlastChampionsPlayer player) {
        this.playersCrashBets.put(player.getNickName(), player);
        getLogger().debug("addPlayersCrashBet: botId={}, nickname={}: player {} is added to playersCrashBets: {}",
                getId(), getNickname(), player.getNickName(), this.playersCrashBets.keySet().toArray());
    }

    public void addPlayersCrashBet(String nickname) {
        ManagedMaxBlastChampionsPlayer player =
                this.mqbBotServiceHandler.getPlayers(getRoomId()).get(nickname);
        if(player != null) {
            addPlayersCrashBet(player);
        } else {
            getLogger().debug("addPlayersCrashBet: botId={}, nickname={}: player is null, no player {} found in {}",
                    getId(), getNickname(), nickname, this.mqbBotServiceHandler.getPlayers(getRoomId()).keySet().toArray());
        }
    }

    public void removePlayerCrashBet(String nickname) {
        this.playersCrashBets.remove(nickname);
        getLogger().debug("removePlayerCrashBet:  botId={}, nickname={}: player {} is removed from playersCrashBets: {}",
                getId(), getNickname(), nickname, this.playersCrashBets.keySet().toArray());
    }

    public void addPlayersCrashCancelBet(ManagedMaxBlastChampionsPlayer player) {
        this.playersCrashCancelBets.put(player.getNickName(), player);
        getLogger().debug("addPlayersCrashCancelBet: botId={}, nickname={}: player {} is added to addPlayersCrashCancelBet: {}",
                getId(), getNickname(), player.getNickName(), this.playersCrashCancelBets.keySet().toArray());
    }

    public void addPlayersCrashCancelBet(String nickname) {
        ManagedMaxBlastChampionsPlayer player =
                this.mqbBotServiceHandler.getPlayers(getRoomId()).get(nickname);
        if(player != null) {
            addPlayersCrashCancelBet(player);
        } else {
            getLogger().debug("addPlayersCrashCancelBet: botId={}, nickname={}: player is null, no player {} found in {}",
                    getId(), getNickname(), nickname, this.mqbBotServiceHandler.getPlayers(getRoomId()).keySet().toArray());
        }
    }

    public void clearPlayersBets() {
        //getLogger().debug("clearPlayersBets: botId={}, nickname={}: save playersCrashBets to playersCrashBetsPrevRound," +
        //                " clear playersCrashBets, playersCrashCancelBets and reset currentAstronautCounter to 0",
        //        getId(), getNickname());

        //getLogger().debug("clearPlayersBets: botId={}, nickname={}: playersCrashBets:{} playersCrashBetsPrevRound:{}",
        //        getId(), getNickname(), playersCrashBets.keySet().toArray(), playersCrashBetsPrevRound.keySet().toArray());

        //save playersCrashBets to playersCrashBetsPrevRound
        this.playersCrashBetsPrevRound.clear();
        for (Map.Entry<String,  ManagedMaxBlastChampionsPlayer> playersCrashBet : playersCrashBets.entrySet()) {
            String key = playersCrashBet.getKey();
            ManagedMaxBlastChampionsPlayer value = playersCrashBet.getValue();
            playersCrashBetsPrevRound.put(key, value);
        }

        this.playersCrashBets.clear();
        this.playersCrashCancelBets.clear();

        this.currentAstronautCounter.set(0);
        getLogger().debug("clearPlayersBets: botId={}, nickname={}: playersCrashBets:{} playersCrashBetsPrevRound:{}",
                getId(), getNickname(), playersCrashBets.keySet().toArray(), playersCrashBetsPrevRound.keySet().toArray());
    }

    public boolean realPlayerExists() {
        Map<String, ManagedMaxBlastChampionsPlayer> players = getPlayers();
        boolean realPlayerExists = players.values().stream()
                .anyMatch(p->!p.isBot());
        //getLogger().debug("realPlayerExists: {}, players: {}", realPlayerExists, players.keySet().toArray());
        return realPlayerExists;
    }

    public boolean hasPlayer(String nickname) {
        return this.mqbBotServiceHandler.getPlayers(getRoomId()).keySet().contains(nickname);
    }

    public boolean playerIsSingleInRocket(String nickname) {
        boolean playerIsSingleInRocket = false;
        //getLogger().debug("playerIsSingleInRocket: botId={}, nickname={}, playersCrashBets:{}, playersCrashCancelBets:{}",
                //getId(), getNickname(), playersCrashBets.keySet().toArray(), playersCrashCancelBets.keySet().toArray());

        List<String> playersNotEjected = new ArrayList<>();
        for (String currNickname: playersCrashBets.keySet()) {
            if(!playersCrashCancelBets.keySet().contains(currNickname)) {
                playersNotEjected.add(currNickname);
            }
        }
        if(playersNotEjected.size() == 1) {
            playerIsSingleInRocket = playersNotEjected.get(0).equals(nickname);
        }
        return playerIsSingleInRocket;
    }

    public void setSelectedBuyIn(Long selectedBuyIn) {
        if(selectedBuyIn == null) {
            this.selectedBuyIn = 0;
        } else {
            this.selectedBuyIn = selectedBuyIn;
        }
    }

    public long getRoomId() {
        return roomId;
    }

    public int getGameId() {
        return gameId;
    }

    public ICrashGameInfo getRoomInfo() {
        return roomInfo;
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

    @Override
    public IRoomBotStrategy getStrategy() {
        return strategy;
    }

    @Override
    public void setRoomInfo(ICrashGameInfo roomInfo) {
        this.roomInfo = roomInfo;
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = (int) roomId;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String getNickname() {
        return nickname;
    }


    @Override
    public void setRoomState(RoomState roomState) {
        getLogger().debug("setRoomState: botId={}, nickname={}, roomState={}, roomId={}",
                getId(), getNickname(), roomState, this.roomInfo.getRoomId());
        RoomState oldRoomState = getRoomState();
        if(oldRoomState != roomState) {
            this.lastRoomStateChangeDate = System.currentTimeMillis();
        }
        this.roomInfo.setState(roomState);
    }

    @Override
    public RoomState getRoomState() {
        return roomInfo == null ? null : roomInfo.getState();
    }

    public long getLastRoomStateChangeDate() {
        return lastRoomStateChangeDate;
    }

    @Override
    public void setState(BotState botState, String reason) {
        getLogger().debug("setState: botId={}, nickname={}, botState={}, reason={}", getId(), getNickname(), botState, reason);
        if (this.state != botState) {
            lastStateChangeDate = System.currentTimeMillis();
        }
        this.state = botState;
    }

    @Override
    public BotState getState() {
        return state;
    }

    @Override
    public long getLastStateChangeDate() {
        return lastStateChangeDate;
    }

    public String getToken() {
        return token;
    }

    public BotStatuses getStatus() {
        BotState state = getRoomBot().getState();
        if (state == PLAYING) {
            return BotStatuses.PLAYING;
        } else if (state == IDLE || state == OBSERVING) {
            return BotStatuses.OBSERVING;
        } else {
            return BotStatuses.WAITING_FOR_NEW_ROUND;
        }
    }

    public IRoomBot getRoomBot() {
        return this;
    }

    @Override
    protected void registerServerMessageHandlers() {
        serverMessageHandlers.put(GameStateChanged.class, new GameStateChangedHandler(this));
        serverMessageHandlers.put(BalanceUpdated.class, new BalanceUpdatedHandler(this));
        serverMessageHandlers.put(CrashStateInfo.class, new CrashStateInfoHandler(this));
        serverMessageHandlers.put(CrashCancelBetResponse.class, new CrashCancelBetHandler(this));
        serverMessageHandlers.put(CrashBetResponse.class, new CrashBetResponseHandler(this));
        serverMessageHandlers.put(CrashAllBetsResponse.class, new CrashAllBetsHandler(this));
        serverMessageHandlers.put(ChangeMap.class, new ChangeMapHandler(this));
        serverMessageHandlers.put(SitOutResponse.class, new SitOutHandler(this));
        serverMessageHandlers.put(RoundResult.class, new RoundResultHandler(this));
        serverMessageHandlers.put(com.betsoft.casino.mp.transport.Stats.class, new StatsLobbyHandler(this));
        serverMessageHandlers.put(SitInResponse.class, new SitInHandler(this));
    }

    @Override
    protected void sendInitialRequest() {
        sleep(300).subscribe(
                t -> {
                    MaxBlastEnterLobbyRequest maxBlastEnterLobbyRequest
                            = new MaxBlastEnterLobbyRequest(this, client, gameId, serverId, sessionId, selectedBuyIn);

                    getLogger().debug("sendInitialRequest: roomId={}, botId={}, nickname={}, sends {}",
                            this.roomId, this.id, this.nickname, maxBlastEnterLobbyRequest);
                    send(maxBlastEnterLobbyRequest);
                });
    }

    @Override
    public void sendGetStartGameUrlRequest() {
        sendOpenRoomRequest();
    }

    private void sendOpenRoomRequest() {
        sleep(300).subscribe(
                t -> {
                    OpenRoomRequest openRoomRequest
                            = new OpenRoomRequest(this, client, roomId, sessionId, serverId, MoneyType.REAL, "en");

                    getLogger().debug("sendOpenRoomRequest: roomId={}, botId={}, nickname={}, sends {}",
                            this.roomId, this.id, this.nickname, openRoomRequest);
                    send(openRoomRequest);
                });
    }

    @Override
    public void restart() {
        getLogger().debug("restart: begin");

        clearPlayersBets();

        stop();

        getLogger().debug("restart: client.isDisconnected()={}", client.isDisconnected());

        startWithOpenRoom();

        getLogger().debug("restart: finish");
    }

    @Override
    public void stop() {
        getLogger().debug("ManagedMaxBlastChampionsRoomBot stop requested for roomId: {}, id={}", roomId, id);
        super.stop();

        try {
            IApiClient apiClient = mqbBotServiceHandler.getCorrectApiClient(bankId);

            if(apiClient == null) {
                getLogger().warn("ManagedMaxBlastChampionsRoomBot stop: apiClient is null for bankId={} " +
                        "requested for roomId: {}, id={}", bankId, roomId, id);
                return;
            }

            FinishGameSessionResponse finishGameSessionResponse =
                    apiClient.finishGameSession(this.getUserName(), this.getPassword(), this.getSessionId());
            getLogger().debug("ManagedMaxBlastChampionsRoomBot stop: bankId={}, roomId: {}, id={}, FinishGameSessionResponse={}",
                    bankId, roomId, id, finishGameSessionResponse);

        } catch (Exception e) {
            getLogger().warn("ManagedMaxBlastChampionsRoomBot stop: error during apiClient.finishGameSession " +
                    "for nickname={}, sessionId: {}, message={}", this.getNickname(), this.getSessionId(), e.getMessage(), e);
        }
    }

    public void startWithOpenRoom() {
        getLogger().debug("startWithOpenRoom: Starting bot with params ws='{}', sid='{}', server={}", url, sessionId, serverId);
        started = true;
        if (stats == null) {
            stats = new Stats();
        }
        requests.clear();
        webSocketClient = new ReactorNettyWebSocketClient();
        webSocketClient.execute(URI.create(url),
                session -> session
                        .send(
                                Flux.create(
                                        (FluxSink<WebSocketMessage> sink) ->
                                                createClientAndEnter(session, sink, getBankId()),
                                        FluxSink.OverflowStrategy.BUFFER)
                                .doFinally(s ->
                                        closeConnection(session)
                                )
                        )
                        .and(
                                session.receive().doOnNext(message ->
                                        processMessage(session, message))
                        )
        ).subscribe();
        startCallback.apply(null);
    }

    protected void createClientAndEnter(WebSocketSession session, FluxSink<WebSocketMessage> sink, int bankId) {
        client = new SocketClient(session, sink, serializer, bankId, getLogger());
        getLogger().debug("createClient: new client created, sendInitialRequest");

        sleep(10).subscribe(
                t -> {
                    MaxBlastEnterLobbyRequest maxBlastEnterLobbyRequest
                            = new MaxBlastEnterLobbyRequest(this, client, gameId, serverId, sessionId, selectedBuyIn);

                    send(maxBlastEnterLobbyRequest);
                });
    }

    protected void doAction(String debugInfo) {

        RoomState roomState = roomInfo.getState();
        BotState botState = getState();
        getLogger().debug("---------------------ManagedMaxBlastChampionsRoomBot doAction: bot: {}, getSeatId(): {}, " +
                "debugInfo={}, botState: {}, roomState={}, getSessionId(): {}", getId(), getSeatId(), debugInfo, botState, roomState, getSessionId());

        scheduledDoSleepAction.set(false);
        boolean needSleepAndRetry = true;

        switch (botState) {
            case WAITING_FOR_RESPONSE:
                break;

            case IDLE:
//                openRoom();
//                needSleepAndRetry = false;
                break;

            case NEED_SIT_OUT:
                sendSitOutRequest();
                needSleepAndRetry = false;
                break;

            case OBSERVING:
                if (roomState.equals(RoomState.WAIT)) {
                    sendSitInRequest(0);

                    needSleepAndRetry = false;
                    if(roundsToPlay <= 0) {
                        setRoundsToPlay(RNG.nextInt(MIN_ROUNDS_TO_PLAY, MAX_ROUNDS_TO_PLAY + 1));
                        setRoundsCount(0);
                    }
                    ejectBetId = "";
                    ejectMultiplier = Long.MAX_VALUE;
                    ejectTimeIfBotIsLast = Long.MAX_VALUE;
                    MaxBlastChampionsBotStrategy botStrategy = (MaxBlastChampionsBotStrategy) getStrategy();
                    selectedBuyIn = botStrategy.getRequestedByInAmount();
                }

                currentAstronautCounter.set(0);
                break;

            case WAIT_BATTLE_PLAYERS:
                if (roomState.equals(RoomState.WAIT)) {

                    int rCount = getRoundsCount();
                    int rToPlay = getRoundsToPlay();
                    long balance = getBalance();
                    getLogger().debug("doAction: nickname={}, rCount={}, rToPlay={}, balance={}, selectedBuyIn={}",
                            getNickname(), rCount, rToPlay, balance, selectedBuyIn);
                    //if bot played more than allowed rounds or balance is less than selected buyIn
                    //then logOut the bot
                    if (rCount > rToPlay || balance < selectedBuyIn) {
                        getLogger().debug("doAction: nickname={}, rCount > rToPlay is {} or balance < selectedBuyIn is {}, logOut bot",
                                getNickname(), rCount > rToPlay, balance < selectedBuyIn);
                        try {
                            Long botId = Long.parseLong(getId());
                            this.mqbBotServiceHandler.logOut(botId, getSessionId(), getNickname(), getRoomId());
                        } catch (Exception e) {
                            getLogger().error("doAction: Cannot logOut bot botId={}, sessionId={}, nickname={}, roomId={}",
                                    getId(), getSessionId(), getNickname(), getRoomId(), e);
                        }
                    }

                    long realPlayersSetBetPrevRound = playersCrashBetsPrevRound.values().stream()
                            .filter(p -> !p.isBot())
                            .count();

                    boolean shouldPlayRound = true;

                    if(((this.strategy instanceof MaxBlastChampionsBotAggressiveStrategy) && (realPlayersSetBetPrevRound > THRESHOLD_AGGRESSIVE)) ||
                       ((this.strategy instanceof MaxBlastChampionsBotMediumStrategy) && (realPlayersSetBetPrevRound > THRESHOLD_MEDIUM)) ||
                       ((this.strategy instanceof MaxBlastChampionsBotRockStrategy) && (realPlayersSetBetPrevRound > THRESHOLD_ROCK))
                    ) {
                        shouldPlayRound = false;
                    }
                    getLogger().debug("doAction: shouldPlayRound={}, Current realPlayersSetBetPrevRound={} and strategy={} is for botId={}, " +
                                    "nickname={}, roomId={}, playersCrashBetsPrevRound={}",
                            shouldPlayRound, realPlayersSetBetPrevRound, strategy.getClass().getName(), getId(), getNickname(), getRoomId(),
                            playersCrashBetsPrevRound.keySet().toArray());

                    long currentTime = System.currentTimeMillis();
                    boolean realPlayerExists = realPlayerExists(); //proceed with bet if at least one real player is present

                    getLogger().debug("doAction: botId={}, nickname={} shouldPlayRound={}, realPlayerExists={}, " +
                                    "currentTime={}, crashBetRequestTime={}, currentTime >= crashBetRequestTime is {}",
                            getId(), getNickname(), shouldPlayRound, realPlayerExists, toHumanReadableFormat(currentTime),
                            toHumanReadableFormat(crashBetRequestTime), currentTime >= crashBetRequestTime);

                    if (shouldPlayRound && realPlayerExists && currentTime >= crashBetRequestTime) {
                        getLogger().debug("doAction: set bot state to PLAYING botId={}, nickname={}", getId(), getNickname());
                        setState(PLAYING, "GameStateChangedHandler");
                    } else {
                        getLogger().debug("doAction: keep bot state WAIT_BATTLE_PLAYERS botId={}, nickname={}", getId(), getNickname());
                    }

                } else if (roomState.equals(RoomState.PLAY)) {
                    //if room state was not changed more 3 min ago, it looks like bot lost room
                    //logOut the bot
                    if(System.currentTimeMillis() - getLastRoomStateChangeDate() >= TimeUnit.MINUTES.toMillis(3)) {
                        getLogger().debug("doAction: nickname={}, room last State Change to {} was done more 3 min ago {}, logOut bot",
                                getNickname(), roomState, toHumanReadableFormat(getLastRoomStateChangeDate()));
                        try {
                            Long botId = Long.parseLong(getId());
                            this.mqbBotServiceHandler.logOut(botId, getSessionId(), getNickname(), getRoomId());
                        } catch (Exception e) {
                            getLogger().error("doAction: Cannot logOut bot botId={}, sessionId={}, nickname={}, roomId={}",
                                    getId(), getSessionId(), getNickname(), getRoomId(), e);
                        }
                    }
                }
                break;

            case PLAYING:

                String nickname = getNickname();
                long currentTime = System.currentTimeMillis();

                if (roomState.equals(RoomState.WAIT)) {
                    MaxBlastChampionsBotStrategy botStrategy = (MaxBlastChampionsBotStrategy) getStrategy();
                    if (getAstronautsCount() == 0) {
                        setCurrentMultiplier(0);
                        setState(BotState.WAITING_FOR_RESPONSE, "betRequest");
                        ejectBetId = getCrashBetKeyFromTimeAndNickname(currentTime, nickname);
                        ejectMultiplier = botStrategy.generateMultiplier();

                        getLogger().debug("doAction: botId={}, nickname={}, ejectBetId={}, ejectMultiplier={}", getId(), getNickname(), ejectBetId, ejectMultiplier);
                        send(new CrashBetRequest(this, client, (int)selectedBuyIn, 0.0, ejectBetId));
                    }

                } else if (roomState.equals(RoomState.PLAY)) {

                    if (!"".equals(ejectBetId)) {

                        boolean botIsOnlyOneInRocket = playerIsSingleInRocket(nickname);

                        if (botIsOnlyOneInRocket && ejectTimeIfBotIsLast == Long.MAX_VALUE) {
                                ejectTimeIfBotIsLast = currentTime  + RNG.nextInt(100,500);
                        }

                        getLogger().debug("doAction: botId={}, nickname={},  currentMultiplier={}, ejectMultiplier={}, currentMultiplier >= ejectMultiplier is {}, " +
                                        "botIsOnlyOneInRocket={}, ejectTimeIfBotIsLast={}, currentTime={}, currentTime >= ejectTimeIfBotIsLast is {}",
                                getId(), getNickname(), currentMultiplier, ejectMultiplier, currentMultiplier >= ejectMultiplier,
                                botIsOnlyOneInRocket, toHumanReadableFormat(ejectTimeIfBotIsLast, "yyyy-MM-dd HH:mm:ss.SSS"),
                                toHumanReadableFormat(currentTime, "yyyy-MM-dd HH:mm:ss.SSS"),
                                currentTime >= ejectTimeIfBotIsLast);

                        if (currentMultiplier >= ejectMultiplier || currentTime >= ejectTimeIfBotIsLast) {
                            ejectTimeIfBotIsLast = Long.MAX_VALUE;
                            setState(BotState.WAITING_FOR_RESPONSE, "crashCancelBetRequest");
                            send(new CrashCancelBetRequest(ejectBetId, false, this, client));
                        }
                    }
                }
                break;
        }

        if (needSleepAndRetry && started) {
            long waitTime = strategy.getWaitTime();
            sleep(waitTime).subscribe(t -> doAction("from doAction()"));
        }
    }

    @Override
    public void pickNickname(boolean retry, String enterLobbyNickname) {
        nickname = enterLobbyNickname;
        //unsupported operation for MQB banks
    }

    @Override
    public void sendCloseRoomRequest() {
        send(new CloseRoomRequest(this, client, roomInfo.getRoomId()));
    }


    @Override
    public void sendSitOutRequest() {
        setState(BotState.WAITING_FOR_RESPONSE, "ManagedMaxBlastChamp sendSitOutRequest");
        send(new SitOutRequest(this, client));
    }

    @Override
    public synchronized void sendSitInRequest(int failedCount) {
        setState(BotState.WAITING_FOR_RESPONSE, "ManagedMaxBlastChamp sendSitInRequest");
        send(new CrashSitInRequest(this, client, "en"));
    }

    @Override
    public void openNewRoom() {
        sleep(300)
            .subscribe(
                t ->
                    send(new OpenRoomRequest(this, client, -1, sessionId, serverId, MoneyType.REAL, "en"))
            );
    }


    public synchronized void doActionWithSleep(long waitTime, String debugInfo) {
        doActionWithSleep(waitTime, debugInfo, true);
    }

    public void doActionWithSleep(long waitTime, String debugInfo, boolean logDebug) {
        lock.lock();
        try {
            if (scheduledDoSleepAction.get()) {
                getLogger().warn("doActionWithSleep: found already scheduled doAction: debugInfo={}", debugInfo);
            } else if (started) {

                sleep(waitTime).subscribe(
                        t ->
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

    public void sitOut() {
        getLogger().debug("ManagedMaxBlastChampionsRoomBot::sitOut: sent sitOut ");
        sendSitOutRequest();
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
        if(balance != null) {
            this.balance.set(balance);
        } else {
            this.balance.set(0);
        }
    }

    @Override
    public long getBalance() {
        return this.balance.get();
    }

    @Override
    public void setRoomInfo(IGetRoomInfoResponse roomInfo) {
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
    public void clearShotRequests() {
    }

    @Override
    public boolean shot() {
        return false;
    }

    @Override
    public void sendBuyInRequest(int failedCount) {
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

    public void confirmNextRoundPlay(long nextRoundId) {
    }
}
