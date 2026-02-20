package com.dgphoenix.casino.tools.kryo.generator;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class LocaleGenerator implements RandomValueGenerator {
    private final Random random;

    public LocaleGenerator(Random random) {
        this.random = random;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Locale.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        Locale[] locales = Locale.getAvailableLocales();
        return locales[random.nextInt(locales.length)];
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
