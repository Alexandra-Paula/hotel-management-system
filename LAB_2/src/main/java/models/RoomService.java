package models;

public class RoomService extends ExtraService {
    public RoomService() {
        description = "Room Service Package";
        price = 30;
    }

    @Override
    public ExtraService clone() {
        return new RoomService();
    }
}
