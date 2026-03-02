package com.abs.casino.promo;

import com.abs.casino.common.cache.data.account.IAccountInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.IPromoCampaign;

import java.util.Map;

/**
 * User: flsh
 * Date: 02.06.2022.
 */
public interface IPlayerAliasService {
    Long getPromoAccountId(long promoId, long bankId, String alias);
    Map<Long, String> getAllPromoAliases(long promoId);
    Map<Long, String> getAllBankAliases(long promoId, long bankId);
    String generateAlias(IAccountInfo account, IPromoCampaign campaign) throws CommonException;
}
