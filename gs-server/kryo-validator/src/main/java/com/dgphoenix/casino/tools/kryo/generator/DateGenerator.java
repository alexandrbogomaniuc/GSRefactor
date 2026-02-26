package com.abs.casino.tools.kryo.generator;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class DateGenerator implements RandomValueGenerator {

    private static final int DEFAULT_RANDOM_MILLIS = 500;

    private final RandomDataGenerator randomDataGenerator;
    private final int randomMillis;

    public DateGenerator(RandomDataGenerator randomDataGenerator) {
        this(randomDataGenerator, DEFAULT_RANDOM_MILLIS);
    }

    public DateGenerator(RandomDataGenerator randomDataGenerator, int randomMillis) {
        this.randomDataGenerator = randomDataGenerator;
        this.randomMillis = randomMillis;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Date.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        long randomDate = System.currentTimeMillis() - randomDataGenerator.nextInt(0, randomMillis);
        if (isSqlDate(type)) {
            return new java.sql.Date(randomDate);
        }
        return new Date(randomDate);
    }

    private boolean isSqlDate(Class<?> type) {
        return java.sql.Date.class.getName().equals(type.getName());
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.MEDIUM;
    }
}
