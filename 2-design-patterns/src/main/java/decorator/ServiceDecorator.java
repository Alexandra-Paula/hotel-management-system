package decorator;

public abstract class ServiceDecorator implements RoomComponent {
    protected RoomComponent room;

    public ServiceDecorator(RoomComponent room) {
        this.room = room;
    }

    @Override
    public String getDescription() { return room.getDescription(); }

    @Override
    public double getPrice() { return room.getPrice(); }
}