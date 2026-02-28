package com.abs.casino.forms.game;

import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.forms.game.CommonStartGameForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public abstract class CommonBonusStartGameForm extends CommonStartGameForm {
    protected Long bonusId;

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    @Override
    public abstract GameMode getGameMode();

    protected abstract boolean isExist(long bonusId);

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        setCheckGameMode(false);
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
            if (!isExist(this.bonusId)) {
                throw new CommonException("incorrect parameters::bonusId is invalid");
            }

        } catch (Exception e) {
            getLogger().error("validate error:", e);
            actionErrors.add("empty_credentials", new ActionMessage("error.login.bonusIdNotDefined"));
            return actionErrors;
        }

        return actionErrors;
    }
}
