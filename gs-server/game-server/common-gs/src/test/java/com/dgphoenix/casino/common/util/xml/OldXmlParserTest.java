package com.dgphoenix.casino.common.util.xml;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.xml.parser.Parser;
import com.dgphoenix.casino.common.util.xml.parser.XmlHandler;
import com.dgphoenix.casino.common.util.xml.parser.XmlHandlerRegistry;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 1/24/12
 */
public class OldXmlParserTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Map<String, XmlHandler> handlers = new HashMap<String, XmlHandler>();
        XmlHandlerRegistry.instance().register("EXTSYSTEM",
                new com.dgphoenix.casino.gs.managers.payment.wallet.common.xml.CWHandler());
    }

    @Test
    public void testBadExtSystem() {
        String response = "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>                                                                                                                                                         \n" +
                "<EXTSYSTEMm>                                                                                                                                                                                          \n" +
                "  <REQUEST>                                                                                                                                                                                          \n" +
                "    <TOKEN>EA12E6B7-68CF-46BE-8B19-DEF5F3FE6BE1</TOKEN>                                                                                                                                              \n" +
                "    <HASH>8d31e25d730f7087ba6712dfd9f38d38</HASH>                                                                                                                                                    \n" +
                "  </REQUEST>                                                                                                                                                                                         \n" +
                "  <TIME>24 January 2012 4:02:55 AM</TIME>                                                                                                                                                            \n" +
                "  <RESPONSE>                                                                                                                                                                                         \n" +
                "    <RESULT>OK</RESULT>                                                                                                                                                                              \n" +
                "    <USERID>155867</USERID>                                                                                                                                                                          \n" +
                "    <USERNAME>ktdtest112</USERNAME>                                                                                                                                                                  \n" +
                "    <FIRSTNAME>Ethth</FIRSTNAME>                                                                                                                                                                     \n" +
                "    <LASTNAME>eethrer</LASTNAME>                                                                                                                                                                     \n" +
                "    <EMAIL>eethrer</EMAIL>                                                                                                                                                                           \n" +
                "    <CURRENCY>EUR</CURRENCY>                                                                                                                                                                         \n" +
                "    <BALANCE>9612.06</BALANCE>                                                                                                                                                                       \n" +
                "  </RESPONSE>                                                                                                                                                                                        \n" +
                "</EXTSYSTEMm>";
        XmlRequestResult result = new XmlRequestResult();
        Parser parser = Parser.instance();
        CommonException exc = null;
        try {
            parser.parse(response, result);
        } catch (CommonException e) {
            exc = e;
            //e.printStackTrace();
        }
        assertEquals("Must be parser exception", true, exc != null);
        assertEquals("Inner exception must be SAXException", true, exc != null && exc.getCause() instanceof org.xml.sax.SAXException);
        //exc.printStackTrace();
    }


    @Test
    public void testGoodExtSystem() {
        String response = "\n<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>                                                                                                                                                         \n" +
                "<EXTSYSTEM>                                                                                                                                                                                          \n" +
                "  <REQUEST>                                                                                                                                                                                          \n" +
                "    <TOKEN>EA12E6B7-68CF-46BE-8B19-DEF5F3FE6BE1</TOKEN>                                                                                                                                              \n" +
                "    <HASH>8d31e25d730f7087ba6712dfd9f38d38</HASH>                                                                                                                                                    \n" +
                "  </REQUEST>                                                                                                                                                                                         \n" +
                "  <TIME>24 January 2012 4:02:55 AM</TIME>                                                                                                                                                            \n" +
                "  <RESPONSE>                                                                                                                                                                                         \n" +
                "    <RESULT>OK</RESULT>                                                                                                                                                                              \n" +
                "    <USERID>155867</USERID>                                                                                                                                                                          \n" +
                "    <USERNAME>ktdtest112</USERNAME>                                                                                                                                                                  \n" +
                "    <FIRSTNAME>Ethth</FIRSTNAME>                                                                                                                                                                     \n" +
                "    <LASTNAME>eethrer</LASTNAME>                                                                                                                                                                     \n" +
                "    <EMAIL>eethrer</EMAIL>                                                                                                                                                                           \n" +
                "    <CURRENCY>EUR</CURRENCY>                                                                                                                                                                         \n" +
                "    <BALANCE>9612.06</BALANCE>                                                                                                                                                                       \n" +
                "  </RESPONSE>                                                                                                                                                                                        \n" +
                "</EXTSYSTEM>";
        XmlRequestResult result = new XmlRequestResult();
        Parser parser = Parser.instance();
        try {
            parser.parse(response.trim(), result);
        } catch (CommonException e) {
            e.printStackTrace();
        }

        assertEquals(true, result.isSuccessful());

        Map<String, String> shouldResponseMap = new HashMap<>();
        shouldResponseMap.put("BALANCE", "9612.06");
        shouldResponseMap.put("FIRSTNAME", "Ethth");
        shouldResponseMap.put("RESULT", "OK");
        shouldResponseMap.put("CURRENCY", "EUR");
        shouldResponseMap.put("EMAIL", "eethrer");
        shouldResponseMap.put("USERNAME", "ktdtest112");
        shouldResponseMap.put("LASTNAME", "eethrer");
        shouldResponseMap.put("USERID", "155867");

        assertEquals(shouldResponseMap, result.getResponseParameters());
    }

    @Test
    public void testGoodExtSystem2() {
        String response = "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?><" +
                "EXTSYSTEM><REQUEST><USERID>pbqs252</USERID><BET></BET><WIN>2.0|32689075</WIN>" +
                "<ROUNDID>32163624</ROUNDID><GAMEID>143</GAMEID></REQUEST><TIME>24 Jan 2012 07:24:34</TIME>" +
                "<RESPONSE><RESULT>FAILED</RESULT><CODE></CODE></RESPONSE></EXTSYSTEM>";
        XmlRequestResult result = new XmlRequestResult();
        Parser parser = Parser.instance();
        try {
            parser.parse(response.trim(), result);
        } catch (CommonException e) {
            e.printStackTrace();
        }
        //System.out.println("result=" + result);
    }

}
