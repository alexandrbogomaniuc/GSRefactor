package com.abs.casino.config;

import com.abs.casino.common.cache.SubCasinoCache;
import com.abs.casino.common.cache.data.bank.SubCasino;
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
        String actionPath = requestURI;
        if (requestURI.endsWith(".do")) {
            actionPath = requestURI.substring(0, requestURI.length() - ACTION_POSTFIX_SIZE);
        }
        if ("/cwguestlogin".equals(actionPath) || "/startGuestgame".equals(actionPath)) {
            String query = request.getQueryString();
            if (query == null) {
                query = "";
            }
            if (query.indexOf("subCasinoId=") < 0) {
                String subCasinoId = inferSubCasinoId(request);
                if (subCasinoId == null || subCasinoId.trim().length() == 0) {
                    subCasinoId = "58";
                }
                if (query.length() == 0) {
                    query = "subCasinoId=" + subCasinoId;
                } else {
                    query = query + "&subCasinoId=" + subCasinoId;
                }
                String target = request.getContextPath() +
                        ("/startGuestgame".equals(actionPath) ? "/startGuestgame" : "/cwguestlogin.do");
                response.sendRedirect(target + "?" + query);
                return;
            }
        }
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

    private String inferSubCasinoId(HttpServletRequest request) {
        String subCasinoId = request.getParameter("subCasinoId");
        if (subCasinoId != null && subCasinoId.trim().length() > 0) {
            return subCasinoId;
        }
        String bankIdParam = request.getParameter("bankId");
        if (bankIdParam != null && bankIdParam.trim().length() > 0) {
            try {
                Long bankId = Long.valueOf(bankIdParam.trim());
                Long byBank = SubCasinoCache.getInstance().getSubCasinoId(bankId);
                if (byBank != null) {
                    return String.valueOf(byBank);
                }
            } catch (NumberFormatException ignored) {
                // ignore and fall back to domain
            }
        }
        String serverName = request.getServerName();
        SubCasino subCasino = SubCasinoCache.getInstance().getSubCasinoByDomainName(serverName);
        if (subCasino == null) {
            String hostHeader = request.getHeader("Host");
            if (hostHeader != null) {
                int colon = hostHeader.indexOf(':');
                String hostOnly = colon > 0 ? hostHeader.substring(0, colon) : hostHeader;
                subCasino = SubCasinoCache.getInstance().getSubCasinoByDomainName(hostOnly);
            }
        }
        return subCasino == null ? null : String.valueOf(subCasino.getId());
    }

    private RequestProcessor getProcessorForModule(ModuleConfig config) {
        String key = Globals.REQUEST_PROCESSOR_KEY + config.getPrefix();
        return (RequestProcessor) getServletContext().getAttribute(key);
    }
}
