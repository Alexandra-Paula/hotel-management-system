package abstractFactory;
import models.Room;
import models.ExtraService;

public interface ReservationPackageFactory {
    Room createRoom();
    ExtraService[] createExtraServices();
}
