<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.common.cache.BackgroundImagesCache" %>
<%
    try {
        ApplicationContextHelper.getApplicationContext().getBean(BackgroundImagesCache.class).invalidate();
    } catch (Throwable e) {
        e.printStackTrace(response.getWriter());
    }
%>
