package com.betsoft.casino.mp.model.bots;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

public class TimeFrame  implements KryoSerializable {
    private static final byte VERSION = 1;
    private LocalTime startTime;
    private LocalTime endTime;
    //if it is null consider all days in the week,
    //ыif not null: 1 for Monday to 7 for Sunday
    private Set<DayOfWeek> daysOfWeek;

    public TimeFrame() {}

    public TimeFrame(LocalTime startTime, LocalTime endTime, Set<DayOfWeek> daysOfWeek) {
        this.startTime = startTime;
        this.endTime = endTime;
        if(daysOfWeek == null) {
            daysOfWeek = getFullWeek();
        }
        this.daysOfWeek = daysOfWeek;
    }

    private Set<DayOfWeek> getFullWeek() {
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
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

    public boolean daysOfWeekContains(DayOfWeek day) {

        Set<DayOfWeek> days = getDaysOfWeek();
        if(days == null) {
            return true;
        }
        return days.contains(day);
    }

    public boolean isCurrentTimeWithin() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        //check today is within a listed days
        if(!daysOfWeekContains(today)) {
            return false;
        }

        //if today is within a listed days, check a time frame
        LocalTime now = LocalTime.now();
        if(startTime.isBefore(endTime)) {
            //Normal case: start time is before end time
            return !now.isBefore(startTime) && !now.isAfter(endTime);
        } else {
            return !now.isBefore(startTime) || !now.isAfter(endTime);
        }
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

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeObject(output, startTime);
        kryo.writeObject(output, endTime);
        kryo.writeClassAndObject(output, daysOfWeek);

    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        startTime = kryo.readObject(input, LocalTime.class);
        endTime = kryo.readObject(input, LocalTime.class);
        if(version >= 1) {
            //noinspection unchecked
            daysOfWeek = (Set<DayOfWeek>) kryo.readClassAndObject(input);
        }
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
