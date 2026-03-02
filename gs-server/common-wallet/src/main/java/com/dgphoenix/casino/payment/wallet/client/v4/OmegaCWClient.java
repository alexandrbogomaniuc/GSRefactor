package com.abs.casino.payment.wallet.client.v4;

import com.abs.casino.common.cache.BaseGameInfoTemplateCache;
import com.abs.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.payment.wallet.CommonWallet;

import java.util.Map;

/**
 * User: flsh
 * Date: 27.04.2022.
 */
@SuppressWarnings("unused")
public class OmegaCWClient extends StandartRESTCWClient {
    private static final String DEBIT_TYPE_NAME = "debitType";
    private static final String BET = "BET";
    private static final String TRANSFER = "TRANSFER";

    public OmegaCWClient(long bankId) {
        super(bankId);
    }

    @Override
    protected Map<String, String> prepareWagerParams(CommonWallet wallet, Map<String, String> params, long accountId, String extUserId, String bet,
                                                     String win, Boolean isRoundFinished, long gsRoundId, long mpRoundId, String gameId, long bankId, long gameSessionId,
                                                     long negativeBet, ClientType clientType, String currencyCode, String cmd) throws CommonException {
        Map<String, String> wagerParams = super.prepareWagerParams(wallet, params, accountId, extUserId, bet, win, isRoundFinished, gsRoundId, mpRoundId, gameId,
                bankId, gameSessionId, negativeBet, clientType, currencyCode, cmd);
        if (!bet.isEmpty()) {
            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getObject(gameId);
            wagerParams.put(DEBIT_TYPE_NAME, template.isMultiplayerGame() ? TRANSFER : BET);
        }
        return wagerParams;
    }

}
