package com.dgphoenix.casino.payment.wallet.client.v4;

import com.dgphoenix.casino.gs.managers.payment.wallet.v4.CWMType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: nieky
 * Date: 08.01.13
 * Time: 16:26
 */
public class StandartRESTCWClient extends RESTCWClient {
    private static final Logger LOG = LogManager.getLogger(StandartRESTCWClient.class);

    public CWMType cwmType;

    public StandartRESTCWClient(long bankId) {
        super(bankId);
        this.cwmType = CWMType.getCWMTypeByString(bankInfo.getCWMType());
    }

    @Override
    protected boolean isPost() {
        return isPostByBankProperty();
    }

    public boolean isIgnoreRoundFinishedParamOnWager() {
        return cwmType.equals(CWMType.SEND_WIN_ONLY) || cwmType.equals(CWMType.SEND_WIN_ACCUMULATED);
    }
}
