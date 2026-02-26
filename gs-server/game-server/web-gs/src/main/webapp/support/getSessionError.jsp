<%@ page import="com.abs.casino.support.SessionErrorsCache" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<html>
<head>
    <title>Get Session Error</title>
</head>
</html>

<SCRIPT>

    function isCorrectValue(event, input) {
        var key, ctrl;

        if (window.event) {
            key = window.event.keyCode;
            ctrl = window.event.ctrlKey;
        } else {
            key = event.which;
            ctrl = event.ctrlKey;
        }

        if ((key == 46) || (key == 8)) return true; // delete, backspace, TAB
        if ((key >= 48) && (key <= 57)) return true;    // 0..9
        if ((key >= 65) && (key <= 90)) return true;    // a..z
        if (((key == 86) || (key == 67) || (key == 88) || (key == 89) || (key == 90)) && ctrl) return true; // X C V Y Z

        return false;
    }
</SCRIPT>

<%
    String strSessionID = request.getParameter("session_id");
    if (strSessionID == null) strSessionID = "";
%>

<FORM ACTION="getSessionError.jsp" METHOD="GET">
    Вывод всех ошибок из Session ID(Support Ticket ID):
    <INPUT style="text-align: left;   width: 230px" type="text" name="session_id" onKeyDown="return isCorrectValue(event, this);" value="<%=strSessionID%>"/>
    <INPUT style="width: 80px" type="submit" value="ОК" value="submit"/>
</FORM>

<%!
    String format(String error) {
        return error
                .replace("\n", "<br>")
                .replace("&", "<br>&nbsp&nbsp")
                .replace("sessionID:", "<b>SessionID:</b>")
                .replace("walletState:", "<b>Wallet State:</b>")
                .replace("sessionID:", "<b>SessionID:")
                .replace("login:", "<b>Login:</b>")
                .replace("timeStamp:", "<b>TimeStamp:</b>")
                .replace("game name:", "<b>GameName:</b>")
                .replace("game id:", "<b>GameID:</b>")
                .replace("date:", "<b>Date:</b>")
                .replace("thread:", "<b>Thread:</b>")
                .replace("gameServerID:", "<b>GameServerID:</b>")
                .replace("stackTrace:", "<b>StackTrace:</b>")
                .replace("userRequest: ", "<b>User Request:</b><br>&nbsp&nbsp")
                .replace("\t", "&nbsp&nbsp")
                .replace("<br><br><br>", "<br><br>");
    }
%>

<%
    if ((request.getParameter("session_id") != null)) {
        strSessionID = strSessionID.trim();

        if (strSessionID.equals("")) // If parameter not correct
        {
%>
<DIV style="font-weight: bold; color: #ff0000;">ОШИБКА! Необходимо ввести ID сессии(Support Ticket ID)</DIV>
<%
} else {
    long time = System.currentTimeMillis();

    Iterable<String> errors = SessionErrorsCache.getInstance().getSessionErrors(strSessionID);
%>
Время обработки запроса: <%=((System.currentTimeMillis() - time) / 1000.0)%> сек. <br>

<HR>

<%if (!errors.iterator().hasNext()) { %> <b>Ничего не найдено</b><br> <%
    }

    for (String error : errors) {
        error = format(error);
%><%=error%>
<br><br><br><br>--------------------------------------------------------------------------------------------------------------------------------------------------<br><br><br><br>
<%
            }
        }
    }
%>
