package com.abs.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;

import java.util.Map;

/**
 * User: flsh
 * Date: 14.11.14.
 */
public interface IWalletDBLink {
    long getAccountId();

    long getBankId();

    long getGameId();

    GameMode getMode();

    IWallet getWallet();

    Long getRoundId();

    void setRoundId(Long roundId);

    long getGameSessionId();

    AccountInfo getAccount();

    void setLastPaymentOperationId(Long lastPaymentOperationId);

    long getWinAmount();

    void setWinAmount(long winAmount);

    GameSession getGameSession();

    Map<String, String[]> getRequestParameters();

    void setRequestParameters(Map<String, String[]> parameters);

    String getRequestParameterValue(String parameterName);

    String getLasthandParameter(String parameterName);
}
