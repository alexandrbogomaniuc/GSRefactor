package com.betsoft.casino.mp.utils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import com.betsoft.casino.mp.model.bots.TimeFrame;
import com.abs.casino.kafka.dto.TimeFrameDto;

public class TimeFrameUtil {
    public static TimeFrameDto toTTimeFrame(TimeFrame tf) {

        long startMillisecondsSinceMidnight = -1;
        long endMillisecondsSinceMidnight = -1;
        Set<Integer> tDaysOfWeek = null;

        LocalTime startTime = tf.getStartTime();
        if (startTime != null) {
            startMillisecondsSinceMidnight = startTime.toSecondOfDay() * 1000L + startTime.getNano() / 1_000_000;
        }

        LocalTime endTime = tf.getEndTime();
        if (endTime != null) {
            endMillisecondsSinceMidnight = endTime.toSecondOfDay() * 1000L + endTime.getNano() / 1_000_000;
        }

        Set<DayOfWeek> daysOfWeek = tf.getDaysOfWeek();
        if (daysOfWeek != null) {
            tDaysOfWeek = new HashSet<>();
            for(DayOfWeek dayOfWeek : daysOfWeek) {
                if(dayOfWeek != null) {
                    tDaysOfWeek.add(dayOfWeek.getValue());
                }
            }
        }
        return new TimeFrameDto(startMillisecondsSinceMidnight, endMillisecondsSinceMidnight, tDaysOfWeek);
    }

    public static TimeFrame fromTTimeFrame(TimeFrameDto tTimeFrame) {
        if (tTimeFrame == null) {
            return null;
        }

        LocalTime startTime = null;
        LocalTime endTime = null;
        Set<DayOfWeek> daysOfWeek = null;

        if(tTimeFrame.getStartTime() >= 0) {
            // Convert milliseconds to LocalTime
            int startTimeSeconds = (int) (tTimeFrame.getStartTime() / 1000); // Get total seconds
            int startTimeMillis = (int) (tTimeFrame.getStartTime() % 1000); // Get remaining milliseconds
            startTime = LocalTime.ofSecondOfDay(startTimeSeconds).plusNanos(startTimeMillis * 1_000_000);
        }

        if(tTimeFrame.getEndTime() >= 0) {
            int endTimeSeconds = (int) (tTimeFrame.getEndTime() / 1000); // Get total seconds
            int endTimeMillis = (int) (tTimeFrame.getEndTime() % 1000); // Get remaining milliseconds
            endTime = LocalTime.ofSecondOfDay(endTimeSeconds).plusNanos(endTimeMillis * 1_000_000);
        }

        if(tTimeFrame.getDaysOfWeek() != null) {

            daysOfWeek = new HashSet<>();

            for(Integer tDayOfWeek : tTimeFrame.getDaysOfWeek()) {
                if(tDayOfWeek != null) {
                    DayOfWeek dayOfWeek = DayOfWeek.of(tDayOfWeek);
                    daysOfWeek.add(dayOfWeek);
                }
            }
        }

        return new TimeFrame(startTime, endTime, daysOfWeek);
    }

    public static Set<TimeFrame> fromTTimeFrames(Set<TimeFrameDto> tTimeFrames) {
        if (tTimeFrames == null) {
            return null;
        }

        Set<TimeFrame> timeFrames = new HashSet<>();

        for (TimeFrameDto tTimeFrame : tTimeFrames) {
            if (tTimeFrame != null) {
                TimeFrame timeFrame = fromTTimeFrame(tTimeFrame);
                if (timeFrame != null) {
                    timeFrames.add(timeFrame);
                }
            }
        }

        return timeFrames;
    }
}
