package com.dgphoenix.casino.common.util;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * User: van0ss
 * Date: 13.02.2017
 */
public class NtpWrapper {
    private NTPUDPClient ntpClient = new NTPUDPClient();

    public void setDefaultTimeout(int timeout) {
        ntpClient.setDefaultTimeout(timeout);
    }

    public void open() throws SocketException {
        ntpClient.open();
    }

    public void close() {
        ntpClient.close();
    }

    public TimeInfo getTime(InetAddress host) throws IOException {
        return ntpClient.getTime(host);
    }
}
