package com.dgphoenix.casino.common.cache.data.account;

public enum PlayerDeviceType {
   PC(""), ANDROID("ANDROID"), IOSMOBILE("MOBILE"), WINDOWSPHONE("WINDOWSPHONE");
   private String gameNameSignature;

    PlayerDeviceType(String gameNameSignature) {
        this.gameNameSignature = gameNameSignature;
    }

    public String getGameNameSignature() {
        return gameNameSignature;
    }
}
