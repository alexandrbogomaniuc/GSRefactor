package com.betsoft.casino.mp.transport;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 08.06.17.
 */
public class Room implements Serializable {
    private long id;
    private String name;
    private short seats;
    private short maxSeats;
    private float minBuyIn;
    private float stake;

    public Room(long id, String name, short seats, short maxSeats, float minBuyIn, float stake) {
        this.id = id;
        this.name = name;
        this.seats = seats;
        this.maxSeats = maxSeats;
        this.minBuyIn = minBuyIn;
        this.stake = stake;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getSeats() {
        return seats;
    }

    public void setSeats(short seats) {
        this.seats = seats;
    }

    public short getMaxSeats() {
        return maxSeats;
    }

    public void setMaxSeats(short maxSeats) {
        this.maxSeats = maxSeats;
    }

    public float getMinBuyIn() {
        return minBuyIn;
    }

    public void setMinBuyIn(int minBuyIn) {
        this.minBuyIn = minBuyIn;
    }

    public float getStake() {
        return stake;
    }

    public void setStake(float stake) {
        this.stake = stake;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        if (id != room.id) return false;
        if (seats != room.seats) return false;
        if (maxSeats != room.maxSeats) return false;
        if (minBuyIn != room.minBuyIn) return false;
        if (Double.compare(room.stake, stake) != 0) return false;
        return name.equals(room.name);

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Room[" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", seats=" + seats +
                ", maxSeats=" + maxSeats +
                ", minBuyIn=" + minBuyIn +
                ", stake=" + stake +
                ']';
    }
}
