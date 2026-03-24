package domain;

import models.Room;
import models.ExtraService;
import enums.PaymentType;

import java.util.List;

public class Reservation {
    private String guestName;
    private Room room;
    private int nights;
    private List<ExtraService> services;
    private boolean loyalty;
    private PaymentType paymentType;

    public Reservation(String guestName, Room room, int nights, List<ExtraService> services, boolean loyalty, PaymentType paymentType) {
        this.guestName = guestName;
        this.room = room;
        this.nights = nights;
        this.services = services;
        this.loyalty = loyalty;
        this.paymentType = paymentType;
    }

    public String getGuestName() { return guestName;}
    public Room getRoom() { return room; }
    public int getNights() { return nights; }
    public List<ExtraService> getExtraServices() { return services; }
    public boolean isLoyalty() { return loyalty; }
    public PaymentType getPaymentType() { return paymentType; }
}
