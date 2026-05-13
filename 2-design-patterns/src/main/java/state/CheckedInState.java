package state;

import domain.Reservation;

public class CheckedInState implements ReservationState {
    @Override
    public void next(Reservation reservation) {
        System.out.println("The client has already checked in. Next step is check-out.");
    }

    @Override
    public void cancel(Reservation reservation) {
        System.out.println("[ERROR] A reservation can no longer be canceled after check-in!");
    }

    @Override
    public String getStatus() {
        return "CHECKED_IN";
    }
}