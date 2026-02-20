<%
    String userId = request.getParameter("userId");
%>
<EXTSYSTEM>
    <REQUEST>
        <USERID><%=userId %>
        </USERID>
        <HASH>13124234234234234234234</HASH>
    </REQUEST>
    <TIME>12 Jan 2004 15:15:15</TIME>
    <RESPONSE>
        <RESULT>OK</RESULT>
        <FIRSTNAME>BonusFirst</FIRSTNAME>
        <LASTNAME>BonusLast</LASTNAME>
        <EMAIL>BonusEmail</EMAIL>
        <USERNAME><%=userId %>
        </USERNAME>
        <CURRENCY>GBP</CURRENCY>
    </RESPONSE>
</EXTSYSTEM>
