package com.dgphoenix.casino.common.util.test.api;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dgphoenix.casino.common.util.test.api.ResultStatus.*;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 21.10.15
 */
public class TestScenarioResultTest {

    private static final String QUANTITY_NOT_EQUALS = "Quantity of test case results not equals";
    private static final String MUST_BE_PASSED = "Test scenario result status must be passed";
    private static final String MUST_BE_FAILED = "Tests scenario result status must be failed";
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private TestScenarioResult scenarioResult;
    private String scenarioName;

    @Before
    public void setUp() {
        scenarioName = "Simple test scenario";
        scenarioResult = new TestScenarioResult(scenarioName);
    }

    @Test
    public void createTestScenarioResult() {
        assertEquals("Test scenario name not equals", scenarioName, scenarioResult.getScenarioName());
    }

    @Test
    public void scenarioWithSingleTestCasePassed() {
        scenarioResult.addCaseResults(getTestCases(PASSED, 1));

        assertEquals(QUANTITY_NOT_EQUALS, 1, scenarioResult.getTotalCount());
        assertEquals(MUST_BE_PASSED, PASSED, scenarioResult.getStatus());
    }

    @Test
    public void scenarioWithSingleTestCaseFailed() {
        scenarioResult.addCaseResults(getTestCases(FAILED, 1));

        assertEquals(QUANTITY_NOT_EQUALS, 1, scenarioResult.getTotalCount());
        assertEquals(MUST_BE_FAILED, FAILED, scenarioResult.getStatus());
    }

    @Test
    public void scenarioWithMultipleTestCasesPassed() {
        scenarioResult.addCaseResults(getTestCases(PASSED, 2));

        assertEquals(QUANTITY_NOT_EQUALS, 2, scenarioResult.getTotalCount());
        assertEquals(MUST_BE_PASSED, PASSED, scenarioResult.getStatus());

        scenarioResult.addCaseResults(getTestCases(PASSED, 2));

        assertEquals(QUANTITY_NOT_EQUALS, 4, scenarioResult.getTotalCount());
        assertEquals(MUST_BE_PASSED, PASSED, scenarioResult.getStatus());
    }

    @Test
    public void scenarioWithMultipleTestCasesFailed() {
        scenarioResult.addCaseResults(getTestCases(PASSED, 1));
        scenarioResult.addCaseResults(getTestCases(FAILED, 1));

        assertEquals(QUANTITY_NOT_EQUALS, 2, scenarioResult.getTotalCount());
        assertEquals(MUST_BE_FAILED, FAILED, scenarioResult.getStatus());

        scenarioResult.addCaseResults(getTestCases(FAILED, 1));
        scenarioResult.addCaseResults(getTestCases(PASSED, 1));

        assertEquals(QUANTITY_NOT_EQUALS, 4, scenarioResult.getTotalCount());
        assertEquals(QUANTITY_NOT_EQUALS, 2, scenarioResult.getFailedCount());
        assertEquals(MUST_BE_FAILED, FAILED, scenarioResult.getStatus());
    }

    @Test
    public void scenarioWithSkipTestCases() {
        scenarioResult.addCaseResults(getTestCases(SKIPPED, 1));
        scenarioResult.addCaseResults(getTestCases(PASSED, 1));
        scenarioResult.addCaseResults(getTestCases(FAILED, 1));
        assertEquals(MUST_BE_FAILED, FAILED, scenarioResult.getStatus());

        scenarioResult = new TestScenarioResult(scenarioName);
        scenarioResult.addCaseResults(getTestCases(SKIPPED, 1));
        scenarioResult.addCaseResults(getTestCases(PASSED, 1));
        scenarioResult.addCaseResults(getTestCases(PASSED, 1));
        assertEquals(MUST_BE_PASSED, PASSED, scenarioResult.getStatus());
    }

    private List<TestCaseResult> getTestCases(ResultStatus status, int number) {
        List<TestCaseResult> caseResults = new ArrayList<TestCaseResult>(number);
        for (int i = 0; i < number; i++) {
            caseResults.add(createTestCaseResult(status));
        }
        return caseResults;
    }

    private TestCaseResult createTestCaseResult(ResultStatus status) {
        TestCaseResult testCaseResult = new TestCaseResult("Some test case" + COUNTER.getAndIncrement());
        TestStepResult testStepResult = new TestStepResult("Some test step" + COUNTER.getAndIncrement());
        if (status == PASSED) {
            testStepResult.setPassed();
        } else if (status == FAILED) {
            testStepResult.setFailed("reason");
        }
        testStepResult.setLogEntry(new RequestLogEntry("url", "request", "response"));
        if (status == SKIPPED) {
            testCaseResult.setSkipped("reason");
        } else {
            testCaseResult.addStepResult(testStepResult);
        }
        return testCaseResult;
    }
}