package com.abs.casino.actions.api.frbonus;


import com.dgphoenix.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

public class CancelFRBLiteForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(CancelFRBLiteForm.class);

    private String bonusId;

    public String getBonusId() {
        return bonusId;
    }

    public void setBonusId(String bonusId) {
        this.bonusId = bonusId;
    }

    @Override
    public String toString() {
        final String TAB = "    ";
        final StringBuilder sb = new StringBuilder();
        sb.append("CancelFRBForm");
        sb.append("[ " + super.toString() + TAB);
        sb.append("bonusId='").append(bonusId).append('\'');
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
