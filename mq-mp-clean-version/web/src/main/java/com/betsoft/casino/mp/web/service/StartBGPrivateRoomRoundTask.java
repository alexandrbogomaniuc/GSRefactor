package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;

@SpringAware
public class StartBGPrivateRoomRoundTask implements Runnable, Serializable, ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(StartBGPrivateRoomRoundTask.class);
    private final long roomId;

    private transient ApplicationContext context;

    public StartBGPrivateRoomRoundTask(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public void run() {
        if (context != null) {
            LOG.debug("StartBGPrivateRoomRoundTask run: {}", this);

            try {
                RoomServiceFactory roomServiceFactory =
                        context.getBean("roomServiceFactory", RoomServiceFactory.class);

                IRoomInfo roomInfo = roomServiceFactory.getRoomInfo(roomId);

                if (roomInfo != null) {

                    IRoom room = roomServiceFactory.getRoomWithoutCreation(roomInfo.getGameType(), roomId);

                    if (room != null) {

                        if (room instanceof BattleAbstractCrashGameRoom) {

                            IGameState gameState = room.getGameState();

                            if (gameState != null) {

                                if (gameState instanceof com.betsoft.casino.mp.maxblastchampions.model.WaitingPlayersGameState) {

                                    LOG.debug("StartBGPrivateRoomRoundTask run onTimer at WaitingPlayersGameState " +
                                            "for roomId: {}", roomId);

                                    gameState.onTimer(false);

                                } else {
                                    LOG.debug("StartBGPrivateRoomRoundTask gameState is not WaitingPlayersGameState " +
                                            "for roomId: {}, just exit", roomId);
                                }
                            } else {
                                LOG.debug("StartBGPrivateRoomRoundTask gameState is null for roomId: {}, just exit", roomId);
                            }
                        } else {
                            LOG.debug("StartBGPrivateRoomRoundTask room not BattleAbstractCrashGameRoom for roomId: {}, just exit", roomId);
                        }
                    } else {
                        LOG.debug("StartBGPrivateRoomRoundTask room is null for roomId: {}, just exit", roomId);
                    }
                } else {
                    LOG.debug("StartBGPrivateRoomRoundTask roomInfo is null for roomId: {}, just exit", roomId);
                }

            } catch (Exception e) {
                LOG.debug("Cannot StartBGPrivateRoomRound for, roomId: {}", roomId, e);
            }
        } else {
            LOG.error("ApplicationContext not found, {}", this);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        return "SitOutTask [" + "roomId=" + roomId +
                ']';
    }
}
