package com.dgphoenix.casino.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.mail.internet.MimeUtility;
import org.apache.log4j.Logger;

public class ZipUtils {
    private static final Logger LOG = Logger.getLogger(ZipUtils.class);

	public static byte[] zipStringToBytes(String input) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedOutputStream bufos = new BufferedOutputStream(new GZIPOutputStream(bos));
		bufos.write(input.getBytes());
		bufos.close();
		byte[] retval = bos.toByteArray();
		bos.close();
		return retval;
	}

	public static String unzipStringFromBytes(byte[] bytes) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		BufferedInputStream bufis = new BufferedInputStream(new GZIPInputStream(bis));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = bufis.read(buf)) > 0) {
			bos.write(buf, 0, len);
		}
		String retval = bos.toString();
		bis.close();
		bufis.close();
		bos.close();
		return retval;
	}

	public static byte[] encodeMIME(byte[] b) throws Exception {
		try {
			LOG.debug("1ZipUtils.encodeMIME()");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			LOG.debug("2ZipUtils.encodeMIME()");
			OutputStream b64os = MimeUtility.encode(baos, "base64");
			LOG.debug("3ZipUtils.encodeMIME()");
			b64os.write(b);
			LOG.debug("4ZipUtils.encodeMIME()");
			b64os.close();
			LOG.debug("5ZipUtils.encodeMIME()");
			return baos.toByteArray();
		} catch (Exception e) {
            LOG.error("Can't encodeMIME", e);
			throw e;
		}
	}

	public static byte[] decodeMIME(byte[] b) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        InputStream b64is = MimeUtility.decode(bais, "base64");
        byte[] tmp = new byte[b.length];
        int n = b64is.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return res;
     }


}