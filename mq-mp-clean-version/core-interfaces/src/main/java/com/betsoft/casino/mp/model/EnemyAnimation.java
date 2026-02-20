package com.betsoft.casino.mp.model;

public enum EnemyAnimation {
    NO_ANIMATION(-1, 0),
    BOSS_WEEK_STATE(0, 3000);

    int animationId;
    int duration;

    EnemyAnimation(int animationId, int duration) {
        this.animationId = animationId;
        this.duration = duration;
    }

    public int getAnimationId() {
        return animationId;
    }

    public void setAnimationId(int animationId) {
        this.animationId = animationId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyAnimation{");
        sb.append("animationId=").append(animationId);
        sb.append(", duration=").append(duration);
        sb.append('}');
        return sb.toString();
    }
}
