<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.data.persister.MapConfigPersister" %>
<%@ page import="com.betsoft.casino.mp.model.IMapConfigEntity" %>
<%
    int mapId;
    try {
        mapId = Integer.parseInt(request.getParameter("mapId"));
    } catch (Exception e) {
        response.getWriter().write("wrong mapId");
        return;
    }

    MapConfigPersister mapConfigPersister = WebSocketRouter.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class)
            .getPersister(MapConfigPersister.class);
    if (mapConfigPersister == null) {
        out.print("persister is null");
        return;
    }
    IMapConfigEntity configEntity = mapConfigPersister.load(mapId);
    if (configEntity != null) {
        out.write("{ \"default\": false, \"date\": \"" + configEntity.getUploadDate() + "\" }");
    } else {
        out.write("{ \"default\": true }");
    }
%>

