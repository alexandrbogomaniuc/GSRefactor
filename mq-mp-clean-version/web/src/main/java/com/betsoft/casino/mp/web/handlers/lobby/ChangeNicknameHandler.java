package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.NicknameValidator;
import com.betsoft.casino.mp.transport.ChangeNickname;
import com.betsoft.casino.mp.transport.Ok;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.NicknameService;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * User: flsh
 * Date: 16.07.18.
 */
@Component
public class ChangeNicknameHandler extends MessageHandler<ChangeNickname, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(ChangeNicknameHandler.class);

    private NicknameValidator validator;
    private NicknameService nicknameService;

    public ChangeNicknameHandler(Gson gson, LobbySessionService lobbySessionService,
                                 LobbyManager lobbyManager, NicknameValidator validator,
                                 NicknameService nicknameService) {
        super(gson, lobbySessionService, lobbyManager);
        this.validator = validator;
        this.nicknameService = nicknameService;
    }

    @Override
    public void handle(WebSocketSession session, ChangeNickname message, ILobbySocketClient client) {
        if (!checkLogin(message, client)) {
            return;
        }
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession != null && !lobbySession.isNicknameEditable()) {
            client.sendMessage(createErrorMessage(ErrorCodes.NOT_ALLOWED_CHANGE_NICKNAME, "Change nickname not allowed",
                    message.getRid()), message);
            return;
        }
        if (StringUtils.isTrimmedEmpty(message.getNickname()) || validator.isObscene(message.getNickname())) {
            client.sendMessage(createErrorMessage(ErrorCodes.ILLEGAL_NICKNAME, "Illegal nickname", message.getRid()),
                    message);
            return;
        }
        Ok okMsg = new Ok(getCurrentTime(), message.getRid());
        String oldNickName = nicknameService.getNickname(client.getBankId(), client.getAccountId());
        if (oldNickName != null && oldNickName.equals(message.getNickname())) {
            client.sendMessage(okMsg, message);
        } else if (client.getPlayerInfo() != null && client.getPlayerInfo().isGuest()) {
            LOG.debug("Changed nickname in guest mode: {}", message.getNickname());
            setNickname(client, message, okMsg);
        } else if (nicknameService.isNicknameAvailable(message.getNickname(),
                client.getBankId(), client.getAccountId())) {
            try {
                boolean success = nicknameService.changeNickname(client.getBankId(), client.getAccountId(),
                        oldNickName, message.getNickname());
                if (success) {
                    LOG.debug("Success change nickName={}", message.getNickname());
                    setNickname(client, message, okMsg);
                } else {
                    LOG.debug("Cannot change nickName={}, old={}, may be collision",
                            message.getNickname(), oldNickName);
                    client.sendMessage(createErrorMessage(ErrorCodes.NICKNAME_NOT_AVAILABLE, "Nickname not available",
                            message.getRid()), message);
                }
            } catch (CommonException e) {
                LOG.error("Cannot change nickName: {}, old={}", message.getNickname(), oldNickName, e);
                client.sendMessage(createErrorMessage(ErrorCodes.INTERNAL_ERROR, "Internal error",
                        message.getRid()), message);
            }
        } else {
            client.sendMessage(createErrorMessage(ErrorCodes.NICKNAME_NOT_AVAILABLE, "Nickname not available",
                    message.getRid()), message);
        }
    }

    private void setNickname(ILobbySocketClient client, ChangeNickname message, Ok okMsg) {
        client.setNickname(message.getNickname());
        client.sendMessage(okMsg, message);
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession != null) {
            lobbySession.setNickname(message.getNickname());
            //commit changed session
            lobbySessionService.add(lobbySession);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
