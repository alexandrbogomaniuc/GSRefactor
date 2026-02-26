package com.abs.casino.controller.frbonus;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.validator.routines.DateValidator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LongDateDeserializer extends StdDeserializer<Long> {
    public static final String ISOFTBET_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final SimpleDateFormat formatter = new SimpleDateFormat(ISOFTBET_DATE_FORMAT);

    public LongDateDeserializer() {
        this(null);
    }

    public LongDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Long deserialize(JsonParser jsonparser, DeserializationContext context)
            throws IOException {
        String date = jsonparser.getText();
        validate(date);
        try {
            return formatter.parse(date).toInstant().toEpochMilli();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void validate(String date) throws IOException {
        DateValidator validator = DateValidator.getInstance();
        Date validate = validator.validate(date, ISOFTBET_DATE_FORMAT);
        if (validate == null) {
            throw new IOException("incorrect date value: " + date);
        }
    }
}
