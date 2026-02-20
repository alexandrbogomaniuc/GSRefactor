package com.dgphoenix.casino.forms.login;

import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.MobileDetector;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: isirbis
 * Date: 14.10.14
 */
public abstract class CommonGameLoginForm extends CommonLoginForm {
    protected Integer gameId;
    protected String mode;

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public GameMode getGameMode() {
        return GameMode.getByName(this.mode);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);

        if (!actionErrors.isEmpty()) {
            return actionErrors;
        }

        try {
            String strGameId = BaseAction.extractRequestParameterIgnoreCase(request, "gameId");
            if (StringUtils.isTrimmedEmpty(strGameId)) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
                return actionErrors;
            }

            Long originalId = BaseGameCache.getInstance().getOriginalGameId(strGameId, bankId);
            if (originalId == null) {
                try {
                    this.gameId = Integer.parseInt(strGameId);
                } catch (NumberFormatException e) {
                    actionErrors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
                    return actionErrors;
                }
            } else {
                this.gameId = originalId.intValue();
                getLogger().info("Game id was redefined from " + strGameId + ", to " + gameId);
            }

            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, null);
            if (gameInfo == null) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.gameInfoNotDefined"));
                return actionErrors;
            }

            gameInfo = MobileDetector.checkGameInfo(gameInfo, getClientType(), request.getHeader("User-Agent"));
            this.gameId = (int) gameInfo.getId();
            if (!gameInfo.isBankEnabled()) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.bankIsNotEnabled"));
                return actionErrors;
            }
            if (!gameInfo.isEnabled()) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.gameIsNotEnabled"));
                return actionErrors;
            }
        } catch (Exception e) {
            getLogger().error("validate error:", e);
            actionErrors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
            return actionErrors;
        }

        this.mode = BaseAction.extractRequestParameterIgnoreCase(request, "mode");
        if (StringUtils.isTrimmedEmpty(mode) || GameMode.getByName(mode) == null) {
            actionErrors.add("invalid_mode", new ActionMessage("error.deposit.modeNotDefined"));
            return actionErrors;
        }
        return actionErrors;
    }

    @Override
    public String toString() {
        return "CommonGameLoginForm[" +
                "gameId=" + gameId +
                ", mode='" + mode + '\'' +
                ']';
    }
}
