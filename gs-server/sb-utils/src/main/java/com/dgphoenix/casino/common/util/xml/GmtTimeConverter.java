package com.abs.casino.common.util.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * User: vtungusov
 * Date: 28.09.2020
 *
 * Converter implementation witch convert date field to XML element with attribute in "timezone=GMT+00:00" format
 * with default server timezone
 *
 * @see Converter
 */
public class GmtTimeConverter implements Converter {
    public static final String ATTRIBUTE_TIMEZONE = "timezone";
    public static final String DATE_FORMAT_PATTERN = "dd.MM.yy HH:mm:ss";
    public static final String TIME_ZONE_OFFSET_PREFIX = "GMT";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        final String zoneOffset = getTimeZoneOffset((Long) source);
        writer.addAttribute(ATTRIBUTE_TIMEZONE, TIME_ZONE_OFFSET_PREFIX + zoneOffset);
        final Date date = new Date((Long) source);
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        writer.setValue(df.format(date));
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(Long.class);
    }

    private static String getTimeZoneOffset(long date) {
        TimeZone timeZone = TimeZone.getDefault();
        final int offset = timeZone.getOffset(date);
        if (offset == 0) {
            return "+00:00";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(offset);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(offset);
        minutes = Math.abs(minutes - TimeUnit.HOURS.toMinutes(hours));
        return String.format("%+03d:%02d", hours, minutes);
    }
}
