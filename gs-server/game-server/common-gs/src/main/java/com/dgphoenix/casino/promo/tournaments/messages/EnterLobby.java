package com.abs.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TInboundObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class EnterLobby extends TInboundObject {
    private static final byte VERSION = 2;
    private String sid;
    private String lang;
    private String CDN;
    private Long gameId;

    public EnterLobby() {}

    public EnterLobby(long date, int rid, String sid, String lang, String CDN, Long gameId) {
        super(date, rid);
        this.sid = sid;
        this.lang = lang;
        this.CDN = CDN;
        this.gameId = gameId;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCdn() {
        return CDN;
    }

    public void setCdn(String CDN) {
        this.CDN = CDN;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeString(sid);
        output.writeString(lang);
        output.writeString(CDN);
        kryo.writeObjectOrNull(output, gameId, Long.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        sid = input.readString();
        lang = input.readString();
        if (version > 0) {
            CDN = input.readString();
        }
        if (version > 1) {
            gameId = kryo.readObjectOrNull(input, Long.class);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EnterLobby that = (EnterLobby) o;
        return Objects.equals(sid, that.sid) &&
                Objects.equals(lang, that.lang) &&
                Objects.equals(CDN, that.CDN) &&
                Objects.equals(gameId, that.gameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sid, lang, CDN, gameId);
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public String toString() {
        return "EnterLobby{" +
                "sid='" + sid + '\'' +
                ", lang='" + lang + '\'' +
                ", cdn='" + CDN + '\'' +
                ", gameId=" + gameId +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
