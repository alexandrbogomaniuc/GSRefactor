package com.abs.casino.common.util;

import com.abs.casino.common.util.CalendarUtils;
import org.junit.Test;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;

/**
 * Created by mic on 18.04.14.
 */
public class CalendarUtilsTest {

    private Calendar instance = Calendar.getInstance();

    @Test
    public void testGetStartWeek() throws Exception {
        long timeInMillis = CalendarUtils.getStartWeek(instance.getTime()).getTimeInMillis();
        long timeInMillis1 = CalendarUtils.getStartWeek(instance).getTimeInMillis();

        assertEquals(timeInMillis, timeInMillis1);

    }

    @Test
    public void testGetEndWeek() throws Exception {
        Calendar endWeek1 = CalendarUtils.getEndWeek(instance.getTime());
        Calendar endWeek = CalendarUtils.getEndWeek(instance);

        assertEquals(endWeek.getTimeInMillis(), endWeek1.getTimeInMillis());
    }


    @Test
    public void testGetStartOfMonth() throws Exception {
        Calendar startOfMonth1 = CalendarUtils.getStartOfMonth(instance.getTime());
        Calendar startOfMonth = CalendarUtils.getStartOfMonth(instance);

        assertEquals(startOfMonth.getTimeInMillis(), startOfMonth1.getTimeInMillis());
    }

    @Test
    public void testGetEndOfMonth() throws Exception {
        Calendar endOfMonth = CalendarUtils.getEndOfMonth(instance.getTime());
        Calendar endOfMonth1 = CalendarUtils.getEndOfMonth(instance);

        assertEquals(endOfMonth.getTimeInMillis(), endOfMonth1.getTimeInMillis());
    }
}
