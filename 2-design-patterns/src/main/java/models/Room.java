package models;

import java.util.ArrayList;
import java.util.List;

public abstract class Room implements Cloneable {
    protected String description;
    protected double pricePerNight;
    protected List<ExtraService> services = new ArrayList<>();

    public String getDescription() { return description; }
    public double getPricePerNight() { return pricePerNight; }
    public List<ExtraService> getServices() { return services; }

    @Override
    public Room clone() {
        try {
            Room cloned = (Room) super.clone();
            cloned.services = new ArrayList<>();
            for (ExtraService s : this.services) {
                cloned.services.add(s.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
