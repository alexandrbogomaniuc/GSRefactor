<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%
    String sversion = (String) request.getAttribute("sversion");
    response.getWriter().write("SVERSION=" + sversion);
%>