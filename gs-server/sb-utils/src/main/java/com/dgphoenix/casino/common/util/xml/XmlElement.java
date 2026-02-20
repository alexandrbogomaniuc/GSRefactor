/**
 * User: val
 * Date: Jan 15, 2003
 * Time: 8:16:17 PM
 */
package com.dgphoenix.casino.common.util.xml;

import java.util.HashMap;
import java.util.LinkedList;

public class XmlElement
        implements IXmlElement {

    private String name;
    private LinkedList <IXmlElement> childs;
    private HashMap <String,String> attributes = new HashMap <String,String> ();
    private String value;

    @SuppressWarnings("unused")
	private XmlElement() {
    }

    public XmlElement(String name) {
        this.name = name;
        childs = new LinkedList <IXmlElement> ();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTrimmedValue() {

        if (value == null) return null;

        String value = this.value.trim();

        // Trim begin "\n"
        while(true) {
            if(!value.startsWith("\n")) break;
            value = value.substring(1,value.length());
        }

        // Trim end "\n"
        while(true) {
            if(!value.endsWith("\n")) break;
            value = value.substring(0,value.length()-1);
        }

        return value;
    }

    public String getName() {
        return name;
    }

    public LinkedList <IXmlElement> getChilds() {
        return childs;
    }

    public HashMap <String,String> getAttributes() {
        return attributes;
    }
    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }
    public String getAttributeByName(String key) {
        if (attributes == null)
            return null;
        if (attributes.get(key) == null)
            return null;
        return (String)attributes.get(key);
    }

    public void addChild(IXmlElement element) {
        childs.addLast(element);
    }

    public String toString() {

        String buffer = "";
        for(int i=0;i<childs.size();i++) {
              IXmlElement element = (IXmlElement)childs.get(i);
              buffer += "\t" + element.toString() + "\n";
        }

        return "\tname=[" + name +
                "], value=[" + value +
                "], attributes=[" + attributes + ", childs count=["+childs.size()+"]"+ "\n" + buffer;
    }


}
