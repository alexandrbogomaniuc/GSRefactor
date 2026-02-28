package com.abs.casino.forms.lobby;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

public class TournamentLobbyForm extends STLobbyForm {
    private String realModeUrl;
    private String sessionId;
    private Long gameId;
    private boolean showBattlegroundTab = false;

    public String getRealModeUrl() {
        return realModeUrl;
    }

    public void setRealModeUrl(String realModeUrl) {
        this.realModeUrl = realModeUrl;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public boolean isShowBattlegroundTab() {
        return showBattlegroundTab;
    }

    public void setShowBattlegroundTab(boolean showBattlegroundTab) {
        this.showBattlegroundTab = showBattlegroundTab;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();
        if (StringUtils.isTrimmedEmpty(SID)) {
            actionErrors.add(super.validate(mapping, request));
        } else {
            String lang = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.LANG_ID_ATTRIBUTE);
            if (!StringUtils.isTrimmedEmpty(lang)) {
                this.lang = lang;
            } else {
                this.lang = "en";
            }
            validateCurrentMode(request, actionErrors);
        }
        String realModeUrl = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.REAL_MODE_URL);
        if (!StringUtils.isTrimmedEmpty(realModeUrl)) {
            this.realModeUrl = realModeUrl;
        }

        String sid = BaseAction.extractRequestParameterIgnoreCase(request, "sessionId");
        if (!StringUtils.isTrimmedEmpty(sid)) {
            this.sessionId = sid;
        }

        String sGameId = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.GAME_ID_ATTRIBUTE);
        this.gameId = StringUtils.isTrimmedEmpty(sGameId) ? null : Long.parseLong(sGameId);

        String sShowBattlegroundTab = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.SHOW_BATTLEGROUND_TAB);
        if (!StringUtils.isTrimmedEmpty(sShowBattlegroundTab)) {
            this.showBattlegroundTab = Boolean.parseBoolean(sShowBattlegroundTab);
        }
        return actionErrors;
    }
}
