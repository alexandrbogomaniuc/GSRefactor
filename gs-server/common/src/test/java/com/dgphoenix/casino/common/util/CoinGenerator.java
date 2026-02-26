package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.abs.casino.tools.kryo.generator.GeneratorPriority;
import com.abs.casino.tools.kryo.generator.RandomValueGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * User: van0ss
 * Date: 21.02.2017
 */
public class CoinGenerator implements RandomValueGenerator {

    private final RandomDataGenerator randomDataGenerator;

    public CoinGenerator(RandomDataGenerator randomDataGenerator) {
        this.randomDataGenerator = randomDataGenerator;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Coin.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        List<Coin> coinList = Coin.getAll();
        return coinList.get(randomDataGenerator.nextInt(0, coinList.size() - 1));
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
