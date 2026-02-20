<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.betsoft.casino.mp.data.persister.GameConfigPersister" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.web.service.RefreshGameConfigTask" %>
<%@ page import="com.betsoft.casino.mp.web.service.RoomServiceFactory" %>
<%@ page import="com.betsoft.casino.mp.common.AbstractGameRoom" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="com.hazelcast.core.Member" %>
<%@ page import="java.util.concurrent.Future" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%

    long roomId = Long.parseLong(request.getParameter("roomId"));
    int gameId = Integer.parseInt(request.getParameter("gameId"));

    ApplicationContext appContext = WebSocketRouter.getApplicationContext();

    GameConfigPersister gameConfigPersister = appContext.getBean("persistenceManager", CassandraPersistenceManager.class)
            .getPersister(GameConfigPersister.class);
    gameConfigPersister.removeConfig(roomId);

    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
    Map<Member, Future<Boolean>> memberFutureMap = playerInfoService.getNotifyService()
            .submitToAllMembers(new RefreshGameConfigTask(roomId));
    PrintWriter writer = response.getWriter();
    memberFutureMap.forEach((member, task) -> {
        try {
            if (!task.get()) {
                throw new CommonException("Some problems with uploading configuration");
            }
        } catch (Exception e) {
            e.printStackTrace(writer);
        }
    });

    RoomServiceFactory roomServiceFactory =
            (RoomServiceFactory) WebSocketRouter.getApplicationContext().getBean("roomServiceFactory");

    AbstractGameRoom room = (AbstractGameRoom) roomServiceFactory.getRoom(GameType.getByGameId(gameId), roomId);

    room.updateWeaponPrices();
%>
