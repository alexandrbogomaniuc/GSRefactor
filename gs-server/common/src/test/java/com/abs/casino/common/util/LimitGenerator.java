package com.abs.casino.common.util;

import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.abs.casino.tools.kryo.generator.GeneratorPriority;
import com.abs.casino.tools.kryo.generator.RandomValueGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * User: van0ss
 * Date: 21.02.2017
 */
public class LimitGenerator implements RandomValueGenerator {

    private final RandomDataGenerator randomDataGenerator;

    public LimitGenerator(RandomDataGenerator randomDataGenerator) {
        this.randomDataGenerator = randomDataGenerator;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Limit.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return Limit.valueOf(randomDataGenerator.nextInt(0, Integer.MAX_VALUE / 2),
                randomDataGenerator.nextInt(Integer.MAX_VALUE / 2, Integer.MAX_VALUE));
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
