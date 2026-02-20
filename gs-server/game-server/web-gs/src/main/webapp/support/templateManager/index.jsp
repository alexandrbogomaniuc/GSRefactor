<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%@include file="ClusterList.jsp" %>

<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script type="text/javascript" src="../js/scriptjava.js"></script>

<script type="text/javascript">

    function showHideCustomGames() {
        var id_select_type = document.getElementById("id_select_type");
        var fieldCustomGames = document.getElementById("id_custom_games");

        fieldCustomGames.style.visibility = (id_select_type.value == "CUSTOM" ? "visible" : "hidden");
    }

    function showHideCustomAddress(index) {
        var id_select_type = document.getElementById("id_select_" + index);
        var fieldCustomAddress = document.getElementById("id_custom_address_" + index);

        fieldCustomAddress.value = id_select_type.value;
    }

    $(window).on("load", function () {
        showHideCustomGames();
        showHideCustomAddress(1);
        showHideCustomAddress(2);
    });

    function getData(address_1, address_2) {
        var select_1 = document.getElementById("id_select_1");
        var select_2 = document.getElementById("id_select_2");
        var select_type = document.getElementById("id_select_type");
        var text_properties = document.getElementById("id_custom_properties");

        var custom_1 = document.getElementById("id_custom_address_1").value;
        var custom_2 = document.getElementById("id_custom_address_2").value;

        var value_1 = select_1.options[select_1.selectedIndex].text;
        var type = select_type.options[select_type.selectedIndex].text;
        var custom_games = "";
        var custom_properties = text_properties.value;

        if (type == "CUSTOM")
            custom_games = document.getElementById("id_custom_games").value;

        address_1 = custom_1 + "/support/templateManager/getTemplates.jsp";
        address_2 = custom_2 + "/support/templateManager/getTemplates.jsp";

        $$a({
            type: 'get',
            url: address_1,
            data: {'type': type, 'custom_games': custom_games, 'custom_properties': custom_properties},
            response: 'text',
            success: function (data) {
                $$('id_result_1').value = data;
                getData_2(address_2, type, custom_games, custom_properties)
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                alert('status:' + XMLHttpRequest.status + ', status text: ' + XMLHttpRequest.statusText);
            },
            endstatus: function (number) {// 404, 200
                if (number != 200)
                    alert("Can't load info from: " + value_1);
            }
        });
    }

    function getData_2(address, type, custom_games, custom_properties) {
        var select_2 = document.getElementById("id_select_2");
        var value_2 = select_2.options[select_2.selectedIndex].text;

        $$a({
            type: 'get',
            url: address,
            data: {'type': type, 'custom_games': custom_games, 'custom_properties': custom_properties},
            response: 'text',
            success: function (data) {
                $$('id_result_2').value = data;
                $$('id_form').submit();
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                alert('status:' + XMLHttpRequest.status + ', status text: ' + XMLHttpRequest.statusText);
            },
            endstatus: function (number) {// 404, 200
                if (number != 200)
                    alert("Can't load info from: " + value_2);
            }
        });
    }
</script>
<FORM id="id_form" ACTION="compareTemplates.jsp" METHOD="POST">
    <TABLE>
        <TR>
            <% for (int i = 1; i <= 2; i++) {%>
            <TD>
                <SELECT id="id_select_<%=i%>" style="width: 280px" name="cluster_<%=i%>" onchange="showHideCustomAddress(<%=i%>)">
                    <optgroup label="local">
                        <%
                            for (Map.Entry<String, String> entry : localClusterMap.entrySet()) { %>
                        <OPTION value="<%=entry.getValue()%>"><%=entry.getKey()%>
                        </OPTION>
                        <% }
                        %>
                    </optgroup>
                    <optgroup label="copy">
                        <%
                            for (Map.Entry<String, String> entry : copyClusterMap.entrySet()) { %>
                        <OPTION value="<%=entry.getValue()%>"><%=entry.getKey()%>
                        </OPTION>
                        <% }
                        %>
                    </optgroup>
                    <optgroup label="live">
                        <%
                            for (Map.Entry<String, String> entry : liveClusterMap.entrySet()) { %>
                        <OPTION value="<%=entry.getValue()%>"><%=entry.getKey()%>
                        </OPTION>
                        <% }
                        %>
                    </optgroup>
                </SELECT>
            </TD>
            <%}%>
            <TD>
                Games:
                <SELECT id="id_select_type" style="width: 160px" name="type" onchange="showHideCustomGames()">
                    <OPTION value="CUSTOM">CUSTOM</OPTION>
                    <OPTION value="STANDARD">STANDARD</OPTION>
                    <OPTION value="ALL">ALL</OPTION>
                    <OPTION value="AGCC">AGCC</OPTION>
                    <OPTION value="CLONES">CLONES</OPTION>
                    <OPTION value="LGA">LGA</OPTION>
                    <OPTION value="AAMS">AAMS</OPTION>
                </SELECT>
                <INPUT type="text" id="id_custom_games" name="custom_games" style="width: 200px; visibility: hidden;" value="828"/>
            </TD>
        </TR>

        <TR>
            <TD>
                <INPUT type="text" id="id_custom_address_1" name="custom_address_1" style="width: 280px; " value=""/>
            </TD>

            <TD>
                <INPUT type="text" id="id_custom_address_2" name="custom_address_2" style="width: 280px; " value=""/>
            </TD>
        <TR>

        <TR>
            <TD> Properties(empty=all):</TD>
        </TR>
        <TR>
            <TD colspan="3">
                <INPUT type="text" id="id_custom_properties" name="custom_properties" style="width: 100%;" value="" title="'DEVELOPMENT_VERSION, REPOSITORY_FILE, ...'"/>
            </TD>
        </TR>
    </TABLE>


    <INPUT id="id_button" style="width: 200px;" type="button" onclick="getData()" value="Сравнить"/>

    <INPUT id="id_result_1" name="result_1" type="hidden">
    <INPUT id="id_result_2" name="result_2" type="hidden">
</FORM>