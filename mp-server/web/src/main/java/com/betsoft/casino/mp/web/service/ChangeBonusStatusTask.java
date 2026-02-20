package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.common.AbstractGameRoom;
import com.betsoft.casino.mp.common.BonusType;
import com.betsoft.casino.mp.data.persister.ActiveCashBonusSessionPersister;
import com.betsoft.casino.mp.data.persister.ActiveFrbSessionPersister;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.web.ISocketClient;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;

@SpringAware
public class ChangeBonusStatusTask implements Runnable, Serializable, ApplicationContextAware {
    private final long accountId;
    private final String status;
    private final String oldStatus;
    private final long bonusId;

    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(ChangeBonusStatusTask.class);

    public ChangeBonusStatusTask(long accountId, String status, String oldStatus, long bonusId) {
        this.accountId = accountId;
        this.status = status;
        this.oldStatus = oldStatus;
        this.bonusId = bonusId;
    }

    public void run() {
        LOG.debug("run: accountId={}, bonusId={}, status={}", accountId, bonusId, status);
        if (context != null) {
            boolean isSeater = false;
            try {
                RoomServiceFactory roomServiceFactory = context.getBean("roomServiceFactory", RoomServiceFactory.class);
                RoomPlayerInfoService playerInfoService = context.getBean("playerInfoService", RoomPlayerInfoService.class);
                LobbySessionService lobbySessionService = context.getBean("lobbySessionService", LobbySessionService.class);
                CassandraPersistenceManager persistenceManager = context
                        .getBean("persistenceManager", CassandraPersistenceManager.class);

                ActiveCashBonusSessionPersister activeCashBonusSessionPersister = persistenceManager
                        .getPersister(ActiveCashBonusSessionPersister.class);

                ActiveFrbSessionPersister activeFrbSessionPersister = persistenceManager
                        .getPersister(ActiveFrbSessionPersister.class);

                ITransportObjectsFactoryService toFactoryService =
                        (ITransportObjectsFactoryService) context.getBean("transportObjectsFactoryService");

                IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
                if (roomPlayerInfo != null) {
                    long roomId = roomPlayerInfo.getRoomId();
                    @SuppressWarnings("rawtypes")
                    IRoom room = roomServiceFactory.getRoomWithoutCreationById(roomId);
                    if (room != null) {
                        @SuppressWarnings("rawtypes")
                        AbstractGameRoom gameRoom = (AbstractGameRoom) room;
                        IActiveCashBonusSession activeCashSession = roomPlayerInfo.getActiveCashBonusSession();
                        IActiveFrbSession activeFrbSession = roomPlayerInfo.getActiveFrbSession();

                        LOG.debug("ChangeBonusStatusTask activeCashSession: {}, new status: {}, " +
                                        "activeFrbSession: {}",
                                activeCashSession, status, activeFrbSession);

                        if (activeCashSession != null && activeCashSession.getId() == bonusId) {
                            ActiveCashBonusSession resultBonus = new ActiveCashBonusSession(bonusId,
                                    activeCashSession.getAccountId(), activeCashSession.getAwardDate(),
                                    activeCashSession.getExpirationDate(), activeCashSession.getBalance(),
                                    activeCashSession.getAmount(),
                                    activeCashSession.getBetSum(),
                                    activeCashSession.getRolloverMultiplier(), status,
                                    activeCashSession.getMaxWinLimit());
                            gameRoom.changeBonusStatus(resultBonus);
                            isSeater = true;
                        } else if (activeFrbSession != null && activeFrbSession.getBonusId() == bonusId
                                && activeFrbSession.getStatus().equals("ACTIVE")) {
                            IActiveFrbSession newActiveFRBSession = new ActiveFrbSession(bonusId, activeFrbSession.getAccountId(),
                                    activeFrbSession.getAwardDate(), activeFrbSession.getStartDate(),
                                    activeFrbSession.getExpirationDate(), activeFrbSession.getStartAmmoAmount(),
                                    activeFrbSession.getCurrentAmmoAmount(), activeFrbSession.getWinSum(),
                                    activeFrbSession.getStake(), status, activeFrbSession.getMaxWinLimit());
                            gameRoom.changeFRBBonusStatus(newActiveFRBSession);
                            isSeater = true;
                        } else {
                            LOG.debug("Bonus status changed, but seater not play this bonus");
                        }
                    } else {
                        LOG.debug("found roomPlayerInfo, but room not found on this server");
                    }
                }
                if (!isSeater) {
                    //need refresh bonus status, may be already saved on other server
                    ActiveCashBonusSession activeCashBonusSession = activeCashBonusSessionPersister.get(bonusId);
                    if (activeCashBonusSession != null && !status.equalsIgnoreCase(activeCashBonusSession.getStatus())) {
                        LOG.debug("Seater not found, need save new status");
                        activeCashBonusSession.setStatus(this.status);
                        activeCashBonusSessionPersister.persist(activeCashBonusSession);
                    }

                    IActiveFrbSession activeFrbSession = activeFrbSessionPersister.get(bonusId);
                    if (activeFrbSession != null && !status.equalsIgnoreCase(activeFrbSession.getStatus())) {
                        LOG.debug("FRB Seater not found, need save new status");
                        activeFrbSession.setStatus(this.status);
                        activeFrbSessionPersister.persist(activeFrbSession);
                    }

                }
                String bonusName = "";
                LobbySession lobbySession = lobbySessionService.get(accountId);
                if (lobbySession != null && lobbySession.getActiveCashBonusSession() != null &&
                        lobbySession.getActiveCashBonusSession().getId() == bonusId) {
                    lobbySession.getActiveCashBonusSession().setStatus(status);
                    lobbySessionService.add(lobbySession);
                    bonusName = BonusType.CASHBONUS.name();
                }

                if (lobbySession != null && lobbySession.getActiveFrbSession() != null &&
                        lobbySession.getActiveFrbSession().getBonusId() == bonusId) {
                    lobbySession.getActiveFrbSession().setStatus(status);
                    lobbySessionService.add(lobbySession);
                    bonusName = BonusType.FRB.name();
                }

                LOG.debug("bonusName: {}, bonusId: {} ", bonusName, bonusId);

                ISocketClient lobbySocketClient = lobbySession == null ? null : lobbySession.getSocketClient();
                if (lobbySocketClient != null && !oldStatus.equalsIgnoreCase(status)) {
                    LOG.debug("found lobbySocketClient, send BonusStatusChangedMessage");
                    lobbySocketClient.sendMessage(toFactoryService.createBonusStatusChangedMessage(
                            bonusId, oldStatus, status, "", bonusName));
                }
            } catch (Exception e) {
                LOG.debug("ChangeBonusStatusTask error", e);
            }
        } else {
            LOG.error("ChangeBonusStatusTask ApplicationContext not found accountId: {}", accountId);
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeBonusStatusTask[");
        sb.append("accountId=").append(accountId);
        sb.append(", status='").append(status).append('\'');
        sb.append(", oldStatus='").append(oldStatus).append('\'');
        sb.append(", bonusId=").append(bonusId);
        sb.append(", context=").append(context);
        sb.append(']');
        return sb.toString();
    }
}
