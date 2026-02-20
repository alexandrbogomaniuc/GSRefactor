package com.dgphoenix.casino.support;

import com.dgphoenix.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: Grien
 * Date: 31.01.2012 9:43
 */
public class SupportJSPAction extends Action {
    private final static String JSPParameterName = "jsp";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String jsp = request.getParameter(JSPParameterName);
            String checkByExist = request.getParameter("check");
            if (StringUtils.isTrimmedEmpty(checkByExist) || (checkByExist.equalsIgnoreCase("true") &&
                    getServlet().getServletConfig().getServletContext().getResource(jsp) != null)) {
                return new ActionForward(jsp, false);
            }
        } catch (Exception e) {
            ThreadLog.error("SupportJSPAction error", e);
        }
        return new ActionForward("/error_pages/error.jsp", false);
    }
}
