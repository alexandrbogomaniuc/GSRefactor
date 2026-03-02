package com.abs.casino.forms.game.cwv3;

import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.forms.game.CommonStartGameForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public class CWOnlyStartSTGameForm extends CommonStartGameForm {
    private final static Logger LOG = LogManager.getLogger(CWOnlyStartSTGameForm.class);

    private boolean updateBalance;

    public boolean isUpdateBalance() {
        return updateBalance;
    }

    public void setUpdateBalance(boolean updateBalance) {
        this.updateBalance = updateBalance;
    }

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
        setCheckToken(false);

        ActionErrors errors = super.validate(mapping, request);

        if (!errors.isEmpty()) {
            return errors;
        }

        this.updateBalance = false;
        String sUpdateBalance = BaseAction.extractRequestParameterIgnoreCase(request, "updateBalance");
        if (!StringUtils.isTrimmedEmpty(sUpdateBalance) && sUpdateBalance.equals("true")) {
            setUpdateBalance(true);
        }

        String sessionId = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.SESSION_ID_ATTRIBUTE);
        if (StringUtils.isTrimmedEmpty(sessionId)) {
            errors.add("invalid_mode", new ActionMessage("error.deposit.FAIL_GAMESESSIONID_INVALID"));
            return errors;
        }
        this.token = sessionId;

        return errors;
    }
}
