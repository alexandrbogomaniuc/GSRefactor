package com.abs.casino.actions.support.walletinfo;

import com.abs.casino.cassandra.persist.CassandraTransactionDataPersister;
import com.abs.casino.common.web.BaseAction;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by quant on 02.02.16.
 */
public class WalletInfoForm extends ActionForm {
    private long accountId;
    private int gameId;
    private int changeType;
    private String accountData;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getChangeType() {
        return changeType;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getAccountData() {
        return accountData;
    }

    public void setAccountData(String accountData) {
        this.accountData = accountData;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (BaseAction.extractRequestParameterIgnoreCase(request, "accountId") == null) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.missingProperty", "accountId"));
        }
        if (BaseAction.extractRequestParameterIgnoreCase(request, "gameId") == null) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.missingProperty", "gameId"));
        }
        if (BaseAction.extractRequestParameterIgnoreCase(request, "changeType") == null) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.missingProperty", "changeType"));
        } else if (changeType != WalletInfoAction.Status.UNRESOLVED && changeType != WalletInfoAction.Status.DELETED
                && changeType != WalletInfoAction.Status.RESOLVED) {
            errors.add(ActionErrors.GLOBAL_MESSAGE,
                    new ActionMessage("error.common.unsupportedPropertyValue", "changeType", changeType));
        }
        if (BaseAction.extractRequestParameterIgnoreCase(request, "accountData") == null) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.missingProperty", "accountData"));
        } else if (!accountData.equals(CassandraTransactionDataPersister.WALLET_FIELD)
                && !accountData.equals(CassandraTransactionDataPersister.LAST_HAND_FIELD)) {
            errors.add(ActionErrors.GLOBAL_MESSAGE,
                    new ActionMessage("error.common.unsupportedPropertyValue", "accountData", accountData));
        }
        return errors;
    }
}
