package com.dgphoenix.casino.gs.api.service.xml;

import com.thoughtworks.xstream.XStream;

public class APIServiceTest {
    public static void main(String[] args) {

        String envResponse = "<EXTSYSTEM>\n" +
                "    <REQUEST>\n" +
                "        <BANKID>123</BANKID>\n" +
                "        <HASH>80dc6ca5c672b4df082fa15277ba7c9a</HASH>\n" +
                "    </REQUEST>\n" +
                "    <TIME>29 Aug 2016 11:20:33</TIME>\n" +
                "    <RESPONSE>\n" +
                "        <RESULT>OK</RESULT>\n" +
                "        <LOBBY_URL>http://siteforlogin.com/login_page</LOBBY_URL>\n" +
                "        <BSG_GAMES_URL>http://siteforlogin.com/bsg_games_page</BSG_GAMES_URL>\n" +
                "        <USE_BSG_PROXY>TRUE</USE_BSG_PROXY>\n" +
                "        <ACCOUNTS>\n" +
                "            <ACCOUNT>\n" +
                "                <USERID>3148</USERID>\n" +
                "                <LOGIN>BetsoftEUR</LOGIN>\n" +
                "                <PASSWORD>qwerty</PASSWORD>\n" +
                "                <CURRENCY>EUR</CURRENCY>\n" +
                "                <BALANCE>100308</BALANCE>\n" +
                "                <INUSE>FALSE</INUSE>\n" +
                "            </ACCOUNT>\n" +
                "            <ACCOUNT>\n" +
                "                <USERID>3148721</USERID>\n" +
                "                <LOGIN>BetsoftGBP</LOGIN>\n" +
                "                <PASSWORD>qwerty</PASSWORD>\n" +
                "                <CURRENCY>GBP</CURRENCY>\n" +
                "                <BALANCE>10000</BALANCE>\n" +
                "                <INUSE>TRUE</INUSE>\n" +
                "            </ACCOUNT>\n" +
                "            <ACCOUNT>\n" +
                "                <USERID>1231</USERID>\n" +
                "                <LOGIN>BetsoftEUR2</LOGIN>\n" +
                "                <PASSWORD>qwerty</PASSWORD>\n" +
                "                <CURRENCY>EUR</CURRENCY>\n" +
                "                <BALANCE>9900</BALANCE>\n" +
                "                <INUSE>TRUE</INUSE>\n" +
                "            </ACCOUNT>\n" +
                "        </ACCOUNTS>\n" +
                "    </RESPONSE>\n" +
                "</EXTSYSTEM>\n";

        XStream parser = new XStream();
        XStream.setupDefaultSecurity(parser);
        parser.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.gs.api.service.xml.**"});
        parser.autodetectAnnotations(true);
        parser.processAnnotations(GetEnvironment.class);
        GetEnvironment envResp = (GetEnvironment) parser.fromXML(envResponse);
        System.out.println(envResp);

        String fundResponse = "<EXTSYSTEM>\n" +
                "    <REQUEST>\n" +
                "        <BANKID>123</BANKID>\n" +
                "        <USERID>3148</USERID>\n" +
                "        <AMOUNT>3148</AMOUNT>\n" +
                "        <HASH>80dc6ca5c672b4df082fa15277ba7c9a</HASH>\n" +
                "    </REQUEST>\n" +
                "    <TIME>29 Aug 2016 11:20:33</TIME>\n" +
                "    <RESPONSE>\n" +
                "        <RESULT>OK</RESULT>\n" +
                "        <BALANCE>11000</BALANCE>\n" +
                "    </RESPONSE>\n" +
                "</EXTSYSTEM>\n";

        parser = new XStream();
        XStream.setupDefaultSecurity(parser);
        parser.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.gs.api.service.xml.**"});
        parser.autodetectAnnotations(true);
        parser.processAnnotations(FundAccount.class);
        FundAccount fundResp = (FundAccount) parser.fromXML(fundResponse);
        System.out.println(fundResp);

        String tokenResponse = "<EXTSYSTEM>\n" +
                "    <REQUEST>\n" +
                "        <BANKID>123</BANKID>\n" +
                "        <USERID>3148</USERID>\n" +
                "        <HASH>80dc6ca5c672b4df082fa15277ba7c9a</HASH>\n" +
                "    </REQUEST>\n" +
                "    <TIME>29 Aug 2016 11:20:33</TIME>\n" +
                "    <RESPONSE>\n" +
                "        <RESULT>OK</RESULT>\n" +
                "        <TOKEN>zqHlY2IMo0C9WpO0FV2jRQYf1N</TOKEN>\n" +
                "        <LONGTERM>TRUE</LONGTERM>\n" +
                "    </RESPONSE>\n" +
                "</EXTSYSTEM>\n";

        parser = new XStream();
        XStream.setupDefaultSecurity(parser);
        parser.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.gs.api.service.xml.**"});
        parser.autodetectAnnotations(true);
        parser.processAnnotations(GetActiveToken.class);
        GetActiveToken tokenResp = (GetActiveToken) parser.fromXML(tokenResponse);
        System.out.println(tokenResp);

    }
}
