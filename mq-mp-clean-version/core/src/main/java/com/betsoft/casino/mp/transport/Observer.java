package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.battleground.ITransportObserver;
import com.betsoft.casino.mp.model.privateroom.Status;

import java.io.Serializable;
import java.util.Objects;

public class Observer implements ITransportObserver, Serializable {
    private String nickname;
    private boolean isKicked;
    private Status status;
    private Boolean isOwner;

    public Observer() {

    }

    public Observer(String nickname, boolean isKicked, Status status, Boolean isOwner) {
        this.nickname = nickname;
        this.isKicked = isKicked;
        this.status = status;
        this.isOwner = isOwner;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public boolean isKicked() {
        return isKicked;
    }

    @Override
    public void setKicked(boolean kicked) {
        isKicked = kicked;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public Boolean isOwner() {
        return isOwner;
    }

    @Override
    public void setOwner(Boolean isOwner) { this.isOwner = isOwner; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Observer observer = (Observer) o;

        if (isKicked != observer.isKicked) return false;
        if (status != observer.status) return false;
        return nickname.equals(observer.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, isKicked, status);
    }

    @Override
    public String toString() {
        return "Observer{" +
                "nickname='" + nickname + '\'' +
                ", isKicked=" + isKicked +
                ", status=" + status +
                ", isOwner=" + isOwner +
                '}';
    }
}
