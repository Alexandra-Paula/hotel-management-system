package repository;

import database.DatabaseConnection;
import domain.Reservation;

import java.sql.*;
import java.util.*;

public class PostgresReservationRepository implements ReservationRepository {

    @Override
    public void save(Reservation reservation) {
        String sql = """
        INSERT INTO reservations 
            (guest_name, phone_number, room_type, nights, loyalty, payment_type, status, check_in_date, check_out_date)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, reservation.getGuestName());
            ps.setString(2, reservation.getPhoneNumber());
            ps.setString(3, reservation.getRoom().getDescription());
            ps.setInt(4, reservation.getNights());
            ps.setBoolean(5, reservation.isLoyalty());
            ps.setString(6, reservation.getPaymentType().toString());
            ps.setString(7, reservation.getStatusName());
            if (reservation.getCheckInDate() != null)
                ps.setDate(8, java.sql.Date.valueOf(reservation.getCheckInDate()));
            else
                ps.setNull(8, java.sql.Types.DATE);
            if (reservation.getCheckOutDate() != null)
                ps.setDate(9, java.sql.Date.valueOf(reservation.getCheckOutDate()));
            else
                ps.setNull(9, java.sql.Types.DATE);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB ERROR] " + e.getMessage());
        }
    }

    @Override
    public List<String> findAll() {
        List<String> results = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM reservations ORDER BY created_at DESC");
            while (rs.next()) {
                results.add(rs.getInt("id") + " | " +
                        rs.getString("guest_name") + " | " +
                        rs.getString("room_type") + " | " +
                        rs.getInt("nights") + " nopti | " +
                        rs.getString("status"));
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] " + e.getMessage());
        }
        return results;
    }
}