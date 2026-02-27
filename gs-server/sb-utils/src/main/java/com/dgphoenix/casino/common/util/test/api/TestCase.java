package com.abs.casino.common.util.test.api;

/**
 * Verification typical use of single API endpoint. May contain multiple verification steps.
 *
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 21.10.15
 */
public interface TestCase {

    TestCaseResult run(TestParameters parameters);

}
