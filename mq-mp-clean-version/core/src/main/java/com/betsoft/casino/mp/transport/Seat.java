package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IAvatar;
import com.betsoft.casino.mp.model.ITransportSeat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User: flsh
 * Date: 09.06.17.
 */
public class Seat implements ITransportSeat, Serializable {
    private int id;
    private String nickname;
    private long enterDate;
    private double totalScore;
    private double currentScore;
    private Avatar avatar;
    private int specialWeaponId;
    private int level;
    private int unplayedFreeShots;
    private double totalDamage;
    private int betLevel;
    private long roundWin;

    public Seat() {
    }

    public Seat(int id, String nickname, long enterDate, double totalScore, double currentScore,
                IAvatar avatar, int specialWeaponId, int level, int unplayedFreeShots, double totalDamage, long roundWin) {
        this.id = id;
        this.nickname = nickname;
        this.enterDate = enterDate;
        this.totalScore = totalScore;
        this.currentScore = currentScore;
        this.avatar = new Avatar(avatar.getBorderStyle(), avatar.getHero(), avatar.getBackground());
        this.specialWeaponId = specialWeaponId;
        this.level = level;
        this.unplayedFreeShots = unplayedFreeShots;
        this.totalDamage = totalDamage;
        this.betLevel = 1;
        this.roundWin = roundWin;
    }

    public static List<Seat> convert(List<ITransportSeat> seats) {
        List<Seat> result = new ArrayList<>();
        for (ITransportSeat seat : seats) {
            if (seat instanceof Seat) {
                result.add((Seat) seat);
            } else {
                result.add(new Seat(seat.getId(), seat.getNickname(), seat.getEnterDate(), seat.getTotalScore(),
                        seat.getCurrentScore(), seat.getAvatar(), seat.getSpecialWeaponId(), seat.getLevel(),
                        seat.getUnplayedFreeShots(), seat.getTotalDamage(), seat.getRoundWin()));
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
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public long getEnterDate() {
        return enterDate;
    }

    @Override
    public void setEnterDate(long enterDate) {
        this.enterDate = enterDate;
    }

    @Override
    public double getTotalScore() {
        return totalScore;
    }

    @Override
    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public double getCurrentScore() {
        return currentScore;
    }

    @Override
    public void setCurrentScore(double currentScore) {
        this.currentScore = currentScore;
    }

    @Override
    public IAvatar getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(IAvatar avatar) {
        this.avatar = new Avatar(avatar.getBorderStyle(), avatar.getHero(), avatar.getBackground());
    }

    @Override
    public int getSpecialWeaponId() {
        return specialWeaponId;
    }

    @Override
    public void setSpecialWeaponId(int specialWeaponId) {
        this.specialWeaponId = specialWeaponId;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getUnplayedFreeShots() {
        return unplayedFreeShots;
    }

    @Override
    public void setUnplayedFreeShots(int unplayedFreeShots) {
        this.unplayedFreeShots = unplayedFreeShots;
    }

    public double getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(double damage) {
        this.totalDamage = damage;
    }

    public int getBetLevel() {
        return betLevel;
    }

    public void setBetLevel(int betLevel) {
        this.betLevel = betLevel;
    }

    public long getRoundWin() {
        return roundWin;
    }

    public void setRoundWin(long roundWin) {
        this.roundWin = roundWin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return id == seat.id && enterDate == seat.enterDate && Double.compare(seat.totalScore, totalScore) == 0
                && Double.compare(seat.currentScore, currentScore) == 0 && specialWeaponId == seat.specialWeaponId
                && level == seat.level && unplayedFreeShots == seat.unplayedFreeShots
                && Double.compare(seat.totalDamage, totalDamage) == 0
                && betLevel == seat.betLevel && Double.compare(seat.roundWin, roundWin) == 0
                && Objects.equals(nickname, seat.nickname) && Objects.equals(avatar, seat.avatar);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + nickname.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Seat[" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", enterDate=" + enterDate +
                ", totalScore=" + totalScore +
                ", currentScore=" + currentScore +
                ", avatar=" + avatar +
                ", avatar=" + specialWeaponId +
                ", level=" + level +
                ", unplayedFreeShots=" + unplayedFreeShots +
                ", totalDamage=" + totalDamage +
                ", betLevel=" + betLevel +
                ", roundWin=" + roundWin +
                ']';
    }
}
