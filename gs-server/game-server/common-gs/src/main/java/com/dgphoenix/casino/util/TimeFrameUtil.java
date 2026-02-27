package com.abs.casino.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

import com.dgphoenix.casino.ats.TimeFrame;
import com.dgphoenix.casino.kafka.dto.TimeFrameDto;

public class TimeFrameUtil {
    public static TimeFrameDto toTTimeFrame(TimeFrame tf) {

        long startMillisecondsSinceMidnight = -1;
        long endMillisecondsSinceMidnight = -1;
        Set<Integer> tDaysOfWeek = null;

        LocalTime startTime = parseFlexibleLocalTime(tf.getStartTime());
        LocalTime endTime = parseFlexibleLocalTime(tf.getEndTime());

        if (startTime != null) {
            startMillisecondsSinceMidnight = startTime.toSecondOfDay() * 1000L + startTime.getNano() / 1_000_000;
        }

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

    public static LocalTime parseFlexibleLocalTime(String timeString) {
        try {
            // Attempt parsing with ISO_LOCAL_TIME (supports HH:mm:ss or HH:mm)
            return LocalTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (DateTimeParseException e) {
            // Handle incomplete formats manually, e.g., "09:00:" (fill missing parts)
            return parseWithDefaults(timeString);
        }
    }

    private static LocalTime parseWithDefaults(String timeString) {
        // Remove trailing colons and pad missing parts
        String[] parts = timeString.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) : 0;
        int seconds = parts.length > 2 && !parts[2].isEmpty() ? Integer.parseInt(parts[2]) : 0;

        // Return LocalTime with parsed or default values
        return LocalTime.of(hours, minutes, seconds);
    }

    public static TimeFrame fromTTimeFrame(TimeFrameDto tTimeFrame) {
        if(tTimeFrame == null) {
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

        return new TimeFrame(
                startTime == null ? "0:0" : startTime.toString(),
                endTime == null? "23:59:59.999999999" : endTime.toString(),
                daysOfWeek);
    }

    public static Set<TimeFrame> fromTTimeFrames(Set<TimeFrameDto> tTimeFrames) {
        if(tTimeFrames == null) {
            return null;
        }

        Set<TimeFrame> timeFrames = new HashSet<>();

        for(TimeFrameDto tTimeFrame : tTimeFrames) {
            if(tTimeFrame != null) {
                TimeFrame timeFrame = TimeFrameUtil.fromTTimeFrame(tTimeFrame);
                if(timeFrame != null) {
                    timeFrames.add(timeFrame);
                }
            }
        }

        return timeFrames;
    }

    public static Set<TimeFrameDto> toTTimeFrames(Set<TimeFrame> timeFrames) {
        if(timeFrames == null) {
            return null;
        }

        Set<TimeFrameDto> tTimeFrames = new HashSet<>();

        for(TimeFrame timeFrame : timeFrames) {
            if(timeFrame != null) {
                TimeFrameDto tTimeFrame = TimeFrameUtil.toTTimeFrame(timeFrame);
                if(tTimeFrame != null) {
                    tTimeFrames.add(tTimeFrame);
                }
            }
        }

        return tTimeFrames;
    }
}
