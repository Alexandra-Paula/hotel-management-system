package builder;

import domain.Reservation;
import models.Room;
import models.ExtraService;
import enums.PaymentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationBuilder implements Builder {

    private String guestName;
    private Room room;
    private int nights;
    private List<ExtraService> services = new ArrayList<>();
    private boolean loyalty;
    private PaymentType paymentType;
    private String phoneNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

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

    public ReservationBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public ReservationBuilder withCheckIn(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
        return this;
    }

    public ReservationBuilder withCheckOut(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
        return this;
    }

    @Override
    public Reservation build() {
        Reservation r = new Reservation(guestName, phoneNumber, room, nights, services, loyalty, paymentType);
        r.setCheckInDate(checkInDate);
        r.setCheckOutDate(checkOutDate);
        return r;
    }
}