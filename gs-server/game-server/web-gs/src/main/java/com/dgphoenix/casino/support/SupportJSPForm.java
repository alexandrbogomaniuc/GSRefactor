package com.abs.casino.support;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Grien
 * Date: 31.01.2012 9:44
 */
public class SupportJSPForm extends ActionForm {
    private String jspName;

    public String getJspName() {
        return jspName;
    }

    public void setJspName(String jspName) {
        this.jspName = jspName;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();
        jspName = BaseAction.extractRequestParameterIgnoreCase(request, "jsp");
        if (StringUtils.isTrimmedEmpty(jspName)) {
            actionErrors.add("valid_error", new ActionMessage("error.login.incorrectParameters"));
        }
        return actionErrors;
    }
}
