package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

import java.util.Set;

public class RemoveMQReservedNicknamesRequest implements KafkaRequest {
    private String region;
    private long owner;
    private Set<String> nicknames;

    public RemoveMQReservedNicknamesRequest() {}

    public RemoveMQReservedNicknamesRequest(String region, long owner, Set<String> nicknames) {
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
