package com.betsoft.casino.bots.model;

public class EnemySize {
    private long typeId;
    private String name;
    private double width;
    private double height;
    private double scale;
    private double minWidth;
    private double minHeight;
    private double maxWidth;
    private double maxHeight;

    public EnemySize(long typeId, String name, double width, double height, double scale, double minWidth, double minHeight, double maxWidth, double maxHeight) {
        this.typeId = typeId;
        this.name = name;

        if(scale <= 0) {
            scale = 1;
        }

        if(scale == 1) {
            this.width = width;
            this.height = height;
            this.scale = scale;
            this.minWidth = minWidth;
            this.minHeight = minHeight;
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        } else { //bring scale to 1
            this.width = width * scale;
            this.height = height * scale;
            this.scale = 1;
            this.minWidth = minWidth * scale;
            this.minHeight = minHeight * scale;
            this.maxWidth = maxWidth * scale ;
            this.maxHeight = maxHeight * scale;
        }
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(double minWidth) {
        this.minWidth = minWidth;
    }

    public double getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(double minHeight) {
        this.minHeight = minHeight;
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(double maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    public String toString() {
        return "EnemySize{" +
                "typeId=" + typeId +
                ", name='" + name + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", scale=" + scale +
                ", minWidth=" + minWidth +
                ", minHeight=" + minHeight +
                ", maxWidth=" + maxWidth +
                ", maxHeight=" + maxHeight +
                '}';
    }
}
