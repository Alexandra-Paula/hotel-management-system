package factory;
import models.StandardRoom;
import models.Room;

public class StandardRoomFactory implements RoomFactory {
    public Room createRoom() {
        return new StandardRoom();
    }
}
