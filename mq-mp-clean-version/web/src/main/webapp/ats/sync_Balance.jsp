<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="com.betsoft.casino.mp.web.socket.BotServiceClient" %>
<%@ page import="com.betsoft.casino.mp.model.bots.dto.BotStatusResult" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.StringTokenizer" %>

<%
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);
    BotServiceClient botServiceClient = WebSocketRouter.getApplicationContext().getBean(BotServiceClient.class);

    try {
        String botIdsParam = request.getParameter("botIds");

        Set<Long> botIds = new HashSet<>();
        StringTokenizer gamesTokenizer = new StringTokenizer(botIdsParam, ",");
        while (gamesTokenizer.hasMoreTokens()) {
            String botIdParam = gamesTokenizer.nextToken();
            long botId = Long.parseLong(botIdParam);
            botIds.add(botId);
        }

        for(long botId : botIds) {

            if (botId > 0) {

                BotConfigInfo botConfigInfo = botConfigInfoService.get(botId);

                if (!botConfigInfo.isActive()) {
                    response.getWriter().print("id=" + botConfigInfo.getId() + " is deactivated, skip it;");
                } else {
                    BotStatusResult status = botServiceClient.getStatusForNewBot(
                            botConfigInfo.getUsername(),
                            botConfigInfo.getPassword(),
                            botConfigInfo.getMqNickname(),
                            botConfigInfo.getBankId(),
                            GameType.BG_DRAGONSTONE.getGameId()
                    );

                    if (status.isSuccess()) {
                        botConfigInfo.setMqcBalance(status.getMqcBalance());
                        botConfigInfo.setMmcBalance(status.getMmcBalance());

                        botConfigInfoService.updateBalance(botId, status.getMmcBalance(), status.getMqcBalance());
                    }

                    response.getWriter().print("id=" + botConfigInfo.getId() + " success;");
                }
            } else {
                response.getWriter().print("id=" + botId + "bad id, skip it;");
            }
            response.setStatus(200);
        }
    } catch (Exception e) {
        response.setStatus(400);
        response.getWriter().print(e.getMessage());
    }
%>
