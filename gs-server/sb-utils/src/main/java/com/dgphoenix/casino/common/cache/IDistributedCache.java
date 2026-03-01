package com.abs.casino.common.cache;

import java.util.Map;

/**
 * User: flsh Date: 09.07.2009 ()
 */
public interface IDistributedCache<CACHE_KEY, CACHED_VALUE> {
    public final static String NO_INFO = "no_info";
    public final static String ID_DELIMITER = "+";
    
    CACHED_VALUE getObject(String id);
    Map<CACHE_KEY, CACHED_VALUE> getAllObjects();    
    int size();
    String getAdditionalInfo();
    String printDebug();
}
