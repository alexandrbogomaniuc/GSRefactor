package com.abs.casino.common.util.test.api;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ObjectCreator;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import static com.abs.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.11.15
 */
public class TestScenarioFactoryProducer {

    private final String packageName;
    private Map<String, TestScenarioFactory> factories;

    public TestScenarioFactoryProducer(String packageName) {
        checkArgument(!isTrimmedEmpty(packageName), "Package name be not null or empty");
        this.packageName = packageName;
        initFactories();
    }

    public Map<String, TestScenarioFactory> getFactories() {
        return factories;
    }

    public TestScenarioFactory getFactory(final String scenarioName) {
        checkNotNull(scenarioName);
        TestScenarioFactory testScenarioFactory = factories.get(scenarioName);
        checkNotNull(testScenarioFactory, "Cannot find factory for test scenario %s in package %s", scenarioName, packageName);
        return testScenarioFactory;
    }

    private void initFactories() {
        Reflections reflections = new Reflections(packageName);
        final Set<Class<? extends TestScenarioFactoryImpl>> scenarioFactories = reflections.getSubTypesOf(TestScenarioFactoryImpl.class);
        factories = FluentIterable.from(scenarioFactories)
                .filter(new Predicate<Class<? extends TestScenarioFactory>>() {
                    @Override
                    public boolean apply(Class<? extends TestScenarioFactory> scenarioFactoryClass) {
                        int modifiers = scenarioFactoryClass.getModifiers();
                        return !Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers) && !scenarioFactoryClass.isMemberClass();
                    }
                })
                .transform(new Function<Class<? extends TestScenarioFactory>, TestScenarioFactory>() {
                    @Nullable
                    @Override
                    public TestScenarioFactory apply(Class<? extends TestScenarioFactory> scenarioFactoryClass) {
                        return createFactory(scenarioFactoryClass);
                    }
                })
                .uniqueIndex(new Function<TestScenarioFactory, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable TestScenarioFactory scenarioFactory) {
                        return scenarioFactory != null ? scenarioFactory.getScenarioName() : null;
                    }
                });
    }

    private TestScenarioFactory createFactory(Class<? extends TestScenarioFactory> scenarioFactoryClass) {
        ObjectCreator<TestScenarioFactory> creator = new ObjectCreator<TestScenarioFactory>();
        try {
            return creator.createInstance(scenarioFactoryClass);
        } catch (CommonException ex) {
            throw new IllegalStateException("Unable create factory instance for class: " + scenarioFactoryClass.getName(), ex);
        }
    }
}
