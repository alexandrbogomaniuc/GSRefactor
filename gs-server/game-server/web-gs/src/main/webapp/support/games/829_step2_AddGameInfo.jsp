<%@ page import="com.dgphoenix.casino.common.cache.CurrencyCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.abs.casino.bgm.BaseGameHelper" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.*" %>
<%@ page import="java.util.ArrayList" %>
<%

    String spGameProcessor = "com.abs.casino.gs.singlegames.tools.cbservtools.SPGameProcessor";
    try {
        Class.forName(spGameProcessor);
    } catch (ClassNotFoundException ignore) {
        spGameProcessor = "com.dgphoenix.casino.gs.singlegames.tools.cbservtools.SPGameProcessor";
    }
    long[] banks = {6274, 6275};
    long gameId = 829;
    String gameBaseName = "REVENGEOFRA";
    String rtp = "97.5";

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

    for (long bankId : banks) {

        Map<String, String> properties = new HashMap(baseProperties);

        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

            if (bankInfo != null) {

                Currency defaultCurrency = bankInfo.getDefaultCurrency();

                //BaseGameCache.getInstance().remove(bankId, gameId, defaultCurrency);

                BaseGameHelper.createGame(
                        bankId,
                        gameId,
                        defaultCurrency,
                        gameBaseName,
                        GameType.MP,
                        GameGroup.ACTION_GAMES,
                        GameVariableType.COIN,
                        null,
                        spGameProcessor,
                        properties,
                        null,
                        new ArrayList<>(),
                        false,
                        null,
                        null
                );
            }

        } catch (CommonException e) {
            e.printStackTrace();
        }
    }

%>
