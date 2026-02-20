package com.dgphoenix.casino.actions.support.walletsmanager;

import com.dgphoenix.casino.common.web.BaseAction;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by quant on 15.06.16.
 */
public class WalletsManagerForm extends ActionForm {
    private long accountId;
    private long bankId;
    private String extUserId;
    private int gameId;
    private String accountData;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getExtUserId() {
        return extUserId;
    }

    public void setExtUserId(String extUserId) {
        this.extUserId = extUserId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
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
        if (BaseAction.extractRequestParameterIgnoreCase(request, "accountId") == null
                && (BaseAction.extractRequestParameterIgnoreCase(request, "bankId") == null
                || BaseAction.extractRequestParameterIgnoreCase(request, "extUserId") == null)) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.missingProperty", "accountId"));
        }
        if (BaseAction.extractRequestParameterIgnoreCase(request, "accountData") == null) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.missingProperty", "accountData"));
        } else if (!accountData.equals(WalletsManagerAction.ACCOUNT_DATA_WALLET)
                && !accountData.equals(WalletsManagerAction.ACCOUNT_DATA_LAST_HAND)
                && !accountData.equals(WalletsManagerAction.ACCOUNT_DATA_FRB_WIN)
                && !accountData.equals(WalletsManagerAction.ACCOUNT_DATA_SHOW)
                && !accountData.equals(WalletsManagerAction.ACCOUNT_DATA_DEL_FRB_NOTIFICATION)
                && !accountData.equals(WalletsManagerAction.ACCOUNT_DATA_RESTART_FRB_NOTIFICATION)
        ) {
            errors.add(ActionErrors.GLOBAL_MESSAGE,
                    new ActionMessage("error.common.unsupportedPropertyValue", "accountData", accountData));
        }
        if (accountData != null
                && BaseAction.extractRequestParameterIgnoreCase(request, "gameId") == null
                && (accountData.equals(WalletsManagerAction.ACCOUNT_DATA_WALLET)
                || accountData.equals(WalletsManagerAction.ACCOUNT_DATA_LAST_HAND)
                || accountData.equals(WalletsManagerAction.ACCOUNT_DATA_FRB_WIN)
        )
        ) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.missingProperty", "gameId"));
        }
        return errors;
    }
}
