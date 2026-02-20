package com.dgphoenix.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class JoinBattlegroundResponse extends TObject {
    private static final byte VERSION = 1;
    private String startGameLink;

    public JoinBattlegroundResponse() {}

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
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeString(startGameLink);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        startGameLink = input.readString();
    }

    @Override
    protected byte getVersion() {
        return VERSION;
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
                ", version=" + version +
                ", startGameLink='" + startGameLink + '\'' +
                '}';
    }
}