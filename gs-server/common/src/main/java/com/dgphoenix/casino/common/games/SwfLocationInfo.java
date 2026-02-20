package com.dgphoenix.casino.common.games;

/**
 * User: flsh
 * Date: 27.10.11
 */
public class SwfLocationInfo {
    private String baseUrl;
    private String swfPath;
    private String serverUrl;
    private String cdnCheck;
    private boolean useCdn;

    public SwfLocationInfo(String baseUrl, String swfPath) {
        this.baseUrl = baseUrl;
        this.swfPath = swfPath;
        this.useCdn = false;
    }

    public SwfLocationInfo(String serverUrl, String baseUrl, String swfPath, boolean useCdn, String cdnCheck) {
        this.baseUrl = baseUrl;
        this.swfPath = swfPath;
        this.useCdn = useCdn;
        this.serverUrl = serverUrl;
        this.cdnCheck = cdnCheck;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSwfPath() {
        return swfPath;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getCdnCheck() {
        return cdnCheck;
    }

    public boolean isUseCdn() {
        return useCdn;
    }

    @Override
    public String toString() {
        return "SwfLocationInfo{" +
                "baseUrl='" + baseUrl + '\'' +
                ", swfPath='" + swfPath + '\'' +
                ", serverUrl='" + serverUrl + '\'' +
                ", cdnCheck='" + cdnCheck + '\'' +
                ", useCdn=" + useCdn +
                '}';
    }
}
