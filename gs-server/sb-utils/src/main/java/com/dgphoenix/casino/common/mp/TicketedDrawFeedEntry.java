package com.abs.casino.common.mp;

import java.io.Serializable;

public class TicketedDrawFeedEntry implements Serializable {
    private long bankId;
    private String bankName;
    private long accountId;
    private long tickets;
    private String nickname;

    public TicketedDrawFeedEntry(long bankId, String bankName, long accountId, long tickets, String nickname) {
        this.bankId = bankId;
        this.bankName = bankName;
        this.accountId = accountId;
        this.tickets = tickets;
        this.nickname = nickname;
    }

    public long getBankId() {
        return bankId;
    }

    public String getBankName() {
        return bankName;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getTickets() {
        return tickets;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return "TicketedDrawFeedEntry{" +
                "bankId=" + bankId +
                ", bankName='" + bankName + '\'' +
                ", accountId=" + accountId +
                ", tickets=" + tickets +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
