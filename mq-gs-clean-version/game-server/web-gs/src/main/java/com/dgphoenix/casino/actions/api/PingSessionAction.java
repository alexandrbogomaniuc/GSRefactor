package com.dgphoenix.casino.actions.api;

import com.dgphoenix.casino.cache.PingSessionCache;
import com.dgphoenix.casino.common.util.web.WebTools;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by inter on 08.12.15.
 */
public class PingSessionAction extends Action {
    private static String SID_PARAM = "SID";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        String sessionId = request.getParameter(SID_PARAM);
        WebTools.setResponseStandardHeader(request, response);
        if (sessionId == null || !PingSessionCache.getInstance().checkSid(sessionId)) {
            response.setStatus(HttpServletResponse.SC_GONE);
        }
        return null;
    }
}
