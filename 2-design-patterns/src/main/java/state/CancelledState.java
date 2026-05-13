package state;

import domain.Reservation;

public class CancelledState implements ReservationState {
    @Override
    public void next(Reservation reservation) {
        System.out.println("[ERROR] Cannot proceed. Reservation is CANCELLED.");
    }

    @Override
    public void cancel(Reservation reservation) {
        System.out.println("[ERROR] Reservation is already CANCELLED.");
    }

    @Override
    public String getStatus() {
        return "CANCELLED";
    }
}