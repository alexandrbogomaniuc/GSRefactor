package com.dgphoenix.casino.tools.kryo.generator;

import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 13.02.18
 */
public abstract class NumberGenerator implements RandomValueGenerator {

    protected long calculateMaxValue(Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        Annotation maxAnnotation = annotations.get(Max.class);
        if (maxAnnotation != null) {
            return ((Max) maxAnnotation).value();
        }
        if (annotations.get(Negative.class) != null) {
            return -1;
        }
        if (annotations.get(NegativeOrZero.class) != null) {
            return 0;
        }
        return Long.MAX_VALUE;
    }

    protected long calculateMinValue(Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        Annotation minAnnotation = annotations.get(Min.class);
        if (minAnnotation != null) {
            return ((Min) minAnnotation).value();
        }
        if (annotations.get(Positive.class) != null) {
            return 1;
        }
        if (annotations.get(PositiveOrZero.class) != null) {
            return 0;
        }
        return Long.MIN_VALUE;
    }
}
