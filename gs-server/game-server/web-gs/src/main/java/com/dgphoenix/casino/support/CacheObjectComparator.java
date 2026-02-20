package com.dgphoenix.casino.support;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * User: shegan
 * Date: 08.10.13
 */
public class CacheObjectComparator {
    public static HashMap<String, HashSet<String>> compare(Object object1, Object object2) {
        return compare(object1, object2, false);
    }

    public static HashMap<String, HashSet<String>> compare(Object object1, Object object2, boolean hasSuper) {
        if (object1 == null || object2 == null) return null;
        if (object1.getClass() != object2.getClass()) return null;

        HashMap<String, HashSet<String>> differentProperties = new HashMap<String, HashSet<String>>();  //method name, fields
        HashMap<String, String> methods = new HashMap<String, String>();  //field name, method name

        ArrayList<Field> fields = new ArrayList<Field>(Arrays.asList(object1.getClass().getDeclaredFields()));
        if (hasSuper)
            fields.addAll(Arrays.asList(object1.getClass().getSuperclass().getDeclaredFields()));

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || field.getName().substring(0, 1).equals("$")) {
                continue;
            }
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String methodName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);

            if (field.getType().isAssignableFrom(Boolean.class)
                    || (field.getType().isPrimitive() && field.getType().getName().toLowerCase().equals("boolean"))
            ) {
                if (field.getName().substring(0, 2).toLowerCase().equals("is")) {
                    methodName = field.getName();
                } else {
                    methodName = "is" + methodName;
                }
            } else {
                methodName = "get" + methodName;
            }
            methods.put(field.getName(), methodName);
        }


        for (Map.Entry<String, String> entry : methods.entrySet()) {
            Object a = null;
            try {
                a = object1.getClass().getMethod(entry.getValue()).invoke(object1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Object b = null;
            try {
                b = object2.getClass().getMethod(entry.getValue()).invoke(object2);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (a == null ? b != null : !a.equals(b)) {
                differentProperties.put(entry.getKey(), new HashSet<String>());
                if (a instanceof Map) {

                    Set<Object> keys = new HashSet<Object>(((Map) a).keySet());
                    keys.addAll(((Map) b).keySet());
                    for (Object o : keys) {
                        if (!((Map) a).keySet().contains(o) || !((Map) b).keySet().contains(o)) {
                            differentProperties.get(entry.getKey()).add(null);
                        }
                    }

                    for (Map.Entry e : (Set<Map.Entry>) ((Map) a).entrySet()) {
                        if (((Map) b).containsKey(e.getKey())) {
                            if ((e.getValue() != null && ((Map) b).get(e.getKey()) != null)) {
                                if (!e.getValue().equals(((Map) b).get(e.getKey()))) {
                                    differentProperties.get(entry.getKey()).add((String) e.getKey());
                                }
                            } else if ((e.getValue() != null ^ ((Map) b).get(e.getKey()) != null)) {
                                differentProperties.get(entry.getKey()).add((String) e.getKey());
                            }
                        }
                    }

                    for (Map.Entry e : (Set<Map.Entry>) ((Map) b).entrySet()) {
                        if (((Map) a).containsKey(e.getKey())) {
                            if (e.getValue() != null && ((Map) a).get(e.getKey()) != null) {
                                if (!e.getValue().equals(((Map) a).get(e.getKey()))) {
                                    differentProperties.get(entry.getKey()).add((String) e.getKey());
                                }
                            } else if (e.getValue() != null ^ ((Map) a).get(e.getKey()) != null) {
                                differentProperties.get(entry.getKey()).add((String) e.getKey());
                            }
                        }
                    }
                    continue;
                }

                if (a instanceof Collection) {

                    for (Object o : (Collection) a) {
                        if (!((Collection) b).contains(o)) {
                            differentProperties.get(entry.getKey()).add(o.toString());
                        }
                    }

                    for (Object o : (Collection) b) {
                        if (!((Collection) a).contains(o)) {
                            differentProperties.get(entry.getKey()).add(o.toString());
                        }
                    }
                    continue;
                }
                differentProperties.get(entry.getKey()).add(null);
            }
        }
        return differentProperties;
    }

}
