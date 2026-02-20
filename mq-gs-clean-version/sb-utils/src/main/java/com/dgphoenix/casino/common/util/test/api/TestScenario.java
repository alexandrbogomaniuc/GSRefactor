package com.dgphoenix.casino.common.util.test.api;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * Verification typical case of integration with EC. Contains one or more API endpoints verification.
 *
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 20.10.15
 */
public interface TestScenario {

    TestScenarioResult execute(TestParameters parameters) throws CommonException;

}
