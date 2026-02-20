package com.dgphoenix.casino.gs.managers.payment.bonus.client;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.web.bonus.CBonus;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kda
 * Date: 11.10.12
 * Time: 15:38
 */
public class RESTEMGetClient extends RESTGetClient {
    private String partnerId;
    private String partnerKey;
    private String delimiter = "|";

    public RESTEMGetClient(long bankId) {
        super(bankId);
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
        partnerId = bankInfo.getPartnerId();
        partnerKey = bankInfo.getPartnerKey();
    }

    @Override
    protected Map<String, String> prepareReleaseParams(Bonus bonus, String extUserId) throws CommonException {
        Map<String, String> htbl = super.prepareReleaseParams(bonus, extUserId);
        htbl.put(CBonus.PARAM_USERID, extUserId + delimiter + partnerId + delimiter + partnerKey);
        return htbl;
    }

    @Override
    protected Map<String, String> prepareAuthParams(String token) throws CommonException {
        Map<String, String> htbl = super.prepareAuthParams(token);
        htbl.put(CBonus.PARAM_TOKEN, token + delimiter + partnerId + delimiter + partnerKey);
        return htbl;
    }

    @Override
    protected Map<String, String> prepareAccountParams(String userId) throws CommonException {
        Map<String, String> htbl = super.prepareAccountParams(userId);
        htbl.put(CBonus.PARAM_USERID, userId + delimiter + partnerId + delimiter + partnerKey);
        return htbl;
    }

}
