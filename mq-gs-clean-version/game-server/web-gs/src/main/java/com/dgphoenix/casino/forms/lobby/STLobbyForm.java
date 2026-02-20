package com.dgphoenix.casino.forms.lobby;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.entities.lobby.LoginHelper;
import com.dgphoenix.casino.entities.lobby.StLobbyMode;
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
public class STLobbyForm extends CommonActionForm {
    private final static Logger LOG = LogManager.getLogger(STLobbyForm.class);
    public static final String BONUS = "bonus";

    protected String lang;
    protected String token;
    protected String mode;
    protected String SID;

    protected LoginHelper helper;

    protected String currentMode;

    public STLobbyForm() {

    }

    public STLobbyForm(String currentMode, String sessionId, String lang, Integer bankId) {
        this.currentMode = currentMode;
        this.SID = sessionId;
        this.lang = lang;
        this.bankId = bankId;
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSID() {
        return SID;
    }

    public void setSID(String SID) {
        this.SID = SID;
    }

    public LoginHelper getHelper() {
        return helper;
    }

    public String getCurrentMode() {
        return currentMode;
    }

    public StLobbyMode getCurrentModeValue() {
        return StLobbyMode.convertKey(currentMode);
    }

    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
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

        this.helper = LoginHelper.getByName(mapping.getParameter());

        if (this.helper != null && this.helper != LoginHelper.GUEST) {
            this.token = BaseAction.extractRequestParameterIgnoreCase(request, "token");
            String sessionId = BaseAction.extractRequestParameterIgnoreCase(request, "sessionId");
            String gameId = BaseAction.extractRequestParameterIgnoreCase(request, "gameId");
            if (StringUtils.isTrimmedEmpty(token) && (StringUtils.isTrimmedEmpty(sessionId) ||
                    (!"1".equals(gameId) && !"0".equals(gameId)))) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.incorrectCredentials"));
                return actionErrors;
            }
        }

        String sessionId = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.SESSION_ID_ATTRIBUTE);
        if (!StringUtils.isTrimmedEmpty(sessionId)) {
            this.SID = sessionId;
        }

        String lang = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.LANG_ID_ATTRIBUTE);
        if (!StringUtils.isTrimmedEmpty(lang)) {
            this.lang = lang;
        } else {
            this.lang = "en";
        }

        validateCurrentMode(request, actionErrors);

        return actionErrors;
    }

    public void validateCurrentMode(HttpServletRequest request, ActionErrors actionErrors) {
        String urlMode = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.GAMEMODE_ATTRIBUTE);
        GameMode mode = null;
        if (!StringUtils.isTrimmedEmpty(urlMode)) {
            mode = GameMode.getByName(urlMode);
        } else if (this.helper != null && this.helper == LoginHelper.GUEST) {
            mode = GameMode.FREE;
        }

        Long bonusId = null;
        if (mode != null && GameMode.BONUS == mode) {
            String bonusStr = BaseAction.extractRequestParameterIgnoreCase(request, BONUS);
            if (!StringUtils.isTrimmedEmpty(bonusStr)) {
                try {
                    bonusId = Long.valueOf(bonusStr);
                } catch (Throwable th) {
                }
            }
        }
        if (mode != null && (mode != GameMode.BONUS || bonusId != null)) {
            setCurrentMode(new StLobbyMode(mode, bonusId).getKey());
        } else if (getCurrentModeValue() == null) {
            actionErrors.add("valid_error", new ActionMessage("error.login.incorrectParameters"));
        }
    }

    @Override
    public String toString() {
        return "STLobbyForm{" +
                "lang='" + lang + '\'' +
                ", token='" + token + '\'' +
                ", mode='" + mode + '\'' +
                ", SID='" + SID + '\'' +
                ", helper=" + helper +
                ", currentMode='" + currentMode + '\'' +
                '}';
    }
}
