package com.abs.casino.websocket.tournaments.handlers;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.mp.BattlegroundHistoryPersister;
import com.abs.casino.cassandra.persist.mp.BattlegroundRound;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.promo.tournaments.messages.Error;
import com.abs.casino.promo.tournaments.messages.GetBattlegroundHistory;
import com.abs.casino.promo.tournaments.messages.GetBattlegroundHistoryResponse;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import com.dgphoenix.casino.websocket.tournaments.IMessageHandler;
import com.dgphoenix.casino.websocket.tournaments.ISocketClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.dgphoenix.casino.promo.tournaments.ErrorCodes.*;

public class GetBattlegroundHistoryHandler implements IMessageHandler<GetBattlegroundHistory> {

    private static final Logger LOG = LogManager.getLogger(GetBattlegroundHistoryHandler.class);

    private final ErrorPersisterHelper errorPersisterHelper;
    private final BattlegroundHistoryPersister battlegroundHistoryPersister;

    public GetBattlegroundHistoryHandler(ErrorPersisterHelper errorPersisterHelper, CassandraPersistenceManager cpm) {
        this.errorPersisterHelper = errorPersisterHelper;
        this.battlegroundHistoryPersister = cpm.getPersister(BattlegroundHistoryPersister.class);
    }

    @Override
    public void handle(GetBattlegroundHistory message, ISocketClient client) {
        Consumer<Error> errorSaver = error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message);
        if (client.isConnected()) {
            try {
                Pair<Long, Integer> accountIdAndBankId = getAccountIdAndBankId(client, message.getRid(), errorSaver);
                if (!isCorrectParameters(message, client, errorSaver)) {
                    return;
                }
                prepareAndSendMessages(message, client, accountIdAndBankId);
            } catch (Exception e) {
                processUnexpectedError(client, message, e,
                        error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message, e));
            }
        } else {
            sendErrorMessageToClient(message.getRid(), client, errorSaver, NOT_LOGGED_IN, "Not logged in");
        }
    }

    private boolean isCorrectParameters(GetBattlegroundHistory message, ISocketClient client, Consumer<Error> errorSaver) {
        if (!isCorrectTimeValue(message.getStartDate(), message.getEndDate())) {
            sendErrorMessageToClient(message.getRid(), client, errorSaver, TIME_IS_NOT_CORRECT, "Time is not" +
                    " correct: ");
            return false;
        }
        return true;
    }

    private boolean isCorrectTimeValue(Long startTime, Long endTime) {
        long start = startTime == null ? 0 : startTime;
        long end = endTime == null ? System.currentTimeMillis() : endTime;
        return end > start;
    }

    private void prepareAndSendMessages(GetBattlegroundHistory message, ISocketClient client, Pair<Long, Integer> accountIdAndBankId) {
        Integer gameId = message.getGameId();
        long startTime = getStartTime(message.getStartDate());
        long endTime = getEndTime(message.getEndDate());
        Long accountId = accountIdAndBankId.getKey();
        List<BattlegroundRound> battlegroundRounds;
        if (gameId != null) {
            battlegroundRounds = battlegroundHistoryPersister.getBattlegroundHistoryByAccountIdAndPeriodAndGameId(accountId,
                    startTime, endTime, gameId);
        } else {
            battlegroundRounds = battlegroundHistoryPersister.getBattlegroundHistoryByAccountIdAndPeriod(accountId, startTime, endTime);
        }

        GetBattlegroundHistoryResponse battlegroundHistoryResponse = new GetBattlegroundHistoryResponse(System.currentTimeMillis(),
                message.getRid(), battlegroundRounds);
        client.sendMessage(battlegroundHistoryResponse);
    }

    private long getStartTime(Long startTime) {
        if (startTime == null) {
            return System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);
        } else {
            return startTime;
        }
    }

    private long getEndTime(Long endTime) {
        if (endTime == null) {
            return System.currentTimeMillis();
        } else {
            return endTime;
        }
    }

    private void sendErrorMessageToClient(int rid, ISocketClient client, Consumer<Error> errorSaver,
                                          int errorCode, String cause) {
        client.sendMessage(createErrorMessage(errorCode, cause, rid, errorSaver));
    }

    private Pair<Long, Integer> getAccountIdAndBankId(ISocketClient client, int rid, Consumer<? super Error> errorSaver) throws CommonException {
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


    @Override
    public Logger getLog() {
        return LOG;
    }
}
