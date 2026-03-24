package abstractFactory;
import models.*;
import java.util.ArrayList;

public class StandardPackageFactory implements ReservationPackageFactory {
    public Room createRoom() {
        return new StandardRoom();
    }

    public ExtraService[] createExtraServices() {
        ArrayList<ExtraService> services = new ArrayList<>();
        services.add(new AirportTransfer().clone());
        return services.toArray(new ExtraService[0]);
    }
}