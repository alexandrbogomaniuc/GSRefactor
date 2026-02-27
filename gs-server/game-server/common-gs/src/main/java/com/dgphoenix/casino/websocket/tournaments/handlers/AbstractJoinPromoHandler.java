package com.abs.casino.websocket.tournaments.handlers;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transport.TObject;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.promo.tournaments.messages.Error;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import com.dgphoenix.casino.websocket.tournaments.IMessageHandler;
import com.dgphoenix.casino.websocket.tournaments.ISocketClient;

import java.util.function.Consumer;

import static com.dgphoenix.casino.promo.tournaments.ErrorCodes.INVALID_SESSION;

public abstract class AbstractJoinPromoHandler<MESSAGE extends TObject> implements IMessageHandler<MESSAGE> {
    protected final ErrorPersisterHelper errorPersisterHelper;

    public AbstractJoinPromoHandler(ErrorPersisterHelper errorPersisterHelper) {
        this.errorPersisterHelper = errorPersisterHelper;
    }

    protected String getCdnParam(ISocketClient client) {
        String cdn = client.getCdn();
        if (StringUtils.isTrimmedEmpty(cdn) || "null".equals(cdn)) {
            return "";
        }
        return "&CDN=" + cdn;
    }

    protected void sendErrorMessageToClient(int rid, ISocketClient client, Consumer<Error> errorSaver,
                                            int errorCode, String cause) {
        client.sendMessage(createErrorMessage(errorCode, cause, rid, errorSaver));
    }

    protected boolean isForceHttps(ISocketClient client) throws CommonException {
        SessionHelper.getInstance().lock(client.getSessionId());
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            return sessionInfo.isForceHttps();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    protected Pair<Long, Integer> getAccountIdAndBankId(ISocketClient client, int rid, Consumer<? super Error> errorSaver) throws CommonException {
        SessionHelper.getInstance().lock(client.getSessionId());
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            int bankId = SessionHelper.getInstance().getTransactionData().getBankId();
            if (sessionInfo == null) {
                client.sendMessage(createErrorMessage(INVALID_SESSION, "Session not found", rid, errorSaver));
                throw new CommonException("SessionInfo not found");
            }
            return new Pair<>(sessionInfo.getAccountId(), bankId);
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    protected boolean checkPlayerAliasHasForbiddenCharacters(String playerAlias) {
        return playerAlias != null && !playerAlias.matches("^[a-zA-Z0-9]+$");
    }
}
