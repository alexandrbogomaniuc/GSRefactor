package com.abs.casino.statistics.http;

public class StatisticsKey {
    public final String date;
    public final String url;

    public StatisticsKey(String date, String url) {
        this.date = date;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatisticsKey)) return false;
        StatisticsKey statKey = (StatisticsKey) o;
        return date.equals(statKey.date) && url.equals(statKey.url);

    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "StatisticsKey{" +
                "date='" + date + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
