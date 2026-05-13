package state;

import domain.Reservation;

public class ConfirmedState implements ReservationState {
    @Override
    public void next(Reservation reservation) {
        reservation.setState(new CheckedInState());
        System.out.println("The client has CHECKED IN.");
    }

    @Override
    public void cancel(Reservation reservation) {
        System.out.println("The CONFIRMED reservation has been CANCELED (penalties apply).");
        reservation.setState(new CancelledState());
    }

    @Override
    public String getStatus() {
        return "CONFIRMED";
    }
}

