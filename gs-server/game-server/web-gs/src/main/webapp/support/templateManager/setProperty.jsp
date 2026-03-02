<%@ page import="com.abs.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.abs.casino.common.cache.data.game.*" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameConstants" %>
<%@ page import="com.abs.casino.common.cache.data.game.RoundFinishedHelper" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%!
    String fixAdditionalFlashvars(String src) {
        return src.replace("+", "=").replace("|", ";");
    }
%>
<%
    response.setHeader("Access-Control-Allow-Origin", "*");

    String id = request.getParameter("id");
    String key = request.getParameter("key");
    String value = request.getParameter("value");


    try {
        if (id != null) {
            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(Integer.parseInt(id));

            if (template != null) {
                if (key.equals("title")) template.setTitle(value);
                else if (key.equals("swfLocation")) template.setSwfLocation(value);
                else if (key.equals("gameControllerClass")) template.setGameControllerClass(value);
                else if (key.equals("roundFinishedHelper"))
                    template.setRoundFinishedHelper(RoundFinishedHelper.valueOf(value));
                else if (key.equals("endRoundSignature")) template.setEndRoundSignature(value);
                else if (key.equals("servlet")) template.setServlet(value);
                else if (key.equals("type")) template.getDefaultGameInfo().setGameType(GameType.valueOf(value));
                else if (key.equals("group")) template.getDefaultGameInfo().setGroup(GameGroup.valueOf(value));
                else if (key.equals("varType"))
                    template.getDefaultGameInfo().setVariableType(GameVariableType.valueOf(value));
                else if (key.equals("oldTranslation")) template.setOldTranslation(Boolean.parseBoolean(value));
                else if (key.equals(BaseGameConstants.KEY_ADDITIONAL_FLASHVARS)) {
                    template.getDefaultGameInfo().setProperty(BaseGameConstants.KEY_ADDITIONAL_FLASHVARS, fixAdditionalFlashvars(value));
                } else {
                    if (value.equals("[none]"))
                        template.getDefaultGameInfo().removeProperty(key);
                    else if (value.equalsIgnoreCase("null"))
                        template.getDefaultGameInfo().setProperty(key, null);
                    else
                        template.getDefaultGameInfo().setProperty(key, value);
                }

                RemoteCallHelper.getInstance().saveAndSendNotification(template);

                response.getWriter().write("SUCCESS");
            } else {
                response.getWriter().write("TEMPLATE=NULL");
            }
        }
    } catch (Exception ex) {
        response.getWriter().write("ERROR: " + ex.getMessage());
    }


%>




