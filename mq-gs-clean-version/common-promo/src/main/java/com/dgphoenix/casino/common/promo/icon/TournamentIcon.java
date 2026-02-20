package com.dgphoenix.casino.common.promo.icon;

public class TournamentIcon {
    private long id;
    private String name;
    private String httpAddress;

    public TournamentIcon(long id, String name, String httpAddress) {
        this.id = id;
        this.name = name;
        this.httpAddress = httpAddress;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHttpAddress() {
        return httpAddress;
    }

    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournamentIcon icon = (TournamentIcon) o;
        return id == icon.id &&
                name.equals(icon.name) &&
                httpAddress.equals(icon.httpAddress);
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "TournamentIcon[" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", httpAddress='" + httpAddress + '\'' +
                ']';
    }
}
