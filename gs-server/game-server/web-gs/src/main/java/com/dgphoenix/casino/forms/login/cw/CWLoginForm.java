package com.abs.casino.forms.login.cw;

import com.abs.casino.common.util.DigitFormatter;
import com.abs.casino.common.util.logkit.ThreadLog;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.forms.login.CommonLoginForm;
import com.abs.casino.gs.managers.payment.wallet.CCommonWallet;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: plastical
 * Date: 02.04.2010
 */
public class CWLoginForm extends CommonLoginForm {
    private final static Logger LOG = LogManager.getLogger(CWLoginForm.class);

    protected String balance;

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        setCheckToken(false);

        ActionErrors actionErrors = super.validate(mapping, request);

        String userId = BaseAction.extractRequestParameterIgnoreCase(request, CCommonWallet.PARAM_USERID);
        if (StringUtils.isTrimmedEmpty(userId)) {
            ThreadLog.error("validate error userId is empty");
            actionErrors.add("valid_error", new ActionMessage("error.login.incorrectCredentials"));
            return actionErrors;
        }
        this.token = userId;

        try {
            String sBalance = BaseAction.extractRequestParameterIgnoreCase(request, CCommonWallet.PARAM_BALANCE);
            this.balance = StringUtils.isTrimmedEmpty(sBalance) ? null :
                    String.valueOf(DigitFormatter.getCentsFromCurrency(Double.parseDouble(sBalance)));
        } catch (NumberFormatException e) {
            ThreadLog.error(this.getClass().getSimpleName() + "::validate error balance is wrong", e);
            actionErrors.add("valid_error", new ActionMessage("error.login.generalValidationError"));
            return actionErrors;
        }

        return actionErrors;
    }

    @Override
    public String toString() {
        return null;
    }
}
