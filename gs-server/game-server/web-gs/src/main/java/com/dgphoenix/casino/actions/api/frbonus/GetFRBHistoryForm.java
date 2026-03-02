package com.abs.casino.actions.api.frbonus;


import com.abs.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

import static com.abs.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static com.abs.casino.common.web.bonus.BonusErrors.INVALID_PARAMETERS;

public class GetFRBHistoryForm extends BonusForm {
    private static final Logger LOG = LogManager.getLogger(GetFRBHistoryForm.class);

    private String userId;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);

        if (isTrimmedEmpty(userId)) {
            LOG.warn("validation error: userId is missing. " + this.toString());
            actionErrors.add("valid_error", new ActionMessage(INVALID_PARAMETERS.getDescription(), false));
            actionErrors.add("valid_error_code", new ActionMessage(String.valueOf(INVALID_PARAMETERS.getCode()), false));
            actionErrors.add("valid_error_comm", new ActionMessage("error.common.missingParameter", "userId"));
        }
        return actionErrors;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final String TAB = "    ";
        return "GetFRBHistoryForm" +
                "[ " + super.toString() + TAB +
                "userId='" + userId + '\'' +
                ']';
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
