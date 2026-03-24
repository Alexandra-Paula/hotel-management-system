package domain;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;


public class HotelManager {
    private static HotelManager instance;
    private Set<String> loyaltyMembers;
    private List<Reservation> reservations;

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

    public boolean isLoyaltyMember(String phoneNumber) {
        return loyaltyMembers.contains(phoneNumber);
    }

    public void addLoyaltyMember(String phoneNumber) {
        loyaltyMembers.add(phoneNumber);
    }

    public double applyLoyaltyDiscount(double price) {
        return price * 0.85; // 15% discount
    }

    public double getTaxRate() {
        return 0.09;
    }

    public void displayLoyaltyInfo() {
        System.out.println("★ Loyalty Program: 15% discount on rooms, priority check-in !!!★");
    }

    //reservation
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}
