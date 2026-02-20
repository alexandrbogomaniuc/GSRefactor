package com.betsoft.casino.mp.bgsectorx.model.math.config;

/*"TurretLevelUp": {
        "TriggerProb": 0.0181818181818182,
        "ReTriggerProb": 0.0208333333333333,
        "NumShotsRewardPerTrigger": 20
},*/
public class TurretLevelUp {
    private double TriggerProb;
    private double ReTriggerProb;
    private int NumShotsRewardPerTrigger;

    public TurretLevelUp(double triggerProb, double reTriggerProb, int numShotsRewardPerTrigger) {
        TriggerProb = triggerProb;
        ReTriggerProb = reTriggerProb;
        NumShotsRewardPerTrigger = numShotsRewardPerTrigger;
    }

    public double getTriggerProb() {
        return TriggerProb;
    }

    public void setTriggerProb(double triggerProb) {
        TriggerProb = triggerProb;
    }

    public double getReTriggerProb() {
        return ReTriggerProb;
    }

    public void setReTriggerProb(double reTriggerProb) {
        ReTriggerProb = reTriggerProb;
    }

    public int getNumShotsRewardPerTrigger() {
        return NumShotsRewardPerTrigger;
    }

    public void setNumShotsRewardPerTrigger(int numShotsRewardPerTrigger) {
        NumShotsRewardPerTrigger = numShotsRewardPerTrigger;
    }
}
