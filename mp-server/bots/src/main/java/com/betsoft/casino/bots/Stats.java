package com.betsoft.casino.bots;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Stats {

    private static final Logger LOG = LogManager.getLogger(Stats.class);

    public static final int SHOTS = 1;
    public static final int MISSES = 2;
    public static final int KILLED_MISSES = 3;
    public static final int HITS = 4;
    public static final int ERRORS = 5;
    public static final int BUY_INS = 6;
    public static final int MOVEMENTS = 7;
    public static final int STATE_WAIT = 8;
    public static final int STATE_CLOSED = 9;
    public static final int STATE_QUALIFY = 10;
    public static final int STATE_PLAY = 11;
    public static final int OTHER_PLAYER_HIT = 12;
    public static final int OTHER_PLAYER_MISS = 13;
    public static final int OTHER_PLAYER_SIT_IN = 14;
    public static final int OTHER_PLAYER_SIT_OUT = 15;
    public static final int NEW_ENEMY = 16;
    public static final int ROUND_RESULT = 17;
    public static final int SELF_SIT_IN = 18;
    public static final int SELF_SIT_OUT = 19;
    public static final int AUTO_EJECT = 20;
    public static final int SELF_EJECT = 21;
    public static final int CRASH_BET = 22;
    public static final int FAILED_CRASH_BET = 23;
    public static final int FAILED_SELF_EJECT = 24;
    public static final int TOO_MANY_PLAYERS_ERROR = 25;
    public static final int CANCEL_BATTLE_ROUND = 26;
    public static final int RE_BUY_INS = 27;
    public static final int INVULNERABLE = 28;

    private Map<Integer, AtomicInteger> stats = new HashMap<>();

    public Stats() {
        stats.put(SHOTS, new AtomicInteger(0));
        stats.put(MISSES, new AtomicInteger(0));
        stats.put(KILLED_MISSES, new AtomicInteger(0));
        stats.put(HITS, new AtomicInteger(0));
        stats.put(ERRORS, new AtomicInteger(0));
        stats.put(BUY_INS, new AtomicInteger(0));
        stats.put(MOVEMENTS, new AtomicInteger(0));
        stats.put(STATE_WAIT, new AtomicInteger(0));
        stats.put(STATE_CLOSED, new AtomicInteger(0));
        stats.put(STATE_QUALIFY, new AtomicInteger(0));
        stats.put(STATE_PLAY, new AtomicInteger(0));
        stats.put(OTHER_PLAYER_HIT, new AtomicInteger(0));
        stats.put(OTHER_PLAYER_MISS, new AtomicInteger(0));
        stats.put(OTHER_PLAYER_SIT_IN, new AtomicInteger(0));
        stats.put(OTHER_PLAYER_SIT_OUT, new AtomicInteger(0));
        stats.put(NEW_ENEMY, new AtomicInteger(0));
        stats.put(ROUND_RESULT, new AtomicInteger(0));
        stats.put(SELF_SIT_IN, new AtomicInteger(0));
        stats.put(SELF_SIT_OUT, new AtomicInteger(0));
        stats.put(AUTO_EJECT, new AtomicInteger(0));
        stats.put(SELF_EJECT, new AtomicInteger(0));
        stats.put(CRASH_BET, new AtomicInteger(0));
        stats.put(FAILED_CRASH_BET, new AtomicInteger(0));
        stats.put(FAILED_SELF_EJECT, new AtomicInteger(0));
        stats.put(TOO_MANY_PLAYERS_ERROR, new AtomicInteger(0));
        stats.put(CANCEL_BATTLE_ROUND, new AtomicInteger(0));
        stats.put(RE_BUY_INS, new AtomicInteger(0));
        stats.put(INVULNERABLE, new AtomicInteger(0));
    }

    public void count(int key) {
        stats.get(key).incrementAndGet();
    }

    public void count(int key, int delta) {
        stats.get(key).addAndGet(delta);
    }

    public Map<Integer, AtomicInteger> getStats() {
        return stats;
    }

    @Override
    public String toString() {
        return "* Shots:          " + stats.get(SHOTS) + "\n" +
                "* Hits:           " + stats.get(HITS) + "\n" +
                "* Misses:         " + stats.get(MISSES) + "\n" +
                "* KilledMisses:   " + stats.get(KILLED_MISSES) + "\n" +
                "* Invulnerable:   " + stats.get(INVULNERABLE) + "\n" +
                "* Errors:         " + stats.get(ERRORS) + "\n" +
                "* Success BuyIns: " + stats.get(BUY_INS) + "\n" +
                "* Success ReBuyIns: " + stats.get(RE_BUY_INS) + "\n" +
                "* Movements:      " + stats.get(MOVEMENTS) + "\n" +
                "* Wait:           " + stats.get(STATE_WAIT) + "\n" +
                "* Closed:         " + stats.get(STATE_CLOSED) + "\n" +
                "* Qualify:        " + stats.get(STATE_QUALIFY) + "\n" +
                "* Play:           " + stats.get(STATE_PLAY) + "\n" +
                "* OtherHits:      " + stats.get(OTHER_PLAYER_HIT) + "\n" +
                "* OtherMiss:      " + stats.get(OTHER_PLAYER_MISS) + "\n" +
                "* OtherSitIn:     " + stats.get(OTHER_PLAYER_SIT_IN) + "\n" +
                "* OtherSitOut:    " + stats.get(OTHER_PLAYER_SIT_OUT) + "\n" +
                "* NewEnemy:       " + stats.get(NEW_ENEMY) + "\n" +
                "* RoundResult:    " + stats.get(ROUND_RESULT) + "\n" +
                "* SitIn:          " + stats.get(SELF_SIT_IN) + "\n" +
                "* SitOut:         " + stats.get(SELF_SIT_OUT) + "\n" +
                "* AutoEjectPlay:  " + stats.get(AUTO_EJECT) + "\n" +
                "* SelfEject:      " + stats.get(SELF_EJECT) + "\n" +
                "* CrashBets:      " + stats.get(CRASH_BET) + "\n" +
                "* FailedCrashBet: " + stats.get(FAILED_CRASH_BET) + "\n" +
                "* FailedSelfEject:" + stats.get(FAILED_SELF_EJECT) + "\n" +
                "* TryReconnect:   " + stats.get(TOO_MANY_PLAYERS_ERROR) + "\n" +
                "* Battle round canceled:   " + stats.get(CANCEL_BATTLE_ROUND) + "\n";
    }

    public String toShortString() {
        return "* Shots:          " + stats.get(SHOTS) + "\n" +
                "* Hits:           " + stats.get(HITS) + "\n" +
                "* Misses:         " + stats.get(MISSES) + "\n";
    }
}
