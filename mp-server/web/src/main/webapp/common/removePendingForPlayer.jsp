<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.ArrayList" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(RoomPlayerInfoService.class);
%>
<%
    LOG.debug("removePendingForPlayer.jsp: query={}", request.getQueryString());
    String accountId = request.getParameter("accountId");
    String sid = request.getParameter("SID");
    String gameSessionId = request.getParameter("gameSessionId");
    LOG.debug("removePendingForPlayer.jsp: accountId={}, gameSessionId={}, sid={}", accountId, gameSessionId, sid);
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    RoomPlayerInfoService roomPlayerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);

    Collection<IRoomPlayerInfo> roomPlayerInfos = null;

    if (!StringUtils.isTrimmedEmpty(accountId)) {
        IRoomPlayerInfo roomPlayer = roomPlayerInfoService.get(Long.parseLong(accountId));
        if(roomPlayer != null) {
            roomPlayerInfos = new ArrayList<>();
            roomPlayerInfos.add(roomPlayer);
        }
    }
    if ((roomPlayerInfos == null || roomPlayerInfos.size() == 0) && (!StringUtils.isTrimmedEmpty(sid))) {
        roomPlayerInfos = roomPlayerInfoService.getBySessionId(sid);

    }
    if ((roomPlayerInfos == null || roomPlayerInfos.size() == 0) && (!StringUtils.isTrimmedEmpty(gameSessionId))) {
        roomPlayerInfos = roomPlayerInfoService.getByGameSessionId(Long.parseLong(gameSessionId));
    }

    if (roomPlayerInfos == null || roomPlayerInfos.size() == 0) {
        response.getWriter().println("No roomPlayerInfos found");
    } else {
        for(IRoomPlayerInfo roomPlayerInfo : roomPlayerInfos) {
            roomPlayerInfo.setPendingOperation(false);
            response.getWriter().println("Pending Operation removed for: " + roomPlayerInfo.getId() + " " + roomPlayerInfo.getNickname());
            roomPlayerInfoService.put(roomPlayerInfo);
        }
    }
%>
