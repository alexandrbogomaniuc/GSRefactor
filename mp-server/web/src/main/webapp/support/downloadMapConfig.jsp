<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="static org.apache.http.entity.ContentType.APPLICATION_JSON" %>
<%@ page import="com.google.gson.*" %>
<%@ page import="com.betsoft.casino.mp.common.GameMapStore" %>
<%
    int mapId;
    try {
        mapId = Integer.parseInt(request.getParameter("mapId"));
    } catch (Exception e) {
        response.getWriter().write("missed roomId param");
        return;
    }

    GameMapStore mapStore = WebSocketRouter.getApplicationContext().getBean(GameMapStore.class);
    response.setContentType(APPLICATION_JSON.toString());
    response.getWriter().write(new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create()
            .toJson(mapStore.getMapConfig(mapId)));
%>
