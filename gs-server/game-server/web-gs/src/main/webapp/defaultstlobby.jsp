<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.abs.casino.actions.lobby.STLobbyAction" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.GameGroup" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<%
    Map<GameGroup, List<IBaseGameInfo>> gamesMap = null;

    try {
        gamesMap = (Map<GameGroup, List<IBaseGameInfo>>) request.getAttribute(STLobbyAction.GAMES);
    } catch (Throwable ex) {
        ThreadLog.error(ex.getMessage(), ex);
    }

%>
<logic:present name="AVAILABLE_MODE" scope="request">
    <div align="right" style="nowrap: nowrap; font-weight: bold;">
        Game Mode:
        <html:select property="currentMode" name="STLobbyForm" onchange="preparedSubmit(); submit();"
                     style="font-weight: bold;">
            <html:optionsCollection name="AVAILABLE_MODE" value="key" label="name"/>
        </html:select>
    </div>
</logic:present>

<%
    if (gamesMap == null) {%>
<span>Games Not Found</span>
<%} else {%>
<table width="100%">

    <%
        int columnsPerPage = gamesMap.size();
        String width = columnsPerPage > 0 ? (100 / columnsPerPage) + "%" : "100%";
    %>

    <tr><%
        Iterator it = gamesMap.keySet().iterator();
        while (it.hasNext()) {
            GameGroup group = (GameGroup) it.next();
    %>
        <td nowrap="nowrap" width="<%=width%>"><b><%=group.getGroupName()%>
        </b></td>
        <%
            }
        %>
    </tr>

    <%
        int size = gamesMap.get(GameGroup.SLOTS).size();
        for (int i = 0; i < size; i += 1) {
    %>

    <tr><%
        for (Map.Entry<GameGroup, List<IBaseGameInfo>> entry : gamesMap.entrySet()) {
            List<IBaseGameInfo> list = entry.getValue();
            IBaseGameInfo gameInfo;
            try {
                gameInfo = list.get(i);
    %>
        <td nowrap="nowrap" width="<%=width%>">
            <a href="#" onclick="openGame(<%=gameInfo.getId()%>);"><%=gameInfo.getLocalizedName(request.getLocale())%>
            </a>
        </td>
        <%
        } catch (Throwable th) {%>
        <td>&nbsp;</td>
        <%
                }
            }
        %>
    </tr>
    <%
        }
    %>
</table>
<%
    }
%>