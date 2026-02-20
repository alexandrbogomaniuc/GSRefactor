package com.dgphoenix.casino.config;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.RequestProcessor;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.ModuleUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.04.2021
 */
public class ForwardActionServlet extends ActionServlet {

    private static final int ACTION_POSTFIX_SIZE = ".do".length();

    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ModuleUtils.getInstance().selectModule(request, getServletContext());
        ModuleConfig config = getModuleConfig(request);
        String requestURI = request.getRequestURI();
        String actionPath = requestURI.substring(0, requestURI.length() - ACTION_POSTFIX_SIZE);
        ActionConfig actionConfig = config.findActionConfig(actionPath);
        if (actionConfig == null) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(actionPath);
            dispatcher.forward(request, response);
        } else {
            RequestProcessor processor = getProcessorForModule(config);
            if (processor == null) {
                processor = getRequestProcessor(config);
            }
            processor.process(request, response);
        }
    }

    private RequestProcessor getProcessorForModule(ModuleConfig config) {
        String key = Globals.REQUEST_PROCESSOR_KEY + config.getPrefix();
        return (RequestProcessor) getServletContext().getAttribute(key);
    }
}
