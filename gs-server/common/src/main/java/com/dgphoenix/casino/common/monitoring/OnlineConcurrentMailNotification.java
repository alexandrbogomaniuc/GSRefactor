package com.dgphoenix.casino.common.monitoring;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by vladislav on 14/08/15.
 */
public class OnlineConcurrentMailNotification implements KryoSerializable {
    private static final int VERSION = 0;
    private static final String MESSAGE = " total concurrent is reached in cluster2";
    private static final String TITLE = " concurrent in cluster2";

    public static final int ID_FOR_ALL_GAME_SERVERS = -1;

    private int concurrentLimit;
    private int gameServerId;
    private boolean limitIsExceeded;
    private long notificationTimeInMillis;

    public OnlineConcurrentMailNotification() {}

    public OnlineConcurrentMailNotification(int concurrentLimit, boolean limitIsExceeded, int gameServerId) {
        this.concurrentLimit = concurrentLimit;
        this.gameServerId = gameServerId;
        this.limitIsExceeded = limitIsExceeded;
        this.notificationTimeInMillis = System.currentTimeMillis();
    }

    public int getConcurrentLimit() {
        return concurrentLimit;
    }

    public int getGameServerId() {
        return gameServerId;
    }

    public boolean isLimitExceeded() {
        return limitIsExceeded;
    }

    public long getNotificationTimeInMillis() {
        return notificationTimeInMillis;
    }

    public String getMessage() {
        return gameServerId == ID_FOR_ALL_GAME_SERVERS
                ? concurrentLimit + MESSAGE
                : concurrentLimit + MESSAGE + " on gameServer: " + gameServerId;
    }

    public String getTitle() {
        return concurrentLimit + TITLE;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION);
        output.writeInt(concurrentLimit);
        output.writeInt(gameServerId);
        output.writeBoolean(limitIsExceeded);
        output.writeLong(notificationTimeInMillis);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int version = input.readInt();
        concurrentLimit = input.readInt();
        gameServerId = input.readInt();
        limitIsExceeded = input.readBoolean();
        notificationTimeInMillis = input.readLong();
    }
}
