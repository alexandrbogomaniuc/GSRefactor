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
<%@ page import="com.betsoft.casino.mp.web.service.RoomServiceFactory" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
%>
<%
    LOG.debug("sitout.jsp: query={}", request.getQueryString());
    String backURL = request.getParameter("backURL");
    String sid = request.getParameter("SID");
    String _gameSessionId = request.getParameter("gameSessionId");
    if (StringUtils.isTrimmedEmpty(backURL)) {
        //response.getWriter().println("backUrl not found");
        //return;
    }
    if (StringUtils.isTrimmedEmpty(sid)) {
        response.getWriter().println("sid not found");
        return;
    }
    Long gameSessionId = StringUtils.isTrimmedEmpty(_gameSessionId) ? null : Long.valueOf(_gameSessionId);
    LOG.debug("sitout.jsp: backURL={}, sid={}, gameSessionId={}", backURL, sid, gameSessionId);
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    LobbySessionService lobbySessionService = appContext.getBean("lobbySessionService", LobbySessionService.class);
    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
    RoomServiceFactory roomServiceFactory = appContext.getBean("roomServiceFactory", RoomServiceFactory.class);
    LobbySession lobbySession = lobbySessionService.get(sid);
    boolean mpnotfound = false;
    IRoomPlayerInfo playerInfo = null;
    if (lobbySession != null) {
        LOG.debug("sitout.jsp: Found lobbySession={}", lobbySession);
        playerInfo = playerInfoService.get(lobbySession.getAccountId());
        if (playerInfo != null && playerInfo.getRoomId() > 0 && playerInfo.getSeatNumber() > 0) {
            LOG.debug("sitout.jsp: Found playerInfo={}", playerInfo);
        } else {
            LOG.debug("sitout.jsp: Not found roomPlayerInfo for SID={}, playerInfo={}", sid, playerInfo);
            Collection<IRoomPlayerInfo> players = playerInfoService.getBySessionId(sid);
            if (players.isEmpty() && gameSessionId != null) {
                players = playerInfoService.getByGameSessionId(gameSessionId);
            }
            if (!players.isEmpty()) {
                playerInfo = players.iterator().next();
            }
            LOG.debug("sitout.jsp: for SID={}, players={}", sid, players);
        }
    } else {
        LOG.debug("sitout.jsp: Not found lobbySession for SID={}", sid);
        Collection<IRoomPlayerInfo> players = playerInfoService.getBySessionId(sid);
        if (players.isEmpty() && gameSessionId != null) {
            players = playerInfoService.getByGameSessionId(gameSessionId);
        }
        if (!players.isEmpty()) {
            playerInfo = players.iterator().next();
        }
        LOG.debug("sitout.jsp: for SID={}, players={}", sid, players);
    }
    if (playerInfo != null) {
        LOG.debug("sitout.jsp: for SID={}, playerInfo={}", sid, playerInfo);
        IRoomInfo roomInfo = roomServiceFactory.getRoomInfo(playerInfo.getRoomId());

        int seatNumber = roomInfo instanceof ISingleNodeRoomInfo ? playerInfo.getSeatNumber() : 0;

        playerInfoService.getNotifyService().executeOnAllMembers(
                new SitOutTask(playerInfo.getRoomId(), playerInfo.getId(), seatNumber)
        );

        Thread.sleep(10000);
    } else {
        mpnotfound = true;
    }
    if (StringUtils.isTrimmedEmpty(backURL)) { //backURL is null for internal call from GS
        response.getWriter().print("OK");
        response.getWriter().flush();
    } else {
        if (backURL.contains("mpnotfound=true")) {
            backURL = backURL.replace("mpnotfound=true", "mpnotfound=" + mpnotfound);
        } else if (backURL.contains("mpnotfound=false")) {
            backURL = backURL.replace("mpnotfound=false", "mpnotfound=" + mpnotfound);
        } else {
            backURL = backURL + "&mpnotfound=" + mpnotfound;
        }
        response.sendRedirect(backURL);
    }
%>
