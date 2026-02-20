package com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.command;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.dgphoenix.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.responses.RefreshBalanceResponse;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;

public class RefreshBalanceProcessor implements IUnlockedCommandProcessor {
    private static final Logger LOG = LogManager.getLogger(RefreshBalanceProcessor.class);

    private final NtpTimeProvider timeProvider;

    public RefreshBalanceProcessor(NtpTimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Nullable
    @Override
    public ServerResponse processUnlocked(ServletRequest request, String sessionId, String command, IDBLink dbLink, ITransactionData data)
            throws CommonException {
        long balanceCents = dbLink != null ? dbLink.getBalanceLong() : data.getAccount().getBalance();
        double balance = DigitFormatter.getDollarsFromCents(balanceCents);
        if (dbLink != null) {
            balance = dbLink.adjustMoneyValue(balance);
        }
        LOG.debug("processRefreshBalance: balance={}", balance);
        return new RefreshBalanceResponse(balance, timeProvider.getTimeMicroseconds());
    }

    @Override
    public String getCommand() {
        return IGameController.CMD_REFRESH_BALANCE;
    }
}
