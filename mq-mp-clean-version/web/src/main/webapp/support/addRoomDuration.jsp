<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.SingleNodeRoomInfoService" %>
<%@ page import="java.util.Collection" %>
<%@ page import="com.betsoft.casino.mp.model.Money" %>
<%@ page import="com.betsoft.casino.mp.model.SingleNodeRoomInfo" %>
<%
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    SingleNodeRoomInfoService roomInfoService = appContext.getBean("singleNodeRoomInfoService", SingleNodeRoomInfoService.class);
    Collection<SingleNodeRoomInfo> allRooms = roomInfoService.getAllRooms();
    String gameId = request.getParameter("gameId");
    if (gameId == null || gameId.isEmpty()) {
        response.getWriter().write("need specify gameId in request. ");
    } else {
        for (SingleNodeRoomInfo room : allRooms) {
            Money stake = room.getStake();
            if (stake.toCents() == 15 && room.getGameType().getGameId() == Integer.parseInt(gameId)) {
                roomInfoService.lock(room.getId());
                try {
                    response.getWriter().write("room: " + room + "<br>");
                    room.setRoundDuration(30);
                    roomInfoService.update(room);
                } finally {
                    roomInfoService.unlock(room.getId());
                }
            }
        }
    }
%>
