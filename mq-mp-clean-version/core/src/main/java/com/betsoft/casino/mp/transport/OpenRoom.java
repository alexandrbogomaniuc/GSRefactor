package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IOpenRoom;
import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

/**
 * User: flsh
 * Date: 06.06.17.
 */
public class OpenRoom extends TInboundObject implements IOpenRoom {
    private int serverId;
    private long roomId;
    private String sid;
    private String lang;

    public OpenRoom(long date, long roomId, int rid, String sid, int serverId, String mode, String lang) {
        super(date, rid);
        this.serverId = serverId;
        this.roomId = roomId;
        this.sid = sid;
        this.lang = lang;
    }

    @Override
    public int getServerId() {
        return serverId;
    }

    @Override
    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public String getSid() {
        return sid;
    }

    @Override
    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        OpenRoom openRoom = (OpenRoom) o;

        if (serverId != openRoom.serverId) return false;
        if (roomId != openRoom.roomId) return false;
        if (!Objects.equals(sid, openRoom.sid)) return false;
        return Objects.equals(lang, openRoom.lang);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + serverId;
        result = 31 * result + (int) (roomId ^ (roomId >>> 32));
        result = 31 * result + (sid != null ? sid.hashCode() : 0);
        result = 31 * result + (lang != null ? lang.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OpenRoom[" +
                "roomId=" + roomId +
                ", sid='" + sid + "'" +
                ", rid=" + rid +
                ", date=" + date +
                ", serverId=" + serverId +
                ", lang='" + lang + "'" +
                ']';
    }
}
