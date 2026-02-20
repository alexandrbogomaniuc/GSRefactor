package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

import java.util.Objects;

public class JoinBattlegroundResponse extends TObject {
    private String startGameLink;

    public JoinBattlegroundResponse(long date, int rid, String startGameLink) {
        super(date, rid);
        this.startGameLink = startGameLink;
    }

    public String getStartGameLink() {
        return startGameLink;
    }

    public void setStartGameLink(String startGameLink) {
        this.startGameLink = startGameLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JoinBattlegroundResponse that = (JoinBattlegroundResponse) o;
        return startGameLink.equals(that.startGameLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startGameLink);
    }

    @Override
    public String toString() {
        return "JoinBattlegroundResponse{" +
                "date=" + date +
                ", rid=" + rid +
                ", startGameLink='" + startGameLink + '\'' +
                '}';
    }
}
