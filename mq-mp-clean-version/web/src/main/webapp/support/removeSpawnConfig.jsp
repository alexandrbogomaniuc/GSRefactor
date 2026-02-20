<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.data.persister.SpawnConfigPersister" %>
<%@ page import="com.betsoft.casino.mp.web.service.RefreshSpawnConfigTask" %>
<%

    long roomId = Long.parseLong(request.getParameter("roomId"));
    int gameId = Integer.parseInt(request.getParameter("gameId"));

    ApplicationContext appContext = WebSocketRouter.getApplicationContext();

    SpawnConfigPersister spawnConfigPersister = appContext.getBean("persistenceManager", CassandraPersistenceManager.class)
            .getPersister(SpawnConfigPersister.class);
    spawnConfigPersister.removeConfig(roomId);

    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
    playerInfoService.getNotifyService().submitToAllMembers(new RefreshSpawnConfigTask(roomId));

%>
