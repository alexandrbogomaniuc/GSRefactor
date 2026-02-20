package com.betsoft.casino.mp.model.gameconfig;

import java.util.Map;

public class QuestParam {
    Map<String, Integer> quest;
    Pays pays;

    public Map<String, Integer> getQuest() {
        return quest;
    }

    public void setQuest(Map<String, Integer> quest) {
        this.quest = quest;
    }

    public Pays getPays() {
        return pays;
    }

    public void setPays(Pays pays) {
        this.pays = pays;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QuestParam{");
        sb.append("quest=").append(quest);
        sb.append(", pays=").append(pays);
        sb.append('}');
        return sb.toString();
    }
}
