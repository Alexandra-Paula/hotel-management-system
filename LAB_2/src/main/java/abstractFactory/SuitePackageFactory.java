package abstractFactory;

import models.*;
import java.util.ArrayList;

public class SuitePackageFactory implements ReservationPackageFactory {
    public Room createRoom() { return new SuiteRoom(); }

    public ExtraService[] createExtraServices() {
        ArrayList<ExtraService> services = new ArrayList<>();
        services.add(new SpaAccess());
        services.add(new RoomService());
        services.add(new AirportTransfer());
        return services.toArray(new ExtraService[0]);
    }
}
