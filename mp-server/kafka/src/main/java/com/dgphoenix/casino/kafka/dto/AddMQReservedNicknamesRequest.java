package com.dgphoenix.casino.kafka.dto;

import java.util.Set;

public class AddMQReservedNicknamesRequest implements KafkaRequest {
    private String region;
    private long owner;
    private Set<String> nicknames;

    public AddMQReservedNicknamesRequest() {}

    public AddMQReservedNicknamesRequest(String region, long owner, Set<String> nicknames) {
        super();
        this.region = region;
        this.owner = owner;
        this.nicknames = nicknames;
    }

    public String getRegion() {
        return region;
    }

    public long getOwner() {
        return owner;
    }

    public Set<String> getNicknames() {
        return nicknames;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public void setNicknames(Set<String> nicknames) {
        this.nicknames = nicknames;
    }
}
