<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
%>
<%
    LOG.debug("getPlayer.jsp: query={}", request.getQueryString());
    String accountId = request.getParameter("accountId");
    String sid = request.getParameter("SID");
    String gameSessionId = request.getParameter("gameSessionId");
    LOG.debug("getPlayer.jsp: gameSessionId={}, sid={}", gameSessionId, sid);
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);

    if (!StringUtils.isTrimmedEmpty(accountId)) {
        response.getWriter().println("By accountId: " + playerInfoService.get(Long.parseLong(accountId)));
    }
    if (!StringUtils.isTrimmedEmpty(sid)) {
        response.getWriter().println("By SID: " + playerInfoService.getBySessionId(sid));
    }
    if (!StringUtils.isTrimmedEmpty(gameSessionId)) {
        response.getWriter().println("By gameSessionId: " + playerInfoService.getByGameSessionId(Long.parseLong(gameSessionId)));
    }
%>
