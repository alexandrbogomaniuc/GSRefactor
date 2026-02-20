package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.common.SeatBullet;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.transport.Observer;
import com.betsoft.casino.mp.model.onlineplayer.Friend;
import com.betsoft.casino.utils.TObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class FullGameInfo extends TObject implements IFullGameInfo<Observer, RoomEnemy, Seat, MinePlace> {
    private int mapId;
    private String subround;
    private long startTime;
    private RoomState state;
    private List<RoomEnemy> roomEnemies;
    private List<Seat> seats;
    private List<MinePlace> mines;
    private Map<Long, Integer> freezeTime;
    private boolean immortalBoss;
    private long roundId;
    private Map<Integer, Integer> seatGems;
    private CashBonusInfo cashBonusInfo;
    private TournamentInfo tournamentInfo;
    private int betLevel;
    private Map<Long, Integer> enemiesModes;
    private Set<SeatBullet> allBullets;
    private Integer fragments;
    private RoomBattlegroundInfo battlegroundInfo;
    private int bossNumberShots;
    private boolean needWaitingWhenEnemiesLeave;
    private long timeToStart;
    private Map<Integer, List<Integer>> reels;
    private Long endTime;
    private int currentPowerUpMultiplier;
    private Map<Integer, Double> gemPrizes;
    private List<Observer> observers;
    private List<Friend> friends;

    public FullGameInfo(long date, int rid, int mapId, String subround, long startTime, RoomState state,
                        List<IRoomEnemy> roomEnemies, List<ITransportSeat> seats, List<IMinePlace> mines,
                        Map<Long, Integer> freezeTime, boolean immortalBoss, long roundId,
                        Map<Integer, Integer> seatGems, int betLevel, Map<Long, Integer> enemiesModes,
                        Set<SeatBullet> allBullets, long timeToStart, Map<Integer, List<Integer>> reels,
                        int currentPowerUpMultiplier, Map<Integer, Double> gemPrizes) {
        super(date, rid);
        this.mapId = mapId;
        this.subround = subround;
        this.startTime = startTime;
        this.state = state;
        if (roomEnemies != null) {
            this.roomEnemies = RoomEnemy.convert(roomEnemies);
        }
        if (seats != null) {
            this.seats = Seat.convert(seats);
        }
        if (mines != null) {
            this.mines = MinePlace.convert(mines);
        }
        this.freezeTime = freezeTime;
        this.immortalBoss = immortalBoss;
        this.roundId = roundId;
        this.seatGems = seatGems;
        this.betLevel = betLevel;
        this.enemiesModes = enemiesModes;
        this.allBullets = allBullets;
        this.needWaitingWhenEnemiesLeave = false;
        this.timeToStart = timeToStart;
        this.reels = reels;
        this.currentPowerUpMultiplier = currentPowerUpMultiplier;
        this.gemPrizes = gemPrizes;
    }

    public int getCurrentPowerUpMultiplier() {
        return currentPowerUpMultiplier;
    }

    public void setCurrentPowerUpMultiplier(int currentPowerUpMultiplier) {
        this.currentPowerUpMultiplier = currentPowerUpMultiplier;
    }

    public Map<Integer, List<Integer>> getReels() {
        return reels;
    }

    public void setReels(Map<Integer, List<Integer>> reels) {
        this.reels = reels;
    }

    public Map<Integer, Double> getGemPrizes() {
        return gemPrizes;
    }

    public void setGemPrizes(Map<Integer, Double> gemPrizes) {
        this.gemPrizes = gemPrizes;
    }

    @Override
    public long getTimeToStart() {
        return timeToStart;
    }

    @Override
    public void setTimeToStart(long timeToStart) {
        this.timeToStart = timeToStart;
    }

    @Override
    public List<RoomEnemy> getRoomEnemies() {
        return roomEnemies;
    }

    public void setRoomEnemies(List<RoomEnemy> roomEnemies) {
        this.roomEnemies = roomEnemies;
    }

    @Override
    public RoomState getState() {
        return state;
    }

    public void setState(RoomState state) {
        this.state = state;
    }

    @Override
    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    @Override
    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String getSubround() {
        return subround;
    }

    public void setSubround(String subround) {
        this.subround = subround;
    }

    @Override
    public List<MinePlace> getMines() {
        return mines;
    }

    public void setMines(List<MinePlace> mines) {
        this.mines = mines;
    }

    @Override
    public Map<Long, Integer> getFreezeTime() {
        return freezeTime;
    }

    public void setFreezeTime(Map<Long, Integer> freezeTime) {
        this.freezeTime = freezeTime;
    }

    public int getBetLevel() {
        return betLevel;
    }

    public void setBetLevel(int betLevel) {
        this.betLevel = betLevel;
    }

    @Override
    public boolean isImmortalBoss() {
        return immortalBoss;
    }

    public void setImmortalBoss(boolean immortalBoss) {
        this.immortalBoss = immortalBoss;
    }

    @Override
    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    @Override
    public Map<Integer, Integer> getSeatGems() {
        return seatGems;
    }

    public void setSeatGems(Map<Integer, Integer> seatGems) {
        this.seatGems = seatGems;
    }

    public CashBonusInfo getCashBonusInfo() {
        return cashBonusInfo;
    }

    public void setCashBonusInfo(CashBonusInfo cashBonusInfo) {
        this.cashBonusInfo = cashBonusInfo;
    }

    public TournamentInfo getTournamentInfo() {
        return tournamentInfo;
    }

    public void setTournamentInfo(TournamentInfo tournamentInfo) {
        this.tournamentInfo = tournamentInfo;
    }

    public Map<Long, Integer> getEnemiesModes() {
        return enemiesModes;
    }

    public void setEnemiesModes(Map<Long, Integer> enemiesModes) {
        this.enemiesModes = enemiesModes;
    }

    public Set<SeatBullet> getAllBullets() {
        return allBullets;
    }

    public void setAllBullets(Set<SeatBullet> allBullets) {
        this.allBullets = allBullets;
    }

    public Integer getFragments() {
        return fragments;
    }

    public void setFragments(Integer fragments) {
        this.fragments = fragments;
    }

    public RoomBattlegroundInfo getBattlegroundInfo() {
        return battlegroundInfo;
    }

    public void setBattlegroundInfo(RoomBattlegroundInfo battlegroundInfo) {
        this.battlegroundInfo = battlegroundInfo;
    }

    @Override
    public int getBossNumberShots() {
        return bossNumberShots;
    }

    @Override
    public void setBossNumberShots(int bossNumberShots) {
        this.bossNumberShots = bossNumberShots;
    }

    @Override
    public boolean isNeedWaitingWhenEnemiesLeave() {
        return needWaitingWhenEnemiesLeave;
    }

    @Override
    public void setNeedWaitingWhenEnemiesLeave(boolean needWaitingWhenEnemiesLeave) {
        this.needWaitingWhenEnemiesLeave = needWaitingWhenEnemiesLeave;
    }

    @Override
    public Long getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Override
    public List<Observer> getObservers() {
        return observers;
    }

    @Override
    public void setObservers(List<Observer> observers) {
        this.observers = observers;
    }

    @Override
    public void setFriends(List<Friend> friends) { this.friends = friends; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FullGameInfo that = (FullGameInfo) o;

        return roomEnemies.equals(that.roomEnemies);
    }

    @Override
    public String toString() {
        return "FullGameInfo{" +
                "date=" + date +
                ", rid=" + rid +
                ", mapId=" + mapId +
                ", subround='" + subround + '\'' +
                ", startTime=" + startTime +
                ", state=" + state +
                ", roomEnemies=" + roomEnemies +
                ", seats=" + seats +
                ", mines=" + mines +
                ", freezeTime=" + freezeTime +
                ", immortalBoss=" + immortalBoss +
                ", roundId=" + roundId +
                ", seatGems=" + seatGems +
                ", cashBonusInfo=" + cashBonusInfo +
                ", tournamentInfo=" + tournamentInfo +
                ", betLevel=" + betLevel +
                ", enemiesModes=" + enemiesModes +
                ", allBullets=" + allBullets +
                ", fragments=" + fragments +
                ", battlegroundInfo=" + battlegroundInfo +
                ", bossNumberShots=" + bossNumberShots +
                ", needWaitingWhenEnemiesLeave=" + needWaitingWhenEnemiesLeave +
                ", timeToStart=" + timeToStart +
                ", reels=" + reels +
                ", endTime=" + endTime +
                ", currentPowerUpMultiplier=" + currentPowerUpMultiplier +
                ", gemPrizes=" + gemPrizes +
                ", observers=" + observers +
                ", friends=" + friends +
                '}';
    }
}
