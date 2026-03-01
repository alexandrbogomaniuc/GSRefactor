package com.abs.casino.common.promo;

import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 06.12.16.
 */
public class PromoCampaignMemberInfos implements KryoSerializable {
    private static final byte VERSION = 0;
    //key is campaignId
    private Map<Long, PromoCampaignMember> promoMembers = new HashMap<Long, PromoCampaignMember>();

    public PromoCampaignMemberInfos() {
    }

    public PromoCampaignMember get(Long campaignId) {
        return promoMembers.get(campaignId);
    }

    public Map<Long, PromoCampaignMember> getPromoMembers() {
        return promoMembers;
    }

    public void add(PromoCampaignMember member) throws CommonException {
        PromoCampaignMember existing = promoMembers.get(member.getCampaignId());
        if (existing != null) {
            throw new CommonException("PromoCampaignMember already registered");
        }
        promoMembers.put(member.getCampaignId(), member);
    }

    public boolean hasAnyWebSocketSupport() {
        for (PromoCampaignMember member : promoMembers.values()) {
            if (member.hasWebSocketSupport()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeObject(output, promoMembers);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        promoMembers = kryo.readObject(input, HashMap.class);
    }

    @Override
    public String toString() {
        return "PromoCampaignMemberInfos[" +
                "promoMembers=" + promoMembers +
                ']';
    }
}
