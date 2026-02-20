<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="java.io.*" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.data.persister.SpawnConfigPersister" %>
<%@ page import="com.betsoft.casino.mp.model.gameconfig.ISpawnConfig" %>
<%@ page import="com.betsoft.casino.mp.dragonstone.model.math.config.*" %>
<%@ page import="com.betsoft.casino.mp.model.SpawnConfigEntity" %>
<%@ page import="com.betsoft.casino.mp.web.service.RefreshSpawnConfigTask" %>
<%
    try {
        ApplicationContext appContext = WebSocketRouter.getApplicationContext();

        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        String nameConfig = "spawnConfig";
        while ((line = reader.readLine()) != null) {
            if (!line.contains("------") && !line.contains("Content-")) {
                buffer.append(line).append("\n");
            } else if (line.contains("filename")) {
                nameConfig = line.substring(line.indexOf("filename=\"") + 9).replace("\"", "");
            }
        }
        long roomId = Long.parseLong(request.getParameter("roomId"));
        int gameId = Integer.parseInt(request.getParameter("gameId"));
        String configString = buffer.toString();

        SpawnConfigPersister spawnConfigPersister = appContext.getBean("persistenceManager", CassandraPersistenceManager.class)
                .getPersister(SpawnConfigPersister.class);

        ISpawnConfig spawnConfig = null;
        String validationResult = "";
        switch (gameId) {
            case 838:
                spawnConfig = new SpawnConfigLoader().parseConfig(configString);
                validationResult = new SpawnConfigValidator().validate((SpawnConfig) spawnConfig);
                break;
            case 856:
                spawnConfig = new com.betsoft.casino.mp.bgdragonstone.model.math.config.SpawnConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.bgdragonstone.model.math.config.SpawnConfigValidator()
                        .validate((com.betsoft.casino.mp.bgdragonstone.model.math.config.SpawnConfig) spawnConfig);
                break;
            case 859:
                spawnConfig = new com.betsoft.casino.mp.missionamazon.model.math.config.SpawnConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.missionamazon.model.math.config.SpawnConfigValidator()
                        .validate((com.betsoft.casino.mp.missionamazon.model.math.config.SpawnConfig) spawnConfig);
                break;
            case 862:
                spawnConfig = new com.betsoft.casino.mp.bgmissionamazon.model.math.config.SpawnConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.bgmissionamazon.model.math.config.SpawnConfigValidator()
                        .validate((com.betsoft.casino.mp.bgmissionamazon.model.math.config.SpawnConfig) spawnConfig);
                break;
            case 866:
                spawnConfig = new com.betsoft.casino.mp.sectorx.model.math.config.SpawnConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.sectorx.model.math.config.SpawnConfigValidator()
                        .validate((com.betsoft.casino.mp.sectorx.model.math.config.SpawnConfig) spawnConfig);
                break;
            case 867:
                spawnConfig = new com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfigValidator()
                        .validate((com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfig)spawnConfig);
                break;
        }

        if (spawnConfig == null) {
            response.setStatus(400);
            response.getWriter().print("error parsing of config");
        } else if (!validationResult.isEmpty()) {
            response.setStatus(400);
            response.getWriter().println(validationResult);
        } else {
            LocalDateTime localDateTime = LocalDateTime.now();
            SpawnConfigEntity configEntity = new SpawnConfigEntity(localDateTime.toString(), nameConfig, spawnConfig);
            spawnConfigPersister.save(roomId, configEntity);

            RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
            playerInfoService.getNotifyService().submitToAllMembers(new RefreshSpawnConfigTask(roomId));

            response.getWriter().print(nameConfig);
        }
    } catch (Throwable e) {
        e.printStackTrace(response.getWriter());
    }
%>
