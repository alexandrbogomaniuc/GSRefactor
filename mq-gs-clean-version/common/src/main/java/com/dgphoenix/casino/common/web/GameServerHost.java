package com.dgphoenix.casino.common.web;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;

/**
 * User: isirbis
 * Date: 14.08.14
 */

/**
 * @author isirbis
 * Methods collection for getting game server domain for bank
 */
public class GameServerHost {

    /**
     * Get game server domain by ServerInfo and BankInfo
     *
     * @param serverInfo Server information
     * @param bankInfo   Bank information
     * @return Game server domain for bank
     */
    public static String getHost(ServerInfo serverInfo, BankInfo bankInfo) {
        if (serverInfo == null) {
            throw new RuntimeException("ServerInfo is null. Could not get game server host.");
        }
        String host = serverInfo.getHost();
        if (bankInfo != null) {
            if (bankInfo.isReplaceStartServerName()) {
                Integer serverId = serverInfo.getServerId();
                String serverNameReplace = host.startsWith(bankInfo.getReplaceStartServerFrom()) ?
                        bankInfo.getReplaceStartServerFrom() : ("gs" + serverId);
                host = host.replaceFirst(serverNameReplace,
                        bankInfo.getReplaceStartServerTo() + (serverId < 0 ? "" : serverId));
            }
            if (bankInfo.isReplaceEndServerName() && host.endsWith(bankInfo.getReplaceEndServerFrom())) {
                String serverNameReplace = bankInfo.getReplaceEndServerFrom();
                host = host.replaceFirst(serverNameReplace, bankInfo.getReplaceEndServerTo());
            }
        }
        return host;
    }

    /**
     * Get game server domain by server name, serverID and BankInfo
     *
     * @param serverName Real game server domain
     * @param serverId   Server ID
     * @param bankInfo   Bank information
     * @return Game server domain for bank
     */
    public static String getHost(String serverName, long serverId, BankInfo bankInfo) {
        if (serverName == null) {
            throw new RuntimeException("ServerName is null. Could not get game server host.");
        }
        String host = serverName;
        if (bankInfo != null && bankInfo.isReplaceStartServerName()) {
            String serverNameReplace = host.startsWith(bankInfo.getReplaceStartServerFrom()) ?
                    bankInfo.getReplaceStartServerFrom() : ("gs" + serverId);
            host = host.replaceFirst(serverNameReplace,
                    bankInfo.getReplaceStartServerTo() + (serverId < 0 ? "" : serverId));
        }
        if (bankInfo != null && bankInfo.isReplaceEndServerName() && host.endsWith(bankInfo.getReplaceEndServerFrom())) {
            String serverNameReplace = bankInfo.getReplaceEndServerFrom();
            host = host.replaceFirst(serverNameReplace, bankInfo.getReplaceEndServerTo());
        }
        return host;
    }

    /**
     * Get game server domain by ServerInfo and bankID
     *
     * @param serverInfo Server information
     * @param bankId     Bank ID
     * @return Game server domain for bank
     */
    public static String getHost(ServerInfo serverInfo, long bankId) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        return GameServerHost.getHost(serverInfo, bankInfo);
    }
}
