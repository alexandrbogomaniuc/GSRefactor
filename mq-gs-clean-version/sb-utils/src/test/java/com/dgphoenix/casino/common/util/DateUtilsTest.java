package com.dgphoenix.casino.common.util;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * User: nieky
 * Date: 07.10.13
 * Time: 13:16
 */
@Ignore
public class DateUtilsTest extends TestCase {
    Map<Integer,Boolean> secondHourOfDay = new HashMap<Integer, Boolean>();

    @Before
    protected void setUp(){
        secondHourOfDay.put(new Integer(0),new Boolean(false));
        secondHourOfDay.put(new Integer(1),new Boolean(false));
        secondHourOfDay.put(new Integer(2),new Boolean(true));
        secondHourOfDay.put(new Integer(3),new Boolean(false));

    }
    @After
    protected void tearDown(){
        secondHourOfDay.clear();
    }
    @Test
    @Ignore //not used, should be checked and removed later
    public void testDateUtils(){

        for(Map.Entry<Integer,Boolean> entry: secondHourOfDay.entrySet()){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY,entry.getKey());
            String errorMessage="Test DateUtils.isSecondHourOfDay fails. Expected value: "+
                    entry.getValue().booleanValue()+", received value: "+DateUtils.isSecondHourOfDay(calendar.getTime());
            assertEquals(errorMessage,entry.getValue().booleanValue(),DateUtils.isSecondHourOfDay(calendar.getTime()));

        }

    }
}
