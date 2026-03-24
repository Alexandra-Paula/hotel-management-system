package models;

public class AirportTransfer extends ExtraService {
    public AirportTransfer() {
        description = "Airport Transfer";
        price = 25;
    }

    @Override
    public ExtraService clone() {
        return new AirportTransfer();
    }
}
