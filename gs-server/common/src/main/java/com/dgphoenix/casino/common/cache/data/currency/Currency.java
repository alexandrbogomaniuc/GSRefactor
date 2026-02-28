package com.dgphoenix.casino.common.cache.data.currency;

import com.abs.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: flsh
 * Date: Jun 28, 2010
 */
public class Currency implements IDistributedConfigEntry, KryoSerializable, ICurrency {
    public static final int CURRENCY_LENGTH = 3;
    public static final CurrencySerializer SERIALIZER = new CurrencySerializer();
    public static final CurrencyListSerializer LIST_SERIALIZER = new CurrencyListSerializer();

    private static final Logger LOG = LogManager.getLogger(Currency.class);
    private static final int VERSION = 1;
    /**
     * ISO 4217 currency code for this currency.
     */
    private String code;
    private String symbol;
    private String basicCurrency;
    private String minimalFraction;

    public Currency() {
    }

    public Currency(String code, String symbol) {
        this.code = code;
        this.symbol = symbol;
        validate();
    }

    private void validate() {
        if (code == null || code.trim().length() != 3) {
            throw new IllegalArgumentException("Currency code");
        }
        if (StringUtils.isTrimmedEmpty(symbol)) {
            throw new IllegalArgumentException("Currency symbol");
        }
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @XmlTransient
    public String getBasicCurrency() {
        return basicCurrency;
    }

    public Currency setBasicCurrency(String basicCurrency) {
        this.basicCurrency = basicCurrency;
        return this;
    }

    @XmlTransient
    public String getMinimalFraction() {
        return minimalFraction;
    }

    public Currency setMinimalFraction(String minimalFraction) {
        this.minimalFraction = minimalFraction;
        return this;
    }

    @Override
    public boolean isDefault(long bankId) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            throw new RuntimeException("BankInfo not found, id=" + bankId);
        }
        return this.equals(bankInfo.getDefaultCurrency());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Currency currency = (Currency) o;

        if (code == null || !code.equals(currency.code)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public void copy(IDistributedConfigEntry entry) {
        Currency from = (Currency) entry;
        this.code = from.code;
        this.symbol = from.symbol;
        this.basicCurrency = from.basicCurrency;
        this.minimalFraction = from.minimalFraction;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Currency");
        sb.append("[code='").append(code).append('\'');
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append(", basic='").append(basicCurrency).append('\'');
        sb.append(", minimal='").append(minimalFraction).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION);
        output.writeString(code);
        output.writeString(symbol);
        output.writeString(basicCurrency);
        output.writeString(minimalFraction);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt();
        code = input.readString();
        symbol = input.readString();
        if (ver > 0) {
            basicCurrency = input.readString();
            minimalFraction = input.readString();
        }
        LOG.warn("Deserialize through default serializer: {}", code);
    }

    static class CurrencySerializer extends Serializer<Currency> {

        @Override
        public void write(Kryo kryo, Output output, Currency object) {
            output.writeInt(VERSION);
            output.writeString(object.getCode());
            output.writeString(object.getSymbol());
            output.writeString(object.getBasicCurrency());
            output.writeString(object.getMinimalFraction());
        }

        @Override
        public Currency read(Kryo kryo, Input input, Class<Currency> type) {
            int ver = input.readInt();
            String code = input.readString();
            String symbol = input.readString();
            String basicCurrency = null;
            String minimalFraction = null;
            if (ver > 0) {
                basicCurrency = input.readString();
                minimalFraction = input.readString();
            }
            Currency currency;
            try {
                currency = CurrencyCache.getInstance().get(code);
            } catch (NullPointerException e) {
                LOG.warn("Currency cache isn't inited");
                currency = null;
            }
            if (currency == null) {
                currency = new Currency();
                currency.code = code;
                currency.symbol = symbol;
                currency.basicCurrency = basicCurrency;
                currency.minimalFraction = minimalFraction;
                LOG.error("Deserialize unregistered currency:: {}", currency);
                return currency;
            }
            return currency;
        }
    }

    static class CurrencyListSerializer extends CollectionSerializer {
        @Override
        public Collection read(Kryo kryo, Input input, Class<Collection> type) {
            Collection collection = create(kryo, input, type);
            kryo.reference(collection);
            int length = input.readVarInt(true);
            if (collection instanceof ArrayList) {
                ((ArrayList<?>) collection).ensureCapacity(length);
            }
            for (int i = 0; i < length; i++) {
                Registration clazz = kryo.readClass(input);
                if (clazz != null) {
                    collection.add(kryo.readObject(input, clazz.getType(), SERIALIZER));
                } else {
                    LOG.warn("CurrencyListSerializer::read null class");
                }
            }
            return collection;
        }

        @Override
        public void write(Kryo kryo, Output output, Collection collection) {
            List<?> serialized;
            if (collection == null || collection instanceof ArrayList) {
                serialized = (List<?>) collection;
            } else {
                serialized = new ArrayList(collection);
            }
            super.write(kryo, output, serialized);
        }
    }

    public void marshal(HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.setValue(code);
    }

}
