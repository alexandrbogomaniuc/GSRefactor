<%@ page import="com.abs.casino.common.util.CookieUtils" %>
<%
    String anyJavaPresent = CookieUtils.getCookieValue(request, "AnyJavaPresent");
    boolean needJavaDetection = anyJavaPresent == null;
    if (!needJavaDetection) {
        if ("no".equals(anyJavaPresent)) {
            //response.sendRedirect(response.encodeRedirectURL("/nojavafound.html"));
%>
<script type="text/javascript">
    window.location.href = "/nojavafound.html";
</script>
<%
            return;
        }
    }
%>
<script type="text/javascript">
    var javaPresent = "yes";
    <% if (needJavaDetection) { %>
    function isJavaPluginPrecent() {
        var present = "no";
        if (navigator.javaEnabled()) {
            if (window.java == null) { // use applet because java.lang.System not defined
                if (navigator.userAgent.indexOf("MSIE") > 0 && navigator.userAgent.indexOf("Win") > 0) { // WinIE
                    document.writeln('<object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" ');
                    document.writeln('name="jvminfo" width="1" height="1"><param name="code" value="jvmdetector.JavaVersion"/>');
                    document.writeln('<param name="codebase" value="/applets/"/></object>');
                } else {
                    document.writeln('<object codetype="application/java" classid="java:jvmdetector.JavaVersion.class" ');
                    document.writeln('codebase="/applets/" name="jvminfo" width="1" height="1"></object>');
                }
                if (document.jvminfo != null) {
                    try {
                        present = document.jvminfo.getJavaVersion();
                    } catch (e) {
                        present = "yes";

                    }
                } else {
                    present = "no";
                }
            } else {
                present = java.lang.System.getProperty("java.version");
            }
        }
        return present;
    }
    javaPresent = isJavaPluginPrecent();
    document.cookie = "AnyJavaPresent=" + escape(javaPresent) + "; path=/";
    <% } %>
</script>
<%
    boolean bPlugin = true; // always use new version of applet tag
    // see in CVS for previous code
%>
