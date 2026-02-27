package com.abs.casino.common.util.test.api;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.11.15
 */
public class TestScenarioFactoryProducerTest {

    private static final String PACKAGE = "com.abs.casino.common.util.test.api";

    private TestScenarioFactoryProducer factoryProducer;

    @Before
    public void setUp() {
        factoryProducer = new TestScenarioFactoryProducer(PACKAGE);
    }

    @Test(expected = NullPointerException.class)
    public void getUnknownFactory() {
        factoryProducer.getFactory("UnknownTestScenario");
    }

    @Test
    public void getFactory() {
        TestScenarioFactory actual = factoryProducer.getFactory("Mocked Scenario");

        assertEquals(new MockTestScenarioFactory(), actual);
    }
}