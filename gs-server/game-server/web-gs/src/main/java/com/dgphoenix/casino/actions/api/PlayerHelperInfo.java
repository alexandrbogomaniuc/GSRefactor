package com.abs.casino.actions.api;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;

/**
 * User: flsh
 * Date: 01.11.13
 */
public class PlayerHelperInfo {
    private AccountInfo account;
    private Long gameId;
    private Long bankId;
    protected BankInfo bankInfo;

    public PlayerHelperInfo() {
    }

    public PlayerHelperInfo(AccountInfo account, Long gameId, Long bankId,
                            BankInfo bankInfo) {
        this.account = account;
        this.gameId = gameId;
        this.bankId = bankId;
        this.bankInfo = bankInfo;
    }

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public Long getAccountId() {
        return account == null ? null : account.getId();
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public BankInfo getBankInfo() {
        return bankInfo;
    }

    public void setBankInfo(BankInfo bankInfo) {
        this.bankInfo = bankInfo;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlayerHelperInfo [");
        sb.append("accountId=").append(account == null ? "null" : account.getId());
        sb.append(", gameId=").append(gameId);
        sb.append(", bankId=").append(bankId);
        sb.append(", bankInfo=").append(bankInfo);
        sb.append(']');
        return sb.toString();
    }
}
