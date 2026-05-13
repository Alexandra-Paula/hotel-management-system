package state;

import domain.Reservation;

public interface ReservationState {
    void next(Reservation reservation);
    void cancel(Reservation reservation);
    String getStatus();
}