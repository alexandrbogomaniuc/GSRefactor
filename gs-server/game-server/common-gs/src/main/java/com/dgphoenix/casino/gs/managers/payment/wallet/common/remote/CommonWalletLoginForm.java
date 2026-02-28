package com.abs.casino.gs.managers.payment.wallet.common.remote;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.util.DigitFormatter;
import com.abs.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class CommonWalletLoginForm extends CommonActionForm {
    private final static Logger LOG = LogManager.getLogger(CommonWalletLoginForm.class);
    private String userId;
    private long balance;
    private String gameId;
    private String mode;

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    public GameMode getGameMode() {
        return GameMode.getByName(mode);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public Map<String, String> getAdditionalParams() {
        return null;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);
        ThreadLog.debug("CWLoginForm: validate subCasinoId=" + getSubCasinoId());
        String userId = BaseAction.extractRequestParameterIgnoreCase(request, "userId");
        if (StringUtils.isTrimmedEmpty(userId)) {
            ThreadLog.error("validate error userId is empty");
            actionErrors.add("valid_error", new ActionMessage("error.login.incorrectCredentials"));
            return actionErrors;
        }
        this.userId = userId;

        try {
            String sBalance = BaseAction.extractRequestParameterIgnoreCase(request, "balance");
            this.balance = StringUtils.isTrimmedEmpty(sBalance) ? 0 :
                    DigitFormatter.getCentsFromCurrency(Double.parseDouble(sBalance));
        } catch (NumberFormatException e) {
            ThreadLog.error(this.getClass().getSimpleName() + "::validate error balance is wrong", e);
            actionErrors.add("valid_error", new ActionMessage("error.login.generalValidationError"));
            return actionErrors;
        }
        return actionErrors;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        userId = null;
        balance = 0;
        gameId = null;
        mode = null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CommonWalletLoginForm");
        sb.append("[userId='").append(userId).append('\'');
        sb.append(", balance=").append(balance);
        sb.append(", bankId=").append(bankId);
        sb.append(", subCasinoId=").append(subCasinoId);
        sb.append(", gameId=").append(gameId);
        sb.append(", clientType=").append(clientType);
        sb.append(", host='").append(getHost()).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
