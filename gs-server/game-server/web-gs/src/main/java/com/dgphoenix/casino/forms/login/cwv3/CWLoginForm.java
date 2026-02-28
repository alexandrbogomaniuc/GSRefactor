package com.abs.casino.forms.login.cwv3;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.forms.login.CommonGameLoginForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

public class CWLoginForm extends CommonGameLoginForm {
    private final static Logger LOG = LogManager.getLogger(CWLoginForm.class);

    protected Boolean getSessionFromCache;

    public Boolean isGetSessionFromCache() {
        return getSessionFromCache;
    }

    public void setGetSessionFromCache(Boolean getSessionFromCache) {
        this.getSessionFromCache = getSessionFromCache;
    }

    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);

        if (!actionErrors.isEmpty()) {
            return actionErrors;
        }

        String getSessionFromCache = BaseAction.extractRequestParameterIgnoreCase(request, "getSessionFromCache");
        if (StringUtils.isTrimmedEmpty(getSessionFromCache)) {
            getLogger().error("CNFBLoginForm:validate token is empty");
            actionErrors.add("valid_error", new ActionMessage("error.login.incorrectParameters"));
            return actionErrors;
        }
        this.getSessionFromCache = Boolean.TRUE.toString().equalsIgnoreCase(getSessionFromCache);
        return actionErrors;
    }

    @Override
    public String toString() {
        return "CWLoginForm{" +
                "getSessionFromCache=" + getSessionFromCache +
                '}';
    }
}
