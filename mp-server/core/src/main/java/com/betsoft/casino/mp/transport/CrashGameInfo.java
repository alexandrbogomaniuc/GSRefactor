package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.maxcrashgame.model.CrashRoundInfo;
import com.betsoft.casino.mp.model.ICrashGameInfo;
import com.betsoft.casino.mp.model.ICrashRoundInfo;
import com.betsoft.casino.mp.model.ITransportSeat;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.model.battleground.ITransportObserver;
import com.betsoft.casino.mp.model.onlineplayer.Friend;
import com.betsoft.casino.utils.TObject;

import java.util.*;

public class CrashGameInfo extends TObject implements ICrashGameInfo {
    private long roomId;
    private int mapId;
    private long startTime;
    private RoomState state;
    private List<Seat> seats;
    private long roundId;
    private int nextMapId;
    private long ttnx;
    private final List<CrashRoundInfo> multHistory;
    private final List<CrashBetInfo> bets = new ArrayList<>();
    private final String function;
    private long canceledBetAmount;
    private final Map<String, Long> canceledBetAmounts = new HashMap<>();
    private final List<com.betsoft.casino.mp.maxblastchampions.model.CrashRoundInfo> battleMultHistory;

    private double kilometerMult;
    private Double rakePercent;
    private Long maxPlayerProfitInRound;
    private Long totalPlayersProfitInRound;
    private Double maxMultiplier;
    private Integer maxRoomPlayers;
    private Long buyIn;
    private boolean pending;
    private Double currentMult;
    private Boolean crash;
    private double timeSpeedMult;
    private Long allEjectedTime;

    private List<ITransportObserver> observers;

    private List<Friend> friends;

    private Boolean isOwner;

    private Short minSeats;

    public CrashGameInfo(long date, int rid, long roomId, int mapId, long startTime, RoomState state, List<ITransportSeat> seats, long roundId,
                         List<ICrashRoundInfo> multHistory, int nextMapId, long ttnx, String function, boolean isBattleGame, double kilometerMult, Double rakePercent) {
        super(date, rid);
        this.roomId = roomId;
        this.mapId = mapId;
        this.startTime = startTime;
        this.state = state;
        this.seats = Seat.convert(seats);
        this.roundId = roundId;
        this.nextMapId = nextMapId;
        this.ttnx = ttnx;
        this.battleMultHistory = isBattleGame ? convertBattleCrashRoundInfo(multHistory) : Collections.emptyList();
        this.multHistory = isBattleGame ? Collections.emptyList() : convertCrashRoundInfo(multHistory);
        this.function = function;
        this.kilometerMult = kilometerMult;
        this.rakePercent = rakePercent;
    }

    private List<CrashRoundInfo> convertCrashRoundInfo(List<ICrashRoundInfo> multHistories) {
        List<CrashRoundInfo> result = new ArrayList<>();
        for (ICrashRoundInfo multHistory : multHistories) {
            result.add(new CrashRoundInfo(multHistory.getMult(), multHistory.getStartTime(),
                        multHistory.getRoundId(), multHistory.getBets(), multHistory.getSalt(), multHistory.getToken()));
        }
        return result;
    }

    private List<com.betsoft.casino.mp.maxblastchampions.model.CrashRoundInfo> convertBattleCrashRoundInfo(List<ICrashRoundInfo> multHistories) {
        List<com.betsoft.casino.mp.maxblastchampions.model.CrashRoundInfo> result = new ArrayList<>();
        for (ICrashRoundInfo multHistory : multHistories) {
            result.add(new com.betsoft.casino.mp.maxblastchampions.model.CrashRoundInfo(multHistory.getMult(), multHistory.getStartTime(),
                    multHistory.getRoundId(), multHistory.getBets(), multHistory.getSalt(), multHistory.getToken(), multHistory.getWinners(), multHistory.getKilometerMult()));
        }
        return result;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    public List<com.betsoft.casino.mp.maxblastchampions.model.CrashRoundInfo> getBattleMultHistory() {
        return battleMultHistory;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public void addBet(String betId, String name, long amount, boolean auto, double mult, long ejectTime, Double autoPlayMultiplier) {
        CrashBetInfo crashBetInfo = new CrashBetInfo(name, amount, auto, mult, betId, ejectTime, autoPlayMultiplier);
        bets.add(crashBetInfo);
    }

    @Override
    public void addBet(String betId, String name, long amount, boolean auto, double mult, long ejectTime, Double autoPlayMultiplier, boolean isReserved) {
        CrashBetInfo crashBetInfo = new CrashBetInfo(name, amount, auto, mult, betId, ejectTime, autoPlayMultiplier, isReserved);
        bets.add(crashBetInfo);
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public RoomState getState() {
        return state;
    }

    @Override
    public void setState(RoomState state) {
        this.state = state;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public void setNextMapId(int nextMapId) {
        this.nextMapId = nextMapId;
    }

    public void setTtnx(long ttnx) {
        this.ttnx = ttnx;
    }

    public long getTtnx() {
        return this.ttnx;
    }

    public long getCanceledBetAmount() {
        return canceledBetAmount;
    }

    public Double getRakePercent() {
        return rakePercent;
    }

    public Long getMaxPlayerProfitInRound() {
        return maxPlayerProfitInRound;
    }

    public void setMaxPlayerProfitInRound(Long maxPlayerProfitInRound) {
        this.maxPlayerProfitInRound = maxPlayerProfitInRound;
    }

    public Long getTotalPlayersProfitInRound() {
        return totalPlayersProfitInRound;
    }

    public void setTotalPlayersProfitInRound(Long totalPlayersProfitInRound) {
        this.totalPlayersProfitInRound = totalPlayersProfitInRound;
    }

    public Double getMaxMultiplier() {
        return maxMultiplier;
    }

    public void setMaxMultiplier(Double maxMultiplier) {
        this.maxMultiplier = maxMultiplier;
    }

    @Override
    public void setMaxRoomPlayers(int maxRoomPlayers) {
        this.maxRoomPlayers = maxRoomPlayers;
    }

    @Override
    public void setPending(boolean isPending) {
        this.pending = isPending;
    }

    public int getMaxRoomPlayers() {
        return maxRoomPlayers;
    }

    @Override
    public void setCanceledBetAmount(long canceledBetAmount) {
        this.canceledBetAmount = canceledBetAmount;
    }

    @Override
    public void setCanceledBetAmount(String nickname, long canceledBetAmount) {
        this.canceledBetAmounts.put(nickname, canceledBetAmount);
    }



    public Long getBuyIn() {
        return buyIn;
    }

    @Override
    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    public Double getCurrentMult() {
        return currentMult;
    }

    @Override
    public void setCurrentMult(Double currentMult) {
        this.currentMult = currentMult;
    }

    public Boolean getCrash() {
        return crash;
    }

    @Override
    public void setCrash(Boolean crash) {
        this.crash = crash;
    }

    public double getTimeSpeedMult() {
        return timeSpeedMult;
    }

    @Override
    public void setTimeSpeedMult(double timeSpeedMult) {
        this.timeSpeedMult = timeSpeedMult;
    }

    public Long getAllEjectedTime() {
        return allEjectedTime;
    }

    @Override
    public void setAllEjectedTime(Long allEjectedTime) {
        this.allEjectedTime = allEjectedTime;
    }

    @Override
    public void setObservers(List<ITransportObserver> observers) {
        this.observers = observers;
    }

    @Override
    public void setFriends(List<Friend> friends) { this.friends = friends; }

    @Override
    public void setOwner(Boolean isOwner) { this.isOwner = isOwner; }

    @Override
    public void setMinSeats(Short minSeats) { this.minSeats = minSeats; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrashGameInfo that = (CrashGameInfo) o;
        return roomId == that.roomId && mapId == that.mapId && startTime == that.startTime && roundId == that.roundId
                && nextMapId == that.nextMapId && ttnx == that.ttnx && state == that.state
                && Objects.equals(multHistory, that.multHistory)
                && Objects.equals(bets, that.bets)
                && Objects.equals(function, that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roomId, mapId, startTime, state, roundId, nextMapId, ttnx, multHistory, bets, function);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashGameInfo.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("roomId=" + roomId)
                .add("mapId=" + mapId)
                .add("startTime=" + startTime)
                .add("state=" + state)
                .add("seats=" + seats)
                .add("roundId=" + roundId)
                .add("nextMapId=" + nextMapId)
                .add("ttnx=" + ttnx)
                .add("multHistory=" + multHistory)
                .add("bets=" + bets)
                .add("function=" + function)
                .add("kilometerMult=" + kilometerMult)
                .add("canceledBetAmount=" + canceledBetAmount)
                .add("rakePercent=" + rakePercent)
                .add("maxPlayerProfitInRound=" + maxPlayerProfitInRound)
                .add("totalPlayersProfitInRound=" + totalPlayersProfitInRound)
                .add("maxMultiplier=" + maxMultiplier)
                .add("maxRoomPlayers=" + maxRoomPlayers)
                .add("buyIn=" + buyIn)
                .add("pending=" + pending)
                .add("currentMult=" + currentMult)
                .add("crash=" + crash)
                .add("timeSpeedMult=" + timeSpeedMult)
                .add("allEjectedTime=" + allEjectedTime)
                .add("observers=" + observers)
                .add("friends=" + friends)
                .add("isOwner=" + isOwner)
                .add("minSeats=" + minSeats)
                .add("canceledBetAmounts=" + canceledBetAmounts)
                .toString();
    }
}
