package com.dgphoenix.casino.gs.singlegames.tools.util;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.util.*;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static org.apache.commons.lang.StringUtils.*;

public class StringUtils {

    private static final String BINARY_DATA_V2_HEADER = "BDv2";
    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String[] concatArrays(String[] a1, String[] a2) {
        String[] a3 = new String[a1.length + a2.length];
        System.arraycopy(a1, 0, a3, 0, a1.length);
        System.arraycopy(a2, 0, a3, a1.length, a2.length);
        return a3;
    }

    /**
     * Encodes binary data into ASCII string insensitive to charset encoding.
     * <p/>
     * <p>Uses Base64 algorithm and prepends resulting string with <tt>BDv2</tt>
     * header to distinguish such a data from <i>ugly deprecated storage format
     * </i> used previously.</p>
     *
     * @see #fromASCII(String)
     */
    public static String toASCII(byte[] source) {

        final byte[] data = Base64.encodeBase64(source);

        return BINARY_DATA_V2_HEADER + new String(data);
    }

    /**
     * Binary data representation reader.
     * <p/>
     * <p>Decodes binary data encoded with {@link #toASCII(byte[])}.</p>
     * <p/>
     * <p>Restores binary data from string in Base64-encoded format if starts
     * with <tt>BDv2</tt> header, or in <i>ugly deprecated storage format</i>,
     * which is present for historical reasons only.</p>
     */
    public static byte[] fromASCII(String source) {
        if (source.startsWith(BINARY_DATA_V2_HEADER)) {
            return Base64.decodeBase64(source.substring(4).getBytes());
        }

        int dp = source.indexOf(' ');
        int dim = Integer.parseInt(source.substring(0, dp));
        source = source.substring(dp + 1);
        byte[] bytes = new byte[dim];

        for (int i = 0; i < bytes.length; i++) {
            dp = source.indexOf(' ');
            if (dp > 0) {
                bytes[i] = Byte.parseByte(source.substring(0, dp));
            } else {
                bytes[i] = Byte.parseByte(source.substring(0));
            }
            source = source.substring(dp + 1);
        }

        return bytes;
    }

    public static String fillWithValue(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static String prepareString(String string) {
        if (string.indexOf("'") != -1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < string.length(); i++) {
                char ch = string.charAt(i);
                if (ch == '\'') {
                    sb.append("'");
                }
                sb.append(ch);
            }
            string = sb.toString();
        }
        return "'" + string + "'";
    }

    public static String quoteString(String str) {
        if (str == null) {
            return "null";
        }
        if (str.indexOf("'") != -1) {
            StringBuilder sb = new StringBuilder(str);
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (c == '\'') {
                    sb.insert(i, '\'');
                    i++;
                }
            }
            return "'" + sb.toString() + "'";
        } else {
            return "'" + str + "'";
        }
    }

//    public static String getAlertInfo(IAlert alert)
//    {
//        StringBuffer info = new StringBuffer();
//        info.append(alert.getClass().getName().substring(alert.getClass().getPackage().getName().length() + 1));
//        info.append(" [AlertId = ");
//        info.append(alert.getId());
//        info.append("; SourceDb = ");
//        info.append(alert.getSourceDB());
//        info.append("; SourceId = ");
//        info.append(alert.getSourceId());
//        info.append("; ClassId = ");
//        info.append(alert.getClassAlert());
//        info.append("; TypeId = ");
//        info.append(alert.getType());
//        info.append("]");
//        return info.toString();
//    }

    public static String getShortClassName(Class clazz) {
        if (clazz.getPackage().getName().length() != 0) {
            return clazz.getName().substring(clazz.getPackage().getName().length() + 1);
        } else {
            return clazz.getName();
        }
    }

    public static String substituteParams(Map<String, String> params, String message) {
        if (params == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        Set<Map.Entry<String, String>> entries = params.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String substring = "<?" + entry.getKey() + "?>";
            int start = sb.toString().indexOf(substring);
            while (start != -1) {
                String val = entry.getValue();
                int end = start + substring.length();
                sb.replace(start, end, val);
                start = sb.toString().indexOf(substring);
            }

        }
        return sb.toString();
    }

    public static Collection split(String str, String delimiter) {
        Collection result = new ArrayList();
        StringTokenizer st = null;
        if (delimiter != null) {
            st = new StringTokenizer(str, delimiter);
        } else {
            st = new StringTokenizer(str);
        }
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public static Collection split(String str) {
        return split(str, null);
    }

    public static String getFormattingBlock(String message) {
        String line = "=============================================================================\n";
        return line + message + "\n" + line;
    }

    public static String parseLandingPage(String landingPage) {
        String result = null;
        if (landingPage != null) {
            landingPage = landingPage.trim();
            if (landingPage.length() != 0) {
                if (!landingPage.startsWith("http://")) {
                    result = "http://" + landingPage;
                } else {
                    result = landingPage;
                }
            }
        }
        return result;
    }

    public static String toHexString(byte[] b) {
        if (b == null || b.length == 0) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(b.length << 1);

        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }

        return sb.toString();
    }

    public static byte[] toByteArray(String hexString) {
        byte[] result = new byte[hexString.length() / 2];

        for (int i = 0; i < result.length; i++) {
            String tmp = hexString.substring(i * 2, i * 2 + 2);
            result[i] = Integer.decode("0x" + tmp).byteValue();
        }
        return result;
    }

    public static String cutTo(String str, int n) {
        return str.length() <= n ? str : str.substring(0, n) + "...";
    }

    public static String[] lastArrayElements(String[] strings, int shift) {
        String[] out = new String[strings.length - shift];
        System.arraycopy(strings, shift, out, 0, out.length);
        return out;
    }

    public static String getMD5(String str) throws Exception {
        MessageDigest algorithm = MessageDigest.getInstance("MD5");
        algorithm.reset();
        algorithm.update(str.getBytes());
        byte messageDigest[] = algorithm.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : messageDigest) {
            String hex = Integer.toHexString(0xff & aMessageDigest);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String extractDomainFromUrl(String referer) {
        if (isTrimmedEmpty(referer)) {
            return null;
        }
        String domain = substringBetween(referer, "://", "/");
        if (isTrimmedEmpty(domain)) {
            domain = substringAfter(referer, "://");
            if (isTrimmedEmpty(domain)) {
                domain = substringBefore(referer, "/");
                if (isTrimmedEmpty(domain)) {
                    domain = referer;
                }
            }
        }
        return domain;
    }
}