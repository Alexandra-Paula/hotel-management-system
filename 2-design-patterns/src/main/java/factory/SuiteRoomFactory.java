package factory;

import models.Room;
import models.SuiteRoom;

public class SuiteRoomFactory implements RoomFactory {
    public Room createRoom() {
        return new SuiteRoom();
    }
}
