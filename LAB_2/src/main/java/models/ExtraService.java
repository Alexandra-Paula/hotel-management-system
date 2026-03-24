package models;

import prototype.Prototype;

public abstract class ExtraService implements Prototype<ExtraService> {

    protected String description;
    protected double price;

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public abstract ExtraService clone();
}