package com.dgphoenix.casino.common.utils;

import org.apache.log4j.Logger;

import javax.management.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by inter on 26.02.15.
 */
public class MBeanUtils {
    private static final Logger LOG = Logger.getLogger(MBeanUtils.class);

    public static int getRequestProcessorCount() {
        try {
            MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);
            ObjectName objectName = new ObjectName("*:type=RequestProcessor,*");
            Set<ObjectInstance> set = server.queryMBeans(objectName, null);
            return set.size();
        } catch (MalformedObjectNameException ex) {
            LOG.error("error in getRequestProcessorCount " + ex);
            return 0;
        }
    }

    public static int getMaxThreads() {
        return getThreadPoolAttribute("maxThreads", 200);
    }

    public static int getCurrentThreadCount() {
        return getThreadPoolAttribute("currentThreadCount", 0);
    }

    public static int getCurrentThreadsBusy () {
        return getThreadPoolAttribute("currentThreadsBusy", 0);
    }

    public static int getThreadPoolAttribute(String attributeName, int defaultValue) {
        try {
            MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);
            ObjectName objectName = new ObjectName("*:type=ThreadPool,*");
            Set<ObjectName> rpObjectNames = server.queryNames(objectName, null);
            for(ObjectName name : rpObjectNames) {
                Object modelerType = server.getAttribute(name,"modelerType");
                // only AJP threadPool
                if(modelerType != null && "org.apache.tomcat.util.threads.ThreadPool".equals(modelerType)) {
                    return (Integer) server.getAttribute(name, attributeName);
                }
            }
        } catch (Exception ex) {
            LOG.error("error getThreadPoolAttribute attributeName=" + attributeName + " " + ex);
        }
        return defaultValue;
    }
}
