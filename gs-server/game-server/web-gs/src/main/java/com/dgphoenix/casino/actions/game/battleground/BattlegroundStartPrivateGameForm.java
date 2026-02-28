package com.abs.casino.actions.game.battleground;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.abs.casino.actions.enter.game.IStartGameForm;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.common.web.ClientTypeFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

public class BattlegroundStartPrivateGameForm extends CommonActionForm implements IStartGameForm {
    private static final Logger LOG = LogManager.getLogger(BattlegroundStartPrivateGameForm.class);

    public static final String EMPTY_CREDENTIALS = "empty_credentials";

    private String mmcToken;
    private String mqcToken;
    private String privateRoomId;
    private String lang;
    private String homeUrl;

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.REAL;
    }

    @Override
    public String getMode() {
        return GameMode.REAL.name();
    }

    @Override
    public String getGameId() {
        return null; //empty
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public boolean isNotGameFRB() {
        return true;
    }

    public String getMmcToken() {
        return mmcToken;
    }

    public void setMmcToken(String mmcToken) {
        this.mmcToken = mmcToken;
    }

    public String getMqcToken() {
        return mqcToken;
    }

    public void setMqcToken(String mqcToken) {
        this.mqcToken = mqcToken;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        try {
            this.mmcToken = BaseAction.extractRequestParameterIgnoreCase(request, "mmcToken");
            if (StringUtils.isTrimmedEmpty(this.mmcToken)) {
                errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.badToken"));
                return errors;
            }

            this.mqcToken = BaseAction.extractRequestParameterIgnoreCase(request, "mqcToken");
            if (StringUtils.isTrimmedEmpty(this.mqcToken)) {
                errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.badToken"));
                return errors;
            }

            this.privateRoomId = BaseAction.extractRequestParameterIgnoreCase(request, "privateRoomId");
            if (StringUtils.isTrimmedEmpty(this.privateRoomId)) {
                errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.privateRoomIdEmpty"));
                return errors;
            }

            this.homeUrl = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.PARAM_HOME_URL);
            this.setHost(request.getRemoteHost());
            this.lang = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.LANG_ID_ATTRIBUTE);
            this.clientType = ClientTypeFactory.getByHttpRequest(request);
        } catch (Exception e) {
            getLogger().error("BattlegroundStartGameFromMQBForm::validate error:", e);
            errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.gameIdNotDefined"));
            return errors;
        }
        return errors;
    }
}
