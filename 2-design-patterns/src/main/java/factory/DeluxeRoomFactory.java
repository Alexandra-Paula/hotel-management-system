package factory;

import models.DeluxeRoom;
import models.Room;

public class DeluxeRoomFactory implements RoomFactory {
    public Room createRoom() {
        return new DeluxeRoom();
    }
}
