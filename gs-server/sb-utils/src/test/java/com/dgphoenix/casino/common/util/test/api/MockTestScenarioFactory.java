package com.abs.casino.common.util.test.api;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 09.12.15
 */
public class MockTestScenarioFactory extends TestScenarioFactoryImpl {

    private final String name = "Mocked Scenario";

    public MockTestScenarioFactory() {
        super("Mocked Scenario");
    }

    @Override
    public String getScenarioName() {
        return name;
    }

    @Override
    public TestScenario build(TestParameters parameters) {
        return new TestScenarioImpl(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockTestScenarioFactory that = (MockTestScenarioFactory) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
