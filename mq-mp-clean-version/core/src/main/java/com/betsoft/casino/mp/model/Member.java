package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.common.util.RNG;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Member implements IMember {
    private static final byte VERSION = 0;
    private int id;
    private float deathDamage;
    private float scale;
    private int skin;
    private String moveConfig;

    public Member() {}


    public Member(int id) {
        this.id = id;
        deathDamage = id * 10 + 10;
        scale = (float) (0.45 + (0.05 * RNG.nextInt(5)));
        skin = RNG.nextInt(2);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(RNG.nextInt(10));
        }
        moveConfig = sb.toString();
    }

    public Member(int id, float deathDamage, float scale, int skin, String moveConfig) {
        this.id = id;
        this.deathDamage = deathDamage;
        this.scale = scale;
        this.skin = skin;
        this.moveConfig = moveConfig;
    }

    public static List<Member> convert(List<IMember> members) {
        List<Member> result = new ArrayList<>();
        for (IMember member : members) {
            if(member instanceof Member) {
                result.add((Member) member);
            } else {
                result.add(new Member(member.getId(), member.getDeathDamage(), member.getScale(),
                        member.getSkin(), member.getMoveConfig()));
            }
        }
        return result;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public float getDeathDamage() {
        return deathDamage;
    }

    @Override
    public void setDeathDamage(float deathDamage) {
        this.deathDamage = deathDamage;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public int getSkin() {
        return skin;
    }

    @Override
    public void setSkin(int skin) {
        this.skin = skin;
    }

    @Override
    public String getMoveConfig() {
        return moveConfig;
    }

    @Override
    public void setMoveConfig(String moveConfig) {
        this.moveConfig = moveConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return id == member.id &&
                Float.compare(member.deathDamage, deathDamage) == 0 &&
                Float.compare(member.scale, scale) == 0 &&
                skin == member.skin &&
                Objects.equals(moveConfig, member.moveConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deathDamage, scale, skin, moveConfig);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Member{");
        sb.append("id=").append(id);
        sb.append(", deathDamage=").append(deathDamage);
        sb.append(", scale=").append(scale);
        sb.append(", skin=").append(skin);
        sb.append(", moveConfig='").append(moveConfig).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(id, true);
        output.writeFloat(deathDamage);
        output.writeFloat(scale);
        output.writeInt(skin, true);
        output.writeString(moveConfig);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readInt(true);
        deathDamage = input.readFloat();
        scale = input.readFloat();
        skin = input.readInt(true);
        moveConfig = input.readString();
    }

}
