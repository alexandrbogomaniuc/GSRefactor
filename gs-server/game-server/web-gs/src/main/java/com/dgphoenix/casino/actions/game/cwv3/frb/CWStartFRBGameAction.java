package com.dgphoenix.casino.actions.game.cwv3.frb;

import com.dgphoenix.casino.actions.game.bonus.AbstractBSStartGameAction;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.entities.game.requests.BonusStartGameRequest;
import com.dgphoenix.casino.exceptions.LoginErrorException;
import com.dgphoenix.casino.forms.game.CommonFRBStartGameForm;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.helpers.login.FRBonusHelper;
import com.dgphoenix.casino.helpers.login.LoginHelper;
import com.dgphoenix.casino.helpers.login.serializers.BonusSerializeLoginForm;
import com.dgphoenix.casino.sm.login.BonusGameLoginRequest;
import com.dgphoenix.casino.sm.login.LoginResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created Galanov
 * Date: 08.02.13
 */
public class CWStartFRBGameAction extends AbstractBSStartGameAction<CommonFRBStartGameForm> {
    private final static Logger LOG = LogManager.getLogger(CWStartFRBGameAction.class);

    @Override
    protected LoginHelper getLoginHelper() {
        return FRBonusHelper.getInstance();
    }

    protected BonusGameLoginRequest createLoginRequest(HttpServletRequest request, CommonFRBStartGameForm form) throws Exception {
        BonusGameLoginRequest loginRequest = new BonusSerializeLoginForm<CommonFRBStartGameForm>().getLoginRequest(form);
        loginRequest.setProperties(LoginHelper.getRequestAuthProperties(request));
        return loginRequest;
    }

    @Override
    protected BonusStartGameRequest createStartGameRequest(SessionInfo sessionInfo, AccountInfo accountInfo, CommonFRBStartGameForm form) {
        return new BonusStartGameRequest<>(sessionInfo, accountInfo, form, false);
    }

    @Override
    protected ActionForward process(ActionMapping mapping, CommonFRBStartGameForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response)
            throws Exception {
        LoginResponse loginResponse;

        try {
            loginResponse = login(createLoginRequest(request, actionForm));
        } catch (LoginErrorException e) {
            LOG.error(e.getDescription());
            addErrorWithPersistence(request, "error.login.internalError", e, System.currentTimeMillis());
            return mapping.findForward(ERROR_FORWARD);
        }

        return startGame(mapping, actionForm, request, response, loginResponse.getSessionInfo().getSessionId());
    }

    @Override
    protected Long validateBonusIdParam(BonusStartGameRequest startGameRequest, GameMode mode, AccountInfo accountInfo) throws CommonException {
        if (startGameRequest.getBonusId() == null || !mode.equals(GameMode.REAL)) {
            return null;
        }

        FRBonus frBonus = FRBonusManager.getInstance().getById(startGameRequest.getBonusId());
        if (frBonus == null) {
            throw new CommonException("FRB bonus is null");
        }
        if (!frBonus.getStatus().equals(BonusStatus.ACTIVE)) {
            throw new CommonException("FRBonus is not active");
        }
        if (frBonus.getAccountId() != accountInfo.getId()) {
            throw new CommonException("FRBonusId is not found for this accountId=" + accountInfo.getId());
        }
        if (!frBonus.getGameIds().contains(startGameRequest.getGameId().longValue())) {
            throw new CommonException("FRBonus is not contains for this game" + startGameRequest.getGameId());
        }
        LOG.info("Choice FRB Mode Game for FRBonus Id:" + frBonus.getId());
        return frBonus.getId();
    }
}
