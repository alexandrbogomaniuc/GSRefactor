<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.LobbySession" %>
<%@ page import="com.betsoft.casino.mp.service.LobbySessionService" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="java.util.Collection" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.web.service.RoomServiceFactory" %>
<%@ page import="com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
%>
<%
    LOG.debug("internalSitOut.jsp: query={}", request.getQueryString());
    String _accountId = request.getParameter("accountId");
    String _gameSessionId = request.getParameter("gameSessionId");
    if (StringUtils.isTrimmedEmpty(_accountId)) {
        response.getWriter().println("accountId not found");
        return;
    }
    Long gameSessionId = StringUtils.isTrimmedEmpty(_gameSessionId) || "null".equalsIgnoreCase(_gameSessionId) ?
            null : Long.valueOf(_gameSessionId);
    Long accountId = Long.valueOf(_accountId);
    LOG.debug("internalSitOut.jsp: accountId={}, gameSessionId={}", accountId, gameSessionId);
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
    RoomServiceFactory roomServiceFactory = appContext.getBean("roomServiceFactory", RoomServiceFactory.class);
    IRoomPlayerInfo playerInfo = playerInfoService.get(accountId);
    if (playerInfo == null) {
        LOG.debug("internalSitOut.jsp: Not found RoomPlayerInfo for accountId={}", accountId);
        Collection<IRoomPlayerInfo> players = gameSessionId == null ? null :
                playerInfoService.getByGameSessionId(gameSessionId);
        if (players != null && !players.isEmpty()) {
            playerInfo = players.iterator().next();
        } else {
            LobbySessionService lobbySessionService = appContext.getBean("lobbySessionService",
                    LobbySessionService.class);
            Collection<LobbySession> lobbySessions = lobbySessionService.getByAccountId(accountId);
            for (LobbySession lobbySession : lobbySessions) {
                if (lobbySession.getSessionId() != null) {
                    players = playerInfoService.getBySessionId(lobbySession.getSessionId());
                    if (!players.isEmpty()) {
                        playerInfo = players.iterator().next();
                        break;
                    }
                }
            }
        }
    }
    if (playerInfo != null) {
        LOG.debug("internalSitOut.jsp: for playerInfo={}", accountId);
        IRoomInfo roomInfo = roomServiceFactory.getRoomInfo(playerInfo.getRoomId());

        int seatNumber = roomInfo instanceof ISingleNodeRoomInfo ? playerInfo.getSeatNumber() : 0;

        playerInfoService.getNotifyService().executeOnAllMembers(
                new SitOutTask(playerInfo.getRoomId(), playerInfo.getId(), seatNumber)
        );
        Thread.sleep(10000);
    }
    response.getWriter().print("OK");
    response.getWriter().flush();
%>
