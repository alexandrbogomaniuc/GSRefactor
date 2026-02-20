<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.SingleNodeRoomInfoService" %>
<%@ page import="com.betsoft.casino.mp.model.SingleNodeRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.model.MoneyType" %>
<%@ page import="com.betsoft.casino.mp.service.RoomTemplateService" %>
<%@ page import="com.betsoft.casino.mp.model.RoomTemplate" %><%
    SingleNodeRoomInfoService service = WebSocketRouter.getApplicationContext().getBean(SingleNodeRoomInfoService.class);
    for (SingleNodeRoomInfo roomInfo : service.getAllRooms()) {
        if (roomInfo.getMoneyType() == MoneyType.TOURNAMENT && roomInfo.getMaxSeats() == 1) {
            long roomId = roomInfo.getId();
            try {
                service.lock(roomId);
                roomInfo.setMaxSeats(roomInfo.getGameType().getMaxSeats());
                service.update(roomInfo);
            } catch (Exception e) {
                response.getWriter().println("Some problem with lock, room id = " + roomId);
            } finally {
                service.unlock(roomId);
            }
        }
    }

    RoomTemplateService templateService = WebSocketRouter.getApplicationContext().getBean(RoomTemplateService.class);
    for (RoomTemplate roomTemplate : templateService.getAll()) {
        if (roomTemplate.getMoneyType() == MoneyType.TOURNAMENT && roomTemplate.getMaxSeats() == 1) {
            roomTemplate.setMaxSeats(roomTemplate.getGameType().getMaxSeats());
            templateService.put(roomTemplate);
        }
    }
%>