package com.dgphoenix.casino.gs.managers.game.favorite;

import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.google.common.base.Joiner;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.dgphoenix.casino.common.util.DigitFormatter.doubleToMoney;
import static junit.framework.Assert.assertEquals;

public class JspClassTest {

    @Test
    public void testFormatCoins() throws Exception {
        List<Coin> coinList = Arrays.asList(Coin.getByValue(15), Coin.getByValue(20), Coin.getByValue(50), Coin.getByValue(100),
                Coin.getByValue(250), Coin.getByValue(500), Coin.getByValue(1000));
        String coins = formatCoins(coinList);
        String coinsOld = formatCoinsOld(coinList);
        assertEquals(coins, coinsOld);
    }

    @Test
    public void testNull() throws Exception {
        String olds = formatCoinsOld(null);
        String news = formatCoins(null);
        assertEquals(olds, news);
    }

    @Test
    public void testEmpty() throws Exception {
        String old = formatCoinsOld(Collections.<Coin>emptyList());
        String nevv = formatCoins(Collections.<Coin>emptyList());
        assertEquals(old, nevv);
    }

    public static String formatCoins(List<Coin> coins) {
        if (coins == null) return "";
        return Joiner.on(", ")
                .join(coins.stream()
                        .map(input -> doubleToMoney((double) input.getValue() / 100))
                        .collect(Collectors.toList()));
    }

    public static String formatCoinsOld(List<Coin> coins) {
        StringBuilder sb = new StringBuilder();
        if (coins != null) {
            Iterator<Coin> it = coins.iterator();
            while (it.hasNext()) {
                Coin coin = it.next();
                String x = doubleToMoney(((double) coin.getValue()) / 100);
                sb.append(x);
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }

        }
        return sb.toString();
    }
}