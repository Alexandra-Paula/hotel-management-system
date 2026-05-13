package decorator;

public class SpaDecorator extends ServiceDecorator {

    public SpaDecorator(RoomComponent room) {
        super(room);
    }

    @Override
    public String getDescription() {
        return room.getDescription() + " + Spa Access";
    }

    @Override
    public double getPrice() {
        return room.getPrice() + 40;
    }
}