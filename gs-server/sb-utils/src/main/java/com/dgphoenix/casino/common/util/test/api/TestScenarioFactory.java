package com.dgphoenix.casino.common.util.test.api;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 08.12.15
 */
public interface TestScenarioFactory {

    String getScenarioName();

    TestScenario build(TestParameters parameters) throws CommonException;
}
