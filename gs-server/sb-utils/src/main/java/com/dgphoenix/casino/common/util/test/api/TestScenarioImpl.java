package com.abs.casino.common.util.test.api;

import com.abs.casino.common.exception.CommonException;
import org.apache.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 20.10.15
 */
public class TestScenarioImpl implements TestScenario {

    private static final Logger LOG = Logger.getLogger(TestScenarioImpl.class);

    private final String name;
    private final Set<TestCase> testCases;

    public TestScenarioImpl(String name) {
        this.name = name;
        testCases = new LinkedHashSet<TestCase>();
    }

    @Override
    public TestScenarioResult execute(final TestParameters parameters) throws CommonException {
        LOG.debug(name + " scenario start");
        TestScenarioResult result = new TestScenarioResult(name);
        for (TestCase testCase : testCases) {
            result.addCaseResult(testCase.run(parameters));
        }
        LOG.debug(name + " scenario finish");
        return result;
    }

    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
    }

    public void addTestCases(List<? extends TestCase> testCases) {
        this.testCases.addAll(testCases);
    }

}
