package com.abs.casino.common.cache.data.game;

/**
 * Created
 * Date: 27.11.2008
 * Time: 16:03:46
 */
public enum GameType{
    SP("SP"),
    MP("MP");
    
    private String name;
    private GameType(String name) {
		this.name = name;
	}
    
    public String getName() {
		return name;
	}
}
