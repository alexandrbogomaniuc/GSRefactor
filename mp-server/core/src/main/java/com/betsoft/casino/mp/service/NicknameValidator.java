package com.betsoft.casino.mp.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NicknameValidator {
    private static final Logger LOG = LogManager.getLogger(NicknameValidator.class);
    private static final String DEFAULT_ALLOWED_SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890#";

    private static final List<String> bannedWords = new ArrayList<>();

    @PostConstruct
    private void init() {
        InputStream resource = this.getClass().getResourceAsStream("/banned-names.txt");
        new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                .lines()
                .forEach(bannedWords::add);
    }

    public boolean isObscene(String string) {
        return isTooLong(string) || bannedWords.contains(string.toLowerCase());
    }

    public boolean isTooLong(String string) {
        return string.length() > 25;
    }

    public boolean isPassableNickname(String nickname, String additionalSymbols) {
        if (nickname == null) {
            return false;
        }

        String allAllowedSymbols = DEFAULT_ALLOWED_SYMBOLS + (additionalSymbols == null ? "" : additionalSymbols);
        for (char character : nickname.toCharArray()) {
            if (allAllowedSymbols.indexOf(character) < 0) {
                return false;
            }
        }

        return true;
    }

    public String getDefaultAllowedSymbols() {
        return DEFAULT_ALLOWED_SYMBOLS;
    }
}
