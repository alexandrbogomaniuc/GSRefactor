package com.abs.casino.bonus;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@XStreamAlias("BONUS")
public class Bonus implements Serializable {
    public static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @XStreamAlias("BONUSID")
    private Long bonusId;

    @XStreamAlias("TYPE")
    private String type;

    @XStreamAlias("AWARDEDDATE")
    private String awardDate;

    @XStreamAlias("AMOUNT")
    private long amount;

    @XStreamAlias("BALANCE")
    private long balance;

    @XStreamAlias("ROLLOVER")
    private long rollover;

    @XStreamAlias("COLLECTED")
    private long collected;

    @XStreamAlias("GAMEIDS")
    private String gameIds;

    @XStreamAlias("DESCRIPTION")
    private String description;

    @XStreamAlias("COMMENT")
    private String comment;

    @XStreamAlias("EXPDATE")
    private String expDate;

    public Bonus() {
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAwardDate() {
        return awardDate;
    }

    public Date getAwardDateAsDate() throws ParseException {
        LocalDateTime time;

        if (StringUtils.isTrimmedEmpty(awardDate)) {
            return null;
        }

        time = LocalDateTime.parse(awardDate, df);
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    public void setAwardDate(String awardDate) {
        this.awardDate = awardDate;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getRollover() {
        return rollover;
    }

    public void setRollover(long rollover) {
        this.rollover = rollover;
    }

    public long getCollected() {
        return collected;
    }

    public void setCollected(long collected) {
        this.collected = collected;
    }

    public String getGameIds() {
        return gameIds;
    }

    public void setGameIds(String gameIds) {
        this.gameIds = gameIds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExpDate() {
        return expDate;
    }

    public Date getExpDateAsDate() throws ParseException {
        LocalDateTime time;

        if (StringUtils.isTrimmedEmpty(expDate)) {
            return null;
        }

        time = LocalDateTime.parse(expDate, df);
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Bonus");
        sb.append("[bonusId=").append(bonusId);
        sb.append(", type='").append(type).append('\'');
        sb.append(", awardDate='").append(awardDate).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", balance=").append(balance);
        sb.append(", rollover=").append(rollover);
        sb.append(", collected=").append(collected);
        sb.append(", gameIds='").append(gameIds).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", expDate='").append(expDate).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
