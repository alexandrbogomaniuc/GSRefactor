<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%

    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
    IRoomPlayerInfo player = playerInfoService.get(2016116442L);
    if (player != null && player.getGameSessionId() <= 0) {
        playerInfoService.lock(player.getId());
        try {
            player.setGameSessionId(2301106949L);
            playerInfoService.put(player);
        } finally {
            playerInfoService.unlock(player.getId());
        }
    }

%>
