package com.abs.casino.actions.api.vba;

import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;


public class GetVBAForm extends ActionForm {
    private String gameSessionId;

    public String getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(String gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();
        if (StringUtils.isTrimmedEmpty(gameSessionId)) {
            actionErrors.add("valid_error", new ActionMessage("error.login.genericError", "Validation error, " +
                    "gameSessionId is empty: "
                    + gameSessionId));
        }
        return actionErrors;
    }

    @Override
    public String toString() {
        return "GetVBAForm{" +
                "gameSessionId='" + gameSessionId + '\'' +
                "} " + super.toString();
    }
}
