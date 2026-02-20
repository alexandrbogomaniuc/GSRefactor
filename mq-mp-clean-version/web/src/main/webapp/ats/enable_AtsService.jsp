<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>

<%
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);

    try {

        String enabledParam = request.getParameter("enabled");
        boolean enabledOld = botConfigInfoService.setBotServiceEnabled(enabledParam);
        response.getWriter().print( "setBotServiceEnabled: enabledParam=" + enabledParam + " enabledOld=" + enabledOld);
        response.setStatus(200);

    } catch (Exception e) {
        response.setStatus(400);
        response.getWriter().print(e.getMessage());
    }
%>
