package com.abs.casino.forms.login;

import com.abs.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.forms.login.CommonGameLoginForm;
import com.abs.casino.gs.managers.payment.bonus.BonusManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: isirbis
 * Date: 13.10.14
 */
public class CommonBSLoginForm extends CommonGameLoginForm {
    private final static Logger LOG = LogManager.getLogger(CommonBSLoginForm.class);

    private Long bonusId;

    public Long getBonusId() {
        return this.bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);

        if (!actionErrors.isEmpty()) {
            return actionErrors;
        }

        try {
            String strBonusId = BaseAction.extractRequestParameterIgnoreCase(request, "bonusId");
            if (StringUtils.isTrimmedEmpty(strBonusId)) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.bonusIdNotDefined"));
                return actionErrors;
            }

            this.bonusId = Long.parseLong(strBonusId);

            Bonus bonus = BonusManager.getInstance().getById(this.bonusId);

            if (bonus == null) {
                throw new CommonException("incorrect parameters::bonusId is invalid");
            }

        } catch (Exception e) {
            getLogger().error("validate error:", e);
            actionErrors.add("empty_credentials", new ActionMessage("error.login.bonusIdNotDefined"));
            return actionErrors;
        }

        return actionErrors;
    }

    @Override
    public String toString() {
        return null;
    }
}
