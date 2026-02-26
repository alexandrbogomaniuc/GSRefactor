package com.abs.casino.ats;

import java.util.Set;

import com.dgphoenix.casino.ats.TimeFrame;
import com.dgphoenix.casino.kafka.dto.BotConfigInfoDto;
import com.dgphoenix.casino.kafka.dto.TimeFrameDto;
import com.dgphoenix.casino.util.TimeFrameUtil;

public class BotConfigInfo {
    private Long id;
    private long bankId;
    private Set<Long> allowedGames;
    private boolean active;
    private String username;
    private String password;
    private String mqNickname;
    private long mmcBalance;
    private long mqcBalance;
    private Set<TimeFrame> timeFrames;
    private Set<Long> allowedBankIds;

    public BotConfigInfo() {

    }

    public BotConfigInfo(Long id, long bankId, Set<Long> allowedGames, boolean active, String username,
                         String password, String mqNickname, long mmcBalance, long mqcBalance,
                         Set<TimeFrame> timeFrames, Set<Long> allowedBankIds) {
        this.id = id;
        this.bankId = bankId;
        this.allowedGames = allowedGames;
        this.active = active;
        this.username = username;
        this.password = password;
        this.mqNickname = mqNickname;
        this.mmcBalance = mmcBalance;
        this.mqcBalance = mqcBalance;
        this.timeFrames = timeFrames;
        this.allowedBankIds = allowedBankIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public Set<Long> getAllowedGames() {
        return allowedGames;
    }

    public void setAllowedGames(Set<Long> allowedGames) {
        this.allowedGames = allowedGames;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMqNickname() {
        return mqNickname;
    }

    public void setMqNickname(String mqNickname) {
        this.mqNickname = mqNickname;
    }

    public long getMmcBalance() {
        return mmcBalance;
    }

    public void setMmcBalance(long mmcBalance) {
        this.mmcBalance = mmcBalance;
    }

    public long getMqcBalance() {
        return mqcBalance;
    }

    public void setMqcBalance(long mqcBalance) {
        this.mqcBalance = mqcBalance;
    }

    public Set<TimeFrame> getTimeFrames() {
        return timeFrames;
    }

    public void setTimeFrames(Set<TimeFrame> timeFrames) {
        this.timeFrames = timeFrames;
    }

    public Set<Long> getAllowedBankIds() {
        return allowedBankIds;
    }

    public void setAllowedBankIds(Set<Long> allowedBankIds) {
        this.allowedBankIds = allowedBankIds;
    }
}
