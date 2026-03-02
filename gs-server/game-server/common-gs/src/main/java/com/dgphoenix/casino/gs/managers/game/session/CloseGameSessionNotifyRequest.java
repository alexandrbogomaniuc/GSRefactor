package com.abs.casino.gs.managers.game.session;

import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by quant on 12.04.16.
 */
public class CloseGameSessionNotifyRequest implements KryoSerializable {
    private static final byte VERSION = 1;
    private long bankId;
    private long gameSessionId;
    private String url;
    private String params;
    private boolean post;
    private long trackingStartTime;
    private INotifyResponseProcessor processor;

    public CloseGameSessionNotifyRequest() {}

    public CloseGameSessionNotifyRequest(GameSession gameSession, String url, String params, boolean post) {
        this.bankId = gameSession.getBankId();
        this.gameSessionId = gameSession.getId();
        this.url = url;
        this.params = params;
        this.post = post;
        this.trackingStartTime = System.currentTimeMillis();
        processor = null;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPost() {
        return post;
    }

    public void setPost(boolean post) {
        this.post = post;
    }

    public long getTrackingStartTime() {
        return trackingStartTime;
    }

    public INotifyResponseProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(INotifyResponseProcessor processor) {
        this.processor = processor;
    }

    void process() throws CommonException {
        if (processor != null) {
            processor.process(this);
        }
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(bankId);
        output.writeLong(gameSessionId);
        output.writeString(url);
        output.writeString(params);
        output.writeBoolean(post);
        output.writeLong(trackingStartTime);
        kryo.writeClassAndObject(output, processor);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        bankId = input.readLong();
        gameSessionId = input.readLong();
        url = input.readString();
        params = input.readString();
        post = input.readBoolean();
        trackingStartTime = input.readLong();
        if (version == 1) {
            processor = (INotifyResponseProcessor) kryo.readClassAndObject(input);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[bankId=").append(bankId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", url=").append(url);
        sb.append(", params=").append(params);
        sb.append(", post=").append(post);
        sb.append(", trackingStartTime=").append(trackingStartTime);
        sb.append(']');
        return sb.toString();
    }
}
