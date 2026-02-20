package com.dgphoenix.casino.forms.game.bonus;

import com.dgphoenix.casino.forms.game.CommonBSStartGameForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

public class BSStartGameForm extends CommonBSStartGameForm {
    private final static Logger LOG = LogManager.getLogger(BSStartGameForm.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        return super.validate(mapping, request);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BSStartGameForm");
        sb.append("[token='").append(token).append('\'');
        sb.append(", bonusId=").append(bonusId);
        sb.append(']');
        return sb.toString();
    }

}
