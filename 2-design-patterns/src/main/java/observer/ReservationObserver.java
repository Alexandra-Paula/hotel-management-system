package observer;

import domain.Reservation;

public interface ReservationObserver {
    void update(Reservation reservation);
}