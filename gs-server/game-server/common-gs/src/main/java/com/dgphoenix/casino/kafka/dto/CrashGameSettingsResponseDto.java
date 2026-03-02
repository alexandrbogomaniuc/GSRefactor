package com.abs.casino.kafka.dto;

import java.util.Set;

import com.abs.casino.kafka.dto.BasicKafkaResponse;
import com.abs.casino.kafka.dto.CrashGameSettingDto;

public class CrashGameSettingsResponseDto extends BasicKafkaResponse {
    private Set<CrashGameSettingDto> settings;

    public CrashGameSettingsResponseDto() {}

    public CrashGameSettingsResponseDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public CrashGameSettingsResponseDto(Set<CrashGameSettingDto> settings) {
        super(true, 0, "");
        this.settings = settings;
    }

    public Set<CrashGameSettingDto> getSettings() {
        return settings;
    }

    public void setSettings(Set<CrashGameSettingDto> settings) {
        this.settings = settings;
    }
}
