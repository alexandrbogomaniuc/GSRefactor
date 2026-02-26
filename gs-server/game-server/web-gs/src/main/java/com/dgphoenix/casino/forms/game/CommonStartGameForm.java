package com.abs.casino.forms.game;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.MobileDetector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public class CommonStartGameForm extends CommonActionForm {
    private final static Logger LOG = LogManager.getLogger(CommonStartGameForm.class);

    //used when login & start game in one action
    protected String token;
    protected Integer gameId;
    protected String mode;

    protected String lang;
    protected String profileId;

    //used for disable check properties when if only need run game in action
    private boolean isCheckToken = true;
    private boolean isCheckGameMode = true;
    private boolean isCheckGameId = true;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

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

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    protected boolean isCheckToken() {
        return isCheckToken;
    }

    protected void setCheckToken(boolean checkToken) {
        this.isCheckToken = checkToken;
    }

    protected boolean isCheckGameMode() {
        return isCheckGameMode;
    }

    protected void setCheckGameMode(boolean isCheckGameMode) {
        this.isCheckGameMode = isCheckGameMode;
    }

    public boolean isCheckGameId() {
        return isCheckGameId;
    }

    public void setCheckGameId(boolean isCheckGameId) {
        this.isCheckGameId = isCheckGameId;
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

        if (isCheckToken()) {
            String token = BaseAction.extractRequestParameterIgnoreCase(request, "token");
            if (StringUtils.isTrimmedEmpty(token)) {
                getLogger().error("validate token is empty");
                actionErrors.add("valid_error", new ActionMessage("error.login.incorrectParameters"));
                return actionErrors;
            }
            this.token = token;
        }

        if (isCheckGameId()) {
            actionErrors = checkerGameId(request);
            if (!actionErrors.isEmpty()) {
                return actionErrors;
            }
        }

        if (isCheckGameMode()) {
            this.mode = BaseAction.extractRequestParameterIgnoreCase(request, "mode");
            if (StringUtils.isTrimmedEmpty(mode) || GameMode.getByName(mode) == null) {
                actionErrors.add("invalid_mode", new ActionMessage("error.deposit.modeNotDefined"));
                return actionErrors;
            }
        }

        this.lang = BaseAction.extractRequestParameterIgnoreCase(request, "lang");

        String profileId = BaseAction.extractRequestParameterIgnoreCase(request, "profileId");
        if (!StringUtils.isTrimmedEmpty(profileId)) {
            this.profileId = profileId;
        }
        return actionErrors;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        gameId = null;
    }

    protected ActionErrors checkerGameId(HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();
        try {
            String strGameId = BaseAction.extractRequestParameterIgnoreCase(request, "gameId");
            if ("1".equals(strGameId) || "0".equals(strGameId)) {
                return actionErrors;
            }
            if (StringUtils.isTrimmedEmpty(strGameId)) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
                return actionErrors;
            }
            //prevent loading by extGameId for 7red
            Long originalId = subCasinoId == 22 ? null :
                    BaseGameCache.getInstance().getOriginalGameId(strGameId, bankId);
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

        return actionErrors;
    }
}
