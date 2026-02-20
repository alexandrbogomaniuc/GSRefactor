package com.dgphoenix.casino.common.util.test.api;

import com.dgphoenix.casino.common.exception.CommonException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 11.11.15
 */
public abstract class TestScenarioFactoryImpl implements TestScenarioFactory {

    private final String scenarioName;

    public TestScenarioFactoryImpl(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public TestScenario build(TestParameters parameters) throws CommonException{
        checkNotNull(parameters, "Test parameters must be not null");
        return new TestScenarioImpl(scenarioName);
    }
}
