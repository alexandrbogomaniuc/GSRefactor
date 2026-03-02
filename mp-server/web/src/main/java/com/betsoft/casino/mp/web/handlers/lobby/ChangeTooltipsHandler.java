package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.persister.PlayerProfilePersister;
import com.betsoft.casino.mp.model.AvatarParts;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.PlayerProfile;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.transport.ChangeToolTips;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.HashSet;

@Component
public class ChangeTooltipsHandler extends MessageHandler<ChangeToolTips, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(ChangeNicknameHandler.class);

    private PlayerProfilePersister playerProfilePersister;
    protected final RoomPlayerInfoService playerInfoService;

    public ChangeTooltipsHandler(Gson gson, LobbySessionService lobbySessionService,
                                 LobbyManager lobbyManager,
                                 RoomPlayerInfoService playerInfoService,
                                 CassandraPersistenceManager cpm) {
        super(gson, lobbySessionService, lobbyManager);
        this.playerProfilePersister = cpm.getPersister(PlayerProfilePersister.class);
        this.playerInfoService = playerInfoService;
    }

    @Override
    public void handle(WebSocketSession session, ChangeToolTips message, ILobbySocketClient client) {
        if (!checkLogin(message, client)) {
            return;
        }
        PlayerProfile profile = playerProfilePersister.load(client.getBankId(), client.getAccountId());
        if (profile == null) {
            LOG.debug("PlayerProfile not found, create new for client={}", client);
            profile = new PlayerProfile(new HashSet<>(), new HashSet<>(), new HashSet<>(),
                    AvatarParts.BORDER.getDefaultPartId(), AvatarParts.HERO.getDefaultPartId(),
                    AvatarParts.BACKGROUND.getDefaultPartId(), false);
        }

        profile.setDisableTooltips(message.isDisableTooltips());

        playerProfilePersister.save(client.getBankId(), client.getAccountId(), profile);

        playerInfoService.lock(client.getAccountId());
        getLog().debug("handle HS lock: {}", client.getAccountId());
        try {
            LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
            if (lobbySession != null && lobbySession.getAvatar() != null) {
                lobbySession.setDisableTooltips(profile.isDisableTooltips());
                lobbySessionService.add(lobbySession);
            }
        } finally {
            try {
                playerInfoService.unlock(client.getAccountId());
            } catch (Exception e) {
                //may be already locked other thread
                getLog().error("changeToolTipsHandler: unlock error in catch");
            }
            getLog().debug("handle HS unlock: {}", client.getAccountId());
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
