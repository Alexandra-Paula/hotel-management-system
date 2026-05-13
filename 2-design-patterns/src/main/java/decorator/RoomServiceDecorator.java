package decorator;

public class RoomServiceDecorator extends ServiceDecorator {

    public RoomServiceDecorator(RoomComponent room) {
        super(room);
    }

    @Override
    public String getDescription() {
        return room.getDescription() + " + Room Service";
    }

    @Override
    public double getPrice() {
        return room.getPrice() + 30;
    }
}