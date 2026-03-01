package com.abs.casino.common.util;

import com.abs.casino.common.util.BidirectionalMultivalueMap;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.strip;

/**
 * User: plastical
 * Date: 24.02.2010
 */
public class CollectionUtils {
    private static final Logger LOG = Logger.getLogger(CollectionUtils.class);

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static <T extends Map<String, String>> T copyProperties(T original) {
        try {
            if (original != null) {
                Class<T> klass = (Class<T>) original.getClass();
                T copy = klass.newInstance();

                if (!original.isEmpty()) {
                    for (Entry<String, String> entry : original.entrySet()) {
                        copy.put(entry.getKey(), entry.getValue());
                    }
                }
                return copy;
            }
        } catch (InstantiationException e) {
            LOG.error("CollectionUtils::copyProperties error:", e);
        } catch (IllegalAccessException e) {
            LOG.error("CollectionUtils::copyProperties error:", e);
        }

        return null;
    }

    public static List<String> stringToListOfStrings(String ids) {
        return stringToListOfStrings(ids, ",");
    }

    public static List<String> stringToListOfStrings(String ids, String delimiter) {
        List<String> result = new ArrayList<String>();
        if (!isTrimmedEmpty(ids)) {
            StringTokenizer st = new StringTokenizer(ids, delimiter);
            while (st.hasMoreTokens()) {
                String value = st.nextToken();
                if (!isTrimmedEmpty(value)) {
                    result.add(value.trim());
                }
            }
        }
        return result;
    }

    public static List<Long> stringToListOfLongs(String ids) {
        return stringToListOfLongs(ids, ",");
    }

    public static List<Long> stringToListOfLongs(String ids, String delim) {
        List<Long> result = new ArrayList<Long>();
        if (!isTrimmedEmpty(ids)) {
            StringTokenizer st = new StringTokenizer(ids, delim);
            while (st.hasMoreTokens()) {
                String value = st.nextToken();
                if (!isTrimmedEmpty(value)) {
                    result.add(Long.valueOf(value.trim()));
                }
            }
        }
        return result;
    }

    public static String listOfLongsToString(List<Long> listOfLongs, String delim) {
        StringBuilder sb = new StringBuilder();
        if (listOfLongs != null) {
            Iterator<Long> iterator = listOfLongs.iterator();
            while (iterator.hasNext()) {
                sb.append(iterator.next());
                if(iterator.hasNext()) {
                    sb.append(delim);
                }
            }
        }
        return sb.toString();
    }

    public static String listOfLongsToString(List<Long> listOfLongs) {
        return listOfLongsToString(listOfLongs, ",");
    }

    public static String listOfStringsToString(List<String> listOfStrings) {
        StringBuilder sb = new StringBuilder();
        if (listOfStrings != null) {
            Iterator<String> iterator = listOfStrings.iterator();
            while (iterator.hasNext()) {
                sb.append(iterator.next());
                if(iterator.hasNext()) {
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }

    public static String mapToString(Map<String, String> map) {
        if(isEmpty(map)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }

    //PARAM1=12;PARAM2=xxx;PARAM3=45.00
    public static Map<String, String> stringToMap(String s) {
        return stringToMap(s, ";", "=");
    }

    //PARAM1=12;PARAM2=xxx;PARAM3=45.00
    public static Map<String, String> stringToMap(String s, String pairDelimiter, String keyValueDelimiter) {
        Map<String, String> result = new HashMap<String, String>();
        if (!isTrimmedEmpty(s)) {
            StringTokenizer st = new StringTokenizer(s, pairDelimiter);
            while (st.hasMoreTokens()) {
                String pair = st.nextToken();
                if (!isTrimmedEmpty(pair)) {
                    StringTokenizer st2 = new StringTokenizer(pair, keyValueDelimiter);
                    int tokensCount = st2.countTokens();
                    if (tokensCount == 1) {
                        result.put(st2.nextToken(), "");
                    } else if (tokensCount == 2) {
                        result.put(st2.nextToken(), st2.nextToken());
                    }
                }
            }
        }
        return result;
    }

    public static BidirectionalMultivalueMap<Long, Long> stringToLongMap(String s) {
        return stringToLongMap(s, ";", "=");
    }

    public static BidirectionalMultivalueMap<Long, Long> stringToLongMap(String s, String pairDelimiter, String keyValueDelimiter) {
        BidirectionalMultivalueMap<Long, Long> result = new BidirectionalMultivalueMap<Long, Long>();
        if (!isTrimmedEmpty(s)) {
            StringTokenizer st = new StringTokenizer(s, pairDelimiter);
            while (st.hasMoreTokens()) {
                String pair = st.nextToken();
                if (!isTrimmedEmpty(pair)) {
                    StringTokenizer st2 = new StringTokenizer(pair, keyValueDelimiter);
                    int tokensCount = st2.countTokens();
                    if (tokensCount == 2) {
                        Long key = Long .valueOf(st2.nextToken());
                        Long value = Long .valueOf(st2.nextToken());
                        result.put(key, value);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return the first element in '<code>candidates</code>' that is contained in
     * '<code>source</code>'. If no element in '<code>candidates</code>' is present in
     * '<code>source</code>' returns <code>null</code>. Iteration order is
     * {@link Collection} implementation specific.
     *
     * @param source     the source Collection
     * @param candidates the candidates to search for
     * @return the first present object, or <code>null</code> if not found
     */
    public static <T> T findFirstMatch(Collection<? extends T> source, Collection<? extends T> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return null;
        }
        if (source.size() < candidates.size()) {
            for (Iterator<? extends T> it = source.iterator(); it.hasNext(); ) {
                T o = it.next();
                if (candidates.contains(o)) {
                    return o;
                }
            }
        } else {
            for (Iterator<? extends T> it = candidates.iterator(); it.hasNext(); ) {
                T o = it.next();
                if (source.contains(o)) {
                    return o;
                }
            }
        }
        return null;
    }

    /**
     * Static factory method for {@link StringPropertyHelperImpl}
     * @param property String, representing map with entries delimited with entrySeparator and keys/values delimited with keyValueSeparator.
     *                 If empty, new will be created.
     * @return {@link StringPropertyHelperImpl}
     */
    public static StringPropertyHelper modifyStringProperty(String property, String entryDelimiter, String keyValueDelimiter) {
        checkArgument(!isTrimmedEmpty(entryDelimiter), "Entry delimiter must be not empty");
        checkArgument(!isTrimmedEmpty(keyValueDelimiter), "Key-Value delimiter must not be empty");
        return new StringPropertyHelperImpl(property, entryDelimiter, keyValueDelimiter);
    }

    public static StringPropertyHelper convertMapToString(Map<String, String> mapProperties, String entryDelimiter, String keyValueDelimiter) {
        checkArgument(!isTrimmedEmpty(entryDelimiter), "Entry delimiter must be not empty");
        checkArgument(!isTrimmedEmpty(keyValueDelimiter), "Key-Value delimiter must not be empty");
        return new StringPropertyHelperImpl(mapProperties, entryDelimiter, keyValueDelimiter);
    }

    public interface StringPropertyHelper {
        StringPropertyHelper add(String key, String value);

        StringPropertyHelper addAll(Map<String, String> properties);

        StringPropertyHelper remove(String key);

        StringPropertyHelper removeAll(Iterable<String> keys);

        StringPropertyHelper removeAll(String[] keys);

        Map<String, String> getMap();

        String getString();
    }

    /**
     * String -> map converter. Trims entries, trims keys/values. Does not permit duplicated and empty keys, empty values.
     * Provides basic Map operations on property in chain-style. Preserves order of entries in string and map.
     *
     * Obtain instance via static factory method modifyStringProperty in {@link com.dgphoenix.casino.common.util.CollectionUtils}
     */
    private static class StringPropertyHelperImpl implements StringPropertyHelper {
        private final String entrySeparator;
        private final String keyValueSeparator;
        private Map<String, String> mapProperties;

        /**
         * @throws java.lang.IllegalArgumentException if the specified property does not split into valid map entries, or if there are duplicate keys
         */
        private StringPropertyHelperImpl(String property, String entrySeparator, String keyValueSeparator) {
            this.entrySeparator = entrySeparator;
            this.keyValueSeparator = keyValueSeparator;
            mapProperties = convertStringToMap(property);
        }

        /**
         * @throws java.lang.IllegalArgumentException if mapProperties contains empty keys or values
         */
        private StringPropertyHelperImpl(Map<String, String> mapProperties, String entrySeparator, String keyValueSeparator) {
            this.keyValueSeparator = keyValueSeparator;
            this.entrySeparator = entrySeparator;
            this.mapProperties = createMap();
            addAll(mapProperties);
        }

        @Override
        public StringPropertyHelper addAll(Map<String, String> properties) {
            if (properties == null || properties.isEmpty()) {
                return this;
            }
            for (Entry<String, String> entry : properties.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
            return this;
        }

        @Override
        public StringPropertyHelper add(String key, String value) {
            checkArgument(!isTrimmedEmpty(key), "Property key cannot be empty");
            checkArgument(!isTrimmedEmpty(value), "Property value cannot be empty");

            mapProperties.put(key.trim(), value.trim());
            return this;
        }

        @Override
        public StringPropertyHelper remove(String key) {
            checkArgument(!isTrimmedEmpty(key), "Property key cannot be empty");

            mapProperties.remove(key);
            return this;
        }

        @Override
        public StringPropertyHelper removeAll(Iterable<String> keys) {
            for (String key : keys) {
                remove(key);
            }
            return this;
        }

        @Override
        public StringPropertyHelper removeAll(String[] keys) {
            return removeAll(Arrays.asList(keys));
        }

        @Override
        public Map<String, String> getMap() {
            return mapProperties;
        }

        @Override
        public String getString() {
            return convertMapToString();
        }

        private String convertMapToString() {
            return Joiner.on(entrySeparator).withKeyValueSeparator(keyValueSeparator).join(mapProperties);
        }

        private Map<String, String> convertStringToMap(String additionalProperties) {
            if (isTrimmedEmpty(additionalProperties)) {
                return createMap();
            }
            //remove leading and trailing separators
            String stripped = strip(additionalProperties, entrySeparator);
            Map<String, String> unmodifiableMap = Splitter
                    .on(entrySeparator)
                    .trimResults()
                    .withKeyValueSeparator(keyValueSeparator)
                    .split(stripped);

            Map<String, String> result = createMap();
            //trim keys and values
            for (Entry<String, String> entry : unmodifiableMap.entrySet()) {
                String trimmedKey = entry.getKey().trim();
                String trimmedValue = entry.getValue().trim();

                if (isTrimmedEmpty(trimmedKey) || isTrimmedEmpty(trimmedValue)) {
                    throw new IllegalArgumentException("One of keys or values is empty: " + additionalProperties);
                }

                result.put(trimmedKey, trimmedValue);
            }

            return result;
        }

        private Map<String, String> createMap() {
            return Maps.newLinkedHashMap();
        }
    }

    public static <K, V extends Comparable<? super V>>Map<K, V> sortByValue(Map<K, V> map) {
        return sortByValue(map, false);
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, final boolean reverseOrder) {
        List<Entry<K, V>> list = new LinkedList<Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Entry<K, V>>() {
            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return reverseOrder
                        ?   o2.getValue().compareTo(o1.getValue())
                        :   o1.getValue().compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void main(String[] args) {
        BidirectionalMultivalueMap<Long, Long> map = stringToLongMap(
                "1=2;3=5;4=5;7=5;8=1");
        for (Entry<Long, Long> entry : map.entrySet()) {
            System.out.println("" + entry.getKey() + "=" + entry.getValue());
        }
        System.out.println("value 2=" + map.getKeysForValue(2L));
        System.out.println("value 5=" + map.getKeysForValue(5L));
    }
}
