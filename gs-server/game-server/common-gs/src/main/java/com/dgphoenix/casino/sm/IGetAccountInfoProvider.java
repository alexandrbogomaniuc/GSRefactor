package com.abs.casino.sm;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAccountInfoResult;

public interface IGetAccountInfoProvider {

    BonusAccountInfoResult getAccountInfo(String userId) throws CommonException;
}
