package abstractFactory;
import models.*;
import java.util.ArrayList;

public class StandardPackageFactory implements ReservationPackageFactory {

    @Override
    public Room createRoom() {
        return new StandardRoom();
    }

    @Override
    public ExtraService[] createExtraServices() {
        ArrayList<ExtraService> services = new ArrayList<>();
        services.add(new AirportTransfer());
        return services.toArray(new ExtraService[0]);
    }
}
