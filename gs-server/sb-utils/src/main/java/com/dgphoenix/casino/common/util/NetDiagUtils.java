package com.abs.casino.common.util;

import com.abs.casino.common.util.string.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * User: flsh
 * Date: 01.10.13
 */
public class NetDiagUtils {
    private static final Logger LOG = Logger.getLogger(NetDiagUtils.class);

    public static String traceRoute(InetAddress address) {

        String route = "";
        try {
            Process traceRt = Runtime.getRuntime().exec("traceroute -n " + address.getHostAddress());
            route = StringUtils.getStreamAsString(traceRt.getInputStream());
            //int exitValue = traceRt.waitFor();
            String errors = StringUtils.getStreamAsString(traceRt.getErrorStream());
            if (!StringUtils.isTrimmedEmpty(errors)) {
                //System.out.println(errors);
                LOG.error(errors);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("error while performing trace route command", e);
        }

        return route;
    }

    public static void main(String[] args) throws Exception {
        URL url = new URL("http://stackoverflow.com/questions/");
        url.getHost();

        //InetAddress address = InetAddress.getByName("google.ru");
        InetAddress address = InetAddress.getByName(url.getHost());
        String result = traceRoute(address);
        System.out.println(result);
    }
}
