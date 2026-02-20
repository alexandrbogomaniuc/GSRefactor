package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ISpin;

import java.util.List;

public class Spin implements ISpin {
    private List<Integer> reels;
    private double win;

    public Spin(List<Integer> reels, double win) {
        this.reels = reels;
        this.win = win;
    }

    @Override
    public List<Integer> getReels() {
        return reels;
    }

    @Override
    public double getWin() {
        return win;
    }

    @Override
    public String toString() {
        return "Spin{" +
                "reels=" + reels +
                ", win=" + win +
                '}';
    }
}
