<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="java.util.*" %>
<%@ page import="com.betsoft.casino.mp.transport.Avatar" %>
<%@ page import="com.dgphoenix.casino.common.util.RNG" %>
<%@ page import="com.betsoft.casino.mp.web.socket.BotServiceClient" %>
<%@ page import="com.abs.casino.kafka.dto.bots.response.BotStatusResponse" %>
<%@ page import="com.betsoft.casino.mp.model.IAvatar" %>
<%
    try {
        BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);
        BotServiceClient botServiceClient = WebSocketRouter.getApplicationContext().getBean(BotServiceClient.class);

        int botCount = Integer.parseInt(request.getParameter("countCreate"));
        int initialBotId = Integer.parseInt(request.getParameter("initialBotId"));
        String botUsernamePrefix = request.getParameter("botUsernamePrefix");
        String botNicknamePrefix = request.getParameter("botNicknamePrefix");
        String botNicknameSuffix = request.getParameter("botNicknameSuffix");
        String emailDomain = request.getParameter("emailDomain");
        String botPassword = request.getParameter("botPassword");


        long bankId = 6274L;
        for (int i = initialBotId; i < botCount + initialBotId; i++) {
            String usernameParam = botUsernamePrefix + i + emailDomain;
            String mqNickNameParam = botNicknamePrefix + i + botNicknameSuffix;
            IAvatar avatar = new Avatar(RNG.nextInt(2), RNG.nextInt(2), RNG.nextInt(2));
            BotConfigInfo botConfigInfo = new BotConfigInfo(0, bankId, availableMQGames, true, usernameParam, botPassword, mqNickNameParam, avatar);
            BotStatusResponse status = botServiceClient.getStatusForNewBot(botConfigInfo.getUsername(), botConfigInfo.getPassword(),
                    botConfigInfo.getMqNickname(), bankId, GameType.BG_DRAGONSTONE.getGameId());
            botConfigInfo.setMqcBalance(status.getMqcBalance());
            botConfigInfo.setMmcBalance(status.getMmcBalance());
            botConfigInfoService.create(botConfigInfo);
        }

        response.setStatus(200);
        response.getWriter().print("Ats was successfully created");
    } catch (Exception e) {
        response.setStatus(400);
        response.getWriter().print(e.getMessage());
    }

%>