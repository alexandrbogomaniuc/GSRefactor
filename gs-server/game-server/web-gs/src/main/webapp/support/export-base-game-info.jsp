<%@ page import="java.io.File" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="java.io.BufferedWriter" %>
<%@ page import="java.io.FileWriter" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    long now = System.currentTimeMillis();
    File file = new File("/www/logs/tomcat.gs1.sb/export-basegameinfo.txt");
    BufferedWriter writer = null;
    try {
        writer = new BufferedWriter(new FileWriter(file));
        Set<Map.Entry<String, IBaseGameInfo>> entries = BaseGameCache.getInstance().getAllObjects().entrySet();
        for (Map.Entry<String, IBaseGameInfo> entry : entries) {
            IBaseGameInfo gameInfo = entry.getValue();
            writer.write(" info=BaseGameCache.getInstance().getObject(\"" + entry.getKey() + "\");\n");
            writer.write("if(info != null) {\n");
            List<String> languages = gameInfo.getLanguages();
            for (String lang : languages) {
                writer.write("info.addLanguage(\"" + lang + "\");\n");
            }
            writer.write("}\n");
        }
        writer.flush();
    } catch (Exception e) {
        response.getWriter().println("Export langs error:" + e);
        e.printStackTrace(response.getWriter());
        // TODO Auto-generated catch block
        e.printStackTrace();
    } finally {
        try {
            if (writer != null)
                writer.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    response.getWriter().print("\nOK, script time = " + (System.currentTimeMillis() - now));
    response.flushBuffer();
%>