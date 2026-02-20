package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IAvatar;
import com.betsoft.casino.mp.model.ISitInResponse;
import com.betsoft.casino.mp.model.IWeapon;
import com.betsoft.casino.utils.TObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User: flsh
 * Date: 09.06.17.
 */
public class SitInResponse extends TObject implements ISitInResponse {
    private int id;
    private String nickname;
    private long enterDate;
    private long ammoAmount;
    private long balance;
    private List<Weapon> weapons;
    private List<Double> weaponLootBoxPrices;
    private Avatar avatar;
    private boolean showRefreshBalanceButton;
    private int level;
    private boolean frbMode;
    private long frbBalance;
    //FRB, CASHBONUS, REAL,TOURNAMENT
    private String mode;
    private Double maxMultiplier;
    private Long maxPlayerProfitInRound;
    private Long totalPlayersProfitInRound;
    private Double rakePercent;

    public SitInResponse(long date, int rid, int id, String nickname, long enterDate, long ammoAmount, long balance,
                         Avatar avatar, List<Weapon> weapons, List<Double> weaponLootBoxPrices,
                         boolean showRefreshBalanceButton, int level, boolean frbMode, long frbBalance, String mode, double rakePercent) {
        super(date, rid);
        this.id = id;
        this.nickname = nickname;
        this.enterDate = enterDate;
        this.ammoAmount = ammoAmount;
        this.balance = balance;
        this.avatar = avatar;
        this.weapons = weapons;
        this.weaponLootBoxPrices = weaponLootBoxPrices;
        this.showRefreshBalanceButton = showRefreshBalanceButton;
        this.level = level;
        this.frbMode = frbMode;
        this.frbBalance = frbBalance;
        this.mode = mode;
        this.rakePercent = rakePercent;
    }

    public SitInResponse(long date, int id, String nickname, long enterDate, long ammoAmount, long balance,
                         IAvatar avatar, List<IWeapon> weapons, List<Double> weaponLootBoxPrices,
                         boolean showRefreshBalanceButton, int level, boolean frbMode, long frbBalance, String mode, double rakePercent) {
        super(date, TObject.SERVER_RID);
        this.id = id;
        this.nickname = nickname;
        this.enterDate = enterDate;
        this.ammoAmount = ammoAmount;
        this.balance = balance;
        if (avatar != null) {
            if (avatar instanceof Avatar) {
                this.avatar = (Avatar) avatar;
            } else {
                this.avatar = new Avatar(avatar.getBorderStyle(), avatar.getHero(), avatar.getBackground());
            }
        }
        if (weapons != null) {
            this.weapons = convert(weapons);
        }
        this.weaponLootBoxPrices = weaponLootBoxPrices;
        this.showRefreshBalanceButton = showRefreshBalanceButton;
        this.level = level;
        this.frbMode = frbMode;
        this.frbBalance = frbBalance;
        this.mode = mode;
        this.rakePercent = rakePercent;
    }

    private List<Weapon> convert(List<IWeapon> weapons) {
        List<Weapon> result = new ArrayList<>(weapons.size());
        for (IWeapon weapon : weapons) {
            if (weapon instanceof Weapon) {
                result.add((Weapon) weapon);
            } else {
                result.add(new Weapon(weapon.getType().getId(), weapon.getShots()));
            }
        }
        return result;
    }

    public Double getRakePercent() {
        return rakePercent;
    }

    public void setRakePercent(Double rakePercent) {
        this.rakePercent = rakePercent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getEnterDate() {
        return enterDate;
    }

    public void setEnterDate(long enterDate) {
        this.enterDate = enterDate;
    }

    public long getAmmoAmount() {
        return ammoAmount;
    }

    public void setAmmoAmount(long ammoAmount) {
        this.ammoAmount = ammoAmount;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public boolean isShowRefreshBalanceButton() {
        return showRefreshBalanceButton;
    }

    public void setShowRefreshBalanceButton(boolean showRefreshBalanceButton) {
        this.showRefreshBalanceButton = showRefreshBalanceButton;
    }

    public boolean isFrbMode() {
        return frbMode;
    }

    public void setFrbMode(boolean frbMode) {
        this.frbMode = frbMode;
    }

    public long getFrbBalance() {
        return frbBalance;
    }

    public void setFrbBalance(long frbBalance) {
        this.frbBalance = frbBalance;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getLevel() {
        return level;
    }

    public Double getMaxMultiplier() {
        return maxMultiplier;
    }

    @Override
    public void setMaxMultiplier(Double maxMultiplier) {
        this.maxMultiplier = maxMultiplier;
    }

    public Long getMaxPlayerProfitInRound() {
        return maxPlayerProfitInRound;
    }

    @Override
    public void setMaxPlayerProfitInRound(Long maxPlayerProfitInRound) {
        this.maxPlayerProfitInRound = maxPlayerProfitInRound;
    }

    public Long getTotalPlayersProfitInRound() {
        return totalPlayersProfitInRound;
    }

    @Override
    public void setTotalPlayersProfitInRound(Long totalPlayersProfitInRound) {
        this.totalPlayersProfitInRound = totalPlayersProfitInRound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SitInResponse that = (SitInResponse) o;
        return id == that.id &&
                enterDate == that.enterDate &&
                ammoAmount == that.ammoAmount &&
                balance == that.balance &&
                Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, nickname, enterDate, ammoAmount, balance);
    }

    @Override
    public String toString() {
        return "SitInResponse[" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", enterDate=" + enterDate +
                ", ammoAmount=" + ammoAmount +
                ", balance=" + balance +
                ", date=" + date +
                ", rid=" + rid +
                ", showRefreshBalanceButton=" + showRefreshBalanceButton +
                ", weapons=" + weapons +
                ", weaponLootBoxPrices=" + weaponLootBoxPrices +
                ", avatar=" + avatar +
                ", level=" + level +
                ", frbMode=" + frbMode +
                ", frbBalance=" + frbBalance +
                ", mode=" + mode +
                ", maxMultiplier=" + maxMultiplier +
                ", maxPlayerProfitInRound=" + maxPlayerProfitInRound +
                ", totalPlayersProfitInRound=" + totalPlayersProfitInRound +
                ", rakePercent=" + rakePercent +
                ']';
    }
}
