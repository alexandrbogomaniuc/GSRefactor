package com.betsoft.casino.mp.model.onlineplayer;

import java.util.Map;
import java.util.Objects;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SocketClientsStats implements KryoSerializable {
    private static final byte VERSION = 0;

    private Map<Integer, SocketClientsStat> stats;

    public SocketClientsStats() {
    }

    public SocketClientsStats(Map<Integer, SocketClientsStat> stats) {
        this.stats = stats;
    }

    public Map<Integer, SocketClientsStat> getStats() {
        return stats;
    }

    public void setStats(Map<Integer, SocketClientsStat> stats) {
        this.stats = stats;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, stats);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        stats = (Map<Integer, SocketClientsStat>) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocketClientsStats)) return false;
        SocketClientsStats that = (SocketClientsStats) o;
        return Objects.equals(stats, that.stats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stats);
    }

    @Override
    public String toString() {
        return "SocketClientsStats{" +
                "stats='" + stats +
                '}';
    }

    public static class SocketClientsStat implements KryoSerializable {
        private String serverIP;
        private Long clientsCount;

        public SocketClientsStat() {}

        public SocketClientsStat(String serverIP, Long clientsCount) {
            this.serverIP = serverIP;
            this.clientsCount = clientsCount;
        }

        public String getServerIP() {
            return serverIP;
        }

        public void setServerIP(String serverIP) {
            this.serverIP = serverIP;
        }

        public Long getClientsCount() {
            return clientsCount;
        }

        public void setClientsCount(Long clientsCount) {
            this.clientsCount = clientsCount;
        }

        @Override
        public void write(Kryo kryo, Output output) {
            output.writeByte(VERSION);
            output.writeString(serverIP);
            output.writeLong(clientsCount);
        }

        @Override
        public void read(Kryo kryo, Input input) {
            input.readByte();
            serverIP = input.readString();
            clientsCount = input.readLong();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SocketClientsStat)) return false;
            SocketClientsStat that = (SocketClientsStat) o;
            return Objects.equals(serverIP, that.serverIP) 
                    && Objects.equals(clientsCount, that.clientsCount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serverIP, clientsCount);
        }

        @Override
        public String toString() {
            return "SocketClientsStats{" +
                    "serverIP='" + serverIP + '\'' +
                    ", clientsCount='" + clientsCount + '\'' +
                    '}';
        }
    }
}
