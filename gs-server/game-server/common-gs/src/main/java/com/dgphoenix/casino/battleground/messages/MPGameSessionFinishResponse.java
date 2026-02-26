package com.abs.casino.battleground.messages;

import java.util.Objects;
import java.util.Set;

public class MPGameSessionFinishResponse {
    private final boolean finishGameSession;
    private final Set<String> users;

    public MPGameSessionFinishResponse(boolean finishGameSession, Set<String> users) {
        this.finishGameSession = finishGameSession;
        this.users = users;
    }

    public boolean isFinishGameSession() {
        return finishGameSession;
    }

    public Set<String> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return "MPGameSessionFinishResponse{" +
                "finishGameSession=" + finishGameSession +
                ", users=" + users +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MPGameSessionFinishResponse that = (MPGameSessionFinishResponse) o;
        return finishGameSession == that.finishGameSession && Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(finishGameSession, users);
    }
}
