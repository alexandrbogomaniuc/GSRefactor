package com.abs.casino.support.cache.bank.edit.forms.domains;


public class GameBean {

    private String id;
    private String name;

    public GameBean() {

    }

    public GameBean(long gameId, String name) {
        this.id = String.valueOf(gameId);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
