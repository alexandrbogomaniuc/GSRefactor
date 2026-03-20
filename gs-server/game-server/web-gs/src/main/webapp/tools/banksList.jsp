<%--
  Created by IntelliJ IDEA.
  User: quant
  Date: 28.09.16
  Time: 14:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String subcasinoId = request.getParameter("subcasinoId");
    if (subcasinoId == null || subcasinoId.trim().length() == 0) {
        request.getRequestDispatcher("/support/cache/bank/common/subcasinoSelect.jsp").forward(request, response);
        return;
    }
    request.getRequestDispatcher("/support/cache/bank/common/banksList.jsp").forward(request, response);
%>
