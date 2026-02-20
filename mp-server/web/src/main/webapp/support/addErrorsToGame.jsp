<%@ page import="com.betsoft.casino.teststand.TestStandError" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.betsoft.casino.teststand.TestStandLocal" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.Optional" %>
<%

    String sid = request.getParameter("sid");
    String errorName = request.getParameter("errorName");

    if (sid == null || sid.isEmpty()) {
        response.getWriter().write("wrong sid");
    } else if (errorName == null || errorName.isEmpty() || Arrays.stream(TestStandError.values())
            .noneMatch(testStandError -> testStandError.name().equals(errorName))) {
        response.getWriter().write("wrong error name, possible names: " + Arrays.toString(TestStandError.values()));
    } else {
        TestStandLocal.getInstance().addTransportError(sid, errorName);
        Map<String, TestStandError> requestedSidForException = TestStandLocal.getInstance().getRequestedSidForException();
        requestedSidForException.forEach((s, testStandError) -> {
            try {
                response.getWriter().write(s + " , " + testStandError + "<br>");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

%>