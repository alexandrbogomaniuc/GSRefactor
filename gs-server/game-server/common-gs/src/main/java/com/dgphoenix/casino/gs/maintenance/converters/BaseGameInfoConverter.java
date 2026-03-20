package com.abs.casino.gs.maintenance.converters;

import com.abs.casino.common.cache.CoinsCache;
import com.abs.casino.common.cache.LimitsCache;
import com.abs.casino.common.cache.data.bank.Coin;
import com.abs.casino.common.cache.data.bank.Limit;
import com.abs.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.game.BaseGameInfo;
import com.abs.casino.common.cache.data.game.GameGroup;
import com.abs.casino.common.cache.data.game.GameType;
import com.abs.casino.common.cache.data.game.GameVariableType;
import com.abs.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.string.StringUtils;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 25.08.14.
 * Old format:
 * <propertiesMap>
 * <entry>
 * <string>ANDROID</string>
 * <string>20314</string>
 * </entry>
 * </propertiesMap>
 * <languages>
 * <string>en</string>
 * </languages>
 * <coins>
 * <com.abs.casino.common.cache.data.bank.Coin>
 * <id>4</id>
 * <value>100</value>
 * </com.abs.casino.common.cache.data.bank.Coin>
 * </coins>
 * <p>
 * New format:
 * <coinsString>4;5;7</coinsString>
 * <langsString>en;ru</langsString>
 * <propertiesString>ANDROID=20314;KEY_PLAYER_DEVICE_TYPE=ANDROID</propertiesString>
 */
public class BaseGameInfoConverter implements Converter {
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        BaseGameInfo info = (BaseGameInfo) source;
        info.marshal(writer, context);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        BaseGameInfo info = new BaseGameInfo();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String propertyName = reader.getNodeName();
            String value = reader.getValue();
            if ("id".equals(propertyName)) {
                info.setId(Long.valueOf(value));
            } else if ("externalId".equals(propertyName)) {
                info.setExternalId(value);
            } else if ("bankId".equals(propertyName)) {
                info.setBankId(Long.valueOf(value));
            } else if ("currency".equals(propertyName)) {
                Currency currency = (Currency) context.convertAnother(info, Currency.class);
                info.setCurrency(currency);
            } else if ("name".equals(propertyName)) {
                info.setName(value);
            } else if ("gameType".equals(propertyName)) {
                GameType type = StringUtils.isTrimmedEmpty(value) ? null : GameType.valueOf(value);
                info.setGameType(type);
            } else if ("group".equals(propertyName)) {
                GameGroup group = StringUtils.isTrimmedEmpty(value) ? null : GameGroup.valueOf(value);
                info.setGroup(group);
            } else if ("variableType".equals(propertyName)) {
                GameVariableType type = StringUtils.isTrimmedEmpty(value) ? null : GameVariableType.valueOf(value);
                info.setVariableType(type);
            } else if ("rmClassName".equals(propertyName)) {
                info.setRmClassName(value);
            } else if ("gsClassName".equals(propertyName)) {
                info.setGsClassName(value);
            } else if ("limit".equals(propertyName)) {
                Limit limit = (Limit) context.convertAnother(info, Limit.class);
                Limit cachedLimit = limit == null ? null : LimitsCache.getInstance().getLimit(limit.getId());
                info.setLimit(cachedLimit != null ? cachedLimit : limit);
            } else if ("coinsString".equals(propertyName)) {
                List<Coin> coins = new ArrayList();
                List<Long> list = CollectionUtils.stringToListOfLongs(value);
                for (Long id : list) {
                    Coin coin = CoinsCache.getInstance().getCoin(id);
                    if (coin != null) {
                        coins.add(coin);
                    }
                }
                info.setCoins(coins);
            } else if ("langsString".equals(propertyName)) {
                List<String> langs = CollectionUtils.stringToListOfStrings(value);
                info.setLanguages(langs);
            } else if ("propertiesString".equals(propertyName)) {
                Map<String, String> propertiesMap = CollectionUtils.stringToMap(value);
                info.setProperties(propertiesMap);
            } else if ("servlet".equals(propertyName)) {
                info.setServlet(value);
            } else if ("isMobile".equals(propertyName)) {
                info.setMobile(Boolean.parseBoolean(value));
            } else if ("lastUpdateDate".equals(propertyName)) {
                info.setLastUpdateDate(Long.valueOf(value));
            } else if ("propertiesMap".equals(propertyName)) { //old format
                Map map = (Map) context.convertAnother(info, HashMap.class);
                if (map != null) {
                    info.setProperties(map);
                }
            } else if ("languages".equals(propertyName)) { //old format
                List list = (List) context.convertAnother(info, ArrayList.class);
                if (list != null) {
                    info.setLanguages(list);
                }
            } else if ("coins".equals(propertyName)) { //old format
                List list = (List) context.convertAnother(info, ArrayList.class);
                if (list != null) {
                    info.setCoins(list);
                }
            }
            reader.moveUp();
        }
        return info;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(BaseGameInfo.class);
    }
}
