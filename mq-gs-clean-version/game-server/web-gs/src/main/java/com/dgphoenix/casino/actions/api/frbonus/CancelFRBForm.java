package com.dgphoenix.casino.actions.api.frbonus;


import com.dgphoenix.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class CancelFRBForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(CancelFRBForm.class);

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
    protected Logger getLogger() {
        return LOG;
    }
}