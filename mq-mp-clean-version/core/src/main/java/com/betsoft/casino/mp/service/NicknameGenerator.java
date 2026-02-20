package com.betsoft.casino.mp.service;

import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NicknameGenerator {
    private static final Logger LOG = LogManager.getLogger(NicknameGenerator.class);

    private List<String> names = new ArrayList<>();

    @PostConstruct
    public void init() {
        InputStream stream = this.getClass().getResourceAsStream("/nicknames.txt");
        new BufferedReader(new InputStreamReader(stream))
                .lines()
                .forEach(name -> names.add(name));
    }

    public String generate() {
        return names.get(RNG.nextInt(names.size() - 1));
    }
}
