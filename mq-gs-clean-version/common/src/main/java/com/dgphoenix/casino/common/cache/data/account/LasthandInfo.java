package com.dgphoenix.casino.common.cache.data.account;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.Identifiable;

/**
 * User: flsh
 * Date: 09.07.2009
 */
public class LasthandInfo implements IDistributedCacheEntry, Identifiable {
    private long id;
    private String lasthandData;

    public LasthandInfo() {
    }

    public LasthandInfo(long id, String lasthandData) {
        this.id = id;
        this.lasthandData = lasthandData;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLasthandData() {
        return lasthandData;
    }

    public void setLasthandData(String lasthandData) {
/*
        if(this.lasthandData != null && lasthandData == null) {
            ThreadLog.debug("setLasthandData null: " + LogUtils.stackTrace(""));
        }
*/
        this.lasthandData = lasthandData;
    }

    @Override
    public String toString() {
        return "LasthandInfo[" +
                "id=" + id +
                ", lasthandData='" + lasthandData + '\'' +
                ']';
    }
}
