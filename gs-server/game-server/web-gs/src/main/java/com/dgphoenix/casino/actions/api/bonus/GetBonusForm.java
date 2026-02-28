package com.abs.casino.actions.api.bonus;

import com.abs.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class GetBonusForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(GetBonusForm.class);

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
        sb.append("GetBonusForm");
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
