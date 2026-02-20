<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.data.persister.MapConfigPersister" %>
<%@ page import="com.betsoft.casino.mp.web.service.RefreshMapConfigTask" %>
<%

    int mapId = Integer.parseInt(request.getParameter("mapId"));
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();

    MapConfigPersister mapConfigPersister = appContext.getBean("persistenceManager", CassandraPersistenceManager.class)
            .getPersister(MapConfigPersister.class);
    mapConfigPersister.removeConfig(mapId);

    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
    playerInfoService.getNotifyService().submitToAllMembers(new RefreshMapConfigTask(mapId));
%>
