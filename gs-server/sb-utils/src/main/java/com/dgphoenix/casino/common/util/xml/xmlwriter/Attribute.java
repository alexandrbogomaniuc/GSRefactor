/*
 * User: val
 * Date: 13.10.2002
 * Time: 16:22:43
 */
package com.dgphoenix.casino.common.util.xml.xmlwriter;

public class Attribute {
    private String name;
    private String value;

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
