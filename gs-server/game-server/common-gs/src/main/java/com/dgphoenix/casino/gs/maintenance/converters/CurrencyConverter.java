package com.abs.casino.gs.maintenance.converters;

import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 25.08.14.
 * <p>
 * OLD format:
 * <currency>
 * <code>EUR</code>
 * <symbol>€</symbol>
 * </currency>
 * NEW format: <currency>BSD</currency>
 */
public class CurrencyConverter implements Converter {
    private static final Logger LOG = LogManager.getLogger(CurrencyConverter.class);

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Currency currency = (Currency) source;
        writer.setValue(currency.getCode());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Currency currency = null;
        String code = reader.getValue();
        if (StringUtils.isTrimmedEmpty(code)) { //case for old format
            String symbol = null;
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                String propertyName = reader.getNodeName();
                if ("code".equals(propertyName)) {
                    code = reader.getValue();
                    currency = CurrencyCache.getInstance().get(code);
                    if (currency != null) {
                        reader.moveUp();
                        break;
                    }
                } else if ("symbol".equals(propertyName)) {
                    symbol = reader.getValue();
                }
                reader.moveUp();
            }
            if (currency == null && !StringUtils.isTrimmedEmpty(code)) {
                LOG.warn("unmarshal: Cannot find currency: " + code + ", symbol=" + symbol);
                currency = new Currency(code, symbol == null ? code : symbol);
            }
        } else {
            currency = CurrencyCache.getInstance().get(code);
        }
        if (currency == null) {
            LOG.warn("unmarshal: Cannot find currency: " + code);
            if (!StringUtils.isTrimmedEmpty(code)) {
                currency = new Currency(code, code);
            }
        }
        return currency;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(Currency.class);
    }
}
