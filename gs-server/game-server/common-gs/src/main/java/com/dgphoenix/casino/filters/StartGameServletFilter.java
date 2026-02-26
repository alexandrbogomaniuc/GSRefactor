package com.dgphoenix.casino.filters;

import com.abs.casino.common.games.IStartGameHelper;
import com.abs.casino.common.games.StartGameHelpers;
import com.abs.casino.common.games.SwfLocationInfo;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * User: flsh
 * Date: 04.03.13
 */
public class StartGameServletFilter implements Filter {
    public static final String TEMPLATE_JSP = "/html5pc/template.jsp";
    public static final String LAUNCH_JSP = "/launch.jsp";

    private static final Logger LOG = LogManager.getLogger(StartGameServletFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //nop
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequestProxy request;
        if (req instanceof HttpServletRequestProxy) {
            request = (HttpServletRequestProxy) req;
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) req;
            request = new HttpServletRequestProxy(httpRequest);
        }
        String requestUrl = request.getRequestURI();
        if (requestUrl.contains("launch")) {
            boolean forceAutoCdn = false;
            try {
                long bankId = Long.parseLong(request.getParameter(BaseAction.BANK_ID_ATTRIBUTE));
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                forceAutoCdn = bankInfo != null && !bankInfo.getCdnUrlsMap().isEmpty() && bankInfo.isCdnForceAuto();
            } catch (Exception ex) {
                LOG.warn("Can't resolve bankInfo, requestUrl=" + requestUrl, ex);
            }

            if ("AUTO".equalsIgnoreCase(request.getParameter(BaseAction.KEY_CDN)) || forceAutoCdn) {
                try {
                    long bankId = Long.parseLong(request.getParameter(BaseAction.BANK_ID_ATTRIBUTE));
                    Long gameId = Long.parseLong(request.getParameter(BaseAction.GAME_ID_ATTRIBUTE));
                    String lang = request.getParameter(BaseAction.LANG_ID_ATTRIBUTE);
                    IStartGameHelper params = StartGameHelpers.getInstance().getHelper(gameId);
                    SwfLocationInfo locationInfo = params.getSwfBase(bankId, lang, false, request, -1);
                    String cdnCheck = locationInfo.getCdnCheck();
                    if (cdnCheck != null) {
                        request.setAttribute("cdnCheck", cdnCheck);
                        LOG.debug("Found launch request: {} replace to /cdn/speedTest.jsp", requestUrl);
                        forward(request, resp, "/cdn/speedTest.jsp");
                        return;
                    }
                } catch (Exception ex) {
                    LOG.warn("Can't process AUTO cdn mode requestUrl=" + requestUrl, ex);
                }
            }
            String shellPath = getShellPath(request);
            if (requestUrl.contains(LAUNCH_JSP)) {
                LOG.debug("Found launch request: {}", requestUrl);
                String newUrl = !isTrimmedEmpty(shellPath) ? shellPath : replaceLaunchUrl(requestUrl, LAUNCH_JSP, TEMPLATE_JSP);
                LOG.debug("Found launch request: {}, replace to: {}", requestUrl, newUrl);
                forward(request, resp, newUrl);
            } else {
                chain.doFilter(req, resp);
            }
        } else {
            chain.doFilter(req, resp);
        }
    }

    private void forward(HttpServletRequest request, ServletResponse response, String path) throws IOException, ServletException {
        if (StringUtils.isTrimmedEmpty(path)) {
            LOG.error("forward: empty path, request={}, response={}", request, response);
        } else if (!path.endsWith(".jsp")) {
            LOG.error("forward: bad request, path={}, request={}, response={}", path, request, response);
            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            request.getRequestDispatcher(path).forward(request, response);
        }
    }

    private String getShellPath(HttpServletRequest request) throws UnsupportedEncodingException {
        String encoded = request.getParameter(BaseAction.PARAM_SHELL_PATH);
        if (isTrimmedEmpty(encoded)) {
            return encoded;
        }
        encoded = encoded.replaceAll("%2F", "/");
        encoded = encoded.replaceAll("[^a-zA-Z0-9./_]", "");
        return URLDecoder.decode(encoded, "UTF-8");
    }

    public static String replaceLaunchUrl(String requestUrl, String launchJsp, String templateJsp) {
        int index = requestUrl.indexOf(launchJsp);
        if (index <= 0) {
            LOG.error("replaced string not found: {}", requestUrl);
            return requestUrl;
        }
        String firstPart = requestUrl.substring(0, index);
        String endPart = requestUrl.substring(index + launchJsp.length());

        final int langIndex = firstPart.lastIndexOf("/");
        String secondPart = firstPart.substring(0, langIndex);
        return secondPart + templateJsp + endPart;
    }

    @Override
    public void destroy() {
        //nop
    }
}
