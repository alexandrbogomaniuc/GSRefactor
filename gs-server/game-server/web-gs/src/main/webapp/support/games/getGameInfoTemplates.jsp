<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%

    java.util.Collection<BaseGameInfoTemplate> templates = new ArrayList<>();

    String gameIdAsString = request.getParameter("gameId");

    if(!StringUtils.isTrimmedEmpty(gameIdAsString)) {

        long gameId = Long.parseLong(gameIdAsString);
        BaseGameInfoTemplate baseGameInfoTemplateById =
                BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);

        if(baseGameInfoTemplateById != null) {
            templates.add(baseGameInfoTemplateById);
        }

    } else {
        Map<Long, BaseGameInfoTemplate> templatesMap = BaseGameInfoTemplateCache.getInstance().getAllObjects();
        templates = templatesMap.values();
    }

    for(BaseGameInfoTemplate template : templates) {
        response.getWriter().println(template.toString());
        response.getWriter().println("</br>");
    }
%>