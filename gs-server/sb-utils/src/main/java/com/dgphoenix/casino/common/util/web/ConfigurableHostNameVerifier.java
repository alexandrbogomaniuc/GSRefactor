package com.dgphoenix.casino.common.util.web;

import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User: flsh
 * Date: 10.12.14.
 */
public class ConfigurableHostNameVerifier implements X509HostnameVerifier {
    private static final Logger LOG = Logger.getLogger(ConfigurableHostNameVerifier.class);
    private X509HostnameVerifier originalVerifier;
    private Set<String> trustedHosts;
    private boolean trustAll = false;

    public ConfigurableHostNameVerifier(X509HostnameVerifier originalVerifier, Set<String> trustedHosts) {
        this.originalVerifier = originalVerifier;
        this.trustedHosts = trustedHosts;
    }

    private boolean isTrusted(String host) {
        return trustAll || (trustedHosts != null && trustedHosts.contains(host));
    }

    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }

    public void addTrustedHosts(Set<String> hosts) {
        if(trustedHosts == null) {
            trustedHosts = new HashSet<String>();
        }
        trustedHosts.addAll(hosts);
    }

    @Override
    public void verify(String host, SSLSocket ssl) throws IOException {
        if (isTrusted(host)) {
            LOG.info("verify: found trusted host=" + host);
        } else {
            originalVerifier.verify(host, ssl);
        }
    }

    @Override
    public void verify(String host, X509Certificate cert) throws SSLException {
        if (isTrusted(host)) {
            LOG.info("verify [cert]: found trusted host=" + host);
        } else {
            originalVerifier.verify(host, cert);
        }
    }

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        if (isTrusted(host)) {
            LOG.info("verify [subjectAlts]: found trusted host=" + host);
        } else {
            originalVerifier.verify(host, cns, subjectAlts);
        }
    }

    @Override
    public boolean verify(String host, SSLSession sslSession) {
        if(isTrusted(host)) {
            LOG.info("verify: found trusted host=" + host);
            return true;
        }
        return originalVerifier.verify(host, sslSession);
    }

    @Override
    public String toString() {
        return "ConfigurableHostNameVerifier[" +
                "originalVerifier=" + originalVerifier +
                ", trustedHosts=" + (trustedHosts == null ? "null" : Arrays.toString(trustedHosts.toArray())) +
                ", trustAll=" + trustAll +
                ']';
    }
}
