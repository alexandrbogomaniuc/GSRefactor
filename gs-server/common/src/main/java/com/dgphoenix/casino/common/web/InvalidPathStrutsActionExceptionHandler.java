package com.abs.casino.common.web;

import com.abs.casino.common.web.BaseAction;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.chain.commands.InvalidPathException;
import org.apache.struts.config.ExceptionConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * User: van0ss
 * Date: 01/20/2016
 */
public class InvalidPathStrutsActionExceptionHandler extends ExceptionHandler {
    private static final Logger LOG = Logger.getLogger(InvalidPathStrutsActionExceptionHandler.class);

    @Override
    public ActionForward execute(Exception ex, ExceptionConfig ae,
                                 ActionMapping mapping, ActionForm formInstance,
                                 HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Invalid request URI: ");
        errorMsg.append(request.getRequestURI()).append("\n");
        errorMsg.append("Type: ").append(request.getMethod()).append("\n");
        errorMsg.append("Headers: ");

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                errorMsg.append(headerName).append(": \"");
                String header = headers.nextElement();
                errorMsg.append(header).append("\"; ");
            }
        }
        errorMsg.append("\n").append("Parameters: ");
        errorMsg.append(BaseAction.getRequestParametersAsString(request));
        errorMsg.append("\n").append("RemoteAddress: ").append(request.getRemoteAddr());

        if (ex instanceof InvalidPathException) {
            LOG.error(errorMsg + ", path=" + ((InvalidPathException) ex).getPath());
        } else {
            LOG.error(errorMsg);
        }

        return super.execute(ex, ae, mapping, formInstance, request, response);
    }
}
