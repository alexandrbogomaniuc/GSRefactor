package com.abs.casino.system.configuration;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.string.StringUtils;

/**
 * User: plastical
 * Date: 06.04.2010
 */
public class PlayerSessionConfiguration {
    private static final PlayerSessionConfiguration instance = new PlayerSessionConfiguration();

    public static PlayerSessionConfiguration getInstance() {
        return instance;
    }

    private PlayerSessionConfiguration() {
    }

    public String getPSMClass(long bankId) throws CommonException {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo != null) {
            String psmClass = bankInfo.getPSMClass();
            if (!StringUtils.isTrimmedEmpty(psmClass)) {
                return psmClass;
            }
        }
        throw new CommonException("PSM_CLASS not configured for bankId=" + bankId);
    }

}
