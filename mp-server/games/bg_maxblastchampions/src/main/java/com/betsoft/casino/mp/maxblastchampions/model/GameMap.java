package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.common.AbstractGameMap;
import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.maxblastchampions.model.math.EnemyRange;
import com.betsoft.casino.mp.maxblastchampions.model.math.EnemyType;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
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
import com.hazelcast.spring.context.SpringAware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringAware
public class GameMap extends AbstractGameMap<Enemy, GameMapShape, EnemyRange, EnemyType, GameMap> {
    private static final int MAX_HISTORY_SIZE = 30;

    private EnemyRange possibleEnemies;
    private List<ICrashRoundInfo> multHistory = new ArrayList<>();

    //empty constructor required for Kryo serialization
    @SuppressWarnings("unused")
    public GameMap() {
        super();
    }

    public GameMap(EnemyRange possibleEnemies, GameMapShape map) {
        super(map);
        this.possibleEnemies = possibleEnemies;
    }

    public void addCrashHistory(CrashRoundInfo crashRoundInfo) {
        multHistory.add(crashRoundInfo);
        if (multHistory.size() > MAX_HISTORY_SIZE) {
            multHistory.remove(0);
        }
    }

    public List<ICrashRoundInfo> getMultHistory() {
        return multHistory;
    }

    @Override
    protected EnemyRange getPossibleEnemies() {
        return possibleEnemies;
    }

    void setPossibleEnemies(EnemyRange possibleEnemies) {
        this.possibleEnemies = possibleEnemies;
    }

    @Override
    protected List<EnemyType> getEnemyTypes() {
        return possibleEnemies.getEnemies();
    }

    @Override
    protected boolean isNotBaseEnemy(Enemy enemy) {
        return !EnemyRange.BASE_ENEMIES.contains(enemy.getEnemyType());
    }

    @Override
    protected EnemyType getEnemyByTypeId(int typeId) {
        return EnemyType.values()[typeId];
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Enemy createEnemy(EnemyType enemyType, int skinId, Trajectory trajectory, float speed,
                             IMathEnemy mathEnemy, long parentEnemyId) {
        EnemyClass enemyClass = new EnemyClass(enemyType.getId(), enemyType.getName(), 1, speed, enemyType);
        Enemy enemy = new Enemy(enemyIdsGenerator.getAndIncrement(), enemyClass, skinId, trajectory, mathEnemy,
                parentEnemyId, new ArrayList<>());
        enemy.setAwardedPrizes(new ArrayList<>());
        enemy.setAwardedSum(Money.ZERO);
        enemy.setEnergy(enemyClass.getEnergy());
        enemy.setSpeed(speed);
        enemy.setMovementStrategy(new TrajectoryMovementStrategy(enemy, this));
        return enemy;
    }

    @Override
    protected void writeEnemies(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, items);
        output.writeInt(possibleEnemies.ordinal(), true);
    }

    @Override
    protected void readEnemies(byte version, Kryo kryo, Input input) {
        items.clear();
        List<Enemy> enemies = (List<Enemy>) kryo.readClassAndObject(input);
        items.addAll(enemies);
        for (Enemy enemy : items) {
            IMovementStrategy<Enemy> movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
        int ordinal = input.readInt(true);
        possibleEnemies = EnemyRange.values()[ordinal];
    }

    @Override
    protected void writeAdditionalFields(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, multHistory);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readAdditionalFields(byte version, Kryo kryo, Input input) {
        if (version == 1) {
            kryo.readClassAndObject(input);
            multHistory = new ArrayList<>();
        }
        if (version > 1) {
            multHistory = (List<ICrashRoundInfo>) kryo.readClassAndObject(input);
        }
    }


    @Override
    protected void serializeEnemies(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        serializeListField(gen, "items", items, new TypeReference<List<Enemy>>() {});
        gen.writeNumberField("possibleEnemiesId", possibleEnemies.ordinal());
    }

    @Override
    protected void deserializeEnemies(JsonParser p, JsonNode node, DeserializationContext ctxt) {
        items.clear();
        List<Enemy> enemies = ((ObjectMapper) p.getCodec()).convertValue(node.get("items"), new TypeReference<List<Enemy>>() {});
        items.addAll(enemies);
        for (Enemy enemy : items) {
            IMovementStrategy<Enemy> movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
        int ordinal = node.get("possibleEnemiesId").intValue();
        possibleEnemies = EnemyRange.values()[ordinal];
    }

    @Override
    protected void serializeAdditionalFields(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        serializeListField(gen, "multHistory", multHistory, new TypeReference<List<ICrashRoundInfo>>() {});
    }

    @Override
    protected void deserializeAdditionalFields(JsonParser p, JsonNode node, DeserializationContext ctxt) {
        multHistory = ((ObjectMapper) p.getCodec()).convertValue(node.get("multHistory"), new TypeReference<List<ICrashRoundInfo>>() {});
    }

    @Override
    protected GameMap getDeserialized() {
        return this;
    }


    @Override
    protected Trajectory getInitialTrajectory(double speed, boolean needFinalSteps, IEnemyType enemyType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, Trajectory> generateUpdateTrajectories(boolean needFinalSteps) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean needDoubleSpeed(int enemyTypeId) {
        return false;
    }

}
