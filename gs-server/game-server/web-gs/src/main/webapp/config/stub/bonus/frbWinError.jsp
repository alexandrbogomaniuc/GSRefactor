<%
    String userId = request.getParameter("userId");
    String bonusId = request.getParameter("bonusId");
    String amount = request.getParameter("amount");
    String transactionId = request.getParameter("transactionId");
%>


<EXTSYSTEM>
    <REQUEST>
        <USEDID><%=userId%>
        </USEDID>
        <BONUSID><%=bonusId%>
        </BONUSID>
        <AMOUNT><%=amount%>
        </AMOUNT>
        <TRANSACTIONID><%=transactionId%>
        </TRANSACTIONID>
        <HASH>1e3b0ae551b1dfdc48137bc50ad26d1c</HASH>
    </REQUEST>
    <TIME>18 Mar 2011 12:13:44</TIME>
    <RESPONSE>
        <RESULT>ERROR</RESULT>
        <CODE>699</CODE>
    </RESPONSE>
</EXTSYSTEM>

