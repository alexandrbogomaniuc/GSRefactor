package com.abs.casino.websocket.tournaments;

import com.abs.casino.cassandra.IEntityUpdateListener;
import com.dgphoenix.casino.common.transport.TObject;
import com.abs.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.abs.casino.promo.tournaments.messages.BalanceUpdated;
import com.abs.casino.promo.tournaments.messages.PlayerTournamentStateChanged;

public class TournamentWebSocketMessageListener implements IEntityUpdateListener<String, TObject> {
    private final TournamentWebSocketSessionsController webSocketSessionsController;
    private final RemoteCallHelper remoteCallHelper;

    public TournamentWebSocketMessageListener(TournamentWebSocketSessionsController webSocketSessionsController,
                                              RemoteCallHelper remoteCallHelper) {
        this.webSocketSessionsController = webSocketSessionsController;
        this.remoteCallHelper = remoteCallHelper;
    }

    @Override
    public void notify(String sessionId, TObject message) {
        webSocketSessionsController.sendMessage(sessionId, message);
    }

    public void sendPlayerTournamentStateChanged(String sessionId, PlayerTournamentStateChanged message) {
        if (webSocketSessionsController.getClients().get(sessionId) != null) {
            webSocketSessionsController.sendMessage(sessionId, message);
        } else {
            remoteCallHelper.sendPlayerTournamentStateChanged(sessionId,
                    message.getTournamentId(), message.isCannotJoin(), message.isJoined());
        }
    }

    public void sendUpdateBalanceToAllServers(String sessionId, Long balance) {
        if (webSocketSessionsController.getClients().get(sessionId) != null) {
            webSocketSessionsController.sendMessage(sessionId, new BalanceUpdated(balance));
        } else {
            remoteCallHelper.sendBalanceUpdated(sessionId, balance);
        }
    }
}
