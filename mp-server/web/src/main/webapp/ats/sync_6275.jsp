<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.Collection" %>

<%
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);

    try {
        String botIdsParam = request.getParameter("botIds");

        Set<Long> paramsBotIds = new HashSet<>();
        StringTokenizer gamesTokenizer = new StringTokenizer(botIdsParam, ",");
        while (gamesTokenizer.hasMoreTokens()) {
            String botIdParam = gamesTokenizer.nextToken();
            long botId = Long.parseLong(botIdParam);
            paramsBotIds.add(botId);
        }

        Collection<BotConfigInfo> allBotConfigInfos = botConfigInfoService.getAll();

        response.getWriter().print( "BotIds:");

        if (allBotConfigInfos != null && !allBotConfigInfos.isEmpty()) {

            for (BotConfigInfo botConfigInfo : allBotConfigInfos) {

                long botId = botConfigInfo.getId();
                response.getWriter().print( ", " + botId);
                if(paramsBotIds.contains(botId)) {
                    botConfigInfo.addAllowedBankId(6275L);
                    response.getWriter().print( " set 6275L");
                } else {
                    botConfigInfo.removeAllowedBankId(6275L);
                    response.getWriter().print(" remove 6275L");
                }

                botConfigInfoService.update(botId,
                        botConfigInfo.getAllowedGames(),
                        botConfigInfo.isActive(),
                        botConfigInfo.getPassword(),
                        botConfigInfo.getMqNickname(),
                        botConfigInfo.getAvatar(),
                        botConfigInfo.getTimeFrames(),
                        botConfigInfo.getAllowedBankIds(),
                        botConfigInfo.getShootsRates(),
                        botConfigInfo.getBulletsRates(),
                        botConfigInfo.getAllowedRoomValues());
            }

            response.setStatus(200);
        }
    } catch (Exception e) {
        response.setStatus(400);
        response.getWriter().print(e.getMessage());
    }
%>
