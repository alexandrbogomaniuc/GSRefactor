package com.dgphoenix.casino.ats;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

public class TimeFrame {
    private String startTime;
    private String endTime;
    //if it is null consider all days in the week,
    //ыif not null: 1 for Monday to 7 for Sunday
    private Set<DayOfWeek> daysOfWeek;

    public TimeFrame() {

    }

    public TimeFrame(String startTime, String endTime, Set<DayOfWeek> daysOfWeek) {
        this.startTime = startTime;
        this.endTime = endTime;
        if(daysOfWeek == null) {
            daysOfWeek = getFullWeek();
        }
        this.daysOfWeek = daysOfWeek;
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

    public static Set<DayOfWeek> getFullWeek() {
        //all days of week
        Set<DayOfWeek> fullWeekDays = new HashSet<>();
        fullWeekDays.add(DayOfWeek.MONDAY);
        fullWeekDays.add(DayOfWeek.TUESDAY);
        fullWeekDays.add(DayOfWeek.WEDNESDAY);
        fullWeekDays.add(DayOfWeek.THURSDAY);
        fullWeekDays.add(DayOfWeek.FRIDAY);
        fullWeekDays.add(DayOfWeek.SATURDAY);
        fullWeekDays.add(DayOfWeek.SUNDAY);
        return fullWeekDays;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Set<DayOfWeek> getDaysOfWeek() {
        if(daysOfWeek == null) {
            daysOfWeek = getFullWeek();
        }
        return daysOfWeek;
    }

    public void setDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        if(daysOfWeek == null) {
            daysOfWeek = getFullWeek();
        }
        this.daysOfWeek = daysOfWeek;
    }

    @Override
    public String toString() {
        return "TimeFrame{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", daysOfWeek=" + daysOfWeek +
                '}';
    }
}
