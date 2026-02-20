package com.dgphoenix.casino.forms.login;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public abstract class CommonLoginForm extends CommonActionForm {
    protected String token;

    //used for disable check properties when if only need run game in action
    private boolean isCheckToken = true;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    protected boolean isCheckToken() {
        return isCheckToken;
    }

    protected void setCheckToken(boolean isCheckToken) {
        this.isCheckToken = isCheckToken;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);

        if (!actionErrors.isEmpty()) {
            return actionErrors;
        }

        if (isCheckToken()) {
            String token = BaseAction.extractRequestParameterIgnoreCase(request, "token");
            if (StringUtils.isTrimmedEmpty(token)) {
                getLogger().error("validate token is empty");
                actionErrors.add("valid_error", new ActionMessage("error.login.incorrectParameters"));
                return actionErrors;
            }
            this.token = token;
        }
        return actionErrors;
    }

    @Override
    public String toString() {
        return null;
    }
}
