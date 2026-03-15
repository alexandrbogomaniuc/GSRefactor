package com.betsoft.casino.mp.model.bots;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IAvatar;
import com.abs.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
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
 * Date: 07.07.2022.
 */
public class BotConfigInfo implements Identifiable, KryoSerializable, JsonSelfSerializable<BotConfigInfo> {
    private static final byte VERSION = 6;
    public static final double DEFAULT_RATE = 1;

    public static Set<GameType> allowedMQGames = new HashSet<>(Arrays.asList(
            GameType.BG_DRAGONSTONE,
            GameType.BG_MISSION_AMAZON,
            GameType.BG_SECTOR_X,
            GameType.BG_MAXCRASHGAME));

    public static long MMC_BankId = 6274L;
    public static long MQC_BankId = 6275L;
    private static final Set<Long> MQB_BANKS = new HashSet<>(Arrays.asList(MMC_BankId, MQC_BankId));

    private static Map<Long, Set<Long>> MQB_ROOMS_VALUES_EMPTY() {
        HashMap<Long, Set<Long>> allowedRoomValues = new HashMap<>();
        allowedRoomValues.put(MMC_BankId, new HashSet<>());
        allowedRoomValues.put(MQC_BankId, new HashSet<>());
        return allowedRoomValues;
    }

    private static Map<Long, Set<Long>> dublicateAllowedRoomValuesMap(Map<Long, Set<Long>> allowedRoomValues) {
        HashMap<Long, Set<Long>> allowedRoomValuesToReturn = new HashMap<>();
        for(Long bankId : allowedRoomValues.keySet()) {
            Set<Long> bankIdRoomValues = allowedRoomValues.get(bankId);
            if(bankIdRoomValues == null) {
                bankIdRoomValues = new HashSet<>();
            } else {
                bankIdRoomValues = new HashSet<>(bankIdRoomValues);
            }

            allowedRoomValuesToReturn.put(bankId, bankIdRoomValues);
        }
        return allowedRoomValuesToReturn;
    }


    private long id;
    private long bankId;
    private Set<GameType> allowedGames;
    private boolean active;
    private String username;
    private String password;
    private String mqNickname;
    private IAvatar avatar;
    private long mmcBalance;
    private long mqcBalance;
    private Set<TimeFrame> timeFrames;
    private long tmpExpiresAt;
    private Set<Long> allowedBankIds;
    private Map<Long, Double> shootsRates;
    private Map<Long, Double> bulletsRates;
    private Map<Long, Set<Long>> allowedRoomValues; //bankId, list of allowed values

    public BotConfigInfo() {}

    public BotConfigInfo(long id, long bankId, Set<GameType> allowedGames, boolean active, String username, String password,
                         String mqNickname, IAvatar avatar, Set<TimeFrame> timeFrames, Set<Long> allowedBankIds,
                         Map<Long, Double> shootsRates, Map<Long, Double> bulletsRates, Map<Long, Set<Long>> allowedRoomValues) {
        this.id = id;
        this.bankId = bankId;
        setAllowedGames(allowedGames);
        this.active = active;
        this.username = username;
        this.password = password;
        this.mqNickname = mqNickname;
        this.avatar = avatar;
        this.timeFrames = timeFrames;
        this.tmpExpiresAt = 0;
        setAllowedBankIds(allowedBankIds);
        this.shootsRates = shootsRates;
        this.bulletsRates = bulletsRates;
        setAllowedRoomValues(allowedRoomValues);
    }

    public BotConfigInfo(long id, long bankId, Set<GameType> allowedGames, boolean active, String username, String password, String mqNickname,
                         IAvatar avatar) {
        this(id, bankId, allowedGames, active, username, password, mqNickname, avatar, null, null, null, null, null);
    }

    public Set<Long> getAllowedBankIds() {
        return allowedBankIds == null ? new HashSet<>(MQB_BANKS) : new HashSet<>(allowedBankIds);
    }

    public void setAllowedBankIds(Set<Long> allowedBankIds) {
        this.allowedBankIds = allowedBankIds == null ? new HashSet<>(MQB_BANKS) : new HashSet<>(allowedBankIds);
    }

    public boolean addAllowedBankId(Long allowedBankId) {
        if(allowedBankId == null) {
            return false;
        }
        if(this.allowedBankIds == null) {
            this.allowedBankIds = new HashSet<>();
        }
        return this.allowedBankIds.add(allowedBankId);
    }

    public boolean removeAllowedBankId(Long allowedBankId) {
        if(this.allowedBankIds == null) {
            this.allowedBankIds = new HashSet<>();
        }
        return this.allowedBankIds.remove(allowedBankId);
    }

    public Map<Long, Set<Long>> getAllowedRoomValues() {
        if(allowedRoomValues == null) {
            return MQB_ROOMS_VALUES_EMPTY();
        } else {
            return dublicateAllowedRoomValuesMap(allowedRoomValues);
        }
    }

    public Set<Long> getAllowedRoomValuesSet(Long allowedBankId) {
        if(allowedRoomValues == null || allowedBankId == null || !allowedRoomValues.containsKey(allowedBankId)) {
            return new HashSet<>();
        } else {
            return new HashSet<>(allowedRoomValues.get(allowedBankId));
        }
    }

    public void setAllowedRoomValues(Map<Long, Set<Long>> allowedRoomValues) {
        if(allowedRoomValues == null) {
            this.allowedRoomValues = MQB_ROOMS_VALUES_EMPTY();
        } else {
            this.allowedRoomValues = dublicateAllowedRoomValuesMap(allowedRoomValues);

        }
    }

    public boolean putAllowedRoomValuesSet(Long allowedBankId, Set<Long> allowedRoomValuesSet) {
        if(allowedBankId == null || allowedRoomValuesSet == null) {
            return false;
        }
        if(this.allowedRoomValues == null) {
            this.allowedRoomValues = new HashMap<>();
        }
        this.allowedRoomValues.put(allowedBankId, new HashSet<>(allowedRoomValuesSet));
        return true;
    }


    public boolean addAllowedRoomValuesSet(Long allowedBankId, Set<Long> allowedRoomValuesSet) {
        if(allowedBankId == null || allowedRoomValuesSet == null) {
            return false;
        }
        if(this.allowedRoomValues == null) {
            this.allowedRoomValues = new HashMap<>();
        }
        if(!this.allowedRoomValues.containsKey(allowedBankId)) {
            this.allowedRoomValues.put(allowedBankId, new HashSet<>(allowedRoomValuesSet));
        } else {
            Set<Long> allowedRoomValuesSetExisting = this.allowedRoomValues.get(allowedBankId);
            allowedRoomValuesSetExisting = new HashSet<>(allowedRoomValuesSetExisting);
            allowedRoomValuesSetExisting.addAll(allowedRoomValuesSet);
            this.allowedRoomValues.put(allowedBankId, new HashSet<>(allowedRoomValuesSetExisting));
        }
        return true;
    }

    public boolean removeAllowedRoomValue(Long allowedBankId, Long allowedRoomValue) {
        if(allowedBankId == null || allowedRoomValue == null) {
            return false;
        }
        if(this.allowedRoomValues == null) {
            this.allowedRoomValues = new HashMap<>();
        }
        if(!this.allowedRoomValues.containsKey(allowedBankId)) {
            return false;
        }

        Set<Long> allowedRoomValuesSet = this.allowedRoomValues.get(allowedBankId);
        return allowedRoomValuesSet.remove(allowedRoomValue);
    }

    public long getTmpExpiresAt() {
        return tmpExpiresAt;
    }

    public void setTmpExpiresAt(long tmpExpiresAt) {
        this.tmpExpiresAt = tmpExpiresAt;
    }

    public static Set<GameType> fromGameIds(Set<Long> gameIds) {

        if(gameIds == null) {
            return null;
        }

        Set<GameType> gameTypes = new HashSet<>();

        for(Long gameId : gameIds) {
            if(gameId != null) {
                GameType gameType = GameType.getByGameId((int) (long) gameId);
                if(gameType != null && allowedMQGames.contains(gameType)) {
                    gameTypes.add(gameType);
                }
            }
        }

        return gameTypes;
    }

    public static Set<Long> toGameIds(Set<GameType> gameTypes) {
        if(gameTypes == null) {
            return null;
        }

        Set<Long> gameIds = new HashSet<>();

        for(GameType gameType : gameTypes) {
            if(gameType != null) {
                int gameId = (int)gameType.getGameId();
                if(gameId > 0) {
                    gameIds.add((long)gameId);
                }
            }
        }

        return gameIds;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public Set<GameType> getAllowedGames() {
        return new HashSet<>(this.allowedGames);
    }

    public void setAllowedGames(Set<GameType> allowedGames) {
        this.allowedGames = allowedGames == null ? new HashSet<>() : new HashSet<>(allowedGames);
    }

    public boolean addAllowedGame(GameType allowedGame) {
        if(allowedGame == null) {
            return false;
        }
        if(this.allowedGames == null) {
            this.allowedGames = new HashSet<>();
        }
        return this.allowedGames.add(allowedGame);
    }

    public boolean removeAllowedGame(GameType allowedGame) {
        if(this.allowedGames == null) {
            this.allowedGames = new HashSet<>();
        }
        return this.allowedGames.remove(allowedGame);
    }

    public Set<TimeFrame> getTimeFrames() {
        return timeFrames;
    }

    public void setTimeFrames(Set<TimeFrame> timeFrames) {
        if(timeFrames == null) {
            this.timeFrames = new HashSet<>();
        } else {

            this.timeFrames = timeFrames instanceof HashSet ?
                    timeFrames : new HashSet<>(timeFrames);
        }
    }

    public void addTimeFrame(TimeFrame timeFrame) {
        if(timeFrames == null) {
            timeFrames = new HashSet<>();
        }

        timeFrames.add(timeFrame);
    }

    public Map<Long, Double> getShootsRates() {
        if(shootsRates == null) {
            this.shootsRates = new HashMap<>();
        }
        return shootsRates;
    }

    public void setShootsRates(Map<Long, Double> shootsRates) {
        if(shootsRates == null) {
            this.shootsRates = new HashMap<>();
        } else {

            this.shootsRates = shootsRates instanceof HashSet ?
                    shootsRates : new HashMap<>(shootsRates);
        }
    }

    public void addShootsRate(long gameId, double rate) {
        if(shootsRates == null) {
            shootsRates = new HashMap<>();
        }

        shootsRates.put(gameId, rate);
    }

    public void addShootsRate(GameType gameType, double rate) {
        if(gameType != null) {
            addShootsRate(gameType.getGameId(), rate);
        }
    }

    public double getShootsRate(long gameId) {
        if(shootsRates == null) {
            shootsRates = new HashMap<>();
        }

        Double rate = shootsRates.get(gameId);

        if(rate == null) {
            return DEFAULT_RATE;
        }

        return rate;
    }

    public double getShootsRate(GameType gameType) {
        if(gameType == null) {
            return DEFAULT_RATE;
        }

        return getShootsRate(gameType.getGameId());
    }

    public Map<Long, Double> getBulletsRates() {
        if(bulletsRates == null) {
            this.bulletsRates = new HashMap<>();
        }
        return bulletsRates;
    }

    public void setBulletsRates(Map<Long, Double> bulletsRates) {
        if(bulletsRates == null) {
            this.bulletsRates = new HashMap<>();
        } else {

            this.bulletsRates = bulletsRates instanceof HashSet ?
                    bulletsRates : new HashMap<>(bulletsRates);
        }
    }

    public void addBulletsRate(long gameId, double rate) {
        if(bulletsRates == null) {
            bulletsRates = new HashMap<>();
        }

        bulletsRates.put(gameId, rate);
    }

    public void addBulletsRate(GameType gameType, double rate) {
        if(gameType != null) {
            addBulletsRate(gameType.getGameId(), rate);
        }
    }

    public double getBulletsRate(long gameId) {
        if(bulletsRates == null) {
            bulletsRates = new HashMap<>();
        }

        Double rate = bulletsRates.get(gameId);

        if(rate == null) {
            return DEFAULT_RATE;
        }

        return rate;
    }

    public double getBulletsRate(GameType gameType) {
        if(gameType == null) {
            return DEFAULT_RATE;
        }

        return getBulletsRate(gameType.getGameId());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMqNickname() {
        return mqNickname;
    }

    public void setMqNickname(String mqNickname) {
        this.mqNickname = mqNickname;
    }

    public IAvatar getAvatar() {
        return avatar;
    }

    public void setAvatar(IAvatar avatar) {
        this.avatar = avatar;
    }

    public long getMmcBalance() {
        return mmcBalance;
    }

    public void setMmcBalance(long mmcBalance) {
        this.mmcBalance = mmcBalance;
    }

    public long getMqcBalance() {
        return mqcBalance;
    }

    public void setMqcBalance(long mqcBalance) {
        this.mqcBalance = mqcBalance;
    }

    public boolean isFake() {
        return this.bankId == 271;
    }

    public boolean currentTimeIsWithInTimeFrames() {

        Set<TimeFrame> timeFrames = getTimeFrames();
        if(timeFrames == null || timeFrames.isEmpty()) {
            return true;
        } else  {
            for(TimeFrame timeFrame : getTimeFrames()) {
                if(timeFrame.isCurrentTimeWithin()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isAllowedGameType(GameType gameType) {
        Set<GameType> allowedGames = getAllowedGames();
        return allowedGames != null && allowedGames.contains(gameType);
    }

    public boolean isAllowedBankId(Long bankId) {
        Set<Long> allowedBankIds = getAllowedBankIds();
        return allowedBankIds != null && allowedBankIds.contains(bankId);
    }

    public boolean isAllowedRoomValue(Long bankId, Long roomValue) {
        Set<Long> allowedRoomValueSet = getAllowedRoomValuesSet(bankId);
        return allowedRoomValueSet != null && allowedRoomValueSet.contains(roomValue);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(bankId, true);
        output.writeBoolean(active);
        output.writeString(username);
        output.writeString(password);
        output.writeString(mqNickname);
        output.writeLong(mmcBalance, true);
        output.writeLong(mqcBalance, true);
        kryo.writeClassAndObject(output, avatar);
        kryo.writeClassAndObject(output, allowedGames);
        kryo.writeClassAndObject(output, timeFrames);
        output.writeLong(tmpExpiresAt, true);
        kryo.writeClassAndObject(output, allowedBankIds);
        kryo.writeClassAndObject(output, shootsRates);
        kryo.writeClassAndObject(output, bulletsRates);
        kryo.writeClassAndObject(output, allowedRoomValues);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        id = input.readLong(true);
        bankId = input.readLong(true);
        active = input.readBoolean();
        username = input.readString();
        password = input.readString();
        mqNickname = input.readString();
        mmcBalance = input.readLong(true);
        mqcBalance = input.readLong(true);
        avatar = (IAvatar) kryo.readClassAndObject(input);

        //noinspection unchecked
        allowedGames = (Set<GameType>) kryo.readClassAndObject(input);

        if (version >= 1) {
            //noinspection unchecked
            timeFrames = (Set<TimeFrame>) kryo.readClassAndObject(input);
        }
        if (version >= 2) {
            tmpExpiresAt = input.readLong(true);
        }
        if (version >= 3) {
            //noinspection unchecked
            allowedBankIds = (Set<Long>) kryo.readClassAndObject(input);
        }
        if (version >= 4) {
            //noinspection unchecked
            shootsRates = (Map<Long, Double>) kryo.readClassAndObject(input);
        }
        if (version >= 5) {
            //noinspection unchecked
            bulletsRates = (Map<Long, Double>) kryo.readClassAndObject(input);
        }
        if (version >= 6) {
            //noinspection unchecked
            allowedRoomValues = (Map<Long, Set<Long>>) kryo.readClassAndObject(input);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeNumberField("id", id);
        gen.writeNumberField("bankId", bankId);
        gen.writeBooleanField("active", active);
        gen.writeStringField("username", username);
        gen.writeStringField("password", password);
        gen.writeStringField("mqNickname", mqNickname);
        gen.writeNumberField("mmcBalance", mmcBalance);
        gen.writeNumberField("mqcBalance", mqcBalance);
        gen.writeObjectField("avatar", avatar);
        serializeSetField(gen, "allowedGames", allowedGames, new TypeReference<Set<GameType>>() {});
        serializeSetField(gen, "timeFrames", timeFrames, new TypeReference<Set<TimeFrame>>() {});
        gen.writeNumberField("tmpExpiresAt", tmpExpiresAt);
        serializeSetField(gen, "allowedBankIds", allowedBankIds, new TypeReference<Set<Long>>() {});
        serializeMapField(gen, "shootsRates", shootsRates, new TypeReference<Map<Long,Double>>() {});
        serializeMapField(gen, "bulletsRates", bulletsRates, new TypeReference<Map<Long,Double>>() {});
        serializeMapField(gen, "allowedRoomValues", allowedRoomValues, new TypeReference<Map<Long, Set<Long>>>() {});
    }

    @Override
    public BotConfigInfo deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = om.readTree(p);

        id = node.get("id").asLong();
        bankId = node.get("bankId").asLong();
        active = node.get("active").asBoolean();
        username = node.get("username").asText();
        password = node.get("password").asText();
        mqNickname = node.get("mqNickname").asText();
        mmcBalance = node.get("mmcBalance").asLong();
        mqcBalance = node.get("mqcBalance").asLong();
        avatar = om.treeToValue(node.get("avatar"), IAvatar.class);

        //noinspection unchecked
        allowedGames = om.treeToValue(node.get("allowedGames"), new TypeReference<Set<GameType>>() {});
        timeFrames = om.treeToValue(node.get("timeFrames"), new TypeReference<Set<TimeFrame>>() {});
        tmpExpiresAt = node.get("tmpExpiresAt").asLong();
        allowedBankIds = om.treeToValue(node.get("allowedBankIds"), new TypeReference<Set<Long>>() {});
        shootsRates = om.treeToValue(node.get("shootsRates"), new TypeReference<Map<Long, Double>>() {});
        bulletsRates = om.treeToValue(node.get("bulletsRates"), new TypeReference<Map<Long, Double>>() {});
        allowedRoomValues = om.treeToValue(node.get("allowedRoomValues"), new TypeReference<Map<Long, Set<Long>>>() {});

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotConfigInfo that = (BotConfigInfo) o;
        if (id != that.id) return false;
        if (!username.equals(that.username)) return false;
        return mqNickname.equals(that.mqNickname);
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BotConfigInfo [");
        sb.append("id=").append(id);
        sb.append(", bankId=").append(bankId);
        sb.append(", allowedGames=").append(allowedGames);
        sb.append(", timeFrames=").append(timeFrames);
        sb.append(", active=").append(active);
        sb.append(", username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", mqNickname='").append(mqNickname).append('\'');
        sb.append(", mmcBalance='").append(mmcBalance).append('\'');
        sb.append(", mqcBalance='").append(mqcBalance).append('\'');
        sb.append(", avatar=").append(avatar);
        sb.append(", allowedBankIds=").append(allowedBankIds);
        sb.append(", shotRates=").append(shootsRates);
        sb.append(", bulletsRates=").append(bulletsRates);
        sb.append(']');
        return sb.toString();
    }
}
