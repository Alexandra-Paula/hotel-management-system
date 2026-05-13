package domain;

import models.Room;
import models.ExtraService;
import enums.PaymentType;
import state.ReservationState;
import state.PendingState;
import java.time.LocalDate;
import java.util.List;

public class Reservation {
    private String guestName;
    private String phoneNumber;
    private Room room;
    private int nights;
    private List<ExtraService> services;
    private boolean loyalty;
    private PaymentType paymentType;
    private ReservationState currentState;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public Reservation(String guestName, String phoneNumber, Room room, int nights,
                       List<ExtraService> services, boolean loyalty, PaymentType paymentType) {
        this.guestName = guestName;
        this.phoneNumber = phoneNumber;
        this.room = room;
        this.nights = nights;
        this.services = services;
        this.loyalty = loyalty;
        this.paymentType = paymentType;
        this.currentState = new PendingState();
    }

    public void setState(ReservationState state) { this.currentState = state; }
    public void nextStep() { currentState.next(this); }
    public void cancelReservation() { currentState.cancel(this); }
    public String getStatusName() { return currentState.getStatus(); }
    public String getGuestName() { return guestName; }
    public String getPhoneNumber() { return phoneNumber; }
    public Room getRoom() { return room; }
    public int getNights() { return nights; }
    public List<ExtraService> getExtraServices() { return services; }
    public boolean isLoyalty() { return loyalty; }
    public PaymentType getPaymentType() { return paymentType; }

    // Date check-in / check-out
    public LocalDate getCheckInDate()  { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckInDate(LocalDate checkInDate)   { this.checkInDate = checkInDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
}