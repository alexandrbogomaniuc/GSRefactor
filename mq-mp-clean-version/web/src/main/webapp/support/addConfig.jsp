<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.betsoft.casino.mp.data.persister.GameConfigPersister" %>
<%@ page import="java.io.*" %>
<%@ page import="com.betsoft.casino.mp.model.GameConfigEntity" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="com.betsoft.casino.mp.web.service.RefreshGameConfigTask" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.model.gameconfig.IGameConfig" %>
<%@ page import="com.betsoft.casino.mp.web.service.RoomServiceFactory" %>
<%@ page import="com.betsoft.casino.mp.common.AbstractGameRoom" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="com.hazelcast.core.Member" %>
<%@ page import="java.util.concurrent.Future" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%
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
        long roomId = Long.parseLong(request.getParameter("roomId"));
        int gameId = Integer.parseInt(request.getParameter("gameId"));
        String configString = buffer.toString();

        GameConfigPersister gameConfigPersister = appContext.getBean("persistenceManager", CassandraPersistenceManager.class)
                .getPersister(GameConfigPersister.class);

        IGameConfig gameConfig = null;
        String validationResult = "";
        switch (gameId) {
            case 838:
                gameConfig = new com.betsoft.casino.mp.dragonstone.model.math.config.GameConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.dragonstone.model.math.config.GameConfigValidator()
                        .validate((com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig) gameConfig);
                break;
            case 856:
                gameConfig = new com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfigValidator()
                        .validate((com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfig) gameConfig);
                break;
            case 859:
                gameConfig = new com.betsoft.casino.mp.missionamazon.model.math.config.GameConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.missionamazon.model.math.config.GameConfigValidator()
                        .validate((com.betsoft.casino.mp.missionamazon.model.math.config.GameConfig) gameConfig);
                break;
            case 862:
                gameConfig = new com.betsoft.casino.mp.bgmissionamazon.model.math.config.GameConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.bgmissionamazon.model.math.config.GameConfigValidator()
                        .validate((com.betsoft.casino.mp.bgmissionamazon.model.math.config.GameConfig) gameConfig);
                break;
            case 863:
                gameConfig = new com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfigValidator()
                        .validate((com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfig) gameConfig);
                break;
            case 864:
                gameConfig = new com.betsoft.casino.mp.maxblastchampions.model.math.config.GameConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.maxblastchampions.model.math.config.GameConfigValidator()
                        .validate((com.betsoft.casino.mp.maxblastchampions.model.math.config.GameConfig) gameConfig);
                break;
            case 866:
                gameConfig = new com.betsoft.casino.mp.sectorx.model.math.config.GameConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.sectorx.model.math.config.GameConfigValidator()
                        .validate((com.betsoft.casino.mp.sectorx.model.math.config.GameConfig) gameConfig);
                break;
            case 867:
                gameConfig = new com.betsoft.casino.mp.bgsectorx.model.math.config.GameConfigLoader().parseConfig(configString);
                validationResult = new com.betsoft.casino.mp.bgsectorx.model.math.config.GameConfigValidator()
                        .validate((com.betsoft.casino.mp.bgsectorx.model.math.config.GameConfig) gameConfig);
                break;
        }

        if (gameConfig == null) {
            response.setStatus(400);
            response.getWriter().print("error parsing of config");
        } else if (!validationResult.isEmpty()) {
            response.setStatus(400);
            response.getWriter().println(validationResult);
        } else {
            LocalDateTime localDateTime = LocalDateTime.now();
            GameConfigEntity configEntity = new GameConfigEntity(localDateTime.toString(), nameConfig, gameConfig);
            gameConfigPersister.save(roomId, configEntity);

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

            response.getWriter().print(nameConfig);
        }
    } catch (Throwable e) {
        e.printStackTrace(response.getWriter());
    }
%>
