package com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.dgphoenix.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletRequest;

public class GetTimeProcessor implements ILockedCommandProcessor {
    private static final Logger LOG = LogManager.getLogger(GetTimeProcessor.class);

    public static final String SESSION_TIMER_REMINDER = "SESSION_TIMER_REMINDER";

    private final NtpTimeProvider timeProvider;
    private final BankInfoCache bankInfoCache;

    public GetTimeProcessor(NtpTimeProvider timeProvider, BankInfoCache bankInfoCache) {
        this.timeProvider = timeProvider;
        this.bankInfoCache = bankInfoCache;
    }

    @Override
    public ServerResponse processLocked(ServletRequest request, String sessionId, String command,
                                        ITransactionData transactionData, IDBLink dbLink, boolean roundFinished) throws CommonException {
        long bankId = dbLink.getBankId();
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        GameSession gameSession = dbLink.getGameSession();
        transactionData.getPlayerSession().updateActivity();
        return null;
    }

    @Override
    public String getCommand() {
        return IGameController.CMDGETTIME;
    }
}
