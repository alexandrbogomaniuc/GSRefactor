<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="java.io.*" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.model.MapConfigEntity" %>
<%@ page import="com.betsoft.casino.mp.data.persister.MapConfigPersister" %>
<%@ page import="com.betsoft.casino.mp.common.GameMapMeta" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.betsoft.casino.mp.web.service.RefreshMapConfigTask" %>
<%
    /* TODO: replace with MapConfigController */
    try {
        ApplicationContext appContext = WebSocketRouter.getApplicationContext();

        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        String nameConfig = "none";
        while ((line = reader.readLine()) != null) {
            if (!line.contains("------") && !line.contains("Content-")) {
                buffer.append(line).append("\n");
            } else if (line.contains("filename")) {
                nameConfig = line.substring(line.indexOf("filename=\"") + 9).replace("\"", "");
            }
        }
        int mapId = Integer.parseInt(request.getParameter("mapId"));
        String configString = buffer.toString();

        MapConfigPersister mapConfigPersister = appContext.getBean("persistenceManager", CassandraPersistenceManager.class)
                .getPersister(MapConfigPersister.class);

        GameMapMeta mapConfig = new Gson().fromJson(configString, GameMapMeta.class);

        if (mapConfig == null) {
            response.setStatus(400);
            response.getWriter().print("error parsing of config");
        } else {
            LocalDateTime localDateTime = LocalDateTime.now();
            MapConfigEntity configEntity = new MapConfigEntity(localDateTime.toString(), mapConfig);
            mapConfigPersister.save(mapId, configEntity);

            RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
            playerInfoService.getNotifyService().submitToAllMembers(new RefreshMapConfigTask(mapId));

            response.getWriter().print(nameConfig);
        }
    } catch (Throwable e) {
        e.printStackTrace(response.getWriter());
    }
%>
