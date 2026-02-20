package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.room.ILatencyResponse;
import com.betsoft.casino.utils.TInboundObject;

public class Latency extends TInboundObject implements ILatencyResponse {
    private int step;
    private long serverTs;
    private long serverAckTs;
    private long clientTs;
    private long clientAckTs;
    private long latencyValue;

    private Latency(long date, int rid, int step) {
        super(date, rid);
        this.step = step;
    }


    @Override
    public String toString() {
        return "Latency{" +
                "step=" + step +
                ", serverTs=" + serverTs +
                ", serverAckTs=" + serverAckTs +
                ", clientTs=" + clientTs +
                ", clientAckTs=" + clientAckTs +
                ", latencyValue=" + latencyValue +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public long getServerTs() {
        return serverTs;
    }

    public void setServerTs(long serverTs) {
        this.serverTs = serverTs;
    }

    public long getServerAckTs() {
        return serverAckTs;
    }

    public void setServerAckTs(long serverAckTs) {
        this.serverAckTs = serverAckTs;
    }

    public long getClientTs() {
        return clientTs;
    }

    public void setClientTs(long clientTs) {
        this.clientTs = clientTs;
    }

    public long getClientAckTs() {
        return clientAckTs;
    }

    public void setClientAckTs(long clientAckTs) {
        this.clientAckTs = clientAckTs;
    }

    public long getLatencyValue() {
        return latencyValue;
    }

    public void setLatencyValue(long latencyValue) {
        this.latencyValue = latencyValue;
    }

    public static final class Builder {
        private long date;
        private int rid;
        private int step;
        private long serverTs;
        private long serverAckTs;
        private long clientTs;
        private long clientAckTs;
        private long latencyValue;

        private Builder(long date, int rid, int step) {
            this.date = date;
            this.rid = rid;
            this.step = step;
        }

        public static Builder newBuilder(long date, int rid, int step) {
            return new Builder(date, rid, step);
        }

        public Builder withServerTs(long serverTs) {
            this.serverTs = serverTs;
            return this;
        }

        public Builder withServerAckTs(long serverAckTs) {
            this.serverAckTs = serverAckTs;
            return this;
        }

        public Builder withClientTs(long clientTs) {
            this.clientTs = clientTs;
            return this;
        }

        public Builder withClientAckTs(long clientAckTs) {
            this.clientAckTs = clientAckTs;
            return this;
        }

        public Builder withLatencyValue(long latencyValue) {
            this.latencyValue = latencyValue;
            return this;
        }

        public Latency build() {
            Latency latency = new Latency(date, rid, step);
            latency.setServerTs(serverTs);
            latency.setServerAckTs(serverAckTs);
            latency.setClientTs(clientTs);
            latency.setClientAckTs(clientAckTs);
            latency.setLatencyValue(latencyValue);
            return latency;
        }
    }
}
