package com.abs.casino.common.cache.data.session;

public enum ClientType {

    FLASH(1l), WIN32(2l), AIR(3l), MOBILE(4l), ANDROID(5l), IOSMOBILE(6l), WINDOWSPHONE(7l), ELECTRON(8l);

    private long id;

    private ClientType(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public static ClientType getById(long id) {
        switch ((int) id) {
            case 1: {
                return FLASH;
            }
            case 2: {
                return WIN32;
            }
            case 3: {
                return AIR;
            }
            case 4: {
                return MOBILE;
            }
            case 5: {
                return ANDROID;
            }
            case 6: {
                return IOSMOBILE;
            }
            case 7: {
                return WINDOWSPHONE;
            }
            case 8: {
                return ELECTRON;
            }
            default: {
                return FLASH;
            }
        }
    }

    public boolean isMobile() {
        return id == 4 || id == 5 || id == 6 || id == 7;
    }
}
