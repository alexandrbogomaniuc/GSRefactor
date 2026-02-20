package com.betsoft.casino.bots.mqb;

import java.util.List;

public class FinishGameSessionResponse {
    private boolean finishGameSession;
    private List<String> users;

    public FinishGameSessionResponse() {
    }

    public FinishGameSessionResponse(boolean finishGameSession, List<String> users) {
        this.finishGameSession = finishGameSession;
        this.users = users;
    }

    public boolean isFinishGameSession() {
        return finishGameSession;
    }

    public void setFinishGameSession(boolean finishGameSession) {
        this.finishGameSession = finishGameSession;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "FinishGameSessionResponse{" +
                "finishGameSession=" + finishGameSession +
                ", users=" + users +
                '}';
    }
}
