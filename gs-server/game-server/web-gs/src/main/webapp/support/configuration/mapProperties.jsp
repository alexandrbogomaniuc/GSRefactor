<%@ page import="com.abs.casino.common.config.GameServerConfigTemplate" %>
<%@ page import="com.abs.casino.common.util.property.*" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="java.lang.reflect.Field" %>
<%@ page import="java.lang.reflect.Modifier" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

<%

    final Map<String, Field> fieldsByValue = new HashMap<String, Field>(); //key - static field value, value - field

    for (Field field : GameServerConfigTemplate.class.getFields()) {
        int fieldModifiers = field.getModifiers();
        if (Modifier.isStatic(fieldModifiers) && Modifier.isFinal(fieldModifiers) && field.getType() == String.class) {
            fieldsByValue.put((String) field.get(null), field);
        }
    }

%>

<ul>
    <logic:iterate id="entry" name="mapProperties">
        <li>
            <bean:define id="propertyName" name="entry" property="key"/>
            <bean:define id="propertyValue" name="entry" property="value"/>

            <%
                String propertyEntry = "property(" + propertyName.toString() + ")";
                Field field = fieldsByValue.get(propertyName.toString());
                String description = null;
                if (field != null) {
                    if (field.isAnnotationPresent(StringProperty.class)) {
                        description = field.getAnnotation(StringProperty.class).description();
                    } else if (field.isAnnotationPresent(JavaClassProperty.class)) {
                        description = field.getAnnotation(JavaClassProperty.class).description();
                    } else if (field.isAnnotationPresent(UrlProperty.class)) {
                        description = field.getAnnotation(UrlProperty.class).description();
                    } else if (field.isAnnotationPresent(BooleanProperty.class)) {
                        description = field.getAnnotation(BooleanProperty.class).description();
                    } else if (field.isAnnotationPresent(NumericProperty.class)) {
                        description = field.getAnnotation(NumericProperty.class).description();
                    }
                }

            %>
            <span class="property-name">
                <% if (!StringUtils.isTrimmedEmpty(description)) { %>
                    <abbr title="<%=description%>"><%=propertyName%></abbr>
                <% } else { %>
                    <%= propertyName %>
                <% } %>
            </span> :

            <% if (field != null) { %>
            <% if (field.isAnnotationPresent(BooleanProperty.class)) { %>
                <%--<input type="checkbox" name="<%= propertyEntry %>" value="true"--%>
                <%--<%= Boolean.TRUE.toString().equalsIgnoreCase((propertyValue.toString().trim())) ? "checked" : "" %>>--%>
            <html:checkbox property="<%= propertyEntry %>" value="<%= propertyValue.toString() %>"/>
            <% } else if (field.isAnnotationPresent(NumericProperty.class)) { %>
            <html:text property="<%= propertyEntry %>" value="<%= propertyValue.toString() %>" size="12"/>
            <% } else if (field.isAnnotationPresent(EnumProperty.class)) {

                Enum<?>[] enumConstants = field.getAnnotation(EnumProperty.class).value().getEnumConstants();

            %>
            <select name="<%=propertyEntry%>">
                <%
                    for (Enum<?> anEnum : enumConstants) {
                        String enumValueAsString = propertyValue.toString();
                        if (!StringUtils.isTrimmedEmpty(enumValueAsString) && enumValueAsString.equals(anEnum.name())) {
                %>
                <option selected="selected" value="<%=anEnum.name()%>"><%=anEnum.name()%>
                </option>
                <%
                } else {
                %>
                <option value="<%=anEnum.name()%>"><%=anEnum.name()%>
                </option>
                <%
                        }
                    }
                %>
            </select>

            <% } else { %>
            <html:text property="<%= propertyEntry %>" value="<%= propertyValue.toString() %>"/>
            <% } %>
            <% } else { %>
            <html:text property="<%= propertyEntry %>" value="<%= propertyValue.toString() %>"/>
            <% } %>

            <html:button property="btn-remove-property" styleId="<%= propertyName.toString() %>" value="[X]"/>
            <span class="changed">Changed</span>
            <logic:present name="updatedMapProperties" scope="request">
                <span class="result-message result-ok">
                    <% if (((List<String>) request.getAttribute("updatedMapProperties")).contains(propertyName.toString())) { %>
                        Updated
                    <% } %>
                </span>
            </logic:present>
            <input type="hidden" id="<%= propertyName %>" value="false"/>
        </li>
    </logic:iterate>

</ul>
<html:button property="btn-add-property" value="Add property"/>

<script type="text/javascript">

    $(document).ready(function () {
        $("#mapProperties").find("input[type='checkbox']").each(function () {
            if ($(this).attr('value') == 'true') {
                $(this).prop('checked', true);
            } else {
                $(this).prop('checked', false);
            }
        });
    });

    $("#mapProperties").delegate("input[type='checkbox']", 'change', function () {
        if ($(this).attr('value') == 'true') {
            $(this).attr('value', 'false');
        } else {
            $(this).attr('value', 'true');
        }
    });

    $("#mapProperties").delegate("input[name='btn-add-property']", 'click', function () {
        var propertyName = prompt("Please enter property name", "");

        if (propertyName != null && propertyName != "") {
            var propertyValue = prompt("Please enter property value", "");

            if (propertyValue == null) {
                propertyValue = "";
            }

            var propertyN = '<span class="property-name">' + propertyName + '</span>\r\n';
            var propertyV = '<input type="text" name="property(' + propertyName + ')" value="' + propertyValue + '">\r\n';
            var propertyU = '<input type="button" id="' + propertyName + '" name="btn-undo-add-property" value="[U]">\r\n';


            $("#mapProperties ul").append('<li>' + propertyN + ' : ' + propertyV + propertyU + '</li>');
            $("#mapProperties ul li").last().data('updated', true);
            $(this).parent();
        }
    });

    $("#mapProperties").delegate("input[name='btn-undo-add-property']", 'click', function () {
        var propertyName = $(this).attr('id');
        $(this).parent().remove();
    });

    $("#mapProperties").delegate("input[name='btn-remove-property']", 'click', function () {
        alert("Remove property disable");
        return false;

        if (!confirm("Remove property: " + $(this).attr('id') + "?")) {
            return false;
        }

        $(this).parent().data('remove', true).data('updated', true).children(".changed").show();
        $(this).parent().find("input[type='text']").attr('value', 'null');
    });
</script>