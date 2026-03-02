package com.abs.casino.helpers.game.processors;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.abs.casino.common.cache.data.payment.transfer.TransactionStatus;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.entities.game.requests.CTStartGameRequest;
import com.abs.casino.gs.managers.payment.transfer.PaymentManager;

import javax.servlet.http.HttpServletResponse;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public class CTStartGameProcessor extends StartGameProcessor<CTStartGameRequest> {
    private final static CTStartGameProcessor instance = new CTStartGameProcessor();

    private CTStartGameProcessor() {
    }

    public static CTStartGameProcessor getInstance() {
        return instance;
    }

    @Override
    public void additionalProcess(CTStartGameRequest startGameRequest, HttpServletResponse response, AccountInfo accountInfo,
                                  SessionInfo sessionInfo, IBaseGameInfo gameInfo, GameMode mode, Long gameSessionId)
            throws CommonException {
        // make DEPOSIT
        if (GameMode.REAL.equals(mode)) {
            Long transactionAmount = startGameRequest.getBalance();
            if (transactionAmount == null) {
                throw new CommonException("transaction amount is null");
            }

            if (transactionAmount < 0) {
                throw new CommonException("transaction amount is not correct, amount:" + transactionAmount);
            }
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            PaymentTransaction transaction = PaymentManager.getInstance().processDeposit(accountInfo,
                    bankInfo, gameSessionId, gameInfo.getId(), transactionAmount, null, true,
                    startGameRequest.getClientType(), null);

            if (TransactionStatus.APPROVED.equals(transaction.getStatus())) {
                accountInfo.incrementBalance(transactionAmount, false);
            } else {
                throw new CommonException(transaction.getDescription());
            }
        }
    }

}
