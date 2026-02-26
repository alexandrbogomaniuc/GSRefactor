package com.abs.casino.actions.api.frbonus;

import com.dgphoenix.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

public class GetFRBInfoLiteForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(GetFRBInfoLiteForm.class);

    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final String TAB = "    ";
        final StringBuilder sb = new StringBuilder();
        sb.append("GetFRBInfoForm");
        sb.append("[ " + super.toString() + TAB);
        sb.append("userId='").append(userId).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();
        return actionErrors;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
