package com.dgphoenix.casino.gs.singlegames.tools.cbservtools;

public class GameMove {
    //variables
    private byte byteGameState;
    private double dBet;
    private double dWin;
    private double dBalance;
    private String sDate;

    //constructor
    public GameMove (byte gameState, double bet, double win, double balance, String date) {
        byteGameState = gameState;
        dBet = bet;
        dWin = win;
        dBalance = balance;
        sDate = date;
    }

    //other functions
    public String toString () {
        return "GameMove object: gameState=" + byteGameState + "  bet=" + dBet + "  win=" + dWin + "  balance=" + dBalance + "  date=" + sDate;
    }

    //getters and setters
    public byte getGameState () {
        return byteGameState;
    }

    public void setGameState (byte byteGameState) {
        this.byteGameState = byteGameState;
    }

    public double getBet () {
        return dBet;
    }

    public void setBet (double dBet) {
        this.dBet = dBet;
    }

    public double getWin () {
        return dWin;
    }

    public void setWin (double dWin) {
        this.dWin = dWin;
    }

    public double getBalance () {
        return dBalance;
    }

    public void setBalance (double dBalance) {
        this.dBalance = dBalance;
    }

    public String getDate () {
        return sDate;
    }

    public void setDate (String sDate) {
        this.sDate = sDate;
    }
}
