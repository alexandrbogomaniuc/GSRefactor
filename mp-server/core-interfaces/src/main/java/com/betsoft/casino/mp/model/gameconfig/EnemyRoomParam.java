package com.betsoft.casino.mp.model.gameconfig;

public class EnemyRoomParam {
    String type;
    int id;
    int quantity;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyRoomParam{");
        sb.append("type='").append(type).append('\'');
        sb.append(", id=").append(id);
        sb.append(", quantity=").append(quantity);
        sb.append('}');
        return sb.toString();
    }
}
