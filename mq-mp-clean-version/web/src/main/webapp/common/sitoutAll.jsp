<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.LobbySession" %>
<%@ page import="com.betsoft.casino.mp.service.LobbySessionService" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%@ page import="com.betsoft.casino.mp.web.service.RoomServiceFactory" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo" %>

<%@ page contentType="text/html;charset=UTF-8" %>
<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
%>
<%
    LOG.debug("sitoutAll.jsp: query={}", request.getQueryString());
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    LobbySessionService lobbySessionService = appContext.getBean("lobbySessionService", LobbySessionService.class);
    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
    RoomServiceFactory roomServiceFactory = appContext.getBean("roomServiceFactory", RoomServiceFactory.class);

    for (LobbySession lobbySession : lobbySessionService.getAllLobbySessions()) {
        LOG.debug("sitout.jsp: Found lobbySession={}", lobbySession);
        IRoomPlayerInfo playerInfo = playerInfoService.get(lobbySession.getAccountId());
        if (playerInfo != null && playerInfo.getRoomId() > 0 && playerInfo.getSeatNumber() > 0) {
            LOG.debug("sitout.jsp: Found playerInfo={}", playerInfo);
        }

        if (playerInfo != null) {
            LOG.debug("sitoutAll.jsp: for playerInfo={}", playerInfo);
            IRoomInfo roomInfo = roomServiceFactory.getRoomInfo(playerInfo.getRoomId());

            int seatNumber = roomInfo instanceof ISingleNodeRoomInfo ? playerInfo.getSeatNumber() : 0;

            playerInfoService.getNotifyService().executeOnAllMembers(
                    new SitOutTask(playerInfo.getRoomId(), playerInfo.getId(), seatNumber)
            );
        }
    }
    Thread.sleep(10000);
    response.getWriter().print("OK");
    response.getWriter().flush();
%>
