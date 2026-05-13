package command;

import domain.HotelManager;
import domain.Reservation;
import repository.PostgresReservationRepository;
import repository.ReservationRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PlaceReservationCommand implements Command {
    private final HotelManager manager;
    private final Reservation reservation;
    private final PostgresReservationRepository repository;

    public PlaceReservationCommand(HotelManager manager, Reservation reservation) {
        this.manager = manager;
        this.reservation = reservation;
        this.repository = new PostgresReservationRepository();
    }

    @Override
    public void execute() {
        manager.addReservation(reservation);
        reservation.nextStep();
        repository.save(reservation);
    }

    @Override
    public void undo() {
        reservation.cancelReservation();
        try {
            Connection conn = database.DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE reservations SET status = ? WHERE id = (SELECT id FROM reservations WHERE guest_name = ? ORDER BY created_at DESC LIMIT 1)"
            );
            ps.setString(1, reservation.getStatusName());
            ps.setString(2, reservation.getGuestName());
            ps.executeUpdate();
            System.out.println("[DB] ✔ Reservation status updated to: " + reservation.getStatusName());
        } catch (Exception e) {
            System.err.println("[DB ERROR] " + e.getMessage());
        }

        System.out.println("[COMMAND] Undo executed: The reservation for "
                + reservation.getGuestName() + " has been removed.");
    }

}