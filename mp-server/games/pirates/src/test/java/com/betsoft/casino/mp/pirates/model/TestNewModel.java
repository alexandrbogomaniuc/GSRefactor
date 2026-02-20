package com.betsoft.casino.mp.pirates.model;


import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.gameconfig.*;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.IOException;
import java.util.*;

public class TestNewModel {
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Pays.class, new PaysDeserializer()).create();

    public static void main(String[] args) throws IOException {
        TestNewModel testNewModel = new TestNewModel();
        GameConfig currentGameConfig = testNewModel.test();
        System.out.println("config: " + currentGameConfig);

    }


    public static MathEnemy getNextMathEnemy(List<MathEnemy> liveSimpleEnemiesOfRound, String enemyType) {
        Optional<MathEnemy> first = liveSimpleEnemiesOfRound.stream()
                .filter(mathEnemy -> mathEnemy.getTypeName().equals(enemyType)).findFirst();

        if (first.isPresent()) {
            MathEnemy mathEnemy = first.get();
            System.out.println(" enemy: " + mathEnemy);
            return mathEnemy;
        }
        return null;
    }


    private GameConfig test() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        Resource[] resources = resolver.getResources("classpath:models/pirates/config_8.json");
        String content = Resources.toString(resources[0].getURL(), Charsets.UTF_8);
        return gson.fromJson(content, GameConfig.class);
    }
}
