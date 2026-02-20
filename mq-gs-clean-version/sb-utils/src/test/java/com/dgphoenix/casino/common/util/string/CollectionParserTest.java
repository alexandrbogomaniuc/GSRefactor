package com.dgphoenix.casino.common.util.string;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CollectionParserTest {
    @Test
    public void parseIntegers() {
        CollectionParser parser = new CollectionParser("|");
        String input = "5|6|7";
        List<Integer> output = new ArrayList<Integer>();

        parser.parseInt(input, output);

        Assert.assertEquals(3, output.size());
        Assert.assertEquals(Integer.valueOf(5), output.get(0));
    }

    @Test
    public void parseDoubles() {
        CollectionParser parser = new CollectionParser("|");
        String input = "5.1|6|7.0";
        List<Double> output = new ArrayList<Double>();

        parser.parseDouble(input, output);

        Assert.assertEquals(3, output.size());
        Assert.assertEquals(5.1, output.get(0), 0.0001);
    }

    @Test
    public void parseEmptyString() {
        CollectionParser parser = new CollectionParser("|");
        String input = "";
        List<Integer> output = new ArrayList<Integer>();

        parser.parseInt(input, output);

        Assert.assertEquals(0, output.size());
    }
}
