<%@ page import="com.abs.casino.common.util.Pair" %>
<%@ page import="com.abs.casino.support.configuration.ServerConfigurationAction" %>
<%@ page import="com.abs.casino.support.configuration.ServerConfigurationAction.ServerConfigurationFieldType" %>
<%@ page import="java.util.List" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

<ul>
    <logic:iterate id="entry" name="fieldProperties">
        <li class="isField">
            <bean:define id="propertyName" name="entry" property="key"/>
            <bean:define id="propertyPair" name="entry" property="value"/>
            <bean:define id="propertyValue" name="propertyPair" property="key"/>
            <bean:define id="propertyType" name="propertyPair" property="value"/>

            <%String propertyEntry = "field(" + propertyName.toString() + ")";%>

            <span class="field-property-name"><%= propertyName %></span> :
            <% if (((ServerConfigurationFieldType) propertyType).getType() == ServerConfigurationFieldType.CHECKBOX.getType()) {%>
            <html:checkbox property="<%= propertyEntry %>" value="<%= propertyValue.toString() %>"/>
            <% } %>
            <% if (((ServerConfigurationFieldType) propertyType).getType() == ServerConfigurationFieldType.TEXT.getType()) { %>
            <html:text property="<%= propertyEntry %>" value="<%= propertyValue.toString() %>"/>
            <% } %>
            <% if (((ServerConfigurationFieldType) propertyType).getType() == ServerConfigurationFieldType.SELECT.getType()) { %>
            <% if (propertyName.toString().equalsIgnoreCase(ServerConfigurationAction.CLASS_FIELD_CASINO_SYSTEM_TYPE)) { %>
            <html:select property="<%= propertyEntry %>" value="<%= propertyValue.toString() %>">
                <html:optionsCollection name="ServerConfigurationForm" property="casinoSystemTypesList" label="key" value="value"/>
            </html:select>
            <% } %>
            <% } %>

            <html:button property="btn-update-property" styleClass="btn-update-property" styleId="<%= propertyName.toString() %>" value="Update"/>
            <span class="changed">Changed</span>
            <logic:present name="updatedFieldProperties" scope="request">
            <span class="result-message result-ok">
                <% if (((List<String>) request.getAttribute("updatedFieldProperties")).contains(propertyName.toString())) { %>
                    Updated
                <% } %>
            </span>
            </logic:present>
            <input type="hidden" id="<%= propertyName %>" value="false"/>
        </li>
    </logic:iterate>
</ul>

<script type="text/javascript">
    $(document).ready(function () {
        $("#fieldProperties").find("input[type='checkbox']").each(function () {
            if ($(this).attr('value') == 'true') {
                $(this).prop('checked', true);
            } else {
                $(this).prop('checked', false);
            }
        });
    });

    $("#fieldProperties").delegate("input[type='checkbox']", 'change', function () {
        if ($(this).attr('value') == 'true') {
            $(this).attr('value', 'false');
        } else {
            $(this).attr('value', 'true');
        }
    });
</script>