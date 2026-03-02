<%@ page import="com.abs.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="java.util.*" %>
<%@include file="GameTemplate.jsp" %>

<%!
    PrintWriter writer = null;

    String[] parseStringArray(String strArray) {
        return strArray.replaceAll(", ", " ").replaceAll(",", " ").split(" ");
    }

    Integer[] createArray(int min, int max) {
        Integer[] array = new Integer[max - min];
        for (int i = 0; i < array.length; i++)
            array[i] = min + i;

        return array;
    }

    Integer[] getGameArray(String type, String custom) {
        if (type.equals("STANDARD")) {
            return createArray(0, 9999);
        } else if (type.equals("AGCC")) {
            return createArray(20000, 29999);
        } else if (type.equals("CLONES")) {
            return createArray(30000, 39999);
        } else if (type.equals("LGA")) {
            return createArray(40000, 49999);
        } else if (type.equals("AAMS")) {
            return createArray(50000, 59999);
        } else if (type.equals("ALL")) {
            return createArray(0, 100000);
        } else if (type.equals("CUSTOM")) {
            String[] strIndexArray = parseStringArray(custom);
            Integer[] arrayGames = new Integer[strIndexArray.length];

            for (int i = 0; i < strIndexArray.length; i++) {
                arrayGames[i] = Integer.parseInt(strIndexArray[i]);
            }

            return arrayGames;
        }

        return null;
    }

    ;

    public HashMap<Long, GameTemplate> getTemplates(PrintWriter writer, Integer[] arrayGame) {
        HashMap<Long, GameTemplate> mapGameTemplate = new HashMap<Long, GameTemplate>();

        for (long gameId : arrayGame) {
            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);

            if (template != null) {
                GameTemplate gameTemplate = new GameTemplate(writer, template);
                mapGameTemplate.put(gameId, gameTemplate);
            }
        }

        return mapGameTemplate;
    }
%>

<%
    writer = response.getWriter();

    String type = request.getParameter("type");
    String custom_games = request.getParameter("custom_games");

    Integer[] arrayGame = getGameArray(type, custom_games);

    HashMap<Long, GameTemplate> mapGameTemplate = getTemplates(response.getWriter(), arrayGame);

    String result = "";
    for (Long key : mapGameTemplate.keySet()) {
        GameTemplate gameTemplate = mapGameTemplate.get(key);
        result += gameTemplate.toString() + "{@###@}";
    }

    if (result.endsWith("{@###@}"))
        result = result.substring(0, result.length() - "{@###@}".length());

    response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(result);
%>
