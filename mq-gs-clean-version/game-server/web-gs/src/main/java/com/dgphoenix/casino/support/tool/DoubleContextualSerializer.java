package com.dgphoenix.casino.support.tool;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DoubleContextualSerializer extends JsonSerializer<Double> implements ContextualSerializer {

    private int precision = 0;

    public DoubleContextualSerializer(int precision) {
        this.precision = precision;
    }

    public DoubleContextualSerializer() {
    }

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (precision == 0) {
            gen.writeNumber(value);
        } else {
            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.setScale(precision, RoundingMode.HALF_UP);
            gen.writeNumber(bd.toPlainString());
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Precision precisionProperty = property.getAnnotation(Precision.class);
        if (precisionProperty != null) {
            return new DoubleContextualSerializer(precisionProperty.precision());
        }
        return this;
    }
}
