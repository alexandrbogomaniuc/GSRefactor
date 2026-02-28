package com.abs.casino.common.util;


import com.dgphoenix.casino.common.util.string.HexStringConverter;
import com.dgphoenix.casino.common.util.string.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RSACrypter {

    public static String getDecrMessage(String message, BigInteger privExp, BigInteger modulus) {
        int countPart = message.length() / 128;
        String result = "";
        for (int i = 0; i < countPart; ++i) {
            String part = message.substring(i*128, i*128 + 128);
            result += getPartDecrMessage(part, privExp, modulus);
        }
        return result;
    }

    public static String getEncrMessage(String message, BigInteger pubExp, BigInteger modulus) {
        String result = "";

        List<String> parts = getParts(message, modulus);
        for (String part : parts) {
            result += getPartEncrMessage(part, pubExp, modulus);
        }

        return result;
    }

    private static List<String> getParts(String str, BigInteger modulus) {
		List<String> list = new ArrayList<String>();
		int start = 0;
		while (start < str.length()) {
			String part = getPart(start, str, modulus);
			start += part.length();
			list.add(part);
		}
		return list;

	}

	private static String getPart(int start, String str, BigInteger modulus) {
		int index = start;
		int end = start;
		while (isEarly(str, index, end, modulus)) {
			++end;
		}
		return str.substring(start, end);
	}

	private static boolean isEarly(String str, int index, int end, BigInteger modulus) {//inculde
		if (end >= str.length()){
			return false;
		} else {
			String part = str.substring(index, end+1);
			if (isLessModul(part, modulus)) {
				return true;
			} else {
				return false;
			}
		}
	}

    private static String getPartDecrMessage(String message, BigInteger privExp, BigInteger modulus) {
        BigInteger src = new BigInteger(message, 16);
        BigInteger res = src.modPow(privExp, modulus);
        return new String(res.toByteArray());
    }

    private static String getPartEncrMessage(String message, BigInteger pubExp, BigInteger modulus) {
        BigInteger src = new BigInteger(message.getBytes());
        BigInteger res = src.modPow(pubExp, modulus);
        return trimTo128(HexStringConverter.byteArrayToHexString(res.toByteArray()));
    }

    private static String trimTo128(String str) {
        String res = "";
        if (str.length() == 128) {
            return str;
        } else if (str.length() > 128) {
            int k = str.length() - 128;
            return str.substring(k);

        } else { // < 128
            int len = str.length();
            for (int i = 0; i < 128 - len; ++i) {
                res += "0";
            }
            return res + str;
        }
    }

    private static boolean isLessModul(String str, BigInteger modulus) {
        if (StringUtils.isTrimmedEmpty(str)) return true;
        String hexStr = HexStringConverter.byteArrayToHexString(str.getBytes());
        return modulus.compareTo(new BigInteger(hexStr, 16)) >= 0;
    }
}
