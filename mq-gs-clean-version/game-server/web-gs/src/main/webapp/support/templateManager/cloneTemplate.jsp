<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankConstants" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>

<%@include file="GameTemplate.jsp" %>


<%!
    public void createTemplate(GameTemplate gameTemplate) throws CommonException {
        List<String> langs = Arrays.asList("en");

        String spGameProcessor = "com.dgphoenix.casino.gs.singlegames.tools.cbservtools.SPGameProcessor";

        BaseGameInfo gameInfo = new BaseGameInfo(gameTemplate.getValueId(), BankConstants.DEFAULT_BANK_ID, gameTemplate.getGameName(),
                gameTemplate.getValueGameType(), gameTemplate.getValueGameGroup(), gameTemplate.getValueGameVariableType(), null, spGameProcessor,
                null, null, gameTemplate.templateProperties, null, langs);

        BaseGameInfoTemplate template = new BaseGameInfoTemplate(gameTemplate.getValueId(), gameTemplate.getGameName(), null, gameInfo,
                gameTemplate.getValueIsJackpot(), gameTemplate.getServlet());
        template.setTitle(gameTemplate.getTitle());
        template.setSwfLocation(gameTemplate.getSwfLocation());
        template.setAdditionalParams("");
        template.setOldTranslation(false);
        template.setGameControllerClass(gameTemplate.getGameControllerClass());
        template.setRoundFinishedHelper(gameTemplate.getValueRoundFinishedHelper());
        template.setEndRoundSignature(gameTemplate.getEndRoundSignature());
        template.setServlet(gameTemplate.getServlet());

        BaseGameInfo info = template.getDefaultGameInfo();

        for (String key : gameTemplate.templateProperties.keySet()) {
            String value = gameTemplate.templateProperties.get(key);
            if (value != null && value.equals("null")) value = null;
            if (value != null && key.equals(BaseGameConstants.KEY_ADDITIONAL_FLASHVARS)) {
                value = value.replace("+", "=").replace("|", ";");
            }
            info.setProperty(key, value);
        }

        BaseGameInfoTemplateCache.getInstance().put(template);
        RemoteCallHelper.getInstance().saveAndSendNotification(template);
    }
%>

<%
    response.setHeader("Access-Control-Allow-Origin", "*");

    String strId = request.getParameter("id");
    String strResult = request.getParameter("result");

    try {
        if (strId != null) {
            GameTemplate gameTemplate = new GameTemplate(response.getWriter(), strResult);

            createTemplate(gameTemplate);

            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(Integer.parseInt(strId));

            if (template != null) {
                response.getWriter().write("SUCCESS: " + gameTemplate.toString());
            } else {
                response.getWriter().write("ERROR: " + gameTemplate.toString());
            }
        }
    } catch (Exception ex) {
        response.getWriter().write("EXCEPTION: " + ex.getMessage());
    }
%>




