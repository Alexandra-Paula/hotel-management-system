package domain;

import java.util.HashSet;
import java.util.Set;

public class HotelManager {
    private static HotelManager instance;
    private Set<String> loyaltyMembers;

    private HotelManager() {
        loyaltyMembers = new HashSet<>();
    }

    public static HotelManager getInstance() {
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
}
