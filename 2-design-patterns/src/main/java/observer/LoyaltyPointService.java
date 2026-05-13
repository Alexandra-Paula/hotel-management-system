package observer;

import domain.Reservation;

public class LoyaltyPointService implements ReservationObserver {
    @Override
    public void update(Reservation reservation) {
        if (reservation.isLoyalty()) {
            int points = reservation.getNights() * 10;
            System.out.println("[LOYALTY] Adding " + points + " points to guest's account.");
        }
    }
}