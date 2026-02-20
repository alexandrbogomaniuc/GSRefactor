<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <script type="text/javascript" src="/support/js/jquery-1.12.4.min.js"></script>
    <style type="text/css">
        .btn-update-property {
            display: none;
        }

        .property-name, .field-property-name {
            width: 450px;
            display: block;
            float: left;
        }


        #updateConfiguration input[type=text] {
            width: 600px;
        }

        #updateConfiguration input[type=checkbox] {
            width: 30px;
            margin-right: 570px;
        }

        .result-message {
            display: none;
        }

        .result-ok {
            display: inline;
            color: green;
        }

        .result-fail {
            display: inline;
            color: red;
        }

        .changed {
            display: none;
            color: red;
        }
        .green { background-color: lightgreen; }
        .orange { background-color: orange; }
    </style>
</head>
<body>
<html:errors/>
<logic:present name="CommonError" scope="request">
    <span class="result-message result-fail"><bean:write name="CommonError" scope="request"/></span>
</logic:present>
<logic:equal name="UpdatedMapPropertiesIsOK" scope="request" value="true">
    <span class="result-message result-ok">Properties updated</span>
</logic:equal>
<logic:equal name="UpdatedFieldPropertiesIsOK" scope="request" value="true">
    <span class="result-message result-ok">Property fields updated</span>
</logic:equal>

<html:form action="/support/serverConfiguration" method="post" styleId="selectServer">
    <div id="servers">
        <select name="serverConfigurationId">
            <option value="G1">TEMPLATE (1)</option>
        </select>
    </div>
    <html:submit property="show-map-properties" value="Show map properties"/>
    <html:submit property="show-field-properties" value="Show field properties"/>
</html:form>
<html:form action="/support/serverConfiguration" method="post" styleId="updateConfiguration">
    <logic:equal name="isNewServer" scope="request" value="true">
        <html:hidden property="isNewServer" value="true"/>
    </logic:equal>

    <logic:present name="fieldProperties" scope="request">
        <div id="fieldProperties">
            <%@ include file="./fieldProperties.jsp" %>
        </div>
    </logic:present>

    <logic:present name="mapProperties" scope="request">
        <div id="mapProperties">
            <%@ include file="./mapProperties.jsp" %>
        </div>
    </logic:present>

    <html:submit property="btn-update" value="Update properties"/><br>
    <%--        Update all servers: <html:checkbox property="allServers"  />--%>
    <html:hidden property="serverConfigurationId"/>
</html:form>

<div id="serverStates">
    <h2>Servers states</h2>
    <table border="1">
        <tr>
            <th>Server id</th>
            <th>Is online?</th>
            <th>Is master?</th>
        </tr>
        <c:forEach var="srvr" items="${ServerConfigurationForm.serversList}">
            <tr class="${srvr.online ? 'green' : 'orange'}">
                <td>${srvr.name}</td>
                <td>${srvr.online ? 'Online' : 'Offline'}</td>
                <td>${srvr.master ? 'Master' : '-'}</td>
            </tr>
        </c:forEach>
    </table>
</div>

<script type="text/javascript">
    $("#selectServer").delegate("input[name='show-map-properties']", 'click', function () {
        $("#selectServer").append("<input type='hidden' name='cmd[0]' value='getMapProperties'>");
        $("#selectServer").append("<input type='hidden' name='action' value='getConfiguration'>");
        $("#selectServer").submit();
    });

    $("#selectServer").delegate("input[name='show-field-properties']", 'click', function () {
        $("#selectServer").append("<input type='hidden' name='cmd[0]' value='getFieldProperties'>");
        $("#selectServer").append("<input type='hidden' name='action' value='getConfiguration'>");
        $("#selectServer").submit();
    });

    $("#selectServer").delegate("input[name='create-gs-configuration']", 'click', function () {
        if (!confirm("Are you want create new Game Server ?")) {
            return false;
        }

        $("#selectServer").append("<input type='hidden' name='cmd[0]' value='getMapProperties'>");
        $("#selectServer").append("<input type='hidden' name='cmd[1]' value='getFieldProperties'>");
        $("#selectServer").append("<input type='hidden' name='action' value='getConfiguration'>");
        $("#selectServer").append("<input type='hidden' name='isNewServer' value='true'>");
        $("#selectServer").submit();
    });

    $("#updateConfiguration").delegate("input[name='btn-update']", 'click', function () {
        $("#updateConfiguration").children("input[name='serverConfigurationId']").attr('value', $("#servers select[name='serverConfigurationId'] option:selected").attr('value'));
        var isNewServer = $("input[type='hidden'][name='isNewServer']");
        if (isNewServer.length == 0 || isNewServer.attr('value') != 'true') {
            $("#updateConfiguration li").each(function () {
                if ($(this).data("updated") != true) {
                    $(this).remove();
                } else {
                    var fieldProperty = $(this).find("input[type='checkbox']");
                    if (fieldProperty.length != 0) {
                        fieldProperty.prop('checked', true);
                    }
                }
                if ($(this).data("remove") == true) {
                    $(this).attr('value', 'null');
                }
            });
        }

        if ($("#updateConfiguration #mapProperties").length != 0 && $("#updateConfiguration #mapProperties ul").children().size() > 0) {
            $("#updateConfiguration").append("<input type='hidden' name='cmd[0]' value='setMapProperties'>");
        }

        if ($("#updateConfiguration #fieldProperties").length != 0 && $("#updateConfiguration #fieldProperties ul").children().size() > 0) {
            $("#updateConfiguration").append("<input type='hidden' name='cmd[1]' value='setFieldProperties'>");
        }

        $("#updateConfiguration").append("<input type='hidden' name='action' value='setConfiguration'>");
        $("#updateConfiguration").submit();
    });

    $("#updateConfiguration").delegate("input[type='text'], input[type='checkbox'], select", 'change', function () {
        $(this).parent().children(".changed").show();
        $(this).parent().data('updated', true);
    });
</script>
</body>
</html>