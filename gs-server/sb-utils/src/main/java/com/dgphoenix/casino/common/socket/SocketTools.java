package com.dgphoenix.casino.common.socket;

import java.util.Set;
import java.util.HashSet;

/**
 * Created
 * Date: 02.12.2008
 * Time: 17:35:04
 */
public class SocketTools {
    public static boolean isPolicyFileRequest(String message) {
        return message != null && message.startsWith("<policy-file-request/>");
    }

    public static String buildPolicyFile(Set<String> domains, Set<Integer> ports) {
        StringBuilder sb = new StringBuilder();
        sb.append("<cross-domain-policy>\n");
        for (String domain : domains) {
            sb.append("<allow-access-from domain=\"").append(domain).append("\" secure=\"false\" to-ports=\"");
            int cnt = 0;
            for (Integer port : ports) {
                cnt++;
                sb.append(port);
                if (cnt < ports.size()) {
                    sb.append(",");
                }
            }
            sb.append("\"/>\n");
        }
        sb.append("</cross-domain-policy>\n");
        return sb.toString();
    }

    public static String buildPolicyFile(String domain, Set<Integer> ports) {
        Set<String> domains = new HashSet<String>();
        domains.add(domain);
        return buildPolicyFile(domains, ports);
    }

    public static String buildPolicyFile(Set<String> domains, int port) {
        Set<Integer> ports = new HashSet<Integer>();
        ports.add(port);
        return buildPolicyFile(domains, ports);
    }

    public static String buildPolicyFile(String domain, int port) {
        Set<String> domains = new HashSet<String>();
        domains.add(domain);
        Set<Integer> ports = new HashSet<Integer>();
        ports.add(port);
        return buildPolicyFile(domains, ports);
    }

}
