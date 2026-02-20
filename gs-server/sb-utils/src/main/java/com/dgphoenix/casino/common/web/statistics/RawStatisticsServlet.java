package com.dgphoenix.casino.common.web.statistics;

import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: flsh
 * Date: 16.03.13
 */
public class RawStatisticsServlet extends HttpServlet {
    protected static final Logger LOG = Logger.getLogger(RawStatisticsServlet.class);
    protected static final String RESET_PARAM = "reset";
    protected static final String CLEAR_CACHE_PARAM = "clearcache";
    protected static final String PRINT_GETTER_STAT_PARAM = "getter";

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        StatisticsManager.getInstance().setEnableStatistics(true);
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    private void handleRequest(HttpServletRequest request,
                               HttpServletResponse response) throws ServletException {
        try {
            response.setContentType("text/plain");
            StringBuilder sb = new StringBuilder();
            if (!StatisticsManager.getInstance().isEnableStatistics()) {
                response.getWriter().println("Statistics disabled");
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            if (request.getParameter(RESET_PARAM) != null) {
                resetStatistics();
                sb.append("Statistics reset");
            } else if (request.getParameter(CLEAR_CACHE_PARAM) != null) {
                clearCache();
            } else {
                boolean printGetterStat = request.getParameter(PRINT_GETTER_STAT_PARAM) != null;
                StatisticsManager.getInstance().printRequestStatistics(sb, printGetterStat,
                        request.getParameter("sort"));
            }
            response.getWriter().print(sb.toString());
            postProcess(request, response);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            LOG.error("Cannot get statistics", e);
            throw new ServletException("Cannot handle request", e);
        }
    }

    protected void resetStatistics() {
        StatisticsManager.getInstance().dropServiceStatistics();
    }

    protected void clearCache() {
        //nop
    }

    protected void postProcess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //nop
    }

    public void init() throws ServletException {
        super.init();
        LOG.debug("Statistics servlet started, collectStatistics =  " +
                StatisticsManager.getInstance().isEnableStatistics());
    }

}
