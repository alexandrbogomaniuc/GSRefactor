<%@ page import="com.betsoft.casino.mp.service.GameConfigProvider" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="static org.apache.http.entity.ContentType.APPLICATION_JSON" %>
<%@ page import="com.google.gson.*" %>
<%
    long roomId;
    try {
        roomId = Long.parseLong(request.getParameter("roomId"));
    } catch (Exception e) {
        response.getWriter().write("missed roomId param");
        return;
    }

    long gameId;
    try {
        gameId = Long.parseLong(request.getParameter(("gameId")));
    } catch (Exception e) {
        response.getWriter().write("missed gameId param");
        return;
    }

    GameConfigProvider provider = WebSocketRouter.getApplicationContext().getBean(GameConfigProvider.class);
    response.setContentType(APPLICATION_JSON.toString());
    response.getWriter().write(new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create()
            .toJson(provider.getConfig(gameId, roomId)));
%>
