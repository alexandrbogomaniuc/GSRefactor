<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="java.util.*" %>
<%@ page import="com.hazelcast.util.StringUtil" %>

<%
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);

    try {
        String bulletsRatesParam = request.getParameter("bulletsRates");
        bulletsRatesParam = bulletsRatesParam.replace("{", "");
        bulletsRatesParam = bulletsRatesParam.replace("}", "");
        bulletsRatesParam = bulletsRatesParam.replace(" ", "");

        Map<Long, Double> bulletsRates = new HashMap<>();
        if(!StringUtil.isNullOrEmpty(bulletsRatesParam) && bulletsRatesParam.contains(",") && bulletsRatesParam.contains("=")) {
            for (String entry : bulletsRatesParam.split(",")) {
                String[] parts = entry.split("=");
                Long key = Long.parseLong(parts[0]);
                Double value = Double.parseDouble(parts[1]);
                bulletsRates.put(key, value);
            }
        }

        String botIdsParam = request.getParameter("botIds");

        Set<Long> paramsBotIds = new HashSet<>();
        StringTokenizer botIdTokenizer = new StringTokenizer(botIdsParam, ",");
        while (botIdTokenizer.hasMoreTokens()) {
            String botIdParam = botIdTokenizer.nextToken();
            long botId = Long.parseLong(botIdParam);
            paramsBotIds.add(botId);
        }

        Collection<BotConfigInfo> allBotConfigInfos = botConfigInfoService.getAll();

        response.getWriter().print( "BotIds:");

        if (allBotConfigInfos != null && !allBotConfigInfos.isEmpty()) {

            for (BotConfigInfo botConfigInfo : allBotConfigInfos) {

                long botId = botConfigInfo.getId();
                if(paramsBotIds.contains(botId)) {
                    botConfigInfoService.update(botId,
                            botConfigInfo.getAllowedGames(),
                            botConfigInfo.isActive(),
                            botConfigInfo.getPassword(),
                            botConfigInfo.getMqNickname(),
                            botConfigInfo.getAvatar(),
                            botConfigInfo.getTimeFrames(),
                            botConfigInfo.getAllowedBankIds(),
                            botConfigInfo.getShootsRates(),
                            bulletsRates,
                            botConfigInfo.getAllowedRoomValues());

                    response.getWriter().print( ", " + botId);
                }
            }

            response.setStatus(200);
        }
    } catch (Exception e) {
        response.setStatus(400);
        response.getWriter().print(e.getMessage());
    }
%>
