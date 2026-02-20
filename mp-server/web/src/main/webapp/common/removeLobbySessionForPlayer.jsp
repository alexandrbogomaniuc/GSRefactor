<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.betsoft.casino.mp.service.LobbySessionService" %>
<%@ page import="com.betsoft.casino.mp.model.LobbySession" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
%>
<%
    LOG.debug("removeLobbySessionForPlayer.jsp: query={}", request.getQueryString());
    String accountId = request.getParameter("accountId");
    String sid = request.getParameter("SID");
    LOG.debug("removeLobbySessionForPlayer.jsp: sid={}", sid);
    LobbySessionService lobbySessionService = WebSocketRouter.getApplicationContext().getBean(LobbySessionService.class);

    Collection<LobbySession> lobbySessions = null;

    if (!StringUtils.isTrimmedEmpty(accountId)) {
        lobbySessions = lobbySessionService.getByAccountId(Long.parseLong(accountId));
    }

    if ((lobbySessions == null || lobbySessions.size() == 0) && (!StringUtils.isTrimmedEmpty(sid))) {
        LobbySession lobbySession = lobbySessionService.get(sid);
        if(lobbySession != null) {
            lobbySessions  = new ArrayList<>();
            lobbySessions.add(lobbySession);
        }
    }
    if ((lobbySessions == null || lobbySessions.size() == 0)) {
        //lobbySessions = lobbySessionService.getAllLobbySessions();
    }

    if (lobbySessions == null || lobbySessions.size() == 0) {
        response.getWriter().println("No lobbySessions found");
    } else {
        for(LobbySession lobbySession : lobbySessions) {
            response.getWriter().println("Lobby Session removed for: " + lobbySession.getSessionId());
            lobbySessionService.remove(lobbySession.getSessionId());
        }
    }
%>
