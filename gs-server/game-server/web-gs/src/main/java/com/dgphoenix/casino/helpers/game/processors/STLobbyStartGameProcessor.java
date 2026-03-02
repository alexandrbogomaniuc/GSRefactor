package com.abs.casino.helpers.game.processors;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.DigitFormatter;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.entities.game.requests.STLobbyStartGameRequest;
import com.abs.casino.gs.managers.payment.wallet.IWalletProtocolManager;
import com.abs.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.abs.casino.gs.managers.payment.wallet.v2.ICommonWalletClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public class STLobbyStartGameProcessor extends StartGameProcessor<STLobbyStartGameRequest> {
    private final static Logger LOG = LogManager.getLogger(STLobbyStartGameProcessor.class);

    private final static STLobbyStartGameProcessor instance = new STLobbyStartGameProcessor();

    private STLobbyStartGameProcessor() {
    }

    public static STLobbyStartGameProcessor getInstance() {
        return instance;
    }

    @Override
    public void additionalProcess(STLobbyStartGameRequest startGameRequest, HttpServletResponse response, AccountInfo accountInfo,
                                  SessionInfo sessionInfo, IBaseGameInfo gameInfo, GameMode mode, Long gameSessionId)
            throws CommonException {
        WalletProtocolFactory factory = WalletProtocolFactory.getInstance();
        if (mode == GameMode.REAL && startGameRequest.isUpdateBalance() &&
                factory.isWalletBank(accountInfo.getBankId())) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            if (!StringUtils.isTrimmedEmpty(bankInfo.getCWBalanceUrl())) {
                IWalletProtocolManager ocwm = factory.getWalletProtocolManager(accountInfo.getBankId());
                ICommonWalletClient client = ocwm.getClient();
                double balance = client.getBalance(accountInfo.getId(), accountInfo.getExternalId(),
                        accountInfo.getBankId(), accountInfo.getCurrency());
                long newBalance;
                if (bankInfo.isParseLong()) {
                    newBalance = (long) balance;
                } else {
                    newBalance = DigitFormatter.getCentsFromCurrency(balance);
                }
                LOG.info("UpdateBalance: sessionId =  " + sessionInfo.getSessionId() +
                        " new balance = " + newBalance +
                        " old balance = " + accountInfo.getBalance() +
                        " accountId = " + accountInfo.getId());

                accountInfo.setBalance(newBalance);
            } else {
                LOG.info("UpdateBalance: forced but no CWBalanceUrl was set for bank: " + bankInfo);
            }
        }
    }
}
