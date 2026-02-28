<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.abs.casino.common.web.BaseAction" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/casino.tld" prefix="casino" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<jsp:include page="error_header.jsp"/>
<%
    String message = "\u8d26\u53f7\u5f02\u5e38\u6216\u88ab\u9501\u5b9a\uff0c\u8bf7\u8054\u7cfb\u5728\u7ebf\u5ba2\u670d\u3002";
%>
<br/><br/>
<div id="center" align=center>
    <ul>
        <li><span style="color: black"><%=message%></span></li>
    </ul>
</div>

<br/><br/><br/><br/>
<jsp:include page="error_footer.jsp"/>