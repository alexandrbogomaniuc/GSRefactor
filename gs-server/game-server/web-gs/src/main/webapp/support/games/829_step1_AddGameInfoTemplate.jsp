<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankConstants" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Limit" %>
<%@ page import="java.util.*" %>
<%
    BaseGameInfoTemplate template;
    BaseGameInfo gameInfo;

    long gameId = 829;
    String gameBaseName = "REVENGEOFRA";
    String servletBaseName = "/MQ_RevengeRa";
    String titleBaseName = "Max Quest: Rise of the Mummy";
    String swfLocation = "/html5pc/actiongames/revengeofra/";
    String rtp = "97.5";
    String additionalParams = "";

    HashMap<String, String> baseProperties = new HashMap();
    baseProperties.put("ISENABLED", "TRUE");
    baseProperties.put("KEY_ACS_ENABLED", "FALSE");
    baseProperties.put("LGA_APPROVED", "TRUE");
    baseProperties.put("CDN_SUPPORT", "TRUE");
    baseProperties.put("JACKPOT3_GAME", "FALSE");
    baseProperties.put("DEVELOPMENT_VERSION", "FALSE");
    baseProperties.put("RTP", rtp);
    baseProperties.put("DEFCOIN", "2");
    baseProperties.put("MAX_WIN", "25000");
    baseProperties.put(BaseGameConstants.KEY_SINGLE_GAME_ID_FOR_ALL_PLATFORMS, "TRUE");

    Map<String, String> properties = new HashMap(baseProperties);

    String spGameProcessor = "com.abs.casino.gs.singlegames.tools.cbservtools.SPGameProcessor";
    try {
        Class.forName(spGameProcessor);
    } catch (ClassNotFoundException ignore) {
        spGameProcessor = "com.dgphoenix.casino.gs.singlegames.tools.cbservtools.SPGameProcessor";
    }
    List langs = Arrays.asList("en");

    gameInfo = new BaseGameInfo(
            gameId,
            BankConstants.DEFAULT_BANK_ID,
            gameBaseName,
            GameType.MP,
            GameGroup.ACTION_GAMES,
            GameVariableType.COIN,
            null,
            spGameProcessor,
            null,
            new ArrayList<>(),
            properties,
            null,
            langs);

    template = new BaseGameInfoTemplate(gameId, gameBaseName, null, gameInfo, false, servletBaseName + ".game");
    template.setTitle(titleBaseName);
    template.setSwfLocation(swfLocation);
    template.setAdditionalParams(additionalParams);
    template.setFrbGame(true);

    BaseGameInfoTemplateCache.getInstance().put(template);

    RemoteCallHelper.getInstance().saveAndSendNotification(template);

    BaseGameInfoTemplateCache.getInstance().addFrbGame(gameId);

    response.getWriter().println(template.toString());
    response.getWriter().println("</br>");

%>
