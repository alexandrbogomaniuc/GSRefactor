package com.abs.casino.forms.api.history.ct;

import com.abs.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: isirbis
 * Date: 10.10.14
 */
public class CTStartHistoryForm extends CommonActionForm {
    private final static Logger LOG = LogManager.getLogger(CTStartHistoryForm.class);
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);

        if (!errors.isEmpty()) {
            return errors;
        }

        this.token = BaseAction.extractRequestParameterIgnoreCase(request, "token");
        if (StringUtils.isTrimmedEmpty(token)) {
            errors.add("empty_credentials", new ActionMessage("error.login.incorrectCredentials"));
            return errors;
        }

        return errors;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        token = null;
    }
}
