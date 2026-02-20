package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.*;
import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.bots.strategies.BaseMetricTimeKey;
import com.betsoft.casino.bots.strategies.IRoomNaturalBotStrategy;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.transport.GameStateChanged;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class GameStateChangedHandler implements IServerMessageHandler<GameStateChanged> {

    private static final Logger LOG = LogManager.getLogger(GameStateChangedHandler.class);

    private final IRoomBot bot;

    public GameStateChangedHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(GameStateChanged response) {
        LOG.info("GameStateChangedHandler::handle: botId={}, nickname={}, GameStateChanged={}", bot.getId(), bot.getNickname(), response);
        toggleGameState(response);
    }

    private void toggleGameState(GameStateChanged gameStateChanged) {
        RoomState roomState = gameStateChanged.getState();
        RoomState oldRoomState = bot.getRoomState();
        BotState oldBotState = bot.getState();
        LOG.info("toggleGameState: botId={}, nickname={},old roomState={}, new roomState={}, old botState={}",
                bot.getId(), bot.getNickname(), oldRoomState, roomState, oldBotState);

        bot.setRoomState(roomState);

        boolean isBattleBotWithNaturalStrategy = bot.getStrategy() instanceof IRoomNaturalBotStrategy;
        boolean isManagedMaxBlastChampionsRoomBot = bot instanceof ManagedMaxBlastChampionsRoomBot;
        boolean isMqbBattleBot = bot.isMqbBattleBot();

        switch (roomState) {

            case WAIT:
                bot.count(Stats.STATE_WAIT);

                //todo check for BG DS/MA
                if(isManagedMaxBlastChampionsRoomBot) {

                    ManagedMaxBlastChampionsRoomBot maxBlastBot = (ManagedMaxBlastChampionsRoomBot)bot;
                    maxBlastBot.clearPlayersBets();

                    if(gameStateChanged.getRoundStartTime() != null) {
                        maxBlastBot.calcCrashBetRequestTime(gameStateChanged.getDate(), gameStateChanged.getRoundStartTime());
                    } else if(gameStateChanged.getTtnx() > 0) {
                        long msgRoundStartTime = gameStateChanged.getDate() + gameStateChanged.getTtnx();
                        maxBlastBot.calcCrashBetRequestTime(gameStateChanged.getDate(), msgRoundStartTime);
                    }

                    //set bot WAIT_BATTLE_PLAYERS state if bot has completed SitIn process and has balance setup
                    if(maxBlastBot.getBalance() > 0) {
                        bot.setState(BotState.WAIT_BATTLE_PLAYERS, "toggleGameState wait");
                    }

                } else {
                    bot.setState(BotState.OBSERVING, "toggleGameState wait");
                    if(isBattleBotWithNaturalStrategy) {
                        if (isMqbBattleBot) {
                            //if bot was playing the last round (BotState.PLAYING or BotState.WAITING_FOR_RESPONSE),
                            //then wait to SitOut message from MP server
                            if(BotState.PLAYING.equals(oldBotState) || BotState.WAITING_FOR_RESPONSE.equals(oldBotState)) {
                                bot.setState(BotState.WAIT_BATTLE_PLAYERS, "MqbBattleBot should sitOut by MP server after round ends");
                            }
                        } else {
                            bot.setState(BotState.IDLE, "not allow shot in QUALIFY roomState");
                        }
                        if(bot.isBattleBot()) {
                            LOG.debug("toggleGameState: bot={}, reset ConfirmBattlegroundBuyIn, isMqbBattleBot: {}", bot.getId(), isMqbBattleBot);
                            BattleGroundRoomBot battleGroundRoomBot = (BattleGroundRoomBot) bot;
                            battleGroundRoomBot.setBattlegroundBuyInConfirmed(false);
                            if(!isMqbBattleBot) {
                                battleGroundRoomBot.setNeedReBuyInRoom(true);
                            }
                        }
                    }
                }
                break;

            case CLOSED:
                bot.count(Stats.STATE_CLOSED);
                bot.stop();
                break;

            case QUALIFY:
                bot.count(Stats.STATE_QUALIFY);
                if(!isManagedMaxBlastChampionsRoomBot) {
                    bot.clearShotRequests();
                    if (shouldCloseRoomWhenRoundFinished()) {
                        bot.setState(BotState.WAITING_FOR_RESPONSE, " waiting  sitOut");
                        Mono.delay(Duration.ofMillis(2000)).subscribe(t -> bot.sendSitOutRequest());
                    }
                    if (bot instanceof RoomBot) {
                        ((RoomBot) bot).setQualifyStateReceived(true);
                    }
                    bot.clearAmmo();
                }
                break;
            case PLAY:
                bot.count(Stats.STATE_PLAY);
                if(bot instanceof RoomBot){
                    ((RoomBot) bot).resetRoundStatesOnPlay();
                }
                if(bot.getState() != BotState.OBSERVING && bot.getState() != BotState.WAITING_FOR_RESPONSE) {
                    bot.setState(BotState.PLAYING, "GameStateChangedHandler: toggleGameState");
                }

                if(isBattleBotWithNaturalStrategy){
                    ((IRoomNaturalBotStrategy) bot.getStrategy())
                            .addLastShootResponseTime(BaseMetricTimeKey.PLAY_STARTED.name(), System.currentTimeMillis());
                }
                break;
        }

        LOG.info("toggleGameState: New botState for bot : botId={}, nickname={} is {}", bot.getId(), bot.getNickname(), bot.getState());
    }

    private boolean shouldCloseRoomWhenRoundFinished() {
        return bot.getStrategy().needSitOutFromRoom();
    }
}
