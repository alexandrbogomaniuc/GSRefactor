<%@ page import="org.apache.commons.io.IOUtils" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.util.List" %>
<%
    String path = config.getServletContext().getRealPath("META-INF/MANIFEST.MF");
    try {
        List<String> lines = IOUtils.readLines(new FileInputStream(path));
        for (String line : lines) {
            if (line.startsWith("Implementation-Version")) {
                out.println(line.split(":")[1].trim());
                return;
            }
        }
    } catch (Exception e) {
        out.println(0);
    }
%>
