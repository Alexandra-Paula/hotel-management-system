package state;

import domain.Reservation;

public class PendingState implements ReservationState {
    @Override
    public void next(Reservation reservation) {
        reservation.setState(new ConfirmedState());
        System.out.println("Reservation is CONFIRMED.");
    }

    @Override
    public void cancel(Reservation reservation) {
        System.out.println("PENDING state was CANCELLED.");
    }

    @Override
    public String getStatus() { return "PENDING"; }
}