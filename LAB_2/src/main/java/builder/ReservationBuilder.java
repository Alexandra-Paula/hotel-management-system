package builder;

import domain.Reservation;
import models.Room;
import models.ExtraService;
import enums.PaymentType;

import java.util.ArrayList;
import java.util.List;

public class ReservationBuilder implements Builder {

    private String guestName;
    private Room room;
    private int nights;
    private List<ExtraService> services = new ArrayList<>();
    private boolean loyalty;
    private PaymentType paymentType;

    public ReservationBuilder withGuestName(String guestName) {
        this.guestName = guestName;
        return this;
    }

    public ReservationBuilder withRoom(Room room) {
        this.room = room;
        return this;
    }

    public ReservationBuilder withNights(int nights) {
        this.nights = nights;
        return this;
    }

    public ReservationBuilder addService(ExtraService service) {
        services.add(service);
        return this;
    }

    public ReservationBuilder withServices(List<ExtraService> services) {
        this.services = services;
        return this;
    }

    public ReservationBuilder withLoyalty(boolean loyalty) {
        this.loyalty = loyalty;
        return this;
    }

    public ReservationBuilder withPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
        return this;
    }

    @Override
    public Reservation build() {
        return new Reservation(guestName, room, nights, services, loyalty, paymentType);
    }
}