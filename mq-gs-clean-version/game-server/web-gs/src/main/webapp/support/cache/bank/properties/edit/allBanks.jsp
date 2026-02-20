<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<html>
<head><title>Bank select page</title></head>
<body>
<div align="center">
    <html:form action="/support/bankSelectAction">
        <b>Select the bank</b>
        <html:select property="bankId">
            <html:optionsCollection property="allBanks"/>
        </html:select>
        <html:submit value="editProperties" property="button"/>
        <html:submit value="languagesSupport" property="button"/>
    </html:form>
</div>

</body>
</html>