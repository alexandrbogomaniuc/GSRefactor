package com.dgphoenix.casino.common.web;

import com.dgphoenix.casino.common.cache.data.session.ClientType;

import javax.servlet.http.HttpServletRequest;

/**
 * User: flsh
 * Date: 11.07.13
 */
public class ClientTypeFactory {
    public static ClientType getByHttpRequest(HttpServletRequest request) {

        if (MobileDetector.isIOSDevice(request.getHeader("User-Agent"))) return ClientType.IOSMOBILE;

        if (MobileDetector.isAndroidDevice(request.getHeader("User-Agent"))) return ClientType.ANDROID;

        if (MobileDetector.isWindowsPhoneDevice(request.getHeader("User-Agent"))) return ClientType.WINDOWSPHONE;

        return ClientType.FLASH;
    }
}
