package decorator;

public class AirportTransferDecorator extends ServiceDecorator {

    public AirportTransferDecorator(RoomComponent room) {
        super(room);
    }

    @Override
    public String getDescription() {
        return room.getDescription() + " + Airport Transfer";
    }

    @Override
    public double getPrice() {
        return room.getPrice() + 25;
    }
}