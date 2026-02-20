package com.dgphoenix.casino.actions.api.frbonus;


import com.dgphoenix.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class CheckFRBForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(CheckFRBForm.class);

    private String extBonusId;

    public String getExtBonusId() {
        return extBonusId;
    }

    public void setExtBonusId(String extBonusId) {
        this.extBonusId = extBonusId;
    }

    @Override
    public String toString() {
        final String TAB = "    ";
        final StringBuilder sb = new StringBuilder();
        sb.append("CheckFRBForm");
        sb.append("[ " + super.toString() + TAB);
        sb.append("extBonusId='").append(extBonusId).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
