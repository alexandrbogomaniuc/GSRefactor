<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page import="com.dgphoenix.casino.common.web.BaseAction" %><%--
  Created by IntelliJ IDEA.
  User: quant
  Date: 07.11.18
  Time: 15:01
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<%
    String userId = request.getParameter("userId");
    if (StringUtils.isTrimmedEmpty(userId)) {
        throw new CommonException("Parameter 'userId' not found");
    }

    String extLoginTime = request.getParameter(BaseAction.PLAYER_LOGIN_TIME);
    if (StringUtils.isTrimmedEmpty(extLoginTime) || !org.apache.commons.lang.StringUtils.isNumeric(extLoginTime)) {
        throw new CommonException("Parameter '" + BaseAction.PLAYER_LOGIN_TIME + "' not found");
    }
    RemoteClientStubHelper.ExtAccountInfoStub accStub = RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy hh:ss:mm");

    response.getWriter().print("<EXTSYSTEM>" +
            "<REQUEST>" +
            "<USERID>" + userId + "</USERID>" +
            "<EXTLOGINTIME>" + extLoginTime + "</EXTLOGINTIME>" +
            "</REQUEST>" +
            "<TIME>" + dateFormatter.format(new Date()) + "</TIME>" +
            "<RESPONSE>" +
            "<RESULT>OK</RESULT>" +
            "<BET>" + accStub.getBet() + "</BET>" +
            "<WIN>" + accStub.getWin() + "</WIN>" +
            "</RESPONSE>" +
            "</EXTSYSTEM>");

%>