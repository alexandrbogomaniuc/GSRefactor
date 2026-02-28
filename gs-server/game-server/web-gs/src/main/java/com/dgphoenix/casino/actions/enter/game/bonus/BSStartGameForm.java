package com.abs.casino.actions.enter.game.bonus;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.abs.casino.actions.enter.game.IStartGameForm;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.common.web.MobileDetector;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

public class BSStartGameForm extends CommonActionForm implements IStartGameForm {
    private static final Logger LOG = LogManager.getLogger(BSStartGameForm.class);

    private String token;
    private String sessionId;
    private String mode;
    private String gameId;
    private long bonusId;

    private String lang;

    private GameMode gameMode;

    public BSStartGameForm() {
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    public long getBonusId() {
        return bonusId;
    }

    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public boolean isNotGameFRB() {
        return false;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);

        if (!errors.isEmpty()) return errors;

        this.token = BaseAction.extractRequestParameterIgnoreCase(request, "token");
        if (StringUtils.isTrimmedEmpty(token)) {
            errors.add("empty_credentials", new ActionMessage("error.login.incorrectCredentials"));
            return errors;
        }

        try {
            String strGameId = BaseAction.extractRequestParameterIgnoreCase(request, "gameId");
            if (StringUtils.isTrimmedEmpty(strGameId)) {
                errors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
                return errors;
            }

            this.gameId = strGameId;

            IBaseGameInfo gameInfo = BaseGameCache.getInstance()
                    .getGameInfoById(getBankId(), Long.parseLong(gameId), null);
            if (gameInfo == null) {
                errors.add("empty_credentials", new ActionMessage("error.login.gameInfoNotDefined"));
                return errors;
            }
            gameInfo = MobileDetector.checkGameInfo(gameInfo, getClientType(), request.getHeader("User-Agent"));
            this.gameId = Long.toString(gameInfo.getId());

            if (!gameInfo.isBankEnabled()) {
                errors.add("empty_credentials", new ActionMessage("error.login.bankIsNotEnabled"));
                return errors;
            }

        } catch (Exception e) {
            getLogger().error("BSStartGameForm::validate error:", e);
            errors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
            return errors;
        }

        try {
            String strBonusId = BaseAction.extractRequestParameterIgnoreCase(request, "bonusId");
            if (StringUtils.isTrimmedEmpty(strBonusId)) {
                errors.add("empty_credentials", new ActionMessage("error.login.bonusIdNotDefined"));
                return errors;
            }

            this.bonusId = Long.parseLong(strBonusId);

            Bonus bonus = BonusManager.getInstance().getById(this.bonusId);

            if (bonus == null) {
                throw new CommonException("incorrect parameters::bonusId is invalid");
            }

        } catch (Exception e) {
            getLogger().error("BSStartGameForm::validate error:", e);
            errors.add("empty_credentials", new ActionMessage("error.login.bonusIdNotDefined"));
            return errors;
        }

        mode = GameMode.BONUS.name();
        gameMode = GameMode.BONUS;

        return errors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BSStartGameForm");
        sb.append("[token='").append(token).append('\'');
        sb.append(", sessionId=").append(sessionId);
        sb.append(", mode='").append(mode).append('\'');
        sb.append(", gameId=").append(gameId);
        sb.append(", bonusId=").append(bonusId);
        sb.append(", gameMode=").append(gameMode);
        sb.append(", bankId=").append(getBankId());
        sb.append(']');
        return sb.toString();
    }

}
