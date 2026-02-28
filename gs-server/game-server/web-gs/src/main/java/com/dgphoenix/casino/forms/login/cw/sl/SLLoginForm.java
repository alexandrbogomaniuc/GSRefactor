package com.abs.casino.forms.login.cw.sl;

import com.abs.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.forms.login.cw.CWLoginForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: plastical
 * Date: 02.06.2010
 */
public class SLLoginForm extends CWLoginForm {
    private String opr;

    public String getOpr() {
        return opr;
    }

    public void setOpr(String opr) {
        this.opr = opr;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);

        if (StringUtils.isTrimmedEmpty(opr)) {
            ThreadLog.error("validate error opr is empty");
            actionErrors.add("valid_error", new ActionMessage("error.login.incorrectCredentials"));
            return actionErrors;
        }

        return super.validate(mapping, request);
    }

    @Override
    public String toString() {
        return null;
    }
}
