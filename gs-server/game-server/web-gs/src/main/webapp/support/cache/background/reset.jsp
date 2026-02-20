<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.BackgroundImagesCache" %>
<%
    try {
        ApplicationContextHelper.getApplicationContext().getBean(BackgroundImagesCache.class).invalidate();
    } catch (Throwable e) {
        e.printStackTrace(response.getWriter());
    }
%>
