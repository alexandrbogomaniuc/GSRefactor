<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.betsoft.casino.mp.service.IRoomInfoService" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
%>
<%
    LOG.debug("removeRoomPlayerInfo.jsp: query={}", request.getQueryString());
    String accountId = request.getParameter("accountId");
    String sid = request.getParameter("SID");
    String gameSessionId = request.getParameter("gameSessionId");
    LOG.debug("removeRoomPlayerInfo.jsp: gameSessionId={}, sid={}", gameSessionId, sid);

    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    RoomPlayerInfoService roomPlayerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);

    AbstractRoomInfoService multiNodeRoomInfoService = appContext.getBean(MultiNodeRoomInfoService.class);
    AbstractRoomInfoService singleNodeRoomInfoService = appContext.getBean(SingleNodeRoomInfoService.class);
    AbstractRoomInfoService bgPrivateRoomInfoService =  appContext.getBean(BGPrivateRoomInfoService.class);

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
            long id = roomPlayerInfo.getId();
            long roomId = roomPlayerInfo.getRoomId();

            AbstractRoomInfoService roomInfoService = null;
            if (multiNodeRoomInfoService != null) {
                if(multiNodeRoomInfoService.getRoom(roomId) != null) {
                    roomInfoService = multiNodeRoomInfoService;
                }
            }
            if (roomInfoService == null && singleNodeRoomInfoService != null) {
                if(singleNodeRoomInfoService.getRoom(roomId) != null) {
                    roomInfoService = singleNodeRoomInfoService;
                }
            }
            if (roomInfoService == null && bgPrivateRoomInfoService != null) {
                if(bgPrivateRoomInfoService.getRoom(roomId) != null) {
                    roomInfoService = bgPrivateRoomInfoService;
                }
            }

            if(roomInfoService == null) {
                response.getWriter().println("Can't identify roomInfoService for account: " + roomPlayerInfo.getId() +
                        " " + roomPlayerInfo.getNickname() + " roomId:" + roomId);
            } else {
                roomPlayerInfoService.remove(roomInfoService, roomId, id);
                response.getWriter().println("Stuck record removed from roomPlayerInfoService for account: " + roomPlayerInfo.getId() +
                        " " + roomPlayerInfo.getNickname() + " roomId:" + roomId + " roomInfoService:" + roomInfoService.getClass());
            }
        }
    }
%>
