package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.common.SeatBullet;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IRoomBattlegroundInfo;
import com.betsoft.casino.utils.TObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 06.06.17.
 */
public class GetRoomInfoResponse extends TObject implements IGetRoomInfoResponse<Seat, Enemy, RoomEnemy, MinePlace> {
    private long roomId;
    private String name;
    private short maxSeats;
    private double minBuyIn;
    private double stake;
    private double playerStake;
    //state for lobby not used, return based on seats. playing if seats>0, waiting if not
    private RoomState state;
    private List<Seat> seats;
    //time to next state in seconds, -1 for unknown;
    private int ttnx;
    private int width;
    private int height;
    private List<Enemy> enemies;
    private List<RoomEnemy> roomEnemies;
    private int alreadySitInNumber;
    private long alreadySitInAmmoCount;
    private long alreadySitInBalance;
    private double alreadySitInWin;
    private int mapId;
    private String subround;
    private List<Integer> ammoValues;
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
    private Map<Integer, List<Integer>> reels;
    private Long endTime;
    private Map<Integer, Double> gemPrizes;
    private Boolean isOwner;
    private Boolean isKicked;

    public GetRoomInfoResponse(long date, long roomId, int rid, String name, short maxSeats, double minBuyIn,
                               double stake, double playerStake, RoomState state, List<ITransportSeat> seats,
                               int ttnx, int width, int height, List<ITransportEnemy> enemies,
                               List<IRoomEnemy> roomEnemies, int alreadySitInNumber, long alreadySitInAmmoCount,
                               long alreadySitInBalance, double alreadySitInWin, int mapId,
                               String subround, List<Integer> ammoValues, List<IMinePlace> mines,
                               Map<Long, Integer> freezeTime, boolean immortalBoss, long roundId,
                               Map<Integer, Integer> seatGems, IActiveCashBonusSession activeCashBonusSession,
                               ITournamentSession tournamentSession, int betLevel, Map<Long, Integer> enemiesModes,
                               Set<SeatBullet> allBullets, IRoomBattlegroundInfo battlegroundInfo,
                               Map<Integer, List<Integer>> reels, Map<Integer, Double> gemPrizes) {
        super(date, rid);
        this.roomId = roomId;
        this.name = name;
        if (seats != null) {
            this.seats = Seat.convert(seats);
        }
        this.maxSeats = maxSeats;
        this.minBuyIn = minBuyIn;
        this.stake = stake;
        this.playerStake = playerStake;
        this.state = state;
        this.ttnx = ttnx;
        this.width = width;
        this.height = height;
        if (enemies != null) {
            this.enemies = Enemy.convert(enemies);
        }
        if (roomEnemies != null) {
            this.roomEnemies = RoomEnemy.convert(roomEnemies);
        }
        this.alreadySitInNumber = alreadySitInNumber;
        this.alreadySitInAmmoCount = alreadySitInAmmoCount;
        this.alreadySitInBalance = alreadySitInBalance;
        this.alreadySitInWin = alreadySitInWin;
        this.mapId = mapId;
        this.subround = subround;
        this.ammoValues = ammoValues;
        if (mines != null) {
            this.mines = MinePlace.convert(mines);
        }
        this.freezeTime = freezeTime;
        this.immortalBoss = immortalBoss;
        this.roundId = roundId;
        this.seatGems = seatGems;
        if (activeCashBonusSession != null) {
            this.cashBonusInfo = new CashBonusInfo(activeCashBonusSession.getId(),
                    activeCashBonusSession.getAwardDate(),
                    activeCashBonusSession.getExpirationDate(),
                    activeCashBonusSession.getBalance(), activeCashBonusSession.getAmount(),
                    activeCashBonusSession.getAmountToRelease(), activeCashBonusSession.getStatus());
        }
        if (tournamentSession != null) {
            this.tournamentInfo = new TournamentInfo(tournamentSession.getTournamentId(), tournamentSession.getName(),
                    tournamentSession.getState(), tournamentSession.getStartDate(), tournamentSession.getEndDate(),
                    tournamentSession.getBalance(), tournamentSession.getBuyInPrice(),
                    tournamentSession.getBuyInAmount(), tournamentSession.isReBuyAllowed(),
                    tournamentSession.getReBuyPrice(), tournamentSession.getReBuyAmount(),
                    tournamentSession.getReBuyCount(), tournamentSession.getReBuyLimit(),
                    tournamentSession.isResetBalanceAfterRebuy());
        }

        this.betLevel = betLevel;
        this.enemiesModes = enemiesModes;
        this.allBullets = allBullets;
        this.needWaitingWhenEnemiesLeave = false;
        if (battlegroundInfo != null) {
            this.battlegroundInfo = new RoomBattlegroundInfo(battlegroundInfo.getBuyIn(), battlegroundInfo.isBuyInConfirmed(),
                    battlegroundInfo.getTimeToStart(), battlegroundInfo.getKingsOfHill(), battlegroundInfo.getScore(),
                    battlegroundInfo.getRank(), battlegroundInfo.getPot(), battlegroundInfo.getPotTaxPercent(), battlegroundInfo.getJoinUrl(),
                    battlegroundInfo.getConfirmedSeatsId());
        }
        this.reels = reels;
        this.gemPrizes = gemPrizes;
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
    public int getBossNumberShots() {
        return bossNumberShots;
    }

    @Override
    public void setBossNumberShots(int bossNumberShots) {
        this.bossNumberShots = bossNumberShots;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public short getMaxSeats() {
        return maxSeats;
    }

    public void setMaxSeats(short maxSeats) {
        this.maxSeats = maxSeats;
    }

    @Override
    public double getMinBuyIn() {
        return minBuyIn;
    }

    public void setMinBuyIn(int minBuyIn) {
        this.minBuyIn = minBuyIn;
    }

    public void setMinBuyIn(double minBuyIn) {
        this.minBuyIn = minBuyIn;
    }

    @Override
    public double getStake() {
        return stake;
    }

    public void setStake(double stake) {
        this.stake = stake;
    }

    @Override
    public double getPlayerStake() {
        return playerStake;
    }

    public void setPlayerStake(double playerStake) {
        this.playerStake = playerStake;
    }

    @Override
    public RoomState getState() {
        return state;
    }

    @Override
    public void setState(RoomState state) {
        this.state = state;
    }

    @Override
    public List<Seat> getSeats() {
        return seats;
    }

    @Override
    public int getTtnx() {
        return ttnx;
    }

    public void setTtnx(int ttnx) {
        this.ttnx = ttnx;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public List<Enemy> getEnemies() {
        return enemies;
    }

    @Override
    public List<RoomEnemy> getRoomEnemies() {
        return roomEnemies;
    }

    @Override
    public int getAlreadySitInNumber() {
        return alreadySitInNumber;
    }

    public void setAlreadySitInNumber(int alreadySitInNumber) {
        this.alreadySitInNumber = alreadySitInNumber;
    }

    @Override
    public long getAlreadySitInAmmoCount() {
        return alreadySitInAmmoCount;
    }

    public void setAlreadySitInAmmoCount(long alreadySitInAmmoCount) {
        this.alreadySitInAmmoCount = alreadySitInAmmoCount;
    }

    @Override
    public long getAlreadySitInBalance() {
        return alreadySitInBalance;
    }

    public void setAlreadySitInBalance(long alreadySitInBalance) {
        this.alreadySitInBalance = alreadySitInBalance;
    }

    @Override
    public double getAlreadySitInWin() {
        return alreadySitInWin;
    }

    public void setAlreadySitInWin(double alreadySitInWin) {
        this.alreadySitInWin = alreadySitInWin;
    }

    @Override
    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    @Override
    public String getSubround() {
        return subround;
    }

    public void setSubround(String subround) {
        this.subround = subround;
    }

    @Override
    public List<Integer> getAmmoValues() {
        return ammoValues;
    }

    public void setAmmoValues(List<Integer> ammoValues) {
        this.ammoValues = ammoValues;
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

    @Override
    public IRoomBattlegroundInfo getBattlegroundInfo() {
        return battlegroundInfo;
    }

    @Override
    public boolean isOwner() {
        return this.isOwner;
    }

    @Override
    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public void setBattlegroundInfo(IRoomBattlegroundInfo battlegroundInfo) {
        this.battlegroundInfo = (RoomBattlegroundInfo) battlegroundInfo;
    }

    public Map<Integer, List<Integer>> getReels() {
        return reels;
    }

    public void setReels(Map<Integer, List<Integer>> reels) {
        this.reels = reels;
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
    public Map<Integer, Double> getGemPrizes() {
        return gemPrizes;
    }

    @Override
    public void setGemPrizes(Map<Integer, Double> gemPrizes) {
        this.gemPrizes = gemPrizes;
    }

    public Boolean getKicked() {
        return isKicked;
    }

    public void setKicked(Boolean kicked) {
        isKicked = kicked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetRoomInfoResponse that = (GetRoomInfoResponse) o;

        if (roomId != that.roomId) return false;
        if (rid != that.rid) return false;
        if (maxSeats != that.maxSeats) return false;
        if (minBuyIn != that.minBuyIn) return false;
        if (ttnx != that.ttnx) return false;
        if (width != that.width) return false;
        if (height != that.height) return false;
        if (Double.compare(that.stake, stake) != 0) return false;
        if (!name.equals(that.name)) return false;
        if (!subround.equals(that.subround)) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        return (int) (roomId ^ (roomId >>> 32));
    }

    @Override
    public String toString() {
        return "GetRoomInfoResponse{" +
                "date=" + date +
                ", rid=" + rid +
                ", roomId=" + roomId +
                ", name='" + name + '\'' +
                ", maxSeats=" + maxSeats +
                ", minBuyIn=" + minBuyIn +
                ", stake=" + stake +
                ", playerStake=" + playerStake +
                ", state=" + state +
                ", seats=" + seats +
                ", ttnx=" + ttnx +
                ", width=" + width +
                ", height=" + height +
                ", enemies=" + enemies +
                ", roomEnemies=" + roomEnemies +
                ", alreadySitInNumber=" + alreadySitInNumber +
                ", alreadySitInAmmoCount=" + alreadySitInAmmoCount +
                ", alreadySitInBalance=" + alreadySitInBalance +
                ", alreadySitInWin=" + alreadySitInWin +
                ", mapId=" + mapId +
                ", subround='" + subround + '\'' +
                ", ammoValues=" + ammoValues +
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
                ", reels=" + reels +
                ", endTime=" + endTime +
                ", gemPrizes=" + gemPrizes +
                ", isOwner=" + isOwner +
                ", isKicked=" + isKicked +
                '}';
    }
}
