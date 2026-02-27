package com.abs.casino.common.util.test.api;

import com.dgphoenix.casino.common.exception.CommonException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 19.11.15
 */
@RunWith(MockitoJUnitRunner.class)
public class TestScenarioImplTest {

    @Mock
    private TestParameters parameters;
    private TestScenarioImpl simpleScenario;

    @Before
    public void setUp() {
        simpleScenario = new TestScenarioImpl("Simple");
    }

    @Test
    public void scenarioExecutionSucceed() throws CommonException {
        simpleScenario.addTestCase(new TestCase() {
            @Override
            public TestCaseResult run(TestParameters parameters) {
                return createCaseResult("FirstTestCase", true);
            }
        });
        simpleScenario.addTestCase(new TestCase() {
            @Override
            public TestCaseResult run(TestParameters parameters) {
                return createCaseResult("SecondTestCase", true);
            }
        });
        TestScenarioResult actual = simpleScenario.execute(parameters);

        assertNotNull(actual);
        assertEquals(2, actual.getTotalCount());
        assertEquals(ResultStatus.PASSED, actual.getStatus());
    }

    @Test
    public void scenarioExecutionFailed() throws CommonException {
        simpleScenario.addTestCase(new TestCase() {
            @Override
            public TestCaseResult run(TestParameters parameters) {
                return createCaseResult("FirstTestCase", true);
            }
        });
        simpleScenario.addTestCase(new TestCase() {
            @Override
            public TestCaseResult run(TestParameters parameters) {
                return createCaseResult("SecondTestCase", false);
            }
        });
        TestScenarioResult actual = simpleScenario.execute(parameters);

        assertNotNull(actual);
        assertEquals(2, actual.getTotalCount());
        assertEquals(ResultStatus.FAILED, actual.getStatus());

    }

    private TestCaseResult createCaseResult(String testCaseName, boolean isPassed) {
        TestStepResult stepResult = new TestStepResult(testCaseName + "step1");
        if (isPassed) {
            stepResult.setPassed();
        } else {
            stepResult.setFailed("failed message");
        }
        stepResult.setLogEntry(new RequestLogEntry("someUrl", "request", "response"));
        TestCaseResult caseResult = new TestCaseResult(testCaseName);
        caseResult.addStepResult(stepResult);
        return caseResult;
    }
}