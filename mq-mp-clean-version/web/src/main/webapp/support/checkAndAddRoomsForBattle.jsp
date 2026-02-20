<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.service.SingleNodeRoomInfoService" %>
<%@ page import="com.betsoft.casino.mp.service.RoomTemplateService" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Optional" %>
<%@ page import="com.dgphoenix.casino.common.util.Pair" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.List" %>
<%@ page import="com.betsoft.casino.mp.model.*" %>
<%@ page import="java.io.IOException" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Add room to mpserver</title>
</head>
<body>
<%
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    SingleNodeRoomInfoService roomInfoService = appContext.getBean("singleNodeRoomInfoService", SingleNodeRoomInfoService.class);

    long stake = 1000;
    String currencyCode = "EUR";
    long bankId = 271;
    String gameIdParamter = request.getParameter("gameId");
    String requestedCountRoomsParameter = request.getParameter("requestedCountRooms");
    String needShowServer = request.getParameter("needShowServer");
    int cntRealBotsInSameRoom =  StringUtils.isTrimmedEmpty(request.getParameter("cntRealBotsInSameRoom")) ? 1
            : Integer.parseInt(request.getParameter("cntRealBotsInSameRoom"));

    if (StringUtils.isTrimmedEmpty(gameIdParamter)) {
        response.getWriter().println("gameId not found");
    } else {
        if (StringUtils.isTrimmedEmpty(requestedCountRoomsParameter)) {
            response.getWriter().println("requestedCountRooms not found");
        } else {
            int requestedCountRooms = Integer.parseInt(requestedCountRoomsParameter);
            GameType gameType = GameType.getByGameId(Integer.parseInt(gameIdParamter));
            if (gameType != null && gameType.isBattleGroundGame()) {
                Collection<SingleNodeRoomInfo> gameBattleRooms = roomInfoService.getBattlegroundRooms(bankId, gameType, Money.fromCents(stake), currencyCode);

                int sizeExistRooms = gameBattleRooms.size();
                response.getWriter().println("found gameBattleRooms size: " + sizeExistRooms + ", requestedCountRooms: " + requestedCountRooms + "<br>");

                if (sizeExistRooms > 0 && requestedCountRooms > sizeExistRooms) {
                    Optional<SingleNodeRoomInfo> first = gameBattleRooms.stream().findFirst();
                    if (first.isPresent()) {
                        long templateId = first.get().getTemplateId();
                        int addedCnt = requestedCountRooms - sizeExistRooms;
                        response.getWriter().println("addedCnt {}" + addedCnt + ", templateId: " + templateId + "<br>");
                        while (addedCnt-- > 0) {
                            RoomTemplateService roomTemplateService = appContext.getBean("roomTemplateService", RoomTemplateService.class);
                            RoomTemplate roomTemplate = roomTemplateService.get(templateId);
                            IRoomInfo newRoom = roomInfoService.createForTemplate(roomTemplate, bankId, Money.fromCents(stake), currencyCode);
                            response.getWriter().println("\n\n<br><br>New room added: " + newRoom);
                        }
                    }
                }
            } else {
                response.getWriter().println("gameId is wrong or not battle");
            }

            Collection<SingleNodeRoomInfo> gameBattleRooms = roomInfoService.getBattlegroundRooms(bankId, gameType, Money.fromCents(stake), currencyCode);


            if(!StringUtils.isTrimmedEmpty(needShowServer) && needShowServer.equals("true")) {
                gameBattleRooms.forEach(singleNodeRoomInfo -> {
                    try {
                        response.getWriter().println("serverId: " + singleNodeRoomInfo.getGameServerId()
                                + ", roomId: " + singleNodeRoomInfo.getId() + "<br>");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }


            List<Long> listRooms = gameBattleRooms.stream().map(AbstractRoomInfo::getId).limit(requestedCountRooms).collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            listRooms.forEach(roomId -> {
                for (int i = 0; i < cntRealBotsInSameRoom; i++) {
                    sb.append(roomId).append(",");
                }
            });
            response.getWriter().println("listRooms: " + sb);

        }
    }
%>
</body>
</html>
