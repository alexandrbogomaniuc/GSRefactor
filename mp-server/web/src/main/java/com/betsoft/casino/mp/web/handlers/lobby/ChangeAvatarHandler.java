package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.persister.PlayerProfilePersister;
import com.betsoft.casino.mp.model.AvatarParts;
import com.betsoft.casino.mp.model.IAvatar;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.PlayerProfile;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.transport.ChangeAvatar;
import com.betsoft.casino.mp.transport.Ok;
import com.betsoft.casino.mp.utils.ErrorCodes;
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

/**
 * User: flsh
 * Date: 16.07.18.
 */
@Component
public class ChangeAvatarHandler extends MessageHandler<ChangeAvatar, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(ChangeAvatarHandler.class);

    private PlayerProfilePersister playerProfilePersister;

    public ChangeAvatarHandler(Gson gson, LobbySessionService lobbySessionService,
                               LobbyManager lobbyManager, CassandraPersistenceManager cpm) {
        super(gson, lobbySessionService, lobbyManager);
        this.playerProfilePersister = cpm.getPersister(PlayerProfilePersister.class);
    }

    @Override
    public void handle(WebSocketSession session, ChangeAvatar message, ILobbySocketClient client) {
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
        boolean borderAvailable = AvatarParts.BORDER.isPartAvailable(profile.getBorders(), message.getBorderStyle());
        boolean heroAvailable = AvatarParts.HERO.isPartAvailable(profile.getHeroes(), message.getHero());
        boolean backgroundAvailable = AvatarParts.BACKGROUND.isPartAvailable(profile.getBackgrounds(), message.getBackground());
        if (borderAvailable && heroAvailable && backgroundAvailable) {
            profile.setBorder(message.getBorderStyle());
            profile.setHero(message.getHero());
            profile.setBackground(message.getBackground());
            playerProfilePersister.save(client.getBankId(), client.getAccountId(), profile);
            LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
            if (lobbySession != null && lobbySession.getAvatar() != null) {
                IAvatar avatar = lobbySession.getAvatar();
                avatar.setBorderStyle(profile.getBorder());
                avatar.setHero(profile.getHero());
                avatar.setBackground(profile.getBackground());
                //commit changed session
                lobbySessionService.add(lobbySession);
            }
            client.sendMessage(new Ok(getCurrentTime(), message.getRid()), message);
        } else {
            client.sendMessage(createErrorMessage(ErrorCodes.AVATAR_PART_NOT_AVAILABLE, "Avatar not available",
                    message.getRid()), message);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
