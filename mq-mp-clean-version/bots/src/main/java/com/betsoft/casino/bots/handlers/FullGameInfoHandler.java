package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.BattleGroundRoomBot;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.transport.FullGameInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FullGameInfoHandler implements IServerMessageHandler<FullGameInfo> {

    private static final Logger LOG = LogManager.getLogger(FullGameInfoHandler.class);

    private final IRoomBot bot;

    public FullGameInfoHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(FullGameInfo fullGameInfo) {
        LOG.debug("FullGameInfoHandler::handle: botId={}, nickname={}, fullGameInfo={}",
                bot.getId(), bot.getNickname(), fullGameInfo);

        if(fullGameInfo.getState() == RoomState.WAIT) {
            bot.setRoomEnemies(fullGameInfo.getRoomEnemies());
            if (bot.isBattleBot()) {
                ((BattleGroundRoomBot) bot).updateRoundStartTime(fullGameInfo.getDate(), fullGameInfo.getTimeToStart());
                ((BattleGroundRoomBot) bot).setLastTimeFullGameInfo(System.currentTimeMillis());
            }
            if(bot.isMqbBattleBot()) {
                ((ManagedBattleGroundRoomBot) bot).setObservers(fullGameInfo.getObservers());
                ((ManagedBattleGroundRoomBot) bot).generateConfirmBuyInTime();
            }
            if (bot instanceof RoomBot) {
                int mapId = fullGameInfo.getMapId();
                ((RoomBot) bot).setCurrentMapId(mapId);
                LOG.debug("update mapId from full game info: {} ", mapId);
            }
        } else if(fullGameInfo.getState() == RoomState.PLAY) {
            if (bot.isBattleBot()) {
                ((BattleGroundRoomBot) bot).updateRoundEndTime(fullGameInfo.getDate(), fullGameInfo.getEndTime());
            }
        }
    }

    @Override
    public String toString() {
        return "FullGameInfoHandler{" +
                "bot=" + bot +
                '}';
    }
}
