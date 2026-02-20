package com.dgphoenix.casino.gs.managers.dblink;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletDBLink;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 16.01.2020
 */
public class EmptyDBLink implements IWalletDBLink {

    private static final String NOT_SUPPORTED = "Method not supported";

    @Override
    public long getAccountId() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public long getBankId() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public long getGameId() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public GameMode getMode() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public IWallet getWallet() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public Long getRoundId() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setRoundId(Long roundId) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public long getGameSessionId() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public AccountInfo getAccount() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setLastPaymentOperationId(Long lastPaymentOperationId) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public long getWinAmount() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setWinAmount(long winAmount) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public GameSession getGameSession() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        return Collections.emptyMap();
    }

    @Override
    public void setRequestParameters(Map<String, String[]> parameters) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public String getRequestParameterValue(String parameterName) {
        return null;
    }

    @Override
    public String getLasthandParameter(String parameterName) {
        return null;
    }

}
