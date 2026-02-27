package com.abs.casino.cache;

import com.google.common.base.Joiner;

import java.util.Objects;

public class GameListKey {
    private static final String ID_DELIMITER = "+";
    private final Joiner joiner = Joiner.on(ID_DELIMITER);
    private long bankId;
    private String version;
    private String showTestingGame;
    private boolean isHttpsRequest;

    GameListKey(long bankId, String version, String showTestingGame, boolean isHttpsRequest) {
        this.bankId = bankId;
        this.version = version;
        this.showTestingGame = showTestingGame;
        this.isHttpsRequest = isHttpsRequest;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    String getShowTestingGame() {
        return showTestingGame;
    }

    boolean isHttpsRequest() {
        return isHttpsRequest;
    }

    @Override
    public String toString() {
        return joiner.join(bankId, version, showTestingGame, isHttpsRequest);
    }

    @Override
    public boolean equals(Object key) {
        if (key == null) return false;
        if (getClass() != key.getClass()) return false;
        final GameListKey other = (GameListKey) key;
        return Objects.equals(this.bankId, other.bankId)
                && Objects.equals(this.version, other.version)
                && Objects.equals(this.showTestingGame, other.showTestingGame)
                && Objects.equals(this.isHttpsRequest, other.isHttpsRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bankId, version, showTestingGame, isHttpsRequest);
    }
}