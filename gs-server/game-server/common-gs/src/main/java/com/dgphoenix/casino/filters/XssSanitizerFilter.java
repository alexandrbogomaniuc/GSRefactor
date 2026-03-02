package com.abs.casino.filters;

import com.abs.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class XssSanitizerFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger(XssSanitizerFilter.class);
    private final GameServerConfiguration gsConfig = GameServerConfiguration.getInstance();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nop
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (gsConfig.isEnableXssSanitizer()
                && isNotServlet(httpRequest.getRequestURI())
                && isFilteringAllowedOnDomain(httpRequest.getServerName())
                && isFilteringAllowedOnURI(httpRequest.getRequestURI())) {
            chain.doFilter(new XssSanitizerRequestWrapper(httpRequest), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // nop
    }

    private boolean isNotServlet(String uri) {
        return !(uri.endsWith(".game") || uri.endsWith(".servlet") || uri.endsWith("webSocket") || uri.endsWith("tournamentWebSocket"));
    }

    private boolean isFilteringAllowedOnDomain(String domain) {
        String[] domainsList = split(gsConfig.getDisabledXssSanitizingDomains());
        for (String entry : domainsList) {
            if (entry.equals(domain)) {
                return false;
            }
        }
        return true;
    }

    private boolean isFilteringAllowedOnURI(String uri) {
        String[] urisList = split(gsConfig.getDisableXssSanitizingUrls());
        for (String entry : urisList) {
            if (!entry.isEmpty() && uri.startsWith(entry)) {
                return false;
            }
        }
        return true;
    }

    private String[] split(String list) {
        if (list == null) {
            return new String[0];
        }
        return list.split(";");
    }
}
