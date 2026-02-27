package com.abs.casino.common.util;

import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.abs.casino.tools.kryo.generator.GeneratorPriority;
import com.abs.casino.tools.kryo.generator.RandomValueGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * User: van0ss
 * Date: 21.02.2017
 */
public class CurrencyGenerator implements RandomValueGenerator {

    private final RandomDataGenerator randomDataGenerator;

    public CurrencyGenerator(RandomDataGenerator randomDataGenerator) {
        this.randomDataGenerator = randomDataGenerator;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return ICurrency.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return new Currency(randomDataGenerator.nextHexString(Currency.CURRENCY_LENGTH),
                randomDataGenerator.nextHexString(Currency.CURRENCY_LENGTH));
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
