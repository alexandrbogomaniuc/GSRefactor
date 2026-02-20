package com.dgphoenix.casino.forms.game.cw.shell;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.forms.game.cw.CWGuestStartGameForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: galanov
 * Date: 17.12.13
 */
public class ShellLauncherForm extends CWGuestStartGameForm {
    private final static Logger LOG = LogManager.getLogger(ShellLauncherForm.class);
    private String shellPath;

    public String getShellPath() {
        return shellPath;
    }

    public void setShellPath(String shellPath) {
        this.shellPath = shellPath;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);

        String strShellPath = BaseAction.extractRequestParameterIgnoreCase(request, "shellPath");
        if (StringUtils.isTrimmedEmpty(strShellPath)) {
            actionErrors.add("empty_credentials", new ActionMessage("error.login.incorrectParameters"));
            return actionErrors;
        }
        this.shellPath = strShellPath;
        return actionErrors;
    }
}
