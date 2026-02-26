package com.abs.casino.actions.api;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: flsh
 * Date: 3/1/12
 */
public class GetBalanceForm extends ActionForm {
    private String sid;
    private String refresh;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        this.sid = BaseAction.extractRequestParameterIgnoreCase(request, "SID");
        ActionErrors actionErrors = new ActionErrors();
        if (StringUtils.isTrimmedEmpty(sid)) {
            actionErrors.add("empty_credentials", new ActionMessage("error.login.incorrectParameters"));
        }
        return actionErrors;
    }

    @Override
    public String toString() {
        return "GetBalanceForm[" +
                "sid='" + sid + '\'' +
                ']';
    }
}
