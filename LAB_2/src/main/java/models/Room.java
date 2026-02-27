package models;

public abstract class Room {
    protected String description;
    protected double pricePerNight;

    public String getDescription() { return description; }
    public double getPricePerNight() { return pricePerNight; }
}
