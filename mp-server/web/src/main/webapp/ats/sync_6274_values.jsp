<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="java.util.*" %>
<%@ page import="com.hazelcast.util.StringUtil" %>
<%@ page import="static com.betsoft.casino.mp.model.bots.BotConfigInfo.MMC_BankId" %>

<%
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);

    try {
        String allowedValuesParam = request.getParameter("allowedValues");
        allowedValuesParam = allowedValuesParam.replace("[", "");
        allowedValuesParam = allowedValuesParam.replace("]", "");
        allowedValuesParam = allowedValuesParam.replace(" ", "");

        Set<Long> allowedValuesSet = new HashSet<>();
        if(!StringUtil.isNullOrEmpty(allowedValuesParam)) {
            if(allowedValuesParam.contains(",")) {
                for (String allowedValueStr : allowedValuesParam.split(",")) {
                    long allowedValue = Long.parseLong(allowedValueStr);
                    allowedValuesSet.add(allowedValue);
                }
            } else {
                long allowedValue = Long.parseLong(allowedValuesParam);
                allowedValuesSet.add(allowedValue);
            }
        }

        String botIdsParam = request.getParameter("botIds");

        Set<Long> paramsBotIds = new HashSet<>();
        StringTokenizer gamesTokenizer = new StringTokenizer(botIdsParam, ",");
        while (gamesTokenizer.hasMoreTokens()) {
            String botIdParam = gamesTokenizer.nextToken();
            long botId = Long.parseLong(botIdParam);
            paramsBotIds.add(botId);
        }

        Collection<BotConfigInfo> allBotConfigInfos = botConfigInfoService.getAll();

        //response.getWriter().print( "BotIds:");

        if (allBotConfigInfos != null && !allBotConfigInfos.isEmpty()) {

            for (BotConfigInfo botConfigInfo : allBotConfigInfos) {

                long botId = botConfigInfo.getId();
                if(paramsBotIds.contains(botId)) {
                    response.getWriter().print( ", " + botId);
                    botConfigInfo.putAllowedRoomValuesSet(MMC_BankId, allowedValuesSet);
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
