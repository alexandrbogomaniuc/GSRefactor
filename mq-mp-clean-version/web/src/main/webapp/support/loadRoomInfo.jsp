<%@ page import="com.betsoft.casino.mp.data.persister.GameConfigPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.GameConfigEntity" %>
<%
    long roomId;
    try {
        roomId = Long.parseLong(request.getParameter("roomId"));
    } catch (Exception e) {
        response.getWriter().write("wrong roomId");
        return;
    }

    GameConfigPersister gameConfigPersister = WebSocketRouter.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class)
            .getPersister(GameConfigPersister.class);
    GameConfigEntity configEntity = gameConfigPersister.load(roomId);
    if (configEntity != null) {
        out.write("{ \"default\": false, \"date\": \"" + configEntity.getUploadDate() + "\" }");
    } else {
        out.write("{ \"default\": true }");
    }
%>

