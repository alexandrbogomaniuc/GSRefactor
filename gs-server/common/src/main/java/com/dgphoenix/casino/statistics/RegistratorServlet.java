package com.abs.casino.statistics;

import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class RegistratorServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(RegistratorServlet.class);

    public final static String LOG_PARAM = "log";
    private static final String BOT_MACHINE_ID = "botMachineId";
    private static final String PLAYERS_NUM = "playersNum";
    private static final String DROPED_PLAYERS_NUM = "dropedPlayersNum";
    private static final String LOGIN_TIME_DATA = "loginTimeData";
    private static final String BETS_TIME_DATA = "betsTimeData";
    private static final String CLOSE_GAME_TIME_DATA = "closeGameTimeData";
    private static final String HISTORY_TIME_DATA = "historyTimeData";
    private static final String LOGOUT_TIME_DATA = "logoutTimeData";
    private static final String OPEN_GAME_TIME_DATA = "openGameTimeData";

    private static final Set<String> basicParams = Collections.unmodifiableSet(new HashSet<String>(Arrays.<String>asList(
            BOT_MACHINE_ID, PLAYERS_NUM, DROPED_PLAYERS_NUM, LOGIN_TIME_DATA, BETS_TIME_DATA, CLOSE_GAME_TIME_DATA,
            HISTORY_TIME_DATA, LOGOUT_TIME_DATA, OPEN_GAME_TIME_DATA
    )));

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        String logFile = servletConfig.getInitParameter(LOG_PARAM);
        StatisticProcessor.getInstance().setLogFile(logFile == null ? "statistics.log" : logFile);
        LOG.debug("RegistratorServlet: Log file: " + StatisticProcessor.getInstance().getLogFile());
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            boolean isClearLog = Boolean.parseBoolean(req.getParameter("clearStatistics"));
            if (isClearLog) {
                StatisticProcessor.getInstance().clearLogFile();
                resp.getWriter().write("RESULT=OK");
                return;
            }
            String botMachineId = req.getParameter(BOT_MACHINE_ID);
            String playersNum = req.getParameter(PLAYERS_NUM);
            String droppedPlayersNum = req.getParameter(DROPED_PLAYERS_NUM);

            String loginData = req.getParameter(LOGIN_TIME_DATA);
            String betsData = req.getParameter(BETS_TIME_DATA);
            String closeGameData = req.getParameter(CLOSE_GAME_TIME_DATA);
            String historyData = req.getParameter(HISTORY_TIME_DATA);
            String logoutData = req.getParameter(LOGOUT_TIME_DATA);
            String openGameData = req.getParameter(OPEN_GAME_TIME_DATA);

            List<String> list = new ArrayList<>();
            Enumeration<String> names = req.getParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if (!basicParams.contains(name)) {
                    list.add(name);
                }
            }
            Collections.sort(list);
            List<Pair<String, String>> additionParams = new ArrayList<>(list.size());
            for (String s : list) {
                String parameter = req.getParameter(s);
                if (!StringUtils.isTrimmedEmpty(parameter)) {
                    additionParams.add(new Pair<>(s, parameter.trim()));
                }
            }
            StatisticProcessor.getInstance().processStatistic(botMachineId, playersNum,
                    droppedPlayersNum, loginData, betsData, closeGameData,
                    historyData, logoutData, openGameData, additionParams);
            resp.getWriter().write("RESULT=OK");
        } catch (Exception e) {
            LOG.error("RegistratorServlet request processing error", e);
            resp.getWriter().write("Request processing error: " + e.getMessage());
        }
    }

}
