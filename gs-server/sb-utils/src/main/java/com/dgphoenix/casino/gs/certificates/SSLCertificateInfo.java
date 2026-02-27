package com.abs.casino.gs.certificates;

import java.util.Date;

/**
 * Created by vladislav on 4/20/16.
 */
public class SSLCertificateInfo {
    public static String GET_INFO_REQUEST_PARAMETER = "getSSLCertificateInfo=true";

    private final String subject;
    private final Date expirationTime;

    public SSLCertificateInfo(String subject, Date expirationTime) {
        this.subject = subject;
        this.expirationTime = expirationTime;
    }

    public String getSubject() {
        return subject;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    @Override
    public String toString() {
        return "SSLCertificateInfo{" +
                "subject='" + subject + '\'' +
                ", expirationTime=" + expirationTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SSLCertificateInfo that = (SSLCertificateInfo) o;

        return !(subject != null ? !subject.equals(that.subject) : that.subject != null);
    }

    @Override
    public int hashCode() {
        return subject != null ? subject.hashCode() : 0;
    }
}
