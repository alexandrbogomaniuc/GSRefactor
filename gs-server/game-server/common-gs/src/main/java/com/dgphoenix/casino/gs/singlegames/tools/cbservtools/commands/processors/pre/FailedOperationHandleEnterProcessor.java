package com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.pre;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTrackerTask;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.ILockedProcessor;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.annotations.PreProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import org.springframework.core.annotation.Order;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@PreProcessor
@Order(0)
public class FailedOperationHandleEnterProcessor implements ILockedProcessor {
    private final BankInfoCache bankInfoCache;
    private static final int TIMEOUT = 10;
    private static final int REPEAT_COUNT = 3;

    public FailedOperationHandleEnterProcessor(BankInfoCache bankInfoCache) {
        this.bankInfoCache = bankInfoCache;
    }

    @Nullable
    @Override
    public ServerResponse processLocked(ServletRequest request, String sessionId, String command,
                                        ITransactionData transactionData, IDBLink dbLink,
                                        boolean roundFinished) throws CommonException, IOException {

        BankInfo bankInfo = bankInfoCache.getBankInfo(transactionData.getBankId());
        if (!bankInfo.isTrackWinInNewGameSession() || dbLink.isFRBGame() || dbLink.isBonusGameSession() || !dbLink.isWalletBank() || !dbLink.isForReal()) {
            return null;
        }

        int gameId = (int) transactionData.getGameSession().getGameId();
        CommonWalletOperation operation = (CommonWalletOperation) transactionData.getWallet().getCurrentWalletOperation(gameId);
        if (operation != null && WalletOperationStatus.PEENDING_SEND_ALERT.equals(operation.getExternalStatus())) {
            AccountInfo account = transactionData.getAccount();
            for (int i = 1; ; i++) {
                try {
                    new WalletTrackerTask(transactionData.getAccountId(), gameId, WalletTracker.getInstance(), true).process(true, 5000);
                    account.setBalance(transactionData.getWallet().getServerBalance());
                    SessionHelper.getInstance().commitTransaction();
                    break;
                } catch (CommonException e) {
                    if (i < REPEAT_COUNT) {
                        delayStart();
                    } else {
                        throw e;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean canProcessCommand(String command, boolean isNewRoundBet) {
        return IGameController.CMDENTER.equals(command) || IGameController.CMDRESTART.equals(command);
    }

    private void delayStart() {
        try {
            TimeUnit.SECONDS.sleep(TIMEOUT);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
