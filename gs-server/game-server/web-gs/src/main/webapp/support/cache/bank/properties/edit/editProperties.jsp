<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraCallIssuesPersister" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.URLCallCounters" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.property.*" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.statistics.http.HttpClientCallbackHandler" %>
<%@ page import="com.dgphoenix.casino.support.cache.bank.edit.forms.editproperties.BankPropertiesForm" %>
<%@ page import="org.apache.commons.lang.time.FastDateFormat" %>
<%@ page import="java.lang.reflect.Field" %>
<%@ page import="java.lang.reflect.Modifier" %>
<%@ page import="java.text.Format" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%!
    private Format df = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss");
    private final CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    private final CassandraCallIssuesPersister callIssuesPersister =
            persistenceManager.getPersister(CassandraCallIssuesPersister.class);
%>
<%
    BankPropertiesForm form = (BankPropertiesForm) request.getAttribute("BankPropertiesForm");
    Long subCasinoId = BankInfoCache.getInstance().getBankInfo(Long.parseLong(form.getBankId())).getSubCasinoId();

    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(Long.parseLong(form.getBankId()));

    HttpClientCallbackHandler handler = HttpClientCallbackHandler.getInstance();
    String date = handler.getDate(System.currentTimeMillis());
    Map<String, URLCallCounters> todayIssues = new HashMap<>();
    Collection<URLCallCounters> counters = callIssuesPersister.getByDate(date);
    if (counters != null && !counters.isEmpty()) {
        for (URLCallCounters data : counters) {
            todayIssues.put(data.getUrl(), data);
        }
    }
%>

<html>
<head>
    <title>Edit properties page</title>
    <style>
        .changed {
            background-color: yellow;
        }
    </style>
</head>
<body>
<script>
    function changeNames() {
        var elements = document.body.getElementsByTagName("input");
        var val = 'value(';
        for (var i = 0; i < elements.length; i++) {
            if (elements[i].name.indexOf(val) == 0) {
                elements[i].name = elements[i].name.slice(val.length, -1);
            }
        }
    }
</script>
<script type="text/javascript" src="/support/js/highlight.js"></script>

<a href="${pageContext.request.contextPath}/support/subCasino.do?subcasinoId=<%= subCasinoId%>">Go to SubCasino id = <%= subCasinoId %>
</a>

<html:form action="/support/propertiesmode">
    <input type="hidden" name="bankId" value="${BankPropertiesForm.bankId}"/>
    <b> Select the view mode </b>
    <html:select property="mode">
        <html:option value="full"> full </html:option>
        <html:option value="simple"> simple </html:option>
    </html:select>
    <html:submit value="enter"/>
</html:form>

<logic:equal value="full" name="BankPropertiesForm" property="mode">
    <html:form action="/support/bankPropEdit">
        <input type="hidden" name="bankInfoXML" value="<%= request.getAttribute("bankInfoXML") %>">
        <input type="hidden" name="mode" value="full"/>
        <input type="hidden" name="bankId" value="${BankPropertiesForm.bankId}"/>
        <html:errors/>
        <html:messages id="msg" message="true" header="messages.header" footer="messages.footer">
            <LI><span style="color: green"><bean:write name="msg"/></span></LI>
        </html:messages>
        <table>
            <tr>
                <td><html:text property="cashierUrl" size="90"/></td>
                <td><b>CashierURL</b><br/></td>
            </tr>
            <tr>
                <td><html:text property="externalBankId" size="90"/></td>
                <td><b>External Bank Id</b><br/></td>
            </tr>

            <tr>
                <td><html:text property="externalBankIdDescription" size="90"/></td>
                <td><b>External Bank Id Description id</b><br/></td>
            </tr>
            <tr>
                <td align="right">
                    <html:select property="defaultCurrencyCode">
                        <html:optionsCollection name="BankPropertiesForm" property="currenciesList"
                                                label="label" value="value"/>

                    </html:select>
                </td>
                <td><b>Default currency</b><br/></td>
            </tr>
            <tr>
                <td align="right">
                    <html:select property="limitId">
                        <html:optionsCollection name="BankPropertiesForm" property="limitsList"
                                                label="label" value="value"/>
                    </html:select>
                </td>
                <td><b>Limit</b><br/></td>
            </tr>
            <tr>
                <td align="right">
                    <html:select property="defaultLanguage">
                        <html:optionsCollection name="BankPropertiesForm" property="languagesList"
                                                label="label" value="value"/>
                    </html:select>
                </td>
                <td><b>Default Language</b><br/></td>
            <tr>
            <tr>
                <td><html:text property="freeGameOverRedirectUrl" size="90"/></td>
                <td><b>FreeGameOverRedirectUrl</b><br/></td>
                <br/><br/>
            </tr>
            <tr>
                <td><html:text property="allowedRefererDomains" size="90"/></td>
                <td><b>Allowed Referer Domains</b><br/></td>
                <br/><br/>
            </tr>
            <tr>
                <td><html:text property="forbiddenRefererDomains" size="90"/></td>
                <td><b>Forbidden Referer Domains</b><br/></td>
                <br/><br/>
            </tr>

            <%
                Class bankInfoClass = BankInfo.class;
                Field[] fields = bankInfoClass.getDeclaredFields();

                for (Field field : fields) {
                    //dirty hack, allowedDomains is inner field of BankPropertiesForm
                    if ("KEY_ALLOWED_REFERER_DOMAINS".equals(field.getName()) ||
                            "KEY_FORBIDDEN_REFERER_DOMAINS".equals(field.getName())) {
                        continue;
                    }
                    field.setAccessible(true);
                    if ((field.isAnnotationPresent(BooleanProperty.class) || field.isAnnotationPresent(StringProperty.class) ||
                            field.isAnnotationPresent(NumericProperty.class) || field.isAnnotationPresent(EnumProperty.class)) &&
                            Modifier.isPublic(field.getModifiers()) &&
                            Modifier.isStatic(field.getModifiers()) &&
                            Modifier.isFinal(field.getModifiers())) {
                        Object key = (Object) field.get(null);
                        String valueKey = "value(" + key + ")";
                        String mandatoryStyle = "";
                        if (field.isAnnotationPresent(MandatoryProperty.class)) {
                            mandatoryStyle = "bgcolor=\"#B1F2C2\"";
                        }
                        if (field.isAnnotationPresent(BooleanProperty.class)) {
            %>
            <tr>
                <td align="right"><html:checkbox property="<%= valueKey %>" value="true"/></td>
                <td>
                <span>
                <% String description = field.getAnnotation(BooleanProperty.class).description();
                    if (description != null && !description.isEmpty()) { %>
                        <abbr title="<%=description%>"><%=key.toString()%></abbr>
                    <% } else { %>
                        <%=key.toString()%>
                    <% } %>
                </span>

                    <% if (key.toString().endsWith("_URL")) {
                        String value = PropertyUtils.getStringProperty(bankInfo.getProperties(), key.toString());
                        if (!StringUtils.isTrimmedEmpty(value) && todayIssues.containsKey(value)) {
                            URLCallCounters callCounters = todayIssues.get(value); %>
                    <span style="color:red;font-weight:bold;">(today issues: <%=callCounters.getFailedCount()%>,
                last: <%=df.format(new Date((callCounters.getLastFailTime())))%>)</span>
                    <% }
                    } %>

                </td>
            <tr>
                        <%
                }
                if(field.isAnnotationPresent(StringProperty.class)){
                    String description = field.getAnnotation(StringProperty.class).description();
            %>
            <tr>
                <td><html:text property="<%=valueKey %>" size="90"/></td>
                <td <%=mandatoryStyle%>><span>
                    <% if (description != null && !description.isEmpty()) { %>
                        <abbr title="<%=description%>"><%=key.toString()%></abbr>
                    <% } else { %>
                        <%=key.toString()%>
                    <% } %>
                </span></td>
            </tr>
            <%
                }
                if (field.isAnnotationPresent(NumericProperty.class)) {
                    String description = field.getAnnotation(NumericProperty.class).description();
            %>
            <tr>
                <td align="right"><html:text property="<%=valueKey %>" size="12"/></td>
                <td <%=mandatoryStyle%>><span>
                    <% if (description != null && !description.isEmpty()) { %>
                        <abbr title="<%=description%>"><%=key.toString()%></abbr>
                    <% } else { %>
                        <%=key.toString()%>
                    <% } %>
                </span></td>
            </tr>
            <%
                }
                if (field.isAnnotationPresent(EnumProperty.class)) {
                    String description = field.getAnnotation(EnumProperty.class).description();
                    Enum<?>[] enumConstants = field.getAnnotation(
                            EnumProperty.class).value().getEnumConstants();
            %>

            <tr>
                <td align="right">
                    <select name="<%=valueKey%>">
                        <%
                            String enumValueAsString = bankInfo.getStringProperty(String.valueOf(key));
                            for (Enum<?> anEnum : enumConstants) {
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

                </td>
                <td><span><% if (description != null && !description.isEmpty()) { %>
                        <abbr title="<%=description%>"><%=key.toString()%></abbr>
                    <% } else { %>
                        <%=key.toString()%>
                    <% } %></span></td>
            </tr>

            <%
                        }
                    }
                }
            %>
        </table>
        <html:submit value="back" property="button"/>
        <html:submit value="save" property="button"/>
    </html:form>
</logic:equal>
<logic:equal value="simple" name="BankPropertiesForm" property="mode">
    <table border="1" align="center" cellpadding="7" cellspacing="0">
        <tr>
            <td align="center">
                <b>KEY</b>
            </td>
            <td align="center">
                <b>VALUE</b>
            </td>
                    <%
                TreeMap<String,String> props = new TreeMap<String, String>(form.getBankProperties());
                for(Map.Entry<String,String> entry : props.entrySet()){               
                    %>
        <tr>
            <td>
                <%=entry.getKey()%>
            </td>
            <td>
                <%=entry.getValue()%>
            </td>
        </tr>
        <%
            }
        %>
        </tr>

    </table>
    <br>

    <b>New property</b> <br>
    <html:form action="/support/addproperty">
        <input type="hidden" name="bankId" value="${BankPropertiesForm.bankId}"/>
        <input type="hidden" name="mode" value="simple"/>
        <html:text property="newKey"/><br>
        <html:text property="newValue"/><br>
        <html:submit value="addProperty"/>
    </html:form>

</logic:equal>


</body>
</html>
