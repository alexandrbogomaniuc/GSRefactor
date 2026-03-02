<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.abs.casino.common.cache.data.session.ClientType" %>
<%@ page import="com.abs.casino.common.web.BaseAction" %>
<%@ page import="com.abs.casino.common.web.ClientTypeFactory" %>
<%@ page import="com.abs.casino.gs.GameServer" %>
<%@ page import="com.abs.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.common.config.HostConfiguration" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/casino.tld" prefix="casino" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<jsp:include page="error_header.jsp"/>
<%
    String fontColor = "black";
    if (request.getServerName().startsWith("phc-")) {
        fontColor = "white";
    }
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    String time = formatter.format(new Date());
    Integer serverId = GameServer.getInstance().getServerId();
%>
<br/><br/>
<%
    GameServerConfiguration serverConfiguration = ApplicationContextHelper.getBean(GameServerConfiguration.class);
    String brandName = serverConfiguration.getBrandName().toLowerCase();
    HostConfiguration hostConfiguration = ApplicationContextHelper.getBean(HostConfiguration.class);
    String clusterName = hostConfiguration.getClusterName();
    String clusterType = hostConfiguration.getClusterType().getStringRepresentation();

%>
<logic:present name="<%=Globals.ERROR_KEY%>">
    <logic:messagesPresent property="blocking_country">
        <style type="text/css">
            html {
                overflow: visible !important;
                background: #575757;
                height: 100%;
                background: linear-gradient(to top, #000, #8a8a8a) no-repeat center center fixed;
                -webkit-background-size: cover;
                -moz-background-size: cover;
                -o-background-size: cover;
                background-size: cover;
            }
            body {
                background-color: transparent;
                color: white;
                margin: 0;
                height: 100%;
                font-family: Tahoma, Geneva, Kalimati, sans-serif;
                overflow: auto;
            }
            #center {color: white;}
            ul {
                list-style: none;
            }
            #main_container {
                background: #3b3b3b;
                background: linear-gradient(to top, #1a1a1a, #494949);
                height: 475px;
                width: 915px;
                border-radius: 20px;
            }
            #white_indent_container {
                height: 8px;
            }
            #white_container {
                background: white;
                height: 455px;
                width: 895px;
                border-radius: 15px;
                padding: 3px 0 0 0;
            }
            #black_indent_container {
                height: 7px;
            }
            #black_container {
                background: black;
                height: 435px;
                width: 875px;
                border-radius: 10px;
                padding: 3px 0 0 0;
            }
            #sorry_indent_container {
                height: 50px;
            }
            #sorry_container {
                color: #ffff00;
                font-size: 140px;
            }
            #gradient_sorry_text {
                background: url(/error_pages/gr.png) repeat-x;
                position: absolute;
                display: block;
                height: 175px;
                background-size: 100% 100%;
                width: 700px;
            }

            #main_text_indent_container {
                height: 20px;
            }
            #main_text {
                color: white;
                font-size: 30px;
                width: 760px;
            }
            #logo_icon {
                background: url("/error_pages/<%=brandName%>_logo.png");
                height: 69px;
                width: 176px;
                margin-top: -30px;
            }
            li span {color: white !important;}
        </style>
        <div id="center" align=center>
            <div id="main_container">
                <div id="white_indent_container">
                </div>
                <div id="white_container">
                    <div id="black_indent_container">
                    </div>
                    <div id="black_container">
                        <div id="sorry_indent_container">
                        </div>
                        <div id="sorry_container">
                            <span id="gradient_sorry_text"></span>Sorry!
                        </div>
                        <div id="main_text_indent_container">
                        </div>
                        <div id="main_text">
                            <logic:messagesPresent message="false">
                                <ul>
                                    <html:messages id="error" message="false">
                                        <li><span style="color: black"><bean:write name="error"/></span></li>
                                    </html:messages>
                                </ul>
                            </logic:messagesPresent>
                        </div>
                    </div>
                </div>
                <div id="logo_icon"></div>
            </div>
        </div>
    </logic:messagesPresent>
    <logic:messagesNotPresent property="blocking_country">
        <div id="center" align=center>
            <logic:messagesPresent message="false">
                <html:messages id="error" message="false">
                    <div><bean:write name="error"/></div>
                    <% if (request.getAttribute(BaseAction.SUPPORT_TICKET_ID_ATTRIBUTE) != null) { %>
                    <div>Time: <%=time%>
                    </div>
                    <div>
                        Support ticket ID: G<%=serverId%> [<%=clusterType%>/<%=clusterName%>
                        ]: <%=request.getAttribute(BaseAction.SUPPORT_TICKET_ID_ATTRIBUTE)%>
                    </div>
                    <% } %>
                    <% if (request.getAttribute(BaseAction.ERROR_INFO_ATTRIBUTE) != null) { %>
                    <div><%=request.getAttribute(BaseAction.ERROR_INFO_ATTRIBUTE)%>
                    </div>
                    <% } %>
                </html:messages>
            </logic:messagesPresent>
        </div>
    </logic:messagesNotPresent>

</logic:present>

<logic:notPresent name="<%=Globals.ERROR_KEY%>">
    <div id="center" align=center>
        <div><bean:message key="error.internal"/></div>
        <div><bean:message key="error.time"/>: <%=time%>
        </div>
        <% if (request.getAttribute(BaseAction.SUPPORT_TICKET_ID_ATTRIBUTE) != null) { %>
        <div>
            Support ticket ID: G<%=serverId%>[<%=clusterType%>/<%=clusterName%>]: <%=request.getAttribute(BaseAction.SUPPORT_TICKET_ID_ATTRIBUTE)%>
        </div>
        <% } %>
    </div>
</logic:notPresent>

<br/><br/><br/><br/>
<jsp:include page="error_footer.jsp"/>