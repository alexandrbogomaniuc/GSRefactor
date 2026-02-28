package com.abs.casino.tracker;

import com.abs.casino.common.currency.CurrencyRate;
import com.abs.casino.common.exception.TransportException;
import com.abs.casino.common.util.web.HttpClientConnection;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExternalSourceCurrencyRateExtractor implements CurrencyRateExtractor {
    private static final Logger LOG = LogManager.getLogger(ExternalSourceCurrencyRateExtractor.class);

    @Override
    public void prepare(Collection<CurrencyRate> currencyRates) {
    }

    @Override
    public double getRate(String sourceCurrencyCode, String targetCurrencyCode) {
        double rate = -1;
        try {
            switch (sourceCurrencyCode) {
                case "PLS":
                    rate = getRateFromCoinGecko("play-token");
                    break;
                case "TRX":
                    rate = getRateFromCoinMarketCap("tron");
                    break;
                case "ETH":
                    rate = getRateFromCoinMarketCap("ethereum");
                    break;
                case "BCH":
                    rate = getRateFromCoinMarketCap("bitcoin-cash");
                    break;
                case "XRP":
                    rate = getRateFromCoinMarketCap("ripple");
                    break;
                case "VES":
                    rate = getBlackMarketCurrencyRate(sourceCurrencyCode);
                    break;
                case "LTC":
                    rate = getLTCToEurCurrencyRate();
                    break;
                case "XDG":
                    rate = getRateFromCoinMarketCap("dogecoin");
                    break;
                case "ZEC":
                    rate = getRateFromCoinMarketCap("zcash");
                    break;
                case "VND":
                    rate = getReverseRate("VND");
                    break;
                case "IRR":
                    rate = getReverseRate("IRR");
                    break;
                default:
                    if (sourceCurrencyCode.equals(targetCurrencyCode)) {
                        rate = 1;
                    } else {
                        rate = getRateFromGoogleFinance(sourceCurrencyCode, targetCurrencyCode);
                        if (rate < 0) {
                            rate = getRateFrom150currency(sourceCurrencyCode, targetCurrencyCode);
                        }
                        if (rate < 0) {
                            LOG.warn("Unable retrieve rate for pair: source={}, target={}", sourceCurrencyCode, targetCurrencyCode);
                        }
                    }
            }
        } catch (Exception ex) {
            LOG.error("Can't get currency from " + sourceCurrencyCode + " to " + targetCurrencyCode, ex);
        }
        return rate;
    }

    private double getRateFromGoogleFinance(String from, String to) {
        String url = "http://finance.google.com/bctzjpnsun/converter";
        Map<String, String> params = new HashMap<>();
        params.put("a", "1");
        params.put("from", from);
        params.put("to", to);
        HttpClientConnection gsConnection = HttpClientConnection.newInstance();

        String response;
        try {
            response = gsConnection.doRequest(url, params, false, false).trim();
        } catch (TransportException e) {
            LOG.warn("Google currency rate extraction failed", e);
            return -1;
        }
        double rate = -1;

        String check = "<span class=bld>";
        if (response.contains(check)) {
            int beginIndex = response.indexOf(check) + check.length();
            try {
                String subString = response.substring(beginIndex, beginIndex + 6);
                rate = Double.parseDouble(subString);
            } catch (NumberFormatException e) {
                //  LOG.error("", e);
            }
            LOG.debug("Google's rate from {} to {} is: {}", from, to, rate);
        } else {
            LOG.debug("Google's rate from {} to {} not found!!!", from, to);
        }
        return rate;
    }

    private double getReverseRate(String sourceCurrency) {
        double rate = getRateFromGoogleFinance("EUR", sourceCurrency);
        if (rate <= 0) {
            return -1;
        } else {
            LOG.debug("CurrencyRate from {} to {} is: {}", "EUR", sourceCurrency, rate);
            rate = 1 / rate;
            rate = new BigDecimal(rate).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            LOG.debug("CurrencyRate from {} to {} after scale is: {}", sourceCurrency, "EUR", rate);
            return rate;
        }
    }

    private double getRateFrom150currency(String from, String to) {
        double rate = -1;
        try {
            URL url = new URL("http://www.150currency.com/convert-" + from + "-" + to + ".htm");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            boolean flag = false;
            int offset = 0;
            while ((inputLine = reader.readLine()) != null) {
                //search value is 4 lines under "<td>Amount: </td>"
                if (!flag && inputLine.contains("<td>Amount: </td>")) {
                    flag = true;
                }
                if (flag) {
                    offset++;
                }
                if (offset == 5) {
                    String sRate = StringUtils.substringBetween(inputLine, "<td>", "</td>").trim();
                    try {
                        //trim comma - hundreds delimiter
                        rate = Double.parseDouble(sRate.replace(",", ""));
                    } catch (NumberFormatException e) {
                        LOG.debug("Result parse error", e);
                    }
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            LOG.error("", e);
        }
        if (rate >= 0) {
            LOG.debug("150currency.com rate from {} to {} is: {}", from, to, rate);
        } else {
            LOG.debug("150currency.com rate from {} to {} not found!!!", from, to);
        }
        return rate;
    }

    private double getLTCToEurCurrencyRate() {
        double rate = -1;
        try {
            URL url = new URL("http://www.cryptocoincharts.info/pair/ltc/eur/therocktrading/10-days");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                if (inputLine.contains("Last Price: ")) {
                    try {
                        String sRate = StringUtils.substringBetween(inputLine, "Last Price:", "EUR</p>").trim();
                        rate = Double.parseDouble(sRate);
                    } catch (NumberFormatException e) {
                        LOG.debug("Result parse error " + e.getMessage());
                    }
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            LOG.error("get XDG To EUR rate " + e.getMessage(), e);
        }
        if (rate >= 0) {
            LOG.debug("www.cryptocoincharts.info rate from LTC to EUR is: {}", String.format("%f", rate));
        } else {
            LOG.debug("www.cryptocoincharts.info rate from LTC to not found!!!");
        }
        return rate;
    }

    private double getRateFromCoinMarketCap(String sourceCurrency) {
        try {
            String page = getHttpsData(String.format("https://api.coinmarketcap.com/v1/ticker/%s/?convert=EUR", sourceCurrency));
            JSONArray jsonArray = new JSONArray(page);
            Double rateToEur = Double.parseDouble(((JSONObject) jsonArray.get(0)).get("price_eur").toString());
            return new BigDecimal(rateToEur).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            LOG.debug("Failed to retrieve {} currency rate by external API", sourceCurrency, e);
        }
        return -1;
    }

    private double getRateFromCoinGecko(String sourceCurrency) {
        try {
            String page = getHttpsData(String.format("https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=eur", sourceCurrency));
            JSONObject json = new JSONObject(page);
            Double rateToEur = Double.parseDouble(((JSONObject) json.get(sourceCurrency)).get("eur").toString());
            return new BigDecimal(rateToEur).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            LOG.debug("Failed to retrieve {} currency rate by external API", sourceCurrency, e);
        }
        return -1;
    }

    private double getBlackMarketCurrencyRate(String fromCurrencyCode) {
        try {
            String page = getHttpsData("https://localbitcoins.com//bitcoinaverage/ticker-all-currencies/");
            JSONObject jsonPage = new JSONObject(page);

            Double eurRate = Double.parseDouble((jsonPage.getJSONObject("EUR")).get("avg_24h").toString());
            Double fromRate = Double.parseDouble((jsonPage.getJSONObject(fromCurrencyCode)).get("avg_24h").toString());

            return new BigDecimal(eurRate).divide(new BigDecimal(fromRate), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            LOG.debug("Failed to retrieve {} currency rate by external API", fromCurrencyCode, e);
        }
        return -1;
    }

    private String getHttpsData(String httpsURL) throws IOException {
        URL url = new URL(httpsURL);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like " +
                "Gecko) Chrome/23.0.1271.95 Safari/537.11");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
                sb.append('\n');
            }
            return sb.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
