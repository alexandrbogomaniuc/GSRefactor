package com.betsoft.casino.mp.revengeofra.model;


import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.gameconfig.Pays;
import com.betsoft.casino.mp.model.gameconfig.PaysDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TestNewModel {
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Pays.class, new PaysDeserializer()).create();

    public static void main(String[] args) throws IOException {
//        TestNewModel testNewModel = new TestNewModel();
//        GameConfig currentGameConfig = testNewModel.test();
//        System.out.println("config: " + currentGameConfig);

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

}
