<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="java.lang.reflect.Field" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.util.property.BooleanProperty" %>
<%@ page import="com.abs.casino.common.util.property.StringProperty" %>
<%@ page import="com.abs.casino.common.util.property.NumericProperty" %>
<%@ page import="java.lang.reflect.Modifier" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="com.abs.casino.common.util.property.EnumProperty" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.common.config.HostConfiguration" %>
<%

    if (StringUtils.isTrimmedEmpty(request.getParameter("bankId"))) {
        response.getWriter().write("Parameter 'bankId' can't be empty");
        return;
    }

    long bankId;
    try {
        bankId = Long.parseLong(request.getParameter("bankId"));
    } catch (NumberFormatException e) {
        response.getWriter().write("Parameter 'bankId' not well formatted");
        return;
    }

    HostConfiguration hostConfiguration = ApplicationContextHelper.getBean(HostConfiguration.class);
    String clusterTypeRepresentation = hostConfiguration.getClusterType().getStringRepresentation();

    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
    response.getWriter().write("<h2>" + bankDescription(bankInfo) + " (" + clusterTypeRepresentation + ")</h2>");

    Map<String, String> bankInfoProperties = bankInfo.getProperties();

    List<String> filteredValues = Arrays.asList("false", "FALSE", "NONE", ".jar", ".xml", "com.dgphoenix.casino.");
    List<String> filteredProperties = Arrays.asList("_CLASS", "PASS", "MIGRATION", "SERVER_TYPE", "KEY_MOST_PLAYED_GAMES");

    Field[] fields = BankInfo.class.getDeclaredFields();
    for (Field field : fields) {

        if ((field.isAnnotationPresent(BooleanProperty.class) || field.isAnnotationPresent(StringProperty.class) ||
                field.isAnnotationPresent(NumericProperty.class) || field.isAnnotationPresent(EnumProperty.class)) &&
                Modifier.isPublic(field.getModifiers()) &&
                Modifier.isStatic(field.getModifiers()) &&
                Modifier.isFinal(field.getModifiers())) {

            try {
                field.setAccessible(true);
                String property = (String) field.get(null);

                boolean propertyToSkip = false;
                for (String toFilter : filteredProperties) {
                    if (property.contains(toFilter)) {
                        propertyToSkip = true;
                        break;
                    }
                }
                if (propertyToSkip) continue;

                String propertyValue = bankInfoProperties.get(property);
                if (StringUtils.isTrimmedEmpty(propertyValue)) continue;

                for (String valueToFilter : filteredValues) {
                    if (propertyValue.contains(valueToFilter)) {
                        propertyToSkip = true;
                        break;
                    }
                }
                if (propertyToSkip) continue;

                String description = null;
                if (field.isAnnotationPresent(StringProperty.class)) {
                    description = field.getAnnotation(StringProperty.class).description();
                } else if (field.isAnnotationPresent(NumericProperty.class)) {
                    description = field.getAnnotation(NumericProperty.class).description();
                } else if (field.isAnnotationPresent(BooleanProperty.class)) {
                    description = field.getAnnotation(BooleanProperty.class).description();
                } else if (field.isAnnotationPresent(EnumProperty.class)) {
                    description = field.getAnnotation(EnumProperty.class).description();
                }

                if (!StringUtils.isTrimmedEmpty(propertyValue)) {
                    if (!StringUtils.isTrimmedEmpty(description)) {
                        response.getWriter().write("<abbr title='" + description + "'>" + property + "</abbr>");
                        response.getWriter().write(": " + propertyValue + "<br><br>");
                    } else {
                        response.getWriter().write(property + ": " + propertyValue + "<br><br>");
                    }
                }

            } catch (IllegalAccessException e) {
                response.getWriter().write(e.getMessage());
            }
        }

    }

%>
<%!
    private String bankDescription(BankInfo bankInfo) {
        String descr = String.valueOf(bankInfo.getId());
        if (!descr.equals(bankInfo.getExternalBankId()) && !StringUtils.isTrimmedEmpty(bankInfo.getExternalBankId())) {
            descr += "-" + bankInfo.getExternalBankId();
        }
        descr += "-" + bankInfo.getExternalBankIdDescription();
        return descr;
    }
%>
