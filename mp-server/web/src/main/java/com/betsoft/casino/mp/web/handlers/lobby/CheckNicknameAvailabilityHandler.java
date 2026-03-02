package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.NicknameValidator;
import com.betsoft.casino.mp.transport.CheckNicknameAvailability;
import com.betsoft.casino.mp.transport.Ok;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.NicknameService;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.abs.casino.common.util.string.StringUtils;
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
public class CheckNicknameAvailabilityHandler extends MessageHandler<CheckNicknameAvailability, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(CheckNicknameAvailabilityHandler.class);

    private NicknameValidator validator;
    private NicknameService nicknameService;

    public CheckNicknameAvailabilityHandler(Gson gson, LobbySessionService lobbySessionService,
                                            LobbyManager lobbyManager, NicknameValidator validator,
                                            NicknameService nicknameService) {
        super(gson, lobbySessionService, lobbyManager);
        this.validator = validator;
        this.nicknameService = nicknameService;
    }

    @Override
    public void handle(WebSocketSession session, CheckNicknameAvailability message, ILobbySocketClient client) {
        if (!checkLogin(message, client)) {
            return;
        }
        if (StringUtils.isTrimmedEmpty(message.getNickname()) || validator.isObscene(message.getNickname())) {
            client.sendMessage(createErrorMessage(ErrorCodes.ILLEGAL_NICKNAME, "Illegal nickname", message.getRid()),
                    message);
            return;
        }
        if (client.getPlayerInfo() != null && client.getPlayerInfo().isGuest()) {
            client.sendMessage(new Ok(getCurrentTime(), message.getRid()), message);
        }
        if (nicknameService.isNicknameAvailable(message.getNickname(), client.getBankId(), client.getAccountId())) {
            client.sendMessage(new Ok(getCurrentTime(), message.getRid()), message);
        } else {
            client.sendMessage(createErrorMessage(ErrorCodes.NICKNAME_NOT_AVAILABLE, "Nickname not available",
                    message.getRid()), message);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
