<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="java.util.*" %>

<%
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);

    try {

        Collection<BotConfigInfo> botConfigInfos = botConfigInfoService.getAll();

        for (BotConfigInfo botConfigInfo : botConfigInfos) {
            String mqNickname = botConfigInfo.getMqNickname();
            mqNickname = mqNickname.replace("by", "By");

            botConfigInfoService.update(
                    botConfigInfo.getId(),
                    botConfigInfo.getAllowedGames(),
                    botConfigInfo.isActive(),
                    botConfigInfo.getPassword(),
                    mqNickname,
                    botConfigInfo.getAvatar(),
                    botConfigInfo.getTimeFrames(),
                    botConfigInfo.getAllowedBankIds());

        }

        response.setStatus(200);
        response.getWriter().print("mqNickname was successfully updated by to By for all Ats");

    } catch (Exception e) {
        response.setStatus(400);
        response.getWriter().print(e.getMessage());
    }
%>
