package com.abs.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;

/**
 * User: ktd
 * Date: 29.03.11
 */
public class AbstractBonusClient {
    private long bankId;
    protected BankInfo bankInfo;

    public AbstractBonusClient(long bankId) {
        this.bankId = bankId;
        bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

}
