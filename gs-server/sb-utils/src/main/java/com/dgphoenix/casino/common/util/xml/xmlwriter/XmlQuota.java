/*
 * User: val
 * Date: 27.10.2002
 * Time: 21:26:10
 */
package com.abs.casino.common.util.xml.xmlwriter;

public class XmlQuota {

    static protected String
            defaultTable[][] = {
                {"&", "&amp;"},
                {"<", "&lt;"},
                {">","&gt;"},
                {"'","&apos;"},
                {"\"","&quot;"}
            };


    static public String quota(String text) {
        return quota(text, defaultTable);
    }

    static public String quota(String text, String[][] table) {
        if(text !=null) {
            int len = text.length();
            int ind = 0;
            StringBuilder sb = new StringBuilder(text);
            for(int i=0;i<len;i++) {
                for (String[] aTable : table) {
                    if (sb.charAt(ind) == aTable[0].charAt(0)) {
                        sb.deleteCharAt(ind);
                        sb.insert(ind, aTable[1]);
                        ind += (aTable[1].length() - 1);
                        break;
                    }
                }
                ind++;
            }
            return sb.toString();
        }
        return "";
    }
}
