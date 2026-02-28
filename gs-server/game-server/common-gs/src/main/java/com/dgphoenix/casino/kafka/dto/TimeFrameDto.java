package com.abs.casino.kafka.dto;

import java.util.Set;

public class TimeFrameDto {
    private long startTime;
    private long endTime;
    private Set<Integer> daysOfWeek;

    public TimeFrameDto() {}

    public TimeFrameDto(long startTime, long endTime, Set<Integer> daysOfWeek) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysOfWeek = daysOfWeek;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Set<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(Set<Integer> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
}
