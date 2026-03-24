package abstractFactory;
import models.*;
import java.util.ArrayList;

public class DeluxePackageFactory implements ReservationPackageFactory {
    public Room createRoom() {
        return new DeluxeRoom();
    }

    public ExtraService[] createExtraServices() {
        ArrayList<ExtraService> services = new ArrayList<>();
        services.add(new SpaAccess().clone());
        services.add(new AirportTransfer().clone());
        return services.toArray(new ExtraService[0]);
    }
}