package com.dgphoenix.casino.actions.api.bonus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class CancelForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(CancelForm.class);

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
        sb.append("CancelForm");
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
