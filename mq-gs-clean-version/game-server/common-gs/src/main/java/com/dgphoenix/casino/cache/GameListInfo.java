package com.dgphoenix.casino.cache;


import com.dgphoenix.casino.common.util.xml.xmlwriter.Attribute;

import java.util.List;
import java.util.Map;

public class GameListInfo {
    private final Map<String, List<Long>> groupedGamesMap;
    private final Map<Long, List<Attribute>> attributesMap;

    GameListInfo(Map<String, List<Long>> groupedGamesMap, Map<Long, List<Attribute>> attributesMap) {
        this.groupedGamesMap = groupedGamesMap;
        this.attributesMap = attributesMap;
    }

    public Map<String, List<Long>> getGroupedGamesMap() {
        return groupedGamesMap;
    }

    public Map<Long, List<Attribute>> getAttributesMap() {
        return attributesMap;
    }
}