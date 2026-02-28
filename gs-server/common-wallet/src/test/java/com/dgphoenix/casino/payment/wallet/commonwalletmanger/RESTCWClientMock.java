package com.abs.casino.payment.wallet.commonwalletmanger;

import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletWagerResult;
import com.dgphoenix.casino.gs.managers.payment.wallet.RemoteClientStubHelper;
import com.abs.casino.payment.wallet.client.v4.RESTCWClient;

import java.util.ArrayList;
import java.util.List;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

public class RESTCWClientMock extends RESTCWClient {
    public RESTCWClientMock(long bankId) {
        super(bankId);
    }

    @Override
    public CommonWalletWagerResult wager(long accountId, String extUserId, String bet, String win, Boolean isRoundFinished, long gsRoundId, long mpRoundId, long gameId, long bankId, CommonWalletOperation operation, CommonWallet wallet, ClientType clientType, Currency curr) throws CommonException {

        List<CharSequence> transaction = new ArrayList<>();
        if (!isTrimmedEmpty(bet)) {
            transaction.addAll(StringUtils.split(bet, "|"));
            RemoteClientStubHelper.getInstance().makeRecordedBet(extUserId, Long.parseLong(transaction.get(0).toString()),
                    Long.parseLong(transaction.get(1).toString()));
        } else {
            transaction.addAll(StringUtils.split(win, "|"));
            RemoteClientStubHelper.getInstance().makeRecordedWin(extUserId, Long.parseLong(transaction.get(0).toString()),
                    Long.parseLong(transaction.get(1).toString()));
        }

        RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo = RemoteClientStubHelper.getInstance().getExtAccountInfo(extUserId);
        return new CommonWalletWagerResult(extUserId + transaction.get(1), extAccountInfo.getBalance(), true, null, null);
    }
}