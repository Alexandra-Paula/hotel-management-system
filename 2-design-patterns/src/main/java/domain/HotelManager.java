package domain;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import observer.ReservationObserver;
import database.DatabaseConnection;

public class HotelManager {
    private static HotelManager instance;
    private Set<String> loyaltyMembers;
    private List<Reservation> reservations;

    private List<ReservationObserver> observers = new ArrayList<>();

    private HotelManager() {
        loyaltyMembers = new HashSet<>();
        reservations = new ArrayList<>();
    }

    public static synchronized HotelManager getInstance() {
        if (instance == null) {
            instance = new HotelManager();
        }
        return instance;
    }

    public void subscribe(ReservationObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(ReservationObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Reservation reservation) {
        for (ReservationObserver observer : observers) {
            observer.update(reservation);
        }
    }

    public boolean isLoyaltyMember(String phoneNumber) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM loyalty_members WHERE phone_number = ?"
            );
            ps.setString(1, phoneNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.err.println("[DB ERROR] " + e.getMessage());
            return false;
        }
    }

    public void addLoyaltyMember(String phoneNumber) {
        loyaltyMembers.add(phoneNumber);
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO loyalty_members (phone_number) VALUES (?) ON CONFLICT DO NOTHING"
            );
            ps.setString(1, phoneNumber);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[DB ERROR] " + e.getMessage());
        }
    }

    public double applyLoyaltyDiscount(double price) {
        return price * 0.85;
    }

    public double getTaxRate() {
        return 0.09;
    }

    public void displayLoyaltyInfo() {
        System.out.println("★ Loyalty Program: 15% discount on rooms, priority check-in !!!★");
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        notifyObservers(reservation);
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}