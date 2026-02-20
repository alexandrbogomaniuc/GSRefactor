<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <style type="text/css">
        p.smallfont {
            font-size: 10px;
        }

        p.smallfontred {
            font-size: 10px;
            color: red;
        }

        p.mediumfont {
            font-size: 12px;
        }

        p.mediumfontred {
            font-size: 12px;
            color: red;
        }
    </style>
    <title>Cache viewer</title>
</head>
<body>
<h1>Cache viewer</h1>

<html:errors/>
<script>
    var keyFormatObject = {};
    <logic:iterate id="caches" indexId="i" name="CacheViewerForm" property="cachesList">
    keyFormatObject["${CacheViewerForm.cachesList[i].key}"] = "${CacheViewerForm.cachesList[i].description}";
    </logic:iterate>

    function selectCacheItem() {
        var t = (document.getElementsByName("cache")[0]).value;
        document.getElementById("key_format_span").innerHTML = keyFormatObject[t];
    }

</script>
<p>Select cache for view:</p>

<div>
    <html:form action="/support/cacheviewer" method="post">
        <html:hidden property="bigCacheSizeAllowed" value="false"/>
        <p>
            Cache:
            <html:select property="cache" onchange="selectCacheItem()">
                <html:optionsCollection name="CacheViewerForm" property="cachesList"
                                        label="value" value="key"/>
            </html:select>
            &nbsp;&nbsp; ObjectId:
            <html:text property="objectId" size="20"/>
            Key format :
            <span id="key_format_span"></span>
            <script>
                selectCacheItem();
            </script>
        </p>

        <p>
            <html:submit property="cmd" value="view" styleClass="viewbutton"/>
            <html:submit property="cmd" value="viewTrackingInfo" styleClass="viewbutton"/>
            <html:submit property="cmd" value="viewSize" styleClass="viewbutton"/>
            <html:submit property="cmd" value="viewAdditionalInfo" styleClass="viewbuttonred"/>
        </p>
    </html:form>
</div>

<logic:notPresent name="error" scope="request">
    <logic:present name="cache" scope="request">
        <p class="mediumfont">Result is:</p>
        <logic:iterate id="cacheId" name="cache">
            <p class="smallfont">
                key:<bean:write name="cacheId" property="key"/> &nbsp;
                value:<bean:write name="cacheId" property="value"/>
            </p>
        </logic:iterate>

        <logic:present name="recordsCount" scope="request">
            <p class="mediumfont">Records count: <bean:write name="recordsCount"/></p>
        </logic:present>
    </logic:present>

    <logic:present name="cacheObject" scope="request">
        <p class="mediumfont">Result is:</p>

        <p class="smallfont"><bean:write name="cacheObject"/></p>
    </logic:present>

    <logic:present name="cacheTrackingInfo" scope="request">
        <p class="mediumfont">Tracking info:</p>

        <p class="smallfont"><bean:write name="cacheTrackingInfo"/></p>
    </logic:present>

    <logic:present name="cacheAdditionalInfo" scope="request">
        <p class="mediumfont">Additional info:</p>

        <p class="smallfont"><bean:write name="cacheAdditionalInfo"/></p>
    </logic:present>

    <logic:present name="cacheSize" scope="request">
        <p class="mediumfont">Cache size:</p>

        <p class="smallfont"><bean:write name="cacheSize"/></p>
    </logic:present>

    <logic:present name="emptyResult" scope="request">
        <p class="mediumfont">Result is:</p>

        <p class="smallfont"><bean:write name="emptyResult"/></p>
    </logic:present>

    <logic:present name="sizeWarning" scope="request">
        <p class="mediumfontred">WARNING:</p>

        <p class="smallfontred"><bean:write name="sizeWarning"/></p>
        <html:form action="/support/cacheviewer">
            <html:hidden property="bigCacheSizeAllowed" value="true"/>
            <html:hidden property="cache"/>
            <p><html:submit property="cmd" value="view" styleClass="viewbuttonred">show anyway</html:submit></p>
        </html:form>
    </logic:present>
</logic:notPresent>

<logic:present name="error" scope="request">
    <p class="mediumfont">error:</p>

    <p class="smallfont"><bean:write name="error"/></p>
</logic:present>
</body>
</html>