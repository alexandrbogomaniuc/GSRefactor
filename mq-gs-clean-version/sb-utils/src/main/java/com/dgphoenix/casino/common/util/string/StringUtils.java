package com.dgphoenix.casino.common.util.string;

import com.dgphoenix.casino.common.util.CollectionUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by ANGeL
 * Date: Jan 10, 2008
 * Time: 5:48:09 PM
 */
public class StringUtils {
    private static final String PATTERN = "                    ";

    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private static final String BINARY_DATA_V2_HEADER = "BDv2";

    private static MessageDigest md5Helper;
    private static MessageDigest sha1Helper;

    static {
        try {
            if (md5Helper == null) {
                md5Helper = MessageDigest.getInstance("MD5");
            }
            if(sha1Helper == null) {
                sha1Helper = MessageDigest.getInstance("SHA-1");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
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
            bytes[i] = dp > 0 ? Byte.parseByte(source.substring(0, dp)) : Byte.parseByte(source.substring(0));
            source = source.substring(dp + 1);
        }

        return bytes;
    }

    public static Collection<CharSequence> split(String str, String delimiter) {
        Collection<CharSequence> result = new ArrayList<CharSequence>();
        StringTokenizer st;
        st = delimiter != null ? new StringTokenizer(str, delimiter) : new StringTokenizer(str);
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public static Collection<CharSequence> split(String str) {
        return split(str, null);
    }

    public static String[] concatArrays(String[] a1, String[] a2) {
        String[] a3 = new String[a1.length + a2.length];
        System.arraycopy(a1, 0, a3, 0, a1.length);
        System.arraycopy(a2, 0, a3, a1.length, a2.length);
        return a3;
    }

    public static String fillWithValue(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static String prepareString(String string) {
        if (string.contains("'")) {
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
        if (str.contains("'")) {
            StringBuilder sb = new StringBuilder(str);
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (c == '\'') {
                    sb.insert(i, '\'');
                    i++;
                }
            }
            return "'" + sb + "'";
        } else {
            return "'" + str + "'";
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

    public static String getFormattingBlock(String message) {
        String line = "=============================================================================\n";
        return line + message + "\n" + line;
    }

    public static String toHexString(byte[] b) {
        if (b == null || b.length == 0) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(b.length << 1);

        for (byte aB : b) {
            sb.append(HEX_DIGITS[(aB & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[aB & 0x0f]);
        }

        return sb.toString();
    }

    public static String parseLandingPage(String landingPage) {
        String result = null;
        if (landingPage != null) {
            landingPage = landingPage.trim();
            if (!landingPage.isEmpty()) {
                result = landingPage.startsWith("http://") ? landingPage : "http://" + landingPage;
            }
        }
        return result;
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

    public static byte[] getSha1AsBytes(byte[] bytes) {
        byte[] messageDigest;
        synchronized (sha1Helper) {
            sha1Helper.reset();
            messageDigest = sha1Helper.digest(bytes);
        }
        return messageDigest;
    }

    public static String getSha1(String s) {
        return getSha1(s.getBytes());
    }

    public static String getSha1(byte[] bytes) {
        byte[] messageDigest;
        synchronized (sha1Helper) {
            sha1Helper.reset();
            messageDigest = sha1Helper.digest(bytes);
        }
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

    public static byte[] getMD5AsBytes(byte[] bytes) {
        byte[] messageDigest;
        synchronized (md5Helper) {
            md5Helper.reset();
            messageDigest = md5Helper.digest(bytes);
        }
        return messageDigest;
    }

    public static String getMD5(byte[] bytes) {
        byte[] messageDigest;
        synchronized (md5Helper) {
            md5Helper.reset();
            messageDigest = md5Helper.digest(bytes);
        }
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

    public static String getMD5(String str) {
        return getMD5(str.getBytes());
    }

    public static String asMoney(long amount) {
        StringBuilder sb = new StringBuilder();
        sb.append(amount / 100).append(".");
        String tmp = Long.toString(amount % 100);
        if (tmp.length() == 1) {
            sb.append("0").append(tmp);
        } else {
            sb.append(tmp);
        }
        return sb.toString();
    }

    public static<T> String toString(List<T> objects, String delimiter, IStringSerializer<T> serializer) {
        if(objects == null || objects.isEmpty()) {
            return "";
        }
        if(objects.size() == 1) {
            return serializer.toString(objects.get(0));
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objects.size(); i++) {
            sb.append(serializer.toString(objects.get(i)));
            if(i + 1 < objects.size()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String getShortClassName(Class clazz) {
        return clazz.getPackage().getName().isEmpty() ? clazz.getName() : clazz.getName().substring(clazz.getPackage().getName().length() + 1);
    }

    public static boolean isTrimmedEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isTrimmedEmpty(String... strings) {
        for(String s : strings) {
            if(isTrimmedEmpty(s)) {
                return true;
            }
        }
        return false;
    }

    public static String formatNumber(long value, int length) {
        String v = String.valueOf(value);
        if (v.length() >= length || v.length() >= PATTERN.length()) {
            return v;
        } else {
            StringBuilder sb = new StringBuilder(PATTERN);
            return sb.replace(length - v.length(),
                    PATTERN.length(), v).toString();
        }
    }

    public static String printProperties(Map<?, ?> properties) {
        StringBuilder sb = new StringBuilder();
        if (!CollectionUtils.isEmpty(properties)) {
            for (Map.Entry<?, ?> entry : properties.entrySet()) {
                sb.append(entry.getKey());
                sb.append("='");
                sb.append(entry.getValue() != null ? (entry.getValue() + "';") : "';");
            }
        }
        return sb.toString();
    }

    public static String substring(StringBuilder target, String startString, String endString) throws Exception {
        return substring(target.toString(), startString, endString);
    }

    public static String substring(String target, String startString, String endString) throws Exception {
        int startIndex = target.indexOf(startString);
        int endIndex = target.indexOf(endString);
        if (startIndex < 0 || endIndex < 0) {
            throw new Exception("Can't substring:" + " startString="
                    + startString + ", endString=" + endString + "target=" + target);
        }
        return target.substring(startIndex, endIndex);
    }

    public static String getStreamAsString(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        for(String line = br.readLine(); line != null; line = br.readLine()) {
            out.append(line);
        }
        br.close();
        return out.toString();
    }

    public static String convertStopReel(String stopreel, String delimert, String replacer, int sizereel) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(stopreel, delimert);
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken()).append(st.countTokens() % sizereel == 0 ? replacer : delimert);
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    /**
     * It is analogue of JS function {@code encodeURIComponent}, which's complimentary
     * function {@code decodeURIComponent} is used to decode parameters on the client side.
     * You should use this function to encode parameters for the client side.
     *
     * Note: it does not replace %21,%27,%28,%29,%7E with !,',(,),~, as it's assumed
     * that we do not use already encoded symbols in the source string.
     * If it is needed, you should add additional replacement for these symbols.
     */
    public static String encodeUriComponent(String source) {
        String result;
        try {
            result = URLEncoder.encode(source, "UTF-8")
                    .replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            result = source;
        }
        return result;
    }
}
