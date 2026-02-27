package com.abs.casino.gs.managers.payment.bonus.client;

import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.wallet.CCommonWallet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: ss
 * Date: 21.06.17
 */
public class RESTBETDSIClient extends RESTClient {
    private static final Logger LOG = LogManager.getLogger(RESTBETDSIClient.class);

    protected Map<String, String> renamedParameters = new HashMap<>();

    public RESTBETDSIClient(long bankId) {
        super(bankId);
        renamedParameters.put(CCommonWallet.PARAM_USERID, "CustomerID");
        renamedParameters.put(CCommonWallet.PARAM_BET, "riskAmount");
        renamedParameters.put(CCommonWallet.PARAM_WIN, "winAmount");
        renamedParameters.put(CCommonWallet.PARAM_ROUNDID, "roundID");
        renamedParameters.put(CCommonWallet.PARAM_GAMEID, "gameID");
        renamedParameters.put(CCommonWallet.PARAM_GAMESESSIONID, "gameSessionID");
        renamedParameters.put(CCommonWallet.PARAM_CASINOTRANSACTIONID, "transactionID");
        renamedParameters.put(CBonus.PARAM_BONUSID, "transactionID");
    }

    protected Map<String, String> processRenamedParameters(Map<String, String> parameterMap) {
        Map<String, String> processedParameters = new HashMap<>();
        return parameterMap.keySet()
                .stream()
                .collect(Collectors.toMap(key -> renamedParameters.containsKey(key) ? renamedParameters.get(key) : key,
                        key -> parameterMap.get(key)));
    }

    @Override
    protected Map<String, String> prepareAccountParams(String userId) throws CommonException {
        return processRenamedParameters(super.prepareAccountParams(userId));
    }

    @Override
    protected Map<String, String> prepareReleaseParams(Bonus bonus, String extUserId) throws CommonException {
        return processRenamedParameters(super.prepareReleaseParams(bonus, extUserId));
    }

    @Override
    protected boolean isPost() {
        return bankInfo.isPOST();
    }
}
