package com.abs.casino.actions.api.bonus;

import com.abs.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class GetHistoryForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(GetHistoryForm.class);

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
        sb.append("GetHistoryForm");
        sb.append("[ " + super.toString() + TAB);
        sb.append("userId='").append(userId).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
