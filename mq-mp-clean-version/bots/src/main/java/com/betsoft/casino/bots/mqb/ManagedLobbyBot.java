package com.betsoft.casino.bots.mqb;

import com.betsoft.casino.bots.*;
import com.betsoft.casino.bots.requests.GetBattlegroundStartGameUrlRequest;
import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.betsoft.casino.bots.strategies.IRoomBotStrategy;
import com.betsoft.casino.mp.web.IMessageSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.betsoft.casino.bots.BotState.*;
import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

/**
 * User: flsh
 * Date: 13.07.2022.
 */
public class ManagedLobbyBot extends LobbyBot implements IManagedLobbyBot {

    private final long roomId;
    private final String openRoomWSUrl;
    private final int gameServerId;
    private final String token;
    private long nextRoundId;
    private long expiresAt;
    private final String userName;
    private final String password;
    private final String externalId;
    private final double shootsRate;
    private final double bulletsRate;

    protected final MQBBotServiceHandler mqbBotServiceHandler;

    public ManagedLobbyBot(MQBBotServiceHandler mqbBotServiceHandler, String nickname, String userName, String password, String externalId, String id, String url, int gameId, int bankId, long roomId, String openRoomWSUrl, int serverId,
                           String sessionId, IMessageSerializer serializer, Function<Void, Integer> shutdownCallback, IRoomBotStrategy botStrategy,
                           Function<Void, Integer> startCallback, String token, long expiresAt, double shootsRate, double bulletsRate) {
        super(nickname, id, url, gameId, bankId, serverId, sessionId, serializer, shutdownCallback, botStrategy, startCallback);
        this.roomId = roomId;
        this.openRoomWSUrl = openRoomWSUrl;
        this.gameServerId = serverId;
        this.token = token;
        this.expiresAt = expiresAt;

        this.userName = userName;
        this.password = password;
        this.externalId = externalId;

        this.shootsRate = shootsRate;
        this.bulletsRate = bulletsRate;

        this.mqbBotServiceHandler = mqbBotServiceHandler;
    }

    @Override
    public double getShootsRate() {
        return shootsRate;
    }

    @Override
    public double getBulletsRate() {
        return bulletsRate;
    }

    @Override
    public boolean isExpired() {
        return this.expiresAt <= System.currentTimeMillis();
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public long getExpiresAt() {
        return expiresAt;
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

    protected boolean shouldOpenNewRoom() {
        boolean isExpired = isExpired();
        getLogger().debug("ManagedLobbyBot: shouldOpenNewRoom, roomId:{}, botIs={}, isExpired={}, expiresAt:{}",
                roomId, id, isExpired, toHumanReadableFormat(expiresAt));
        return !isExpired;
    }

    @Override
    public void sendGetStartGameUrlRequest() {
        long requestRoomId = roomFromArgs != null ? roomFromArgs : roomId;
        getLogger().debug("ManagedLobbyBot: sendGetStartGameUrlRequest, roomFromArgs: {}, roomId: {}, selectedBuyIn: {}",
                roomFromArgs, roomId, selectedBuyIn);
        sleep(300).subscribe(t ->
                send(new GetBattlegroundStartGameUrlRequest(this, client, selectedBuyIn, requestRoomId))
        );
    }

    @Override
    public void connectToRoom(String socketUrl, long roomId, int roomServerId, String sessionId, boolean isBattle) {
        if (isBattle) {
            roomBot = new ManagedBattleGroundRoomBot(this, id, socketUrl, roomServerId,
                    sessionId, serializer, roomId, balance, selectedStake, nickname, botStrategy,
                    aVoid -> onRoomClosed(), unused -> 0, expiresAt);
        } else {
            throw new IllegalArgumentException("Only battleground bots supported");
        }
        roomBot.setStats(getStats());
        roomBot.start();
    }

    @Override
    public void pickNickname(boolean retry, String enterLobbyNickname) {
        nickname = enterLobbyNickname;
        //unsupported operation for MQB banks
    }

    @Override
    public void stop() {
        getLogger().debug("ManagedLobbyBot stop requested for roomId: {}, id={}", roomId, id);
        IRoomBot roomBot = getRoomBot();
        if (roomBot != null) {
            roomBot.stop();
        }
        super.stop();

        try {

            IApiClient apiClient = mqbBotServiceHandler.getCorrectApiClient(bankId);
            if(apiClient == null) {
                getLogger().warn("ManagedLobbyBot stop: apiClient is null for bankId={} requested for roomId: {}, " +
                        "id={}", bankId, roomId, id);
                return;
            }

            FinishGameSessionResponse finishGameSessionResponse =
                    apiClient.finishGameSession(this.getUserName(), this.getPassword(), this.getSessionId());
            getLogger().debug("ManagedLobbyBot stop: bankId={}, roomId: {}, id={}, FinishGameSessionResponse={}",
                    bankId, roomId, id, finishGameSessionResponse);

            LOG.debug("logOut: id={}, nickname={}, roomId=-1 remove from botsMap", id, nickname);
            mqbBotServiceHandler.removeBot(Long.parseLong(id), nickname, -1);

        } catch (Exception e) {
            getLogger().warn("ManagedLobbyBot stop: error during apiClient.finishGameSession for nickname={}, sessionId: {}, " +
                    "message={}", this.getNickname(), this.getSessionId(), e.getMessage(), e);
        }
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public void setSelectedBuyIn(Long buyIn) {
        this.selectedBuyIn = buyIn;
    }

    public String getOpenRoomWSUrl() {
        return openRoomWSUrl;
    }

    public int getGameServerId() {
        return gameServerId;
    }

    public void setAllowNextRoundPlay(boolean allowNextRoundPlay) {
        getManagedRoomBot().setAllowNextRoundPlay(allowNextRoundPlay);
    }

    @Override
    public void confirmNextRoundPlay(long nextRoundId) {
        setAllowNextRoundPlay(true);
        this.nextRoundId = nextRoundId;
        //todo: send buyIn request for play next round
        getLogger().debug("ManagedLobbyBot confirmNextRoundPlay sent buyIn ");
        ManagedBattleGroundRoomBot gameRoomBot = (ManagedBattleGroundRoomBot) getRoomBot();
        gameRoomBot.getStrategy().resetWeapons();
        gameRoomBot.setBattlegroundBuyInConfirmed(false);
        gameRoomBot.sendConfirmBattlegroundBuyIn();
    }

    @Override
    public void sitOut() {
        //todo: close bots

        IRoomBot roomBot = getRoomBot();
        if(roomBot == null) {
            getLogger().debug("ManagedLobbyBot sitOut for botId={}: room bot is null", getId());
        } else {
            getLogger().debug("ManagedLobbyBot sitOut for botId={}: send sendSitOutRequest over room bot", getId());
            getRoomBot().sendSitOutRequest();
        }
    }

    @Override
    public String getToken() {
        return token;
    }

    public ManagedBattleGroundRoomBot getManagedRoomBot() {
        return (ManagedBattleGroundRoomBot) roomBot;
    }

    @Override
    public BotStatuses getStatus() {
        BotState state = getManagedRoomBot().getState();
        if (state == PLAYING) {
            return BotStatuses.PLAYING;
        } else if (state == IDLE || state == OBSERVING) {
            return BotStatuses.OBSERVING;
        } else {
            return BotStatuses.WAITING_FOR_NEW_ROUND;
        }
    }

    public long getNextRoundId() {
        return nextRoundId;
    }

    @Override
    public void setBuyIns(List<Long> buyIns) {
        this.buyIns = new ArrayList<>(buyIns);
        getLogger().debug("ManagedLobbyBot buyIns: {}, selectedBuyIn: {}", buyIns, selectedBuyIn);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ManagedLobbyBot [");
        sb.append("roomId=").append(roomId);
        sb.append(", openRoomWSUrl='").append(openRoomWSUrl).append('\'');
        sb.append(", gameServerId=").append(gameServerId);
        sb.append(", balance=").append(balance);
        sb.append(", gameId=").append(gameId);
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", selectedBuyIn=").append(selectedBuyIn);
        sb.append(", bankId=").append(bankId);
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", started=").append(started);
        sb.append(", token=").append(token);
        sb.append(", nextRoundId=").append(nextRoundId);
        sb.append(", expiresAt='").append(toHumanReadableFormat(expiresAt)).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", externalId='").append(externalId).append('\'');
        sb.append(", shootsRate=").append(shootsRate);
        sb.append(", bulletsRate=").append(bulletsRate);
        sb.append(']');
        return sb.toString();
    }
}
