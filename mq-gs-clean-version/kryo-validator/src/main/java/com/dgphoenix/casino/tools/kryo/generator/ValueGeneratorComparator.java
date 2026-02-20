package com.dgphoenix.casino.tools.kryo.generator;

import java.util.Comparator;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class ValueGeneratorComparator implements Comparator<RandomValueGenerator> {
    @Override
    public int compare(RandomValueGenerator generator1, RandomValueGenerator generator2) {
        GeneratorPriority priority1 = generator1.getPriority();
        GeneratorPriority priority2 = generator2.getPriority();
        if (priority1.compareTo(priority2) == 0) {
            String className1 = generator1.getClass().getName();
            String className2 = generator2.getClass().getName();
            return className1.compareTo(className2);
        } else {
            return priority1.compareTo(priority2);
        }
    }
}
