package com.abs.casino.slottest.utils;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.logkit.ThreadLog;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

public class Tools {
    private static final Logger LOG = Logger.getLogger(Tools.class);
    protected static final String[] DELIMITERS = {"|", ",", ";", "#", "$"};
    private static final String digits = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int MIN_RADIX = 2, MAX_RADIX = digits.length();

    public static int search(String[] array, String key) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(key))
                return i;
        }
        return -1;
    }

    public static String[] parseToStringArrays(String line,
                                                     String delimeter) {
        String[] result = null;
        StringTokenizer st = new StringTokenizer(line, delimeter);
        result = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            result[i++] = st.nextToken();
        }
        return result;
    }

    public static int transformToIndex(int col, int row, int rowCount) {
        LOG.debug("Transform to index method");
        LOG.debug("col=" + col + " row=" + row + " rowCount="
                + rowCount);
        return col * rowCount + (rowCount - 1 - row);
    }

    public static List intersectionOfLists(List a, List b) {
        List result = new LinkedList();
        if (a == null || b == null) {
            return null;
        }
        for (Object obj : a) {
            if (b.contains(obj)) {
                result.add(obj);
            }
        }
        return result;
    }

    public static Set intersectionOfSets(Set a, Set b) {
        Set result = new HashSet();
        if (a == null || b == null) {
            return null;
        }
        for (Object obj : a) {
            if (b.contains(obj)) {
                result.add(obj);
            }
        }
        return result;
    }

    public static Document generateDocumentFromReelList(List[][] reels) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element reelElement = doc.createElement("reels");
            doc.appendChild(reelElement);
            Element reel;
            for (List[] reel1 : reels) {
                reel = doc.createElement("reel");
                reelElement.appendChild(reel);
                Element cell;
                for (int j = 0; j < reel1.length; j++) {
                    cell = doc.createElement("cell");
                    reel.appendChild(cell);
                    Element icon;
                    for (int z = 0; z < reel1[reel1.length - 1 - j]
                            .size(); z++) {
                        icon = doc.createElement("icon");
                        cell.appendChild(icon);
                        Node iconInfo = doc.createTextNode("iconinfo");
                        icon.appendChild(iconInfo);
                        iconInfo.setNodeValue((String) reel1[reel1.length
                                - 1 - j].get(z));
                    }

                }
            }
            return doc;
        } catch (ParserConfigurationException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }

    public static Set getPossibleIconsFromReel(int[] reel, String[] strIcons) {
        Set set = new HashSet();
        for (int aReel : reel) {
            try {
                set.add(strIcons[aReel]);
            } catch (Exception e) {
                ThreadLog.error("[SLOTTEST] GET POSSIBLE ICONS FROM REEL ERROR", e);
            }
        }
        return set;

    }

    static public StringBuilder packArray (Object objArray) {
        StringBuilder sb = new StringBuilder ();

        int intLevelsCount = 0;
        while (objArray.getClass ().getName ().charAt (intLevelsCount + 1) == '[')
            ++intLevelsCount;

        packArray_0 (sb, objArray, intLevelsCount);

        return sb;
    }

    static private StringBuilder packArray_0 (StringBuilder sb, Object objArray, int intLevel) {
        String strName = objArray.getClass ().getName ();

        if (strName.charAt (0) == '[') {
            switch (strName.charAt (1)) {
                case '[':
                {
                    Object[] objArrays = (Object[]) objArray;

                    if (objArrays.length == 0) {
                        sb.append (" ");
                    } else {
                        for (int i = 0; i < objArrays.length; ++i) {
                            if (i > 0) {
                                sb.append (DELIMITERS[intLevel]);
                            }

                            if (objArrays[i] == null) {
                                sb.append ("n");
                            } else {
                                packArray_0 (sb, objArrays[i], intLevel - 1);
                            }
                        }
                    }
                }
                break;

                case 'B':
                {
                    byte[] byteArray = (byte[]) objArray;

                    if (byteArray.length == 0) {
                        sb.append (" ");
                    } else {
                        for (int i = 0; i < byteArray.length; ++i) {
                            if (i > 0)
                                sb.append (DELIMITERS[intLevel]);
                            sb.append (byteArray[i]);
                        }
                    }
                }
                break;

                case 'I':
                {
                    int[] intArray = (int[]) objArray;

                    if (intArray.length == 0) {
                        sb.append (" ");
                    } else {
                        for (int i = 0; i < intArray.length; ++i) {
                            if (i > 0)
                                sb.append (DELIMITERS[intLevel]);
                            sb.append (intArray[i]);
                        }
                    }
                }
                break;

                case 'D':
                {
                    double[] dblArray = (double[]) objArray;

                    if (dblArray.length == 0) {
                        sb.append (" ");
                    } else {
                        for (int i = 0; i < dblArray.length; ++i) {
                            if (i > 0)
                                sb.append (DELIMITERS[intLevel]);
                            sb.append (dblArray[i]);
                        }
                    }
                }
                break;

                case 'J':
                {
                    long[] lngArray = (long[]) objArray;

                    if (lngArray.length == 0) {
                        sb.append (" ");
                    } else {
                        for (int i = 0; i < lngArray.length; ++i) {
                            if (i > 0)
                                sb.append (DELIMITERS[intLevel]);
                            sb.append (lngArray[i]);
                        }
                    }
                }
                break;

                case 'L':
                {
                    if (strName.substring (2).startsWith ("java.lang.String")) {
                        String[] strArray = (String[]) objArray;

                        if (strArray.length == 0) {
                            sb.append (" ");
                        } else {
                            for (int i = 0; i < strArray.length; ++i) {
                                if (i > 0)
                                    sb.append (DELIMITERS[intLevel]);
                                sb.append (strArray[i]);
                            }
                        }
                    }
                }
                break;
            }
        }

        return sb;
    }

    static public StringBuilder packArraySmallInt (int[] intArray) {
        StringBuilder sb = new StringBuilder ();
        packArraySmallInt (sb, intArray);
        return sb;
    }

    static public void packArraySmallInt (StringBuilder sb, int[] intArray) {
        sb.append ((char) (intArray.length + 32));
        for (int i = 0; i < intArray.length; ++i) {
            sb.append ((char) (intArray[i] + 32));
        }
    }

    static public StringBuilder packArraySmallInt (int[][] intArray) {
        StringBuilder sb = new StringBuilder ();
        packArraySmallInt (sb, intArray);
        return sb;
    }

    static public void packArraySmallInt (StringBuilder sb, int[][] intArray) {
        sb.append ((char) (intArray.length + 32));
        for (int i = 0; i < intArray.length; ++i) {
            packArraySmallInt (sb, intArray[i]);
        }
    }

    //
    //	Unpack functions
    //

    static public int[] unpackArrayIntOneLevel (String strArray) throws CommonException {
        int[] intResult = null;

        try {
            if (!strArray.equals ("n")) {
                StringTokenizer st = new StringTokenizer (strArray.trim (), DELIMITERS[0]);
                intResult = new int[st.countTokens ()];
                for (int i = 0; st.hasMoreTokens (); ++i) {
                    intResult[i] = Integer.valueOf (st.nextToken ()).intValue ();
                }
            }
        } catch (Exception e) {
            throw new CommonException (e);
        }

        return intResult;
    }

    static public int[][] unpackArrayIntTwoLevels (String strArray) throws CommonException {
        int[][] intResult = null;

        try {
            StringTokenizer stArray = new StringTokenizer (strArray.trim (), DELIMITERS[1]);
            intResult = new int[stArray.countTokens ()][];
            for (int i = 0; stArray.hasMoreTokens (); ++i) {
                intResult[i] = Tools.unpackArrayIntOneLevel (stArray.nextToken ());
            }
        } catch (Exception e) {
            throw new CommonException (e);
        }

        return intResult;
    }

    static public int[][][] unpackArrayIntThreeLevels (String strArray) throws CommonException {
        int[][][] intResult = null;

        try {
            StringTokenizer stArray = new StringTokenizer (strArray.trim (), DELIMITERS[2]);
            intResult = new int[stArray.countTokens ()][][];
            for (int i = 0; stArray.hasMoreTokens (); ++i) {
                intResult[i] = Tools.unpackArrayIntTwoLevels (stArray.nextToken ());
            }
        } catch (Exception e) {
            throw new CommonException (e);
        }

        return intResult;
    }

    static public long[] unpackArrayLongOneLevel (String strArray) throws CommonException {
        long[] intResult = null;

        try {
            if (!strArray.equals ("n")) {
                StringTokenizer st = new StringTokenizer (strArray.trim (), DELIMITERS[0]);
                intResult = new long[st.countTokens ()];
                for (int i = 0; st.hasMoreTokens (); ++i) {
                    intResult[i] = Long.valueOf (st.nextToken ()).longValue ();
                }
            }
        } catch (Exception e) {
            throw new CommonException (e);
        }
        return intResult;
    }

    static public double[] unpackArrayDoubleOneLevel (String strArray) throws CommonException {
        double[] dblResult = null;

        try {
            if (!strArray.equals ("n")) {
                StringTokenizer st = new StringTokenizer (strArray.trim (), DELIMITERS[0]);
                dblResult = new double[st.countTokens ()];
                for (int i = 0; st.hasMoreTokens (); ++i) {
                    dblResult[i] = Double.valueOf (st.nextToken ()).doubleValue ();
                }
            }
        } catch (Exception e) {
            throw new CommonException (e);
        }

        return dblResult;
    }

    static public double[][] unpackArrayDoubleTwoLevels (String strArray) throws CommonException {
        double[][] dblResult = null;

        try {
            StringTokenizer stArray = new StringTokenizer (strArray.trim (), DELIMITERS[1]);
            dblResult = new double[stArray.countTokens ()][];
            for (int i = 0; stArray.hasMoreTokens (); ++i) {
                dblResult[i] = Tools.unpackArrayDoubleOneLevel (stArray.nextToken ());
            }
        } catch (Exception e) {
            throw new CommonException (e);
        }

        return dblResult;
    }

    static public int[] unpackArraySmallIntOneLevel (String strArray) {
        int index = 0;
        int[] intResult = new int[unpackArraySmallIntOneLevel_getInt (strArray, index++)];
        for (int i = 0; i < intResult.length; ++i) {
            intResult[i] = unpackArraySmallIntOneLevel_getInt (strArray, index++);
        }

        return intResult;
    }

    static public int[][] unpackArraySmallIntTwoLevels (String strArray) {
        int i = 0;
        int[][] intResult = new int[unpackArraySmallIntOneLevel_getInt (strArray, i++)][];
        for (int i1 = 0; i1 < intResult.length; ++i1) {
            intResult[i1] = new int[unpackArraySmallIntOneLevel_getInt (strArray, i++)];
            for (int i2 = 0; i2 < intResult[i1].length; ++i2) {
                intResult[i1][i2] = unpackArraySmallIntOneLevel_getInt (strArray, i++);
            }
        }

        return intResult;
    }

    static private int unpackArraySmallIntOneLevel_getInt (String str, int index) {
        return (int) str.charAt (index) - 32;
    }


    //////////////////////////////////////////////////////////////////////////
    //
    //		Debug Functions
    //

    static protected boolean m_Debug = true;

    static public void _log (String strMessage) {
        _log_nolf (strMessage + "\n");
    }

    static public void _log_nolf (String strMessage) {
        if (m_Debug) {
            System.out.print (strMessage);
        }
    }

    static public void _log (String strMessage, Hashtable htbl) {
        _log_nolf (strMessage, htbl);
        _log_nolf ("\n");
    }

    static public void _log_nolf (String strMessage, Hashtable htbl) {
        _log_nolf (strMessage + "=" + htbl);
    }

    static protected String _getModuleName () {
        return "Tools";
    }

    static protected String _where (String strWhere) {
        return _getModuleName () + "::" + strWhere;
    }

    public static String toRadix(int number, int radix) throws Exception {
        if ((radix < MIN_RADIX) || (radix > MAX_RADIX)) {
            throw new Exception("toRadix: radix is too big: " + radix);
        }
        StringBuilder buffer = new StringBuilder();
        while (number >= radix) {
            int remainder = number % radix;
            buffer.insert(0, digits.charAt(remainder));
            number = number / radix;
        }
        buffer.insert(0, digits.charAt(number));
        return buffer.toString();
    }

    public static int fromRadix(String number, int radix) throws Exception {
        if ((radix < MIN_RADIX) || (radix > MAX_RADIX)) {
            throw new Exception("fromRadix: radix is too big: " + radix);
        }
        if (number == null) {
            throw new Exception("fromRadix: number is null");
        }
        int res = 0;
        int power = number.length();
        for (int i = 0; i < number.length(); ++i) {
            power--;
            int index = digits.indexOf(number.charAt(i));
            if (index == 0) {
                continue;
            }
            res += index * Math.pow(radix, power);
        }
        return res;
    }

    public static int bytesToInt2(byte[] b)
            throws Exception {
        return ((b[0] >= 0 ? b[0] : 256 + b[0]) + (b[1] >= 0 ? b[1] : 256 + b[1]) * 0x100);
    }

    public static byte[] int2ToBytes(int v)
            throws Exception {
        byte[] bytes = new byte[2];
        for (int i = 0; i < 2; ++i)
            bytes[i] = (byte) (v >> (i * 8));
        return bytes;
    }

    public static int bytesToInt4(byte[] b)
            throws Exception {
        return ((b[0] >= 0 ? b[0] : 256 + b[0]) * 0x1 + (b[1] >= 0 ? b[1] : 256 + b[1]) * 0x100 + (b[2] >= 0 ? b[2] : 256 + b[2]) * 0x10000 + (b[3] >= 0 ? b[3] : 256 + b[3]) * 0x1000000);
    }

    public static byte[] int4ToBytes(int i)
            throws Exception {
        byte[] bytes = new byte[4];
        for (int j = 0; j < 4; ++j)
            bytes[j] = (byte) (i >> (j * 8));
        return bytes;
    }

    public static double bytesToDouble(byte[] b)
            throws Exception {
        int i = ((b[0] >= 0 ? b[0] : 256 + b[0]) * 0x1 + (b[1] >= 0 ? b[1] : 256 + b[1]) * 0x100 + (b[2] >= 0 ? b[2] : 256 + b[2]) * 0x10000 + (b[3] >= 0 ? b[3] : 256 + b[3]) * 0x1000000);
        return 1.0 * i / 100;
    }

    public static byte[] doubleToBytes(double f)
            throws Exception {
        byte[] bytes = new byte[4];
        int i = (int) (f * 100.0);
        for (int j = 0; j < 4; ++j)
            bytes[j] = (byte) (i >> (j * 8));
        return bytes;
    }

}
