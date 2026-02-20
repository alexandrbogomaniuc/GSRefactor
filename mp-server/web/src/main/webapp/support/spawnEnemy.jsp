<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="com.betsoft.casino.mp.transport.SpawnEnemy" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.web.service.RoomServiceFactory" %>
<%@ page import="com.betsoft.casino.mp.model.movement.Trajectory" %>
<%@ page import="com.betsoft.casino.mp.model.movement.Point" %>
<%@ page import="com.betsoft.casino.mp.common.Coords" %>
<%@ page import="com.betsoft.casino.mp.common.AbstractGameRoom" %>
<%@ page import="com.betsoft.casino.mp.common.AbstractActionGameRoom" %>
<%
    try {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        String data = buffer.toString();

        Gson gson = new Gson();
        SpawnEnemy message = gson.fromJson(data, SpawnEnemy.class);

        Trajectory trajectory = null;
        if(message.getTrajectory()!=null && !message.getTrajectory().isEmpty()) {
            trajectory = gson.fromJson(message.getTrajectory(), Trajectory.class);
            Coords coords = new Coords(960, 540, 96, 96);
            for (Point point : trajectory.getPoints()) {
                double screenX = point.getX() - 0.5;
                double screenY = point.getY() - 0.5;
                point.setX(coords.toX(screenX, screenY));
                point.setY(coords.toY(screenX, screenY));
            }
        }

        if (message.isFixTime() && trajectory != null) {
            long shift = System.currentTimeMillis() + 1000 - trajectory.getPoints().get(0).getTime();
            for (Point point : trajectory.getPoints()) {
                point.setTime(point.getTime() + shift);
            }
        }

        RoomServiceFactory roomServiceFactory =
                (RoomServiceFactory) WebSocketRouter.getApplicationContext().getBean("roomServiceFactory");

        AbstractActionGameRoom room = (AbstractActionGameRoom) roomServiceFactory.getRoom(GameType.getByGameId(message.getGameId()), message.getRoomId());
        room.addEnemyFromTeststand(message.getTypeId(), message.getSkinId(), trajectory);
        response.getWriter().print("Ok");
    } catch (Throwable e) {
        e.printStackTrace(response.getWriter());
    }
%>
