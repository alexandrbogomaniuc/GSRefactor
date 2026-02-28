package com.abs.casino.common.util.string;

import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.net.util.Base64;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ANGeL
 * Date: Dec 26, 2007
 * Time: 6:18:35 PM
 */
public class StringIdGenerator {
    public static final String ID_DELIMITER = "_";
    public static final String USER_BANK_DELIMITER = "-";
    static String stdcharset = "[a-zA-Z0-9]";
    static int defaultLength = 20;

    private char[] chars;
    private int length;
    private static AtomicLong baseTime = new AtomicLong(System.currentTimeMillis());
    private static final byte[] SECRET_KEY = ("34u534mnbfsdkf9872nm,siueoi./28972398579387598375nvvxxcvxxc" +
            "00-==-0=,mkxjvcxkvjkj538975985xcnvmxcnv908907834sduu9334cxvxc[[]].x,mcvhj11jkl!jlkjsxceteerte" +
            "88723423sfnmvccxbvz0918287873478zmzbaa0-9325iuou982jkhskjhc1`3123hjkhjkzxczxczxlkjcl55sxxvxjj" +
            "2348709284xvkxkvkjhksfshfks84982mbsmbx9x98v98nbnmb 1qe q/o[vjiouiu4824982sccnbzn3975394ksdhkj" +
            "sdfsdlgfidsgiudfyguiydf34598375vmx mnoiqoiqwurob nmb mvxbncvnxv5879739753987598375bvm").getBytes();

    public StringIdGenerator() {
        this(stdcharset, defaultLength);
    }

    public StringIdGenerator(String regexp) {
        this(regexp, defaultLength);
    }

    public StringIdGenerator(int length) {
        this(stdcharset, length);
    }

    public StringIdGenerator(String regexp, int length) {
        char[] charsTmp = new char[256];
        int cnt = 0;
        for (char ch = 0; ch < 256; ch++) {
            if (new String(new char[]{ch}).matches(regexp)) {
                charsTmp[cnt++] = ch;
            }
        }
        chars = new char[cnt];
        System.arraycopy(charsTmp, 0, chars, 0, cnt);

        this.length = length;
    }

    public String generate() {
        return generate(length);
    }

    public String generate(int length) {
        return generateToSB(length).toString();
    }

    public StringBuilder generateToSB(int length) {
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < length; ii++) {
            sb.append(chars[RNG.nextInt(chars.length)]);
        }
        return sb;
    }

    //serverId_sessionId_extUserId+bankId
    public static int extractServerId(String id) {
        if(StringUtils.isTrimmedEmpty(id)) {
            return -1;
        }
        int index = id.indexOf(ID_DELIMITER);
        if(index <= 0) {
            return -1;
        }
        return Integer.valueOf(id.substring(0, index));
    }

    public static Pair<Integer, String> extractBankAndExternalUserId(String sessionId) {
        String hash = extractUserHash(sessionId);
        return extractBankAndExternalUserIdFromUserHash(hash);
    }

    public static Pair<Integer, String> extractBankAndExternalUserIdFromUserHash(String userHash) {
        String decoded = decodeFromXOR(userHash);
        if(!decoded.contains(USER_BANK_DELIMITER)) {
            throw new IllegalArgumentException("Invalid user hash: " + decoded);
        }
        int index = decoded.lastIndexOf(USER_BANK_DELIMITER);
        String userId = decoded.substring(0, index);
        String bankId = decoded.substring(index + 1);
        return new Pair<Integer, String>(Integer.valueOf(bankId), userId);
    }

    public static String extractUserHash(String sessionId) {
        if(StringUtils.isTrimmedEmpty(sessionId)) {
            throw new IllegalArgumentException("sessionId is empty: '" + sessionId + "'");
        }
        int index = sessionId.indexOf(ID_DELIMITER);
        if (index <= 0) {
            throw new IllegalArgumentException("Cannot extract user hash from sessionId: '" + sessionId + "'");
        }
        index = sessionId.indexOf(ID_DELIMITER, index + 1);
        if (index <= 0) {
            throw new IllegalArgumentException("Cannot extract user hash from sessionId: '" + sessionId + "'");
        }
        return sessionId.substring(index + 1);
    }

    public static String getAccountHash(int bankId, String externalUserId) {
        return encodeToXOR(externalUserId + USER_BANK_DELIMITER + bankId);
    }

    public static String encodeToXOR(String s) {
        byte[] input = s.getBytes();
        byte[] out = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = (byte) (input[i] ^ (i >= SECRET_KEY.length ? 0xb : SECRET_KEY[i]));
        }
        return Base64.encodeBase64URLSafeString(out);
    }

    public static String decodeFromXOR(String s) {
        final byte[] input = Base64.decodeBase64(s);
        byte[] out = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = (byte) (input[i] ^ (i >= SECRET_KEY.length ? 0xb : SECRET_KEY[i]));
        }
        return new String(out);
    }

    public static String generateSessionId(long gameServerId, int bankId, String externalUserId) {
        String hash = getAccountHash(bankId, externalUserId);
        return gameServerId + ID_DELIMITER + generateTimeAndRandomBased() + ID_DELIMITER + hash;
    }

    public static String getFastMD5(String str) {
        return DigestUtils.md5Hex(str);
    }

    public static String generateSession(int length) {
        String result = "";
        for (int i = 0; i < length; i++) {
            result += Integer.toString(RNG.nextInt() & Integer.MAX_VALUE, 36);
        }
        result = result.substring(0, length);
        return result;
    }

    //length is fixed - 32
    public static String generateTimeAndRandomBased() {
        long mostSigBits = baseTime.getAndIncrement();
        long leastSigBits = UUID.randomUUID().getLeastSignificantBits();
        //commented is natural order
/*
        return (digits(mostSigBits >> 32, 8) +
            digits(mostSigBits >> 16, 4) +
            digits(mostSigBits, 4) +
            digits(leastSigBits >> 48, 4) +
            digits(leastSigBits, 12));
*/
        return (digits(leastSigBits, 12) +
                digits(mostSigBits >> 16, 4) +
                digits(mostSigBits, 4) +
                digits(mostSigBits >> 32, 8) +
                digits(leastSigBits >> 48, 4));

    }

    public static String generateRandomBasedEightSymbols() {
        long leastSigBits = UUID.randomUUID().getLeastSignificantBits();
        return (digits(leastSigBits, 5) +digits(leastSigBits >> 48, 3));
    }
    /**
     * Returns val represented by the specified number of hex digits.
     */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    public static void main(String[] args) {
        System.out.println(extractBankAndExternalUserId("1_ba620e0c9a7fc8470e5300000166931b_XlsHRlpfXENQUUI"));
        for (int ii = 0; ii < 10; ii++) {
            //System.out.println("id = " + generator.generate(20));
            //String rnd = StringIdGenerator.generateTimeAndRandomBased();
            //System.out.println("id: size = " + rnd.length() + ", val = " + rnd);
        }
    }
}
