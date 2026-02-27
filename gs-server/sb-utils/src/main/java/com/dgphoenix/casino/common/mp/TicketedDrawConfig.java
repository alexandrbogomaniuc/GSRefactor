package com.abs.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.List;

public class TicketedDrawConfig implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;

    private long id;
    private long startDate;
    private long endDate;
    private long xpPerTicket;
    private List<Long> banks;
    private List<Long> games;
    private List<LeaderboardAward> awards;
    private List<String> clusters;

    private String sshHost;
    private int sshPort;
    private String sshUser;
    private String sshPass;
    private String uploadPath;
    private String baseUrl;

    public TicketedDrawConfig() {
    }

    public TicketedDrawConfig(long id, long startDate, long endDate, long xpPerTicket, List<Long> banks,
                              List<Long> games, List<LeaderboardAward> awards, List<String> clusters,
                              String sshHost, int sshPort, String sshUser, String sshPass, String uploadPath, String baseUrl) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.xpPerTicket = xpPerTicket;
        this.banks = banks;
        this.games = games;
        this.awards = awards;
        this.clusters = clusters;
        this.sshHost = sshHost;
        this.sshPort = sshPort;
        this.sshUser = sshUser;
        this.sshPass = sshPass;
        this.uploadPath = uploadPath;
        this.baseUrl = baseUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getXpPerTicket() {
        return xpPerTicket;
    }

    public void setXpPerTicket(long xpPerTicket) {
        this.xpPerTicket = xpPerTicket;
    }

    public List<Long> getBanks() {
        return banks;
    }

    public void setBanks(List<Long> banks) {
        this.banks = banks;
    }

    public List<Long> getGames() {
        return games;
    }

    public void setGames(List<Long> games) {
        this.games = games;
    }

    public List<LeaderboardAward> getAwards() {
        return awards;
    }

    public void setAwards(List<LeaderboardAward> awards) {
        this.awards = awards;
    }

    public List<String> getClusters() {
        return clusters;
    }

    public void setClusters(List<String> clusters) {
        this.clusters = clusters;
    }

    public String getSshHost() {
        return sshHost;
    }

    public void setSshHost(String sshHost) {
        this.sshHost = sshHost;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public String getSshUser() {
        return sshUser;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }

    public String getSshPass() {
        return sshPass;
    }

    public void setSshPass(String sshPass) {
        this.sshPass = sshPass;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(startDate, true);
        output.writeLong(endDate, true);
        output.writeLong(xpPerTicket, true);
        kryo.writeClassAndObject(output, banks);
        kryo.writeClassAndObject(output, games);
        kryo.writeClassAndObject(output, awards);
        kryo.writeClassAndObject(output, clusters);
        output.writeString(sshHost);
        output.writeInt(sshPort, true);
        output.writeString(sshUser);
        output.writeString(sshPass);
        output.writeString(uploadPath);
        output.writeString(baseUrl);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readLong(true);
        startDate = input.readLong(true);
        endDate = input.readLong(true);
        xpPerTicket = input.readLong(true);
        banks = (List<Long>) kryo.readClassAndObject(input);
        games = (List<Long>) kryo.readClassAndObject(input);
        awards = (List<LeaderboardAward>) kryo.readClassAndObject(input);
        clusters = (List<String>) kryo.readClassAndObject(input);
        sshHost = input.readString();
        sshPort = input.readInt(true);
        sshUser = input.readString();
        sshPass = input.readString();
        uploadPath = input.readString();
        baseUrl = input.readString();
    }

    @Override
    public String toString() {
        return "TicketedDrawConfig{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", xpPerTicket=" + xpPerTicket +
                ", banks=" + banks +
                ", games=" + games +
                ", awards=" + awards +
                ", clusters=" + clusters +
                ", sshHost='" + sshHost + '\'' +
                ", sshPort=" + sshPort +
                ", sshUser='" + sshUser + '\'' +
                ", sshPass='" + sshPass + '\'' +
                ", uploadPath='" + uploadPath + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                '}';
    }
}
