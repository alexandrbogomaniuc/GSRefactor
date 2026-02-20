package com.dgphoenix.casino.ats;

import java.util.List;

public class AllAtsResponse {
    private boolean isEnabled;
    private List<BotConfigInfo> botConfigInfos;

    public AllAtsResponse(boolean isEnabled, List<BotConfigInfo> botConfigInfos) {
        this.botConfigInfos = botConfigInfos;
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public List<BotConfigInfo> getBotConfigInfos() {
        return botConfigInfos;
    }

    public void setBotConfigInfos(List<BotConfigInfo> botConfigInfos) {
        this.botConfigInfos = botConfigInfos;
    }

    @Override
    public String toString() {
        return "AllAtsResponse{" +
                "isEnabled=" + isEnabled +
                ", botConfigInfos=" + (botConfigInfos != null ? botConfigInfos.size() : null) +
                '}';
    }
}
