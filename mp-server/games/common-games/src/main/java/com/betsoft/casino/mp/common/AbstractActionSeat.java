package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ConcurrentHashSet;
import com.abs.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.*;

/**
 * User: flsh
 * Date: 18.01.2022.
 */

/**
 * Abstract class for action players.
 */
public abstract class AbstractActionSeat<WEAPON extends IWeapon, PLAYER_ROUND_INFO extends IActionGamePlayerRoundInfo<?, ?, ?>,
        TREASURE extends ITreasure, RPI extends IActionRoomPlayerInfo, S extends ISeat>
        extends AbstractSingleNodeSeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S> 
        implements IActionGameSeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S> {
    public static final String ADD_COUNTER_WEAPON_DROPPED = "WEAPON_DROPPED";

    public static final String ADD_COUNTER_POWER_UP_MULT = "POWER_UP_MULT";
    private static final byte VERSION = 0;
    /** not used */
    protected IFreeShots freeShots;
    /** current weapons of player   {@link SpecialWeaponType} , {@link IWeapon} */
    protected Map<SpecialWeaponType, WEAPON> weapons = new EnumMap<>(SpecialWeaponType.class);
    protected int currentWeaponId = -1;
    /** compensation of weapons. Used for poor playing/return weapons in end of round {@link IWeaponSurplus}  */
    protected List<IWeaponSurplus> weaponSurplus = new ArrayList<>();
    protected Money compensateSpecialWeapons = Money.ZERO;
    protected Money totalReturnedSpecialWeapons = Money.ZERO;
    protected Map<TREASURE, Integer> roundTreasures = new HashMap<>();
    /** list of mines on map in current round  */
    protected List<MinePoint> seatMines = new ArrayList<>();
    protected Map<Long, Double> damageToEnemies = new HashMap<>();
    protected int totalTreasuresCount;

    protected Map<String, Boolean> mineStates = new HashMap<>();

    /** list of returned of weapons. Used for return unused mines   */
    protected List<IWeaponSurplus> weaponsReturned = new ArrayList<>();

    protected int hitCount;

    protected int missCount;

    protected int enemiesKilledCount;

    /** last actual shot message from client */
    protected transient IShot actualShot;

    protected int bulletsFired;

    /** list of bullets of player on map */
    protected Set<SeatBullet> bulletsOnMap;

    protected long totalBossPayout;

    protected double totalKillsXP;

    protected double totalTreasuresXP;

    protected Map<Integer, Integer> weaponFromWC = new HashMap<>();

    /** available ammo amount of player, if there are not enough ammo, an ammo purchase is made {@see BuyInHandler, ReBuyHandler} */
    protected int ammoAmount;

    protected int ammoAmountTotalInRound;

    protected AbstractActionSeat() {
    }

    protected AbstractActionSeat(RPI playerInfo, IGameSocketClient socketClient, double currentRate) {
        super(playerInfo, socketClient, currentRate);
    }

    protected AbstractActionSeat(RPI playerInfo, Money stake) {
        super(playerInfo, stake);
    }

    @Override
    public void initCurrentRoundInfo(RPI playerInfo) {
        super.initCurrentRoundInfo(playerInfo);
        this.freeShots = playerInfo.createNewFreeShots();
        this.weaponSurplus = new ArrayList<>();
        this.compensateSpecialWeapons = Money.ZERO;
        this.totalReturnedSpecialWeapons = Money.ZERO;
        this.seatMines = new ArrayList<>();
        this.damageToEnemies = new HashMap<>();
        this.totalTreasuresCount = 0;
        this.mineStates = new HashMap<>();
        this.weaponsReturned = new ArrayList<>();
        this.hitCount = 0;
        this.missCount = 0;
        this.bulletsFired = 0;
        this.totalBossPayout = 0;
        this.totalTreasuresXP = 0;
        this.totalKillsXP = 0;
        this.ammoAmountTotalInRound = 0;
    }

    /**
     * Get all mines on map for player
     * @return {@code List<IMinePlace>} list of all mines
     * @see IMinePlace
     * @see Coords
     */
    @Override
    public List<IMinePlace> getMinePlaces(Coords coords) {
        List<IMinePlace> res = new ArrayList<>();
        if (!seatMines.isEmpty()) {
            for (MinePoint seatMine : seatMines) {
                double x = coords.toScreenX(seatMine.getX(), seatMine.getY());
                double y = coords.toScreenY(seatMine.getX(), seatMine.getY());
                res.add(playerInfo.getNewMinePlace(seatMine.getTimePlace(), -1, getNumber(), (float) x, (float) y,
                        seatMine.getMineId(getNumber())));
            }
        }
        return res;
    }

    /**
     * Return free shots of player (not used)
     * @deprecated
     * @return {@link IFreeShots}
     */
    @Override
    public IFreeShots getFreeShots() {
        if (freeShots == null) {
            freeShots = playerInfo.createNewFreeShots();
        }
        return freeShots;
    }

    public MaxQuestWeaponMode getWeaponMode() {
        return playerInfo.getWeaponMode() == null ? MaxQuestWeaponMode.LOOT_BOX : playerInfo.getWeaponMode();
    }

    @Override
    public WEAPON getCurrentWeapon() {

        if(currentWeaponId < 0 || weapons == null) {
            return null;
        }

        SpecialWeaponType specialWeaponType = getSpecialWeaponType(currentWeaponId);
        if(specialWeaponType == null) {
            return null;
        }

        return weapons.get(specialWeaponType);
    }

    @Override
    public int getCurrentWeaponId() {
        return currentWeaponId;
    }

    @Override
    public Map<SpecialWeaponType, WEAPON> getWeapons() {
        return weapons;
    }

    /**
     * Checks if the player has a special weapon
     * @return true/false - player has special weapons for shot
     */
    @Override
    public boolean isAnyWeaponShotAvailable() {
        for (WEAPON weapon : weapons.values()) {
            if (weapon.getShots() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addWeapon(WEAPON weapon) {
        if (weapon != null) {
            weapons.get(weapon.getType()).addShots(weapon.getShots());
        }
    }

    @Override
    public int getSpecialWeaponId() {
        return currentWeaponId;
    }

    @Override
    public int getSpecialWeaponRemaining() {

        if(currentWeaponId < 0 || weapons == null) {
            return 0;
        }

        SpecialWeaponType specialWeaponType = getSpecialWeaponType(currentWeaponId);
        if(specialWeaponType == null) {
            return 0;
        }

        IWeapon weapon = weapons.get(specialWeaponType);
        if(weapon == null) {
            return 0;
        }

        return weapon.getShots();
    }

    /**
     * Decrement weapon count after using
     * @param weaponId - weaponId of used weapon for shot
     */
    @Override
    public void consumeSpecialWeapon(int weaponId) {
        SpecialWeaponType specialWeaponType = getSpecialWeaponType(weaponId);
        WEAPON weapon = weapons.get(specialWeaponType);
        weapon.addShots(-1);

        if (weapon.getShots() < 0) {
            throw new IllegalStateException("Special weapons amount reduced below zero");
        }
    }

    @Override
    public void setWeapons(Map<Integer, WEAPON> weapons) {
        for (Map.Entry<Integer, WEAPON> entry : weapons.entrySet()) {
            this.weapons.put(getSpecialWeaponType(entry.getKey()), entry.getValue());
        }
    }

    private SpecialWeaponType getSpecialWeaponType(int id) {
        return id < 0 ? null : SpecialWeaponType.values()[id];
    }


    /**
     * Add new special weapon to player
     * @param weaponId weaponId from SpecialWeaponType
     * @param shots number of shots for adding
     */
    @Override
    public void addWeapon(int weaponId, int shots) {
        SpecialWeaponType specialWeaponType = getSpecialWeaponType(weaponId);
        WEAPON weapon = weapons.get(specialWeaponType);
        if (weapon != null) {
            weapon.addShots(shots);
        } else if (specialWeaponType != null) {
            weapons.put(specialWeaponType, createWeapon(shots, specialWeaponType));
        } else {
            getLogger().error("Unknown special weapon, weaponId={}", weaponId);
            //todo: this impossible but may be throw exception ?
        }
    }

    @Override
    public void setWeapon(int weaponId) {
        currentWeaponId = weaponId;
    }

    /**
     * Reset player special weapons
     */
    @Override
    public void resetWeapons() {
        for (WEAPON weapon : weapons.values()) {
            weapon.setShots(0);
        }
    }

    @Override
    public void setWeaponSurplus(ArrayList<IWeaponSurplus> weaponSurplus) {
        this.weaponSurplus = weaponSurplus;
    }

    @Override
    public List<IWeaponSurplus> getWeaponSurplus() {
        return weaponSurplus;
    }

    @Override
    public Money getCompensateSpecialWeapons() {
        return compensateSpecialWeapons;
    }

    @Override
    public void setCompensateSpecialWeapons(Money compensateSpecialWeapons) {
        this.compensateSpecialWeapons = compensateSpecialWeapons;
    }

    @Override
    public Money getTotalReturnedSpecialWeapons() {
        return totalReturnedSpecialWeapons == null ? Money.ZERO : totalReturnedSpecialWeapons;
    }

    @Override
    public void setTotalReturnedSpecialWeapons(Money totalReturnedSpecialWeapons) {
        this.totalReturnedSpecialWeapons = totalReturnedSpecialWeapons;
    }

    @Override
    public void addTreasures(List<TREASURE> treasures) {
        for (TREASURE treasure : treasures) {
            roundTreasures.put(treasure, roundTreasures.getOrDefault(treasure, 0) + 1);
        }
    }

    @Override
    public Map<TREASURE, Integer> getRoundTreasures() {
        return roundTreasures;
    }

    @Override
    public List<MinePoint> getSeatMines() {
        return seatMines;
    }

    @Override
    public void setSeatMines(List<MinePoint> seatMines) {
        this.seatMines = seatMines;
    }

    @Override
    public Map<Long, Double> getDamageToEnemies() {
        return damageToEnemies;
    }

    public void setDamageToEnemies(Map<Long, Double> damageToEnemies) {
        this.damageToEnemies = damageToEnemies;
    }

    @Override
    public int getTotalTreasuresCount() {
        return totalTreasuresCount;
    }

    @Override
    public void setTotalTreasuresCount(int totalTreasuresCount) {
        this.totalTreasuresCount = totalTreasuresCount;
    }

    @Override
    public List<IWeaponSurplus> getWeaponsReturned() {
        return weaponsReturned == null ? new ArrayList<>() : weaponsReturned;
    }

    @Override
    public void setWeaponsReturned(List<IWeaponSurplus> weaponsReturned) {
        this.weaponsReturned = weaponsReturned;
    }

    @Override
    public Map<String, Boolean> getMineStates() {
        return mineStates == null ? new HashMap<>() : mineStates;
    }

    @Override
    public void setMineStates(Map<String, Boolean> mineStates) {
        this.mineStates = mineStates;
    }

    @Override
    public void addMineState(String mineId, boolean mineState) {
        this.mineStates.put(mineId, mineState);
    }

    @Override
    public int getHitCount() {
        return hitCount;
    }

    @Override
    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    @Override
    public void incrementHitsCount() {
        this.hitCount++;
    }

    @Override
    public int getMissCount() {
        return missCount;
    }

    @Override
    public void setMissCount(int missCount) {
        this.missCount = missCount;
    }

    @Override
    public void incrementMissCount() {
        this.missCount++;
    }

    @Override
    public int getEnemiesKilledCount() {
        return enemiesKilledCount;
    }

    @Override
    public void setEnemiesKilledCount(int enemiesKilledCount) {
        this.enemiesKilledCount = enemiesKilledCount;
    }

    @Override
    public void incCountEnemiesKilled() {
        this.enemiesKilledCount++;
    }

    public IShot getActualShot() {
        return actualShot;
    }

    public void setActualShot(IShot actualShot) {
        this.actualShot = actualShot;
    }

    @Override
    public void setBulletsFired(int bulletsFired) {
        this.bulletsFired = bulletsFired;
    }

    @Override
    public void incrementBulletsFired() {
        this.bulletsFired++;
    }

    @Override
    public int getBulletsFired() {
        return bulletsFired;
    }

    @Override
    public Set<SeatBullet> getBulletsOnMap() {
        if (bulletsOnMap == null) {
            bulletsOnMap = new ConcurrentHashSet<>();
        }
        return bulletsOnMap;
    }

    public void setBulletsOnMap(Set<SeatBullet> bulletsOnMap) {
        this.bulletsOnMap = bulletsOnMap;
    }

    /**
     *
     * @param seatBullet Add new bullet of player
     * @return boolean, true if bullet was added
     */
    @Override
    public boolean addSeatBullet(SeatBullet seatBullet) {

        String bulletId = seatBullet != null ? seatBullet.getBulletId() : null;

        if(StringUtils.isTrimmedEmpty(bulletId)) {
            getLogger().error("addSeatBullet: wrong BulletId={} in seatBullet={}", bulletId, seatBullet);
            return false;
        }

        Set<SeatBullet> bullets = getBulletsOnMap();
        boolean bulletExistsAlready = bullets.stream()
                .anyMatch(sBullet ->
                        bulletId.equals(sBullet.getBulletId())
                );

        if (bulletExistsAlready) {
            getLogger().debug("addSeatBullet: bullet with BulletId={} already exists in the set", bulletId);
            return false;
        } else {
            bullets.add(seatBullet);
            getLogger().debug("addSeatBullet: bullet with BulletId={} added", bulletId);
            return true;
        }
    }

    @Override
    public void removeBulletById(String bulletId) {
        SeatBullet bulletById = getBulletById(bulletId);
        if (bulletById != null) {
            getBulletsOnMap().remove(bulletById);
            getLogger().debug("removeBulletById: bullet with BulletId={} removed", bulletId);
        } else {
            getLogger().debug("removeBulletById: no bullet with BulletId={} found in the set", bulletId);
        }
    }

    @Override
    public SeatBullet getBulletById(String bulletId) {
        Optional<SeatBullet> first = getBulletsOnMap().stream()
                .filter(seatBullet -> seatBullet.getBulletId().equals(bulletId)).findFirst();
        return first.orElse(null);
    }

    @Override
    public long getTotalBossPayout() {
        return totalBossPayout;
    }

    @Override
    public void resetTotalBossPayout() {
        this.totalBossPayout = 0;
    }

    @Override
    public void addTotalBossPayout(long bossPayout) {
        this.totalBossPayout += bossPayout;
    }

    @Override
    public double getTotalTreasuresXP() {
        return totalTreasuresXP;
    }

    @Override
    public long getTotalTreasuresXPAsLong() {
        return (long) Math.floor(totalTreasuresXP);
    }

    @Override
    public void setTotalTreasuresXP(double totalTreasuresXP) {
        this.totalTreasuresXP = totalTreasuresXP;
    }

    @Override
    public void addTotalTreasuresXP(double totalTreasuresXP) {
        this.totalTreasuresXP += totalTreasuresXP;
    }

    @Override
    public double getTotalKillsXP() {
        return totalKillsXP;
    }

    @Override
    public long getTotalKillsXPAsLong() {
        return (long) Math.floor(totalKillsXP);
    }

    @Override
    public void setTotalKillsXP(double totalKillsXP) {
        this.totalKillsXP = totalKillsXP;
    }

    @Override
    public void addTotalKillsXP(double totalKillsXP) {
        this.totalKillsXP += totalKillsXP;
    }

    public int addWC(Integer weaponId, Integer bulletsAmount) {
        return weaponFromWC.merge(weaponId, bulletsAmount, Integer::sum);
    }

    public boolean containsWeapon(Integer weaponId) {
        return weaponFromWC.get(weaponId) != null && weaponFromWC.get(weaponId) > 0;
    }

    public void decrementShot(Integer weaponId) {
        int currentShots = addWC(weaponId, -1);
        if (currentShots <= 0) {
            weaponFromWC.remove(weaponId);
        }
    }

    public int getNumberWeaponFromWC(Integer weaponId) {
        return weaponFromWC.get(weaponId) != null ? weaponFromWC.get(weaponId) : 0;
    }

    public void resetWeaponFromWC() {
        weaponFromWC.clear();
    }

    @Override
    public int getAmmoAmount() {
        return ammoAmount;
    }

    @Override
    public int getAmmoAmountTotalInRound() {
        return ammoAmountTotalInRound;
    }

    @Override
    public void setAmmoAmount(int ammoAmount) {
        this.ammoAmount = ammoAmount;
    }

    @Override
    public void decrementAmmoAmount() {
        if (ammoAmount <= 0) {
            getLogger().error("decrementAmmoAmount: illegal ammoAmount={}", ammoAmount);
        } else {
            this.ammoAmount--;
        }
    }

    /**
     * Decrement ammo amount of player from shots for pistol and paid special weapons
     * @param decrement amount for decrement
     */
    @Override
    public void decrementAmmoAmount(int decrement) {
        if (this.ammoAmount - decrement < 0) {
            getLogger().error("decrementAmmoAmount: illegal ammoAmount={}, decrement={}", ammoAmount, decrement);
            throw new IllegalStateException("Not enough ammo");
        } else {
            this.ammoAmount -= decrement;
        }
    }

    /**
     * Increment ammo amount of player from current round wins and buyIns
     * @param increment amount for increment
     */
    @Override
    public void incrementAmmoAmount(int increment) {
        this.ammoAmount += increment;
    }

    @Override
    public void incrementTotalAmmoAmount(int increment) {
        this.ammoAmountTotalInRound += increment;
    }

    /**
     * Try transfer current round win to ammo amount
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void transferWinToAmmo() throws CommonException {
        boolean isFrbSession = getPlayerInfo().getActiveFrbSession() != null;
        if (roundWin.greaterThan(Money.ZERO) && !isFrbSession) {
            long transferedAmmo = roundWin.divideBy(stake);
            if (transferedAmmo > 0) {
                Money reBuyFromWin = stake.multiply(transferedAmmo);
                getLogger().debug("transferWinToAmmo: accountId={}, transferedAmmo={}, roundWin={}, reBuyFromWin={}, " +
                                "ammoAmount={}", playerInfo.getId(), transferedAmmo, roundWin.toCents(),
                        reBuyFromWin.toCents(), ammoAmount);
                makeRebuyFromWin(reBuyFromWin);
                incrementAmmoAmount((int) transferedAmmo);
            }
        }
    }

    @Override
    public Money retrieveRemainingAmmo() {
        Money result = stake.multiply(ammoAmount);
        ammoAmount = 0;
        return result;
    }

    @Override
    public Money getPossibleBalanceAmount() {
        Money res = Money.ZERO;
        res = res.add(roundWin);
        res = res.add(stake.multiply(ammoAmount));
        return res;
    }

    @Override
    public void rollbackRoundWinAndAmmo(Money roundWin, int ammoAmount) {
        this.roundWin = roundWin;
        this.ammoAmount = ammoAmount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractActionSeat [");
        sb.append("number=").append(number);
        sb.append(", weapons=").append(weapons);
        sb.append(", currentWeaponId=").append(currentWeaponId);
        sb.append(", freeShots=").append(freeShots);
        sb.append(", weaponSurplus=").append(weaponSurplus);
        sb.append(", compensateSpecialWeapons=").append(compensateSpecialWeapons);
        sb.append(", totalReturnedSpecialWeapons=").append(totalReturnedSpecialWeapons);
        sb.append(", seatMines=").append(seatMines);
        sb.append(", damageToEnemies=").append(damageToEnemies);
        sb.append(", totalTreasuresCount=").append(totalTreasuresCount);
        sb.append(", weaponsReturned=").append(weaponsReturned);
        sb.append(", missCount=").append(missCount);
        sb.append(", hitCount=").append(hitCount);
        sb.append(", enemyKilled=").append(getEnemiesKilledCount());
        sb.append(", bulletsOnMap=").append(getBulletsOnMap());
        sb.append(", totalBossPayout=").append(totalBossPayout);
        sb.append(", totalTreasuresXP=").append(totalTreasuresXP);
        sb.append(", totalKillsXP=").append(totalKillsXP);
        sb.append(", ammoAmount=").append(getAmmoAmount());
        sb.append(", ammoAmountTotalInRound=").append(ammoAmountTotalInRound);

        sb.append(super.toString());
        sb.append(']');
        return sb.toString();
    }
    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(number);
        output.writeInt(hitCount, true);
        output.writeInt(missCount, true);
        output.writeInt(enemiesKilledCount, true);
        kryo.writeClassAndObject(output, freeShots);
        kryo.writeClassAndObject(output, weapons);
        output.writeInt(currentWeaponId);
        kryo.writeObject(output, weaponSurplus);
        kryo.writeObject(output, compensateSpecialWeapons);
        kryo.writeObject(output, getTotalReturnedSpecialWeapons());
        kryo.writeObject(output, roundTreasures);
        kryo.writeObject(output, seatMines);
        kryo.writeObject(output, damageToEnemies);
        output.writeInt(totalTreasuresCount, true);
        kryo.writeObject(output, getMineStates());
        kryo.writeObject(output, getWeaponsReturned());
        output.writeInt(bulletsFired, true);
        kryo.writeObject(output, getBulletsOnMap());
        output.writeLong(totalBossPayout, true);
        output.writeDouble(totalKillsXP);
        output.writeDouble(totalTreasuresXP);
        kryo.writeObject(output, weaponFromWC);
        output.writeInt(ammoAmount, true);
        output.writeInt(ammoAmountTotalInRound, true);


        super.write(kryo, output);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        number = input.readInt();
        hitCount = input.readInt(true);
        missCount = input.readInt(true);
        enemiesKilledCount = input.readInt(true);
        freeShots = (IFreeShots) kryo.readClassAndObject(input);
        weapons = (Map<SpecialWeaponType, WEAPON>) kryo.readClassAndObject(input);
        currentWeaponId = input.readInt();
        weaponSurplus = kryo.readObject(input, ArrayList.class);
        compensateSpecialWeapons = kryo.readObject(input, Money.class);
        totalReturnedSpecialWeapons = kryo.readObject(input, Money.class);
        roundTreasures = kryo.readObject(input, HashMap.class);
        seatMines = kryo.readObject(input, ArrayList.class);
        damageToEnemies = kryo.readObject(input, HashMap.class);
        totalTreasuresCount = input.readInt(true);
        mineStates = kryo.readObject(input, HashMap.class);
        weaponsReturned = kryo.readObject(input, ArrayList.class);
        bulletsFired = input.readInt(true);
        bulletsOnMap = kryo.readObject(input, ConcurrentHashSet.class);
        totalBossPayout = input.readLong(true);
        totalKillsXP = input.readDouble();
        totalTreasuresXP = input.readDouble();
        weaponFromWC = kryo.readObject(input, HashMap.class);
        ammoAmount = input.readInt(true);
        ammoAmountTotalInRound = input.readInt(true);

        super.read(kryo, input);
    }

    @Override
    protected void serializeAdditionalFields(JsonGenerator gen,
                                             SerializerProvider serializers) throws IOException {
        gen.writeNumberField("number", number);
        gen.writeNumberField("hitCount", hitCount);
        gen.writeNumberField("missCount", missCount);
        gen.writeNumberField("enemiesKilledCount", enemiesKilledCount);
        gen.writeObjectField("freeShots", freeShots);
        serializeMapField(gen, "weapons", weapons, new TypeReference<Map<SpecialWeaponType, WEAPON>>() {});
        gen.writeNumberField("currentWeaponId", currentWeaponId);
        serializeListField(gen, "weaponSurplus", weaponSurplus, new TypeReference<List<IWeaponSurplus>>() {});
        gen.writeObjectField("compensateSpecialWeapons", compensateSpecialWeapons);
        gen.writeObjectField("totalReturnedSpecialWeapons", getTotalReturnedSpecialWeapons());
        serializeMapField(gen, "roundTreasures", roundTreasures, new TypeReference<Map<TREASURE, Integer>>() {});
        serializeListField(gen, "seatMines", seatMines, new TypeReference<List<MinePoint>>() {});
        serializeMapField(gen, "damageToEnemies", damageToEnemies, new TypeReference<Map<Long,Double>>() {});
        gen.writeNumberField("totalTreasuresCount", totalTreasuresCount);
        serializeMapField(gen, "mineStates", getMineStates(), new TypeReference<Map<String,Boolean>>() {});
        serializeListField(gen, "weaponsReturned", getWeaponsReturned(), new TypeReference<List<IWeaponSurplus>>() {});
        gen.writeNumberField("bulletsFired", bulletsFired);
        serializeSetField(gen, "bulletsOnMap", getBulletsOnMap(), new TypeReference<Set<SeatBullet>>() {});
        gen.writeNumberField("totalBossPayout", totalBossPayout);
        gen.writeNumberField("totalKillsXP", totalKillsXP);
        gen.writeNumberField("totalTreasuresXP", totalTreasuresXP);
        serializeMapField(gen, "weaponFromWC", weaponFromWC, new TypeReference<Map<Integer,Integer>>() {});
        gen.writeNumberField("ammoAmount", ammoAmount);
        gen.writeNumberField("ammoAmountTotalInRound", ammoAmountTotalInRound);
    }

    @Override
    protected void deserializeAdditionalFields(JsonParser p,
                                               JsonNode node,
                                               DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();

        number = node.get("number").intValue();
        hitCount = node.get("hitCount").intValue();
        missCount = node.get("missCount").intValue();
        enemiesKilledCount = node.get("enemiesKilledCount").intValue();
        freeShots = om.convertValue(node.get("freeShots"), IFreeShots.class);
        weapons = om.convertValue(node.get("weapons"), new TypeReference<EnumMap<SpecialWeaponType, WEAPON>>() {});
        currentWeaponId = node.get("currentWeaponId").asInt();
        weaponSurplus = om.convertValue(node.get("weaponSurplus"), new TypeReference<ArrayList<IWeaponSurplus>>() {});
        compensateSpecialWeapons = om.convertValue(node.get("compensateSpecialWeapons"), Money.class);
        totalReturnedSpecialWeapons = om.convertValue(node.get("totalReturnedSpecialWeapons"), Money.class);
        roundTreasures = om.convertValue(node.get("roundTreasures"), new TypeReference<HashMap<TREASURE, Integer>>() {});
        seatMines = om.convertValue(node.get("seatMines"), new TypeReference<ArrayList<MinePoint>>() {});
        damageToEnemies = om.convertValue(node.get("damageToEnemies"), new TypeReference<HashMap<Long, Double>>() {});
        totalTreasuresCount = node.get("totalTreasuresCount").intValue();
        mineStates = om.convertValue(node.get("mineStates"), new TypeReference<HashMap<String, Boolean>>() {});
        weaponsReturned = om.convertValue(node.get("weaponsReturned"), new TypeReference<ArrayList<IWeaponSurplus> >() {});
        bulletsFired = node.get("bulletsFired").intValue();;
        bulletsOnMap = om.convertValue(node.get("bulletsOnMap"), new TypeReference<ConcurrentHashSet<SeatBullet>>() {});
        totalBossPayout = node.get("totalBossPayout").longValue();
        totalKillsXP = node.get("totalKillsXP").doubleValue();
        totalTreasuresXP = node.get("totalTreasuresXP").doubleValue();
        weaponFromWC = om.convertValue(node.get("weaponFromWC"), new TypeReference<HashMap<Integer, Integer>>() {});
        ammoAmount = node.get("ammoAmount").intValue();
        ammoAmountTotalInRound = node.get("ammoAmountTotalInRound").intValue();
    }

}
