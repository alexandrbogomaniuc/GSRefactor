package com.dgphoenix.casino.common;

import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GameSessionExtendedProperties implements KryoSerializable {
    private static final Logger LOG = LogManager.getLogger(GameSessionExtendedProperties.class);
    private static final byte VERSION = 6;

    // roundId => leaderboardId => contribution
    private Map<Long, Map<Long, Double>> leaderboardContributions = new HashMap<Long, Map<Long, Double>>();

    private Double model;

    private Long lossLimit;
    private Long sessionTimeLimit;
    private boolean lossLimitReminderTriggered;
    private boolean sessionTimerReminderTriggered;
    private Long unfinishedRoundPayout;

    private Double unjPlayerCurrencySideBet;

    // Don't forget to add gameId to SBMigrationManager.gamesWithExtendedProperties when new properties are introduced here

    public Map<Long, Map<Long, Double>> getLeaderboardContributions() {
        return leaderboardContributions;
    }

    // leaderboardId => contribution
    public Map<Long, Double> getSummarizedContributions() {
        Map<Long, Double> result = new HashMap<Long, Double>();

        for (Map.Entry<Long, Map<Long, Double>> round : leaderboardContributions.entrySet()) {
            for (Map.Entry<Long, Double> contribution : round.getValue().entrySet()) {
                if (result.containsKey(contribution.getKey())) {
                    result.put(contribution.getKey(), contribution.getValue() + result.get(contribution.getKey()));
                } else {
                    result.put(contribution.getKey(), contribution.getValue());
                }

            }
        }

        return result;
    }

    public void addLeaderboardContributions(long roundId, Map<Long, Double> leaderboardContributions) throws CommonException {
        if (leaderboardContributions != null && !leaderboardContributions.isEmpty()) {
            Map<Long, Double> oldContributions = this.leaderboardContributions.get(roundId);
            if (oldContributions != null) {
                LOG.error("Override of leaderboard contributions: RoundId: " + roundId + "Old: " + oldContributions + ", New: " + leaderboardContributions);
//                if (!oldContributions.isEmpty() && !oldContributions.equals(leaderboardContributions)) {
//                    throw new CommonException("Attempt to override non empty leaderboard contributions for round: " + roundId +
//                            ", old: " + this.leaderboardContributions.get(roundId) + ", new: " + leaderboardContributions);
//                }
            }
            this.leaderboardContributions.put(roundId, leaderboardContributions);
        }
    }

    public void setModel(Double model) {
        this.model = model;
    }

    public Double getModel() {
        return model;
    }

    public Long getLossLimit() {
        return lossLimit;
    }

    public void setLossLimit(Long lossLimit) {
        this.lossLimit = lossLimit;
    }

    public Long getSessionTimeLimit() {
        return sessionTimeLimit;
    }

    public void setSessionTimeLimit(Long sessionTimeLimit) {
        this.sessionTimeLimit = sessionTimeLimit;
    }

    public boolean isLossLimitReminderTriggered() {
        return lossLimitReminderTriggered;
    }

    public void setLossLimitReminderTriggered(boolean lossLimitReminderTriggered) {
        this.lossLimitReminderTriggered = lossLimitReminderTriggered;
    }

    public boolean isSessionTimerReminderTriggered() {
        return sessionTimerReminderTriggered;
    }

    public void setSessionTimerReminderTriggered(boolean sessionTimerReminderTriggered) {
        this.sessionTimerReminderTriggered = sessionTimerReminderTriggered;
    }

    public Long getUnfinishedRoundPayout() {
        return unfinishedRoundPayout;
    }

    public void setUnfinishedRoundPayout(Long unfinishedRoundPayout) {
        this.unfinishedRoundPayout = unfinishedRoundPayout;
    }

    public Double getUnjPlayerCurrencySideBet() {
        return unjPlayerCurrencySideBet;
    }

    public void setUnjPlayerCurrencySideBet(Double unjPlayerCurrencySideBet) {
        this.unjPlayerCurrencySideBet = unjPlayerCurrencySideBet;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, leaderboardContributions);
        kryo.writeObjectOrNull(output, model, Double.class);
        kryo.writeObjectOrNull(output, lossLimit, Long.class);
        kryo.writeObjectOrNull(output, sessionTimeLimit, Long.class);
        output.writeBoolean(lossLimitReminderTriggered);
        output.writeBoolean(sessionTimerReminderTriggered);
        kryo.writeObjectOrNull(output, unfinishedRoundPayout, Long.class);
        kryo.writeObjectOrNull(output, unjPlayerCurrencySideBet, Double.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        if (version > 0) {
            leaderboardContributions = (Map<Long, Map<Long, Double>>) kryo.readClassAndObject(input);
        } else {
            leaderboardContributions.put(0L, (Map<Long, Double>) kryo.readClassAndObject(input));
        }

        if (version > 1) {
            model = kryo.readObjectOrNull(input, Double.class);
        }

        if (version > 2) {
            lossLimit = kryo.readObjectOrNull(input, Long.class);
            sessionTimeLimit = kryo.readObjectOrNull(input, Long.class);
        }
        if (version > 3) {
            lossLimitReminderTriggered = input.readBoolean();
            sessionTimerReminderTriggered = input.readBoolean();
        }
        if (version > 4) {
            unfinishedRoundPayout = kryo.readObjectOrNull(input, Long.class);
        }
        if (version > 5) {
            unjPlayerCurrencySideBet = kryo.readObjectOrNull(input, Double.class);
        }
    }

    @Override
    public String toString() {
        return "GameSessionExtendedProperties{" +
                "leaderboardContributions=" + leaderboardContributions +
                ",model=" + model +
                ",lossLimit=" + lossLimit +
                ",sessionTimeLimit=" + sessionTimeLimit +
                ",lossLimitReminderTriggered=" + lossLimitReminderTriggered +
                ",sessionTimerReminderTriggered=" + sessionTimerReminderTriggered +
                ",unfinishedRoundPayout=" + unfinishedRoundPayout +
                ",unjPlayerCurrencySideBet=" + unjPlayerCurrencySideBet +
                '}';
    }
}
