package com.abs.casino.common.mail;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailHelper {
    private static final String UTF8_ENCODING = "utf-8";

    protected static MailHelper instance = new MailHelper();

    public static MailHelper getInstance() {
        return instance;
    }

    public MailHelper() {
    }

    public void send(String smtpHost, String from, String to, String subject, String message)
            throws CommonException {
        try {
            MimeMessage mimeMessage = new MimeMessage(getSession(smtpHost));
            mimeMessage.setFrom(new InternetAddress(from));
            mimeMessage.setRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));
            mimeMessage.setSubject(subject, UTF8_ENCODING);
            mimeMessage.setText(crlfize(message), UTF8_ENCODING);
            mimeMessage.setHeader("Content-Type", "text/html; charset=utf-8");
            Transport.send(mimeMessage);
        } catch (Exception e) {
            throw new CommonException("Unable to send message", e);
        }
    }

    private String crlfize(String str) {
        return str.replaceAll("\\n", "\r\n");
    }

    private Session getSession(String smtpHost) throws CommonException {
        if (StringUtils.isTrimmedEmpty(smtpHost)) {
            throw new CommonException("Empty smptHost");
        }
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        return Session.getInstance(properties);
    }

}