<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.BackgroundImagesCache" %>
<%
    try {
        out.println(ApplicationContextHelper.getApplicationContext().getBean(BackgroundImagesCache.class));
    } catch (Throwable e) {
        e.printStackTrace(response.getWriter());
    }
%>
