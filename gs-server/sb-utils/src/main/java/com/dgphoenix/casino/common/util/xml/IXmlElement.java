/**
 * User: val
 * Date: Jan 15, 2003
 * Time: 8:10:35 PM
 */
package com.abs.casino.common.util.xml;

import java.util.HashMap;
import java.util.LinkedList;

public interface IXmlElement {

    String getName();

    String getValue();
    String getTrimmedValue();

    LinkedList <IXmlElement> getChilds();
    HashMap <String,String> getAttributes();
    void addAttribute(String key, String value);
    String getAttributeByName(String key);

    void addChild(IXmlElement element);
    void setValue(String value);
}
