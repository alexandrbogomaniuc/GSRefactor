package com.betsoft.casino.bots;

import com.betsoft.casino.bots.handlers.BalanceUpdatedLobbyHandler;
import com.betsoft.casino.bots.handlers.LobbyErrorHandler;
import com.betsoft.casino.bots.handlers.StatsLobbyHandler;
import com.betsoft.casino.bots.requests.*;
import com.betsoft.casino.bots.strategies.IRoomBotStrategy;
import com.betsoft.casino.mp.common.math.SWPaidCosts;
import com.betsoft.casino.mp.transport.BalanceUpdated;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.abs.casino.common.util.RNG;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LobbyBot extends AbstractBot implements ILobbyBot {
    protected IRoomBot roomBot;
    protected long balance;
    protected final int gameId;
    protected String nickname;
    protected List<Float> stakes;
    protected List<Float> allowedStakes;
    protected int stakesLimit;
    protected IRoomBotStrategy botStrategy;
    protected Map<Integer, Integer> weaponPrice;
    protected Float selectedStake;
    protected List<Long> buyIns;
    protected Long selectedBuyIn;
    protected boolean isSpecificRoom;
    protected Long roomFromArgs;
    protected boolean needTryRandomExitInWaitState;

    public void setRoomFromArgs(long roomFromArgs) {
        this.roomFromArgs = roomFromArgs;
    }

    public LobbyBot(String nickname, String id, String url, int gameId, int bankId, int serverId, String sessionId,
                    IMessageSerializer serializer,
                    Function<Void, Integer> shutdownCallback, IRoomBotStrategy botStrategy, Function<Void, Integer> startCallback) {
        super(id, url, serverId, bankId, sessionId, serializer, shutdownCallback, startCallback);
        this.nickname = nickname;
        this.gameId = gameId;
        this.botStrategy = botStrategy;
        this.needTryRandomExitInWaitState = false;
    }

    @Override
    protected void registerServerMessageHandlers() {
        this.serverMessageHandlers.put(Error.class, new LobbyErrorHandler(this));
        serverMessageHandlers.put(BalanceUpdated.class, new BalanceUpdatedLobbyHandler(this));
        serverMessageHandlers.put(com.betsoft.casino.mp.transport.Stats.class, new StatsLobbyHandler(this));
    }

    @Override
    protected void sendInitialRequest() {
        sleep(300)
            .subscribe(t ->
                send(new EnterLobbyRequest(this, client, gameId, serverId, sessionId))
            );
    }

    @Override
    public void sendGetStartGameUrlRequest() {
        getLogger().debug("sendGetStartGameUrlRequest ");
        sleep(300)
                .subscribe(t ->
                        send(selectedBuyIn != null ?
                            new GetBattlegroundStartGameUrlRequest(this, client, selectedBuyIn, roomFromArgs) :
                            new GetStartGameUrlRequest(this, client, selectedStake.intValue(), roomFromArgs)
                        )
                );
    }

    @Override
    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public void setStakes(List<Float> stakes) {
        this.stakes = stakes;
        this.allowedStakes = new ArrayList<>(stakes.size());
        allowedStakes.addAll(stakes);
        selectedStake = allowedStakes.get(RNG.nextInt(allowedStakes.size()));
    }

    public List<Long> getBuyIns() {
        return buyIns;
    }

    @Override
    public void setBuyIns(List<Long> buyIns) {
        this.buyIns = new ArrayList<>(buyIns);
        selectedBuyIn = botStrategy.requestedByInAmount() == 0 ? buyIns.get(RNG.nextInt(buyIns.size())) : botStrategy.requestedByInAmount();
        getLogger().debug("buyIns: {}, selectedBuyIn: {}", buyIns, selectedBuyIn);
    }

    @Override
    public void setStakesLimit(int limit) {
        this.stakesLimit = limit;
    }

    @Override
    public void setWeaponPrices(List<SWPaidCosts> weaponPrices) {
        this.weaponPrice = weaponPrices.stream()
                .collect(Collectors.toMap(SWPaidCosts::getId, SWPaidCosts::getCostMultiplier));
    }

    @Override
    public Integer getWeaponPriceById(int id) {
        return this.weaponPrice.get(id);
    }

    @Override
    public void pickNickname(boolean retry, String enterLobbyNickname) {
        send(new ChangeNicknameRequest(this, client, retry ? nickname + RNG.nextInt(2000000000) : nickname));
    }

    @Override
    public void pickAvatar() {
        send(new ChangeAvatarRequest(this, client, RNG.nextInt(4), RNG.nextInt(4), RNG.nextInt(4)));
    }

    public void connectToRoom(String socketUrl, long roomId, int roomServerId, String sessionId, boolean isBattle) {
        if (roomBot == null) {
            if (isBattle) {
                long delay = needTryRandomExitInWaitState ? 10000 : 0;
                roomBot = new BattleGroundRoomBot(this, id, socketUrl, roomServerId, sessionId, serializer,
                        roomId, balance, selectedStake, nickname, botStrategy, aVoid -> onRoomClosedWithDelay(delay), unused -> 0);
                if(needTryRandomExitInWaitState){
                    ((BattleGroundRoomBot) roomBot).setNeedRandomExitAfterSitInOrReBuyIn(RNG.nextBoolean());
                }
            } else {
                roomBot = new RoomBot(this, id, socketUrl, roomServerId, sessionId, serializer,
                        roomId, balance, selectedStake, nickname, botStrategy, aVoid -> onRoomClosed(), unused -> 0);
            }
            roomBot.setStats(getStats());
        } else {
            getLogger().debug("connectToRoom: roomBot initialized, just set roomId,socketUrl,roomServerId and start");
            roomBot.setRoomId(roomId);
            roomBot.setUrl(socketUrl);
            roomBot.setServerId(roomServerId);
        }
        roomBot.start();
    }

    protected int onRoomClosed() {

        boolean shouldOpenNewRoom = shouldOpenNewRoom();
        getLogger().debug("LobbyBot onRoomClosed, botId={}, shouldOpenNewRoom={}", id, shouldOpenNewRoom);

        if (shouldOpenNewRoom) {
            getLogger().debug("LobbyBot onRoomClosed, sendGetStartGameUrlRequest botId={}", id);
            sendGetStartGameUrlRequest();
        } else {
            getLogger().debug("LobbyBot onRoomClosed, stop botId={}", id);
            stop();
        }
        return 0;
    }

    protected int onRoomClosedWithDelay(long delay) {

        boolean shouldOpenNewRoom = shouldOpenNewRoom();
        getLogger().debug("LobbyBot onRoomClosedWithDelay, botId={}, delay={} shouldOpenNewRoom={}", id, delay, shouldOpenNewRoom);

        if (shouldOpenNewRoom) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }
            getLogger().debug("LobbyBot onRoomClosedWithDelay, sendGetStartGameUrlRequest botId={}", id);
            sendGetStartGameUrlRequest();
        } else {
            getLogger().debug("LobbyBot onRoomClosedWithDelay, stop botId={}", id);
            stop();
        }
        return 0;
    }

    protected boolean shouldOpenNewRoom() {
        return true;
    }

    public IRoomBot getRoomBot() {
        return roomBot;
    }

    public long getBalance() {
        return balance;
    }

    public int getGameId() {
        return gameId;
    }

    public String getNickname() {
        return nickname;
    }

    public List<Float> getStakes() {
        return stakes;
    }

    public List<Float> getAllowedStakes() {
        return allowedStakes;
    }

    public int getStakesLimit() {
        return stakesLimit;
    }

    public IRoomBotStrategy getBotStrategy() {
        return botStrategy;
    }


    @Override
    public boolean isSpecificRoom() {
        return isSpecificRoom;
    }

    public void setSpecificRoom(boolean specificRoom) {
        isSpecificRoom = specificRoom;
    }

    public boolean isNeedTryRandomExitInWaitState() {
        return needTryRandomExitInWaitState;
    }

    public void setNeedTryRandomExitInWaitState(boolean needTryRandomExitInWaitState) {
        this.needTryRandomExitInWaitState = needTryRandomExitInWaitState;
    }
}
