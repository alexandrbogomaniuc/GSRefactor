<%--
  Created by IntelliJ IDEA.
  User: maxvish
  Date: 19.05.2023
  Time: 10:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.SingleNodeRoomInfoService" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.betsoft.casino.mp.data.persister.GameConfigPersister" %>
<%@ page import="com.hazelcast.core.Member" %>
<%@ page import="java.util.concurrent.Future" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.betsoft.casino.mp.web.service.RefreshGameConfigTask" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.betsoft.casino.mp.web.service.RoomServiceFactory" %>
<%@ page import="com.betsoft.casino.mp.common.AbstractGameRoom" %>
<%! Set<Integer> allowedBanks = new HashSet<>(Arrays.asList(271, 583, 1728, 3618, 6274, 6275, 9138)); %>
<%
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    String bank = request.getParameter("bankId");
    String gameId = request.getParameter("gameId");


    int bankId;
    try {
        bankId = Integer.parseInt(bank);
    } catch (Exception e) {
        response.getWriter().write("missed bankId param");
        return;
    }
    if (!allowedBanks.contains(bankId)) {
        response.getWriter().write("wrong bank id");
        return;
    }

    GameType gameType;
    try {
        gameType = GameType.getByGameId(Integer.parseInt(gameId));
    } catch (Exception e) {
        response.getWriter().write("missed gameId param");
        return;
    }

    GameConfigPersister gameConfigPersister = appContext.getBean("persistenceManager", CassandraPersistenceManager.class)
            .getPersister(GameConfigPersister.class);

    SingleNodeRoomInfoService roomInfoService = WebSocketRouter.getApplicationContext().getBean(SingleNodeRoomInfoService.class);
    List<Long> roomIds = roomInfoService.getRoomIds(bankId, gameType);
    for (Long roomId : roomIds) {
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

        AbstractGameRoom room = (AbstractGameRoom) roomServiceFactory.getRoom(GameType.getByGameId(Integer.parseInt(gameId)), roomId);

        room.updateWeaponPrices();
    }

    response.getWriter().print("Done !");
%>
