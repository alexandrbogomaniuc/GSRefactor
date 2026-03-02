<%@ page import="com.abs.casino.cassandra.persist.CassandraCurrencyRatesPersister" %>
<%@ page import="com.abs.casino.common.currency.CurrencyRate" %>
<%@ page import="com.abs.casino.tracker.CurrencyUpdateProcessor" %>
<%@ page import="com.abs.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%
    String startData = "AOA;EUR;0.0087\n" +
            "BRL;EUR;0.3069\n" +
            "TOP;EUR;0.4764\n" +
            "SRD;EUR;0.2862\n" +
            "ESP;EUR;1.0\n" +
            "TVD;EUR;1.0\n" +
            "GMR;EUR;0.528\n" +
            "BAM;EUR;0.5117\n" +
            "HKD;EUR;0.1218\n" +
            "XOF;EUR;0.0015\n" +
            "TYR;EUR;1.0\n" +
            "MWK;EUR;0.0022\n" +
            "TTD;EUR;0.1485\n" +
            "RWF;EUR;0.0014\n" +
            "XAG;EUR;15.458\n" +
            "ETB;EUR;0.0462\n" +
            "CUP;EUR;0.9443\n" +
            "CNY;EUR;0.1519\n" +
            "YED;EUR;1.0\n" +
            "RVP;EUR;0.9\n" +
            "YER;EUR;0.0044\n" +
            "NJN;EUR;1.0\n" +
            "BYR;EUR;1.0E-4\n" +
            "AWG;EUR;0.5274\n" +
            "CHF;EUR;0.9632\n" +
            "MNT;EUR;5.0E-4\n" +
            "SVC;EUR;0.108\n" +
            "DAN;EUR;1.0\n" +
            "SAR;EUR;0.2517\n" +
            "HTG;EUR;0.02\n" +
            "KRW;EUR;9.0E-4\n" +
            "JMD;EUR;0.0082\n" +
            "CVE;EUR;0.0091\n" +
            "PYG;EUR;2.0E-4\n" +
            "MKD;EUR;0.0164\n" +
            "POO;EUR;1.0\n" +
            "FMN;EUR;1.0\n" +
            "III;EUR;1.0\n" +
            "TJS;EUR;0.1574\n" +
            "BTC;EUR;224.25\n" +
            "MBC;EUR;0.22463999999999998\n" +
            "SCR;EUR;0.0691\n" +
            "IMP;EUR;1.0\n" +
            "BOV;EUR;1.0\n" +
            "SDG;EUR;0.1582\n" +
            "SLL;EUR;2.0E-4\n" +
            "TND;EUR;0.4776\n" +
            "FUN;EUR;1.0\n" +
            "CLP;EUR;0.0015\n" +
            "WCW;EUR;1.0\n" +
            "XDR;EUR;1.294\n" +
            "ALL;EUR;0.0072\n" +
            "ZMK;EUR;1.0E-4\n" +
            "NZD;EUR;0.7037\n" +
            "OMR;EUR;2.4532\n" +
            "CRC;EUR;0.0018\n" +
            "RSD;EUR;0.0084\n" +
            "UAH;EUR;0.0403\n" +
            "XYZ;EUR;1.0\n" +
            "NGN;EUR;0.0047\n" +
            "HUF;EUR;0.0034\n" +
            "CHW;EUR;1.0\n" +
            "BWP;EUR;0.0953\n" +
            "SZL;EUR;0.0785\n" +
            "FDS;EUR;1.0\n" +
            "JEP;EUR;1.0\n" +
            "ARS;EUR;0.1066\n" +
            "DZD;EUR;0.0096\n" +
            "ANG;EUR;0.5276\n" +
            "NIE;EUR;1.0\n" +
            "KGS;EUR;0.0148\n" +
            "TST;EUR;1.0\n" +
            "EGP;EUR;0.1238\n" +
            "RON;EUR;0.2269\n" +
            "PEN;EUR;0.3027\n" +
            "FLY;EUR;1.0\n" +
            "XTS;EUR;1.0\n" +
            "ZAR;EUR;0.0782\n" +
            "CUC;EUR;1.0\n" +
            "MYR;EUR;0.2569\n" +
            "LTL;EUR;0.3218\n" +
            "BBB;EUR;1.0\n" +
            "ZWL;EUR;0.0029\n" +
            "XFU;EUR;1.0\n" +
            "AUD;EUR;0.7167\n" +
            "AFN;EUR;0.0163\n" +
            "XAU;EUR;1136.8\n" +
            "VND;EUR;4.371E-5\n" +
            "BGN;EUR;0.5113\n" +
            "PAB;EUR;0.9443\n" +
            "UYU;EUR;0.0361\n" +
            "TZS;EUR;5.0E-4\n" +
            "VVV;EUR;1.0\n" +
            "KWD;EUR;3.1263\n" +
            "GRU;EUR;1.0\n" +
            "CDF;EUR;0.0010\n" +
            "QWE;EUR;1.0\n" +
            "BZD;EUR;0.4734\n" +
            "INR;EUR;0.0151\n" +
            "XBC;EUR;1.0\n" +
            "VUV;EUR;0.0087\n" +
            "MOP;EUR;0.1183\n" +
            "RUB;EUR;0.0176\n" +
            "BMD;EUR;0.9442\n" +
            "LKR;EUR;0.0071\n" +
            "NAD;EUR;0.0785\n" +
            "GNF;EUR;1.0E-4\n" +
            "XPD;EUR;732.43\n" +
            "MBT;EUR;1.0\n" +
            "MXV;EUR;1.0\n" +
            "DOP;EUR;0.0211\n" +
            "USD;EUR;0.9444\n" +
            "XBA;EUR;1.0\n" +
            "KKL;EUR;1.0\n" +
            "SOS;EUR;0.0014\n" +
            "YUN;EUR;1.0\n" +
            "CZK;EUR;0.0365\n" +
            "PGK;EUR;0.3516\n" +
            "LAK;EUR;1.0E-4\n" +
            "KMF;EUR;0.0020\n" +
            "TMT;EUR;0.2698\n" +
            "BHD;EUR;2.507\n" +
            "BSD;EUR;0.9444\n" +
            "HHG;EUR;1.0\n" +
            "BOB;EUR;0.1366\n" +
            "WDW;EUR;0.85984523\n" +
            "INC;EUR;1.0\n" +
            "MUR;EUR;0.026\n" +
            "LYD;EUR;0.6918\n" +
            "KES;EUR;0.0101\n" +
            "WST;EUR;0.3821\n" +
            "XPF;EUR;0.0084\n" +
            "PLN;EUR;0.2486\n" +
            "ISK;EUR;0.0068\n" +
            "WCC;EUR;1.0\n" +
            "IQD;EUR;8.0E-4\n" +
            "ZWD;EUR;0.0026\n" +
            "GSN;EUR;1.0\n" +
            "GKZ;EUR;0.255\n" +
            "KZT;EUR;0.0051\n" +
            "NOK;EUR;0.1164\n" +
            "SKK;EUR;0.0332\n" +
            "DKK;EUR;0.1338\n" +
            "GHS;EUR;0.2469\n" +
            "SGD;EUR;0.6897\n" +
            "LSL;EUR;0.0784\n" +
            "TRR;EUR;1.0\n" +
            "MDL;EUR;0.053\n" +
            "RRR;EUR;1.0\n" +
            "STD;EUR;1.0\n" +
            "IDN;EUR;1.0\n" +
            "UZS;EUR;4.0E-4\n" +
            "THB;EUR;0.0289\n" +
            "JPY;EUR;0.0078\n" +
            "ILS;EUR;0.2368\n" +
            "VEF;EUR;0.1487\n" +
            "FKP;EUR;1.4596\n" +
            "MMK;EUR;9.0E-4\n" +
            "RDV;EUR;1.0\n" +
            "SYP;EUR;0.0050\n" +
            "KHR;EUR;2.0E-4\n" +
            "VEB;EUR;1.0E-4\n" +
            "LBP;EUR;6.0E-4\n" +
            "ZMW;EUR;0.131\n" +
            "GMK;EUR;0.255\n" +
            "PHP;EUR;0.0211\n" +
            "AZN;EUR;0.8964\n" +
            "BND;EUR;0.69\n" +
            "LVL;EUR;1.4239\n" +
            "BDT;EUR;0.0122\n" +
            "XXX;EUR;1.0\n" +
            "HNL;EUR;0.0444\n" +
            "GTQ;EUR;0.1233\n" +
            "GIP;EUR;1.3799\n" +
            "SBD;EUR;0.1207\n" +
            "UGX;EUR;3.0E-4\n" +
            "GPB;EUR;1.0\n" +
            "MXN;EUR;0.0618\n" +
            "BIF;EUR;6.0E-4\n" +
            "PTS;EUR;1.0\n" +
            "YEC;EUR;1.0\n" +
            "EUR;EUR;1.0\n" +
            "MVR;EUR;0.0614\n" +
            "USS;EUR;1.0\n" +
            "ZMF;EUR;1.0\n" +
            "SEK;EUR;0.1069\n" +
            "MAD;EUR;0.094\n" +
            "XAF;EUR;0.0015\n" +
            "981;EUR;1.0\n" +
            "GEL;EUR;0.4213\n" +
            "BBD;EUR;0.4722\n" +
            "CAD;EUR;0.7484\n" +
            "GHC;EUR;1.0\n" +
            "LRD;EUR;0.0112\n" +
            "NPR;EUR;0.0095\n" +
            "SHP;EUR;1.3797\n" +
            "GBP;EUR;1.3793\n" +
            "BTN;EUR;0.0151\n" +
            "PMC;EUR;1.0\n" +
            "HRK;EUR;0.1321\n" +
            "TWB;EUR;1.0\n" +
            "KYD;EUR;1.1514\n" +
            "CHE;EUR;1.0\n" +
            "100;EUR;0.0093\n" +
            "FER;EUR;1.0\n" +
            "DDD;EUR;1.0\n" +
            "MGA;EUR;3.0E-4\n" +
            "GGP;EUR;1.0\n" +
            "TWD;EUR;0.0302\n" +
            "COP;EUR;4.0E-4\n" +
            "XBD;EUR;1.0\n" +
            "RMB;EUR;0.1519\n" +
            "QAR;EUR;0.2596\n" +
            "ERN;EUR;0.0618\n" +
            "NNN;EUR;1.0\n" +
            "AED;EUR;0.2571\n" +
            "XPT;EUR;1104.8\n" +
            "GMD;EUR;0.022\n" +
            "XBB;EUR;1.0\n" +
            "IRR;EUR;3.0E-5\n" +
            "XCD;EUR;0.3497\n" +
            "FJD;EUR;0.4597\n" +
            "GYD;EUR;0.0046\n" +
            "EEK;EUR;0.0639\n" +
            "USN;EUR;1.0\n" +
            "TRY;EUR;0.3578\n" +
            "NIO;EUR;0.035\n" +
            "SKL;EUR;1.0\n" +
            "UUS;EUR;1.0\n" +
            "AMD;EUR;0.0020\n" +
            "JOD;EUR;1.3333\n" +
            "IDR;EUR;1.0E-4\n" +
            "KPW;EUR;0.0010\n" +
            "MRO;EUR;0.0030\n" +
            "PKR;EUR;0.0093\n" +
            "DJF;EUR;0.0053\n" +
            "COU;EUR;1.0\n" +
            "CLF;EUR;38.388\n" +
            "MZN;EUR;0.0263\n";

    String[] records = startData.split("\n");
    long expiredDate = 0;
    ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    CassandraPersistenceManager persistenceManager = applicationContext.getBean(CassandraPersistenceManager.class);
    CassandraCurrencyRatesPersister currencyRatesPersister = persistenceManager.getPersister(CassandraCurrencyRatesPersister.class);
    for (String record : records) {
        String[] recordParams = record.split(";");
        String from = recordParams[0];
        String to = recordParams[1];
        double rate = Double.parseDouble(recordParams[2]);
        CurrencyRate currencyRate = new CurrencyRate(from, to, rate, expiredDate);
        currencyRatesPersister.createOrUpdate(currencyRate);
    }
    CurrencyUpdateProcessor currencyUpdateProcessor = applicationContext.getBean(CurrencyUpdateProcessor.class);
    currencyUpdateProcessor.updateRates();
%>