package com.abs.casino.cassandra.persist;

public class CassandraPlayerSessionState {

    private String sid;
    private String extId;
    private String privateRoomId;
    private boolean isFinishGameSession;
    private long dayTime;

    public CassandraPlayerSessionState(String sid, String extId, String privateRoomId, boolean isFinishGameSession, long dayTime) {
        this.sid = sid;
        this.extId = extId;
        this.privateRoomId = privateRoomId;
        this.isFinishGameSession = isFinishGameSession;
        this.dayTime = dayTime;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public boolean isFinishGameSession() {
        return isFinishGameSession;
    }

    public void setFinishGameSession(boolean finishGameSession) {
        isFinishGameSession = finishGameSession;
    }

    public long getDayTime() {
        return dayTime;
    }

    public void setDayTime(long dayTime) {
        this.dayTime = dayTime;
    }

    @Override
    public String toString() {
        return "CassandraPlayerSessionState{" +
                "sid='" + sid + '\'' +
                ", extId='" + extId + '\'' +
                ", privateRoomId='" + privateRoomId + '\'' +
                ", isFinishGameSession=" + isFinishGameSession +
                ", dayTime=" + dayTime +
                '}';
    }
}
