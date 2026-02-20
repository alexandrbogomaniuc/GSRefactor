<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head><title>New Bank and SubCasino</title></head>
<body>
<html:link href="/support/cache/bank/common/subcasinoSelect.jsp"> BACK </html:link>
<html:form action="/support/newSubCasino">
    SubCasinoId: <html:text property="subCasinoId" size="20"/>
    BankId: <html:select property="bankId"><br>
    <html:optionsCollection property="bankIds"/>
</html:select><br>
    Domain name: <html:text property="domainName" size="50"/><br>
    <html:submit value="New SubCasino"/>
</html:form> <br>
<html:form action="/support/loadFreeBank">
    New Free Bank<html:submit value="create"/>
</html:form>
</body>