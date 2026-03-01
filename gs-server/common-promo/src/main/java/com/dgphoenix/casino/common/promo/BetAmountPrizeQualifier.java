package com.abs.casino.common.promo;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.DesiredPrize;
import com.dgphoenix.casino.common.promo.IPromoTemplate;
import com.dgphoenix.casino.common.promo.IPrizeQualifier;
import com.dgphoenix.casino.common.promo.PromoCampaignMember;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Preconditions;

import javax.validation.constraints.Min;

/**
 * Created by vladislav on 10/20/17.
 */
public class BetAmountPrizeQualifier implements IPrizeQualifier {
    private static final byte VERSION = 1;

    private long betAmount;
    private transient boolean multiplePrizesAtOnce;

    public BetAmountPrizeQualifier(@Min(1) long betAmount) {
        Preconditions.checkArgument(betAmount > 0, "Bet amount must be greater than 0.");
        this.betAmount = betAmount;
    }

    @Override
    public boolean qualifyPrize(IPromoTemplate template, PromoCampaignMember member, DesiredPrize prize,
                                ICurrencyRateManager currencyRateManager,
                                String baseCurrency, String playerCurrency) throws CommonException {
        double betSumToQualify = currencyRateManager.convert(betAmount, baseCurrency, playerCurrency);
        return prize.getQualifiedBetSum() >= betSumToQualify;
    }

    /**
     * We have to round the current bet amount after decreasing it by the bet amount to qualify.
     * It might cause either loss of players' bet amounts or appearing of extra bet amounts.
     */
    @Override
    public void resetCurrentProgress(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager,
                                     String baseCurrency, String playerCurrency) throws CommonException {
        if (multiplePrizesAtOnce) {
            desiredPrize.resetCurrentProgress(0, 0, 0);
        } else {
            double betAmountInPlayerCurrency = currencyRateManager.convert(betAmount, baseCurrency, playerCurrency);
            long qualifiedBetSum = desiredPrize.getQualifiedBetSum();
            long remainBetSum = Math.round(qualifiedBetSum - betAmountInPlayerCurrency);
            desiredPrize.resetCurrentProgress(0, remainBetSum, 0);
        }
    }

    @Override
    public int getQualifiedPrizesAtOnce(PromoCampaignMember member, DesiredPrize prize,
                                        ICurrencyRateManager currencyRateManager, String baseCurrency,
                                        String playerCurrency) throws CommonException {
        if (multiplePrizesAtOnce) {
            double betSumToQualify = currencyRateManager.convert(betAmount, baseCurrency, playerCurrency);
            return (int) (prize.getQualifiedBetSum() / betSumToQualify);
        }
        return 1;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.write(VERSION);
        output.writeLong(betAmount, true);
        output.writeBoolean(multiplePrizesAtOnce);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        betAmount = input.readLong(true);
        if (version > 0) {
            multiplePrizesAtOnce = input.readBoolean();
        }
    }

    public long getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(long betAmount) {
        this.betAmount = betAmount;
    }

    public boolean isMultiplePrizesAtOnce() {
        return multiplePrizesAtOnce;
    }

    public void setMultiplePrizesAtOnce(boolean multiplePrizesAtOnce) {
        this.multiplePrizesAtOnce = multiplePrizesAtOnce;
    }

    @Override
    public String toString() {
        return "BetAmountPrizeQualifier[" +
                "betAmount=" + betAmount +
                ", multiplePrizesAtOnce=" + multiplePrizesAtOnce +
                ']';
    }

/*    public static void main(String[] args) {
        XStream xStream = new XStream(new TransientFieldsAllowedProvider());
        //XStream xStream = new XStream();
        System.out.println(xStream.getReflectionProvider());
        xStream.registerConverter(new TransientFieldsReflectionProvider(xStream.getMapper(), xStream.getReflectionProvider()),
                XStream.PRIORITY_VERY_LOW);

        XStream.setupDefaultSecurity(xStream);
        xStream.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.**"});
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(BetAmountPrizeQualifier.class);

        BetAmountPrizeQualifier qualifier = new BetAmountPrizeQualifier(100);
        qualifier.setMultiplePrizesAtOnce(true);

        String xml = xStream.toXML(qualifier);
        System.out.println("xml=" +xml);
        System.out.println("obj=" +xStream.fromXML(xml));
    }*/
}
