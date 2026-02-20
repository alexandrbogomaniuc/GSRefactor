<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%@ page import="com.betsoft.casino.mp.service.LobbySessionService" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.model.LobbySession" %>
<%@ page import="com.betsoft.casino.mp.service.IRoomInfoService" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger("com.betsoft.casino.mp");
%>
<%
    LOG.debug("fixPlayer.jsp: query={}", request.getQueryString());
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);

    long accountId = 1260439900;
    IRoomPlayerInfo playerInfo = playerInfoService.get(accountId);
    response.getWriter().println("playerInfo: " + playerInfo);
    if (playerInfo != null) {
        IRoomInfoService roomInfoService = (IRoomInfoService) appContext.getBean("singleNodeRoomInfoService");
        //playerInfoService.remove(roomInfoService, playerInfo.getRoomId(), playerInfo.getId());
    }

    LobbySessionService lobbySessionService = appContext.getBean("lobbySessionService", LobbySessionService.class);
    LobbySession lobbySession = lobbySessionService.get(accountId);
    response.getWriter().println("lobbySession: " + lobbySession);
    if (lobbySession != null) {
        //lobbySessionService.remove(lobbySession.getSessionId());
    }
%>
