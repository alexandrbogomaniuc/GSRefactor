package com.abs.casino.common.util.test.api;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 21.10.15
 */
public class TestCaseResultTest {

    private TestCaseResult caseResult;
    private String caseName;

    @Before
    public void setUp() {
        caseName = "Simple test case result";
        caseResult = new TestCaseResult(caseName);
    }

    @Test
    public void createTestCaseResult() {
        assertEquals("Test case name not equals", caseName, caseResult.getCaseName());
    }

    @Test
    public void testCasePassed() {
        String url = "http://server.com/api/service";
        String request = "request parameters: paramName : paramValue;";
        String response = "<EXTSYSTEM></EXTSYSTEM>";
        TestStepResult stepResult = new TestStepResult("Successful step");
        stepResult.setPassed();
        stepResult.setLogEntry(new RequestLogEntry(url, request, response));
        caseResult.addStepResult(stepResult);

        assertEquals("Result status must be passed", ResultStatus.PASSED, caseResult.getStatus());
        Set<TestStepResult> stepResults = caseResult.getStepResults();
        assertEquals("Number of step entries not equals", 1, stepResults.size());
        RequestLogEntry requestLogEntry = stepResults.iterator().next().getLogEntry();
        assertEquals("URL not equals", url, requestLogEntry.getUrl());
        assertEquals("Request not equals", request, requestLogEntry.getRequest());
        assertEquals("Response not equals", response, requestLogEntry.getResponse());
        assertNull("Fault reason must be empty", caseResult.getMessage());
    }

    @Test
    public void testCaseFailed() {
        String url = "http://server.com/api/service";
        String request = "request parameters: paramName : paramValue;";
        String response = "<EXTSYSTEM></EXTSYSTEM>";
        String faultReason = "Unavailable";
        TestStepResult stepResult = new TestStepResult("Successful step");
        stepResult.setFailed(faultReason);
        stepResult.setLogEntry(new RequestLogEntry(url, request, response));
        caseResult.addStepResult(stepResult);

        assertEquals("Result status must be failed", ResultStatus.FAILED, caseResult.getStatus());
        Set<TestStepResult> stepResults = caseResult.getStepResults();
        assertEquals("Number of step entries not equals", 1, stepResults.size());
        RequestLogEntry logEntry = stepResults.iterator().next().getLogEntry();
        assertEquals("URL not equals", url, logEntry.getUrl());
        assertEquals("Request not equals", request, logEntry.getRequest());
        assertEquals("Response not equals", response, logEntry.getResponse());
        assertEquals("Fault reason not equals", faultReason, caseResult.getMessage());
    }
}