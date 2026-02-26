package com.abs.casino.forms.game.cwv3.bingomania;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * User: mic
 * Date: 09.09.14
 */
public class CWStartGameForm extends com.abs.casino.forms.game.cwv3.CWStartGameForm {
    private static final Logger LOG = LogManager.getLogger(CWStartGameForm.class);

    private static final long serialVersionUID = -129937447697881192L;
    private String Jsessionid;
    private String PgameSessionid;

    public String getJsessionid() {
        return Jsessionid;
    }

    public void setJsessionid(String jsessionid) {
        Jsessionid = jsessionid;
    }

    public String getPgameSessionid() {
        return PgameSessionid;
    }

    public void setPgameSessionid(String pgameSessionid) {
        PgameSessionid = pgameSessionid;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);

        setJsessionid(request.getParameter("Jsessionid"));
        setPgameSessionid(request.getParameter("PgameSessionid"));

        if (isTrimmedEmpty(Jsessionid) || isTrimmedEmpty(PgameSessionid)) {
            actionErrors.add("empty_credentials", new ActionMessage("error.bingomania.startgame.mandatoryParametersMissing",
                    Jsessionid, PgameSessionid));
        }

        return actionErrors;
    }
}
