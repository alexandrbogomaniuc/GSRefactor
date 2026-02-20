<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>

<%
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);

    try {
        String botIdParam = request.getParameter("botId");
        long botId = Long.parseLong(botIdParam);

        if (botId > 0) {

                botConfigInfoService.remove(botId);
                response.setStatus(200);
                response.getWriter().print("id=" + botId + "removed successfully;");
        } else {
            response.setStatus(400);
            response.getWriter().print("id=" + botId + "bad id, skip to remove;");
        }
    } catch (Exception e) {
        response.setStatus(400);
        response.getWriter().print(e.getMessage());
    }
%>
