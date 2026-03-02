<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.abs.casino.common.cache.BaseGameCache" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    long now = System.currentTimeMillis();
    BaseGameInfo info;
    //begin insert code from export file

    //end insert code from export file
    response.getWriter().print("OK, script time = " + (System.currentTimeMillis() - now));
    response.flushBuffer();
%>