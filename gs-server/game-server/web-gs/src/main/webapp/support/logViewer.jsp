<%@ page import="com.dgphoenix.casino.common.util.support.*" %>
<%@ page import="com.abs.casino.support.logviewer.LogViewerForm" %>
<%@ page import="javax.xml.transform.Source" %>
<%@ page import="javax.xml.transform.Transformer" %>
<%@ page import="javax.xml.transform.TransformerFactory" %>
<%@ page import="javax.xml.transform.stream.StreamResult" %>
<%@ page import="static org.apache.commons.lang3.StringUtils.substringAfter" %>
<%@ page import="static com.dgphoenix.casino.common.util.support.AdditionalInfoAttribute.EXTERNAL_ID" %>
<%@ page import="static com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister.ID_DELIMITER" %>
<%@ page import="javax.xml.transform.stream.StreamSource" %>
<%@ page import="static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION" %>
<%@ page import="static javax.xml.transform.OutputKeys.INDENT" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="static org.apache.commons.lang3.StringUtils.substringBefore" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%!
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyy HH:mm:ss,S");

    private static String formatTime(long time) {
        return FORMAT.format(new Date(time));
    }

    private static String formatHeaders(Map<String, String> headers) {
        if (headers == null) {
            return "";
        }
        StringBuilder headersBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headersBuilder
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("<br/>");
        }
        return headersBuilder.toString();
    }

    private static String formatXmlForHtml(String xml) {
        return xml.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    public static String prettyFormat(String input) {
        try {
            String xml = removeWhitespacesBetweenTags(input);
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            return "Cannot parse XML: " + e.getMessage();
        }
    }

    private static String removeWhitespacesBetweenTags(String xml) {
        return xml.replaceAll(">\\s*<", "><");
    }
%>

<html>
<head>
    <title>Log Viewer</title>
    <link rel="stylesheet" href="css/bootstrap.min.css">
    <script type="text/javascript" src="js/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src="js/bootstrap.min.js"></script>
    <script>
        function checkSearchAttribute() {
            if ($('#attribute :input').val() === 'EXTERNAL_ID') {
                $('#bank :input').attr('disabled', false);
            } else {
                $('#bank :input').attr('disabled', true);
            }
        }

        document.addEventListener("DOMContentLoaded", checkSearchAttribute);
    </script>
</head>
<body>
<div class="container-fluid" style="margin-top: 10px;">

    <div class="col-md-3">
        <html:form action="/support/logviewer">
            <div class="form-group" id="attribute">
                <label>Search attribute</label>
                <html:select styleClass="form-control" property="searchAttribute" onchange="checkSearchAttribute()">
                    <html:optionsCollection property="searchAttributeLabelValues"/>
                </html:select>
            </div>
            <div class="form-group">
                <label>Search value</label>
                <html:text styleClass="form-control" property="searchValue"/>
            </div>
            <div class="form-group" id="bank">
                <label>BankId</label>
                <html:text styleClass="form-control" property="bankId"/>
                <small class="form-text text-muted">Provide bankId along with externalId</small>
            </div>
            <div class="form-group">
                <html:submit styleClass="btn btn-primary" value="Search"/>
            </div>
            <div class="form-group">
                <html:errors/>
                <bean:write name="LogViewerForm" property="message"/>
            </div>
        </html:form>
    </div>

    <%
        LogViewerForm form = (LogViewerForm) request.getAttribute("form");
        if (form != null && form.getHttpCallInfoList() != null) {
            List<HttpCallInfo> httpCallInfoList = form.getHttpCallInfoList();
            int stackTraceCount = 0;
            String stackTraceId;
            for (HttpCallInfo httpCallInfo : httpCallInfoList) {
                HttpMessage httpMessage = httpCallInfo.getHttpMessage();
    %>

    <table class="table table-bordered table-hover">
        <% if (httpMessage != null && httpMessage.getRequest() != null) {
            Request req = httpMessage.getRequest();
        %>
        <tr>
            <td colspan="2" class="info"><strong>Request Info</strong></td>
        </tr>
        <tr>
            <td>url</td>
            <td style="word-break:break-all;"><%= httpMessage.getUrl() %>
            </td>
        </tr>
        <tr>
            <td>parameters</td>
            <td style="word-break:break-all;"><%= req.getRequest().replaceAll("&", "<br/>") %>
            </td>
        </tr>
        <% if (req.getHeaders() != null && !req.getHeaders().isEmpty()) { %>
        <tr>
            <td>headers</td>
            <td><%= formatHeaders(req.getHeaders()) %>
            </td>
        </tr>
        <% } %>
        <tr>
            <td>isPost</td>
            <td><%= req.isPost() %>
            </td>
        </tr>
        <tr>
            <td>time</td>
            <td><%= formatTime(req.getTime()) %>
            </td>
        </tr>
        <% } %>

        <% if (httpMessage != null && httpMessage.getResponse() != null) {
            Response resp = httpCallInfo.getHttpMessage().getResponse();
            String body = resp.getResponseBody();
        %>
        <tr>
            <td colspan="2" class="info"><strong>Response Info</strong></td>
        </tr>
        <tr>
            <td>body</td>
            <td style="word-break:break-all;"><%= formatXmlForHtml(body) %>
            </td>
        </tr>
        <% if (body.trim().startsWith("<") && body.trim().endsWith(">")) {
        %>
        <tr>
            <td>prettyFormattedBody</td>
            <td style="word-break:break-all;">
                <pre><%= formatXmlForHtml(prettyFormat(body)) %></pre>
            </td>
        </tr>
        <%
            }
        %>
        <tr>
            <td>statusCode</td>
            <td><%= resp.getStatusCode() %>
            </td>
        </tr>
        <tr>
            <td>time</td>
            <td><%= formatTime(resp.getTime()) %>
            </td>
        </tr>
        <% if (resp.getExceptionInfo() != null) {
            ExceptionInfo exceptionInfo = resp.getExceptionInfo();
            ++stackTraceCount;
            stackTraceId = "st" + stackTraceCount;
        %>
        <tr>
            <td colspan="2" class="info"><strong>Response Exception Info</strong></td>
        </tr>
        <tr>
            <td>className</td>
            <td><%= exceptionInfo.getClassName() %>
            </td>
        </tr>
        <tr>
            <td>message</td>
            <td><%= exceptionInfo.getMessage() %>
            </td>
        </tr>
        <tr>
            <td>time</td>
            <td><%= formatTime(exceptionInfo.getTime()) %>
            </td>
        </tr>
        <tr>
            <td>stackTrace</td>
            <td>
                <a href="#<%=stackTraceId%>" data-toggle="collapse">Show/Hide</a>
                <div class="collapse" id="<%=stackTraceId%>">
                    <pre><code><%= exceptionInfo.getStackTrace() %></code></pre>
                </div>
            </td>
        </tr>
        <%
                }
            }
        %>

        <tr>
            <td colspan="2" class="info"><strong>Additional Info</strong></td>
        </tr>
        <tr>
            <td>threadName</td>
            <td><%= httpCallInfo.getThreadName() %>
            </td>
        </tr>
        <tr>
            <td>gameServerId</td>
            <td><%= httpCallInfo.getGameServerId() %>
            </td>
        </tr>
        <% for (Map.Entry<String, String> entry : httpCallInfo.getAdditionalInfo().entrySet()) {
            if (entry.getKey().equals(EXTERNAL_ID.getAttributeName())) {
                String bankId = substringBefore(entry.getValue(), ID_DELIMITER);
                String externalId = substringAfter(entry.getValue(), ID_DELIMITER);
        %>
        <tr>
            <td>bankId</td>
            <td><%= bankId %>
            </td>
        </tr>
        <tr>
            <td>externalId</td>
            <td><%= externalId %>
            </td>
        </tr>
        <%
        } else {
        %>
        <tr>
            <td><%= entry.getKey() %>
            </td>
            <td><%= entry.getValue() %>
            </td>
        </tr>
        <% }
        }
        %>

        <%
            ExceptionInfo exceptionInfo = httpCallInfo.getExceptionInfo();
            if (exceptionInfo != null) {
                ++stackTraceCount;
                stackTraceId = "st" + stackTraceCount;
        %>
        <tr>
            <td colspan="2" class="info"><strong>Exception Info</strong></td>
        </tr>
        <tr>
            <td>className</td>
            <td><%= exceptionInfo.getClassName() %>
            </td>
        </tr>
        <tr>
            <td>message</td>
            <td><%= exceptionInfo.getMessage() %>
            </td>
        </tr>
        <tr>
            <td>time</td>
            <td><%= formatTime(exceptionInfo.getTime()) %>
            </td>
        </tr>
        <tr>
            <td>stackTrace</td>
            <td>
                <a href="#<%=stackTraceId%>" data-toggle="collapse">Show/Hide</a>
                <div class="collapse" id="<%=stackTraceId%>">
                    <pre><code><%= formatXmlForHtml(exceptionInfo.getStackTrace()) %></code></pre>
                </div>
            </td>
        </tr>
        <%
            }
        %>
    </table>
    <hr/>
    <%
            }
        }
    %>

</div>
</body>
</html>
