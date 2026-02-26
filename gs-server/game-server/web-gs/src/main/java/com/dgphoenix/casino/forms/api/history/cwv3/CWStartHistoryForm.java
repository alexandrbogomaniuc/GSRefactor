package com.abs.casino.forms.api.history.cwv3;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.common.cache.ExternalGameIdsCache;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * User: isirbis
 * Date: 10.10.14
 */
public class CWStartHistoryForm extends CommonActionForm {
    private static final Logger LOG = LogManager.getLogger(CWStartHistoryForm.class);
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private String token;
    private String gameId;
    private String startDate;
    private String endDate;
    private String lang;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        if (!errors.isEmpty()) {
            return errors;
        }

        this.token = BaseAction.extractRequestParameterIgnoreCase(request, "token");
        if (StringUtils.isTrimmedEmpty(token)) {
            errors.add("empty_credentials", new ActionMessage("error.login.incorrectCredentials"));
            return errors;
        }
        String strSDate = BaseAction.extractRequestParameterIgnoreCase(request, "startDate");
        if (!StringUtils.isTrimmedEmpty(strSDate)) {
            try {
                new SimpleDateFormat(DATE_FORMAT).parse(strSDate);
            } catch (ParseException e) {
                errors.add("error date format", new ActionMessage("error.login.incorrectParameters"));
                return errors;
            }
        }
        if (!StringUtils.isTrimmedEmpty(gameId)) {
            try {
                Long originalId = ExternalGameIdsCache.getInstance().getOriginalId(gameId, getBankId());
                if (originalId != null) {
                    gameId = String.valueOf(originalId);
                }
            } catch (Exception e) {
                errors.add("invalid gameId", new ActionMessage("error.login.incorrectParameters"));
            }
        }
        String strEDate = BaseAction.extractRequestParameterIgnoreCase(request, "endDate");
        if (!StringUtils.isTrimmedEmpty(strEDate)) {
            try {
                new SimpleDateFormat(DATE_FORMAT).parse(strEDate);
            } catch (ParseException e) {
                errors.add("error date format", new ActionMessage("error.login.incorrectParameters"));
                return errors;
            }
        }

        if (getGameId() != null && getStartDate() == null) {
            errors.add("empty date", new ActionMessage("error.login.incorrectParameters"));
            return errors;
        }

        return errors;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
