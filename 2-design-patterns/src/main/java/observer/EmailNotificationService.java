package observer;

import domain.Reservation;

public class EmailNotificationService implements ReservationObserver {
    @Override
    public void update(Reservation reservation) {
        System.out.println("Sending confirmation to " + reservation.getGuestName() + "...");
        System.out.println("Reservation for " + reservation.getRoom().getDescription() + " confirmed!");
    }
}
