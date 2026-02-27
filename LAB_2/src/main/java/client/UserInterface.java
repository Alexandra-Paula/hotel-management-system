package client;

import domain.HotelManager;
import abstractFactory.*;
import factory.*;
import models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserInterface {

    private final Scanner scanner;
    private final HotelManager manager;

    public UserInterface(Scanner scanner) {
        this.scanner = scanner;
        this.manager = HotelManager.getInstance();
    }

    // === WELCOME AND LOYALTY PROGRAM ===
    public void displayWelcome() {
        System.out.println("============================================================");
        System.out.println("        Welcome to Aurora Smart Hotel Management System ");
        System.out.println("============================================================");
        System.out.println("VAT Rate: 9% * Loyalty Program Available!");
        System.out.println("Loyalty Program Benefits:");
        System.out.println(" - 15% discount on room reservations");
        System.out.println(" - Priority check-in");
        System.out.println();
    }

    public boolean handleLoyaltyProgram() {
        System.out.print("Are you a loyalty member? (yes/no): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (answer.equals("yes")) {
            System.out.print("Enter your phone number: ");
            String phone = scanner.nextLine().trim();
            if (manager.isLoyaltyMember(phone)) {
                System.out.println("✔ Membership verified! + Loyalty benefits activated!\n");
                return true;
            } else {
                System.out.println("✗ Not found. Continuing without discount.\n");
                return false;
            }
        } else {
            System.out.print("Would you like to join our loyalty program? (yes/no): ");
            String join = scanner.nextLine().trim().toLowerCase();
            if (join.equals("yes")) {
                System.out.print("Enter your phone number to register: ");
                String phone = scanner.nextLine().trim();
                manager.addLoyaltyMember(phone);
                System.out.println("✔ Registration successful! + Loyalty benefits activated!\n");
                return true;
            } else {
                System.out.println("No problem! You can join anytime.\n");
            }
        }
        return false;
    }

    public int selectReservationType() {
        System.out.println("============================================================");
        System.out.println("Select reservation type:");
        System.out.println("1. Simple Room Reservation");
        System.out.println("2. Complete Package Reservation");
        System.out.print("Your choice (1-2): ");
        return readInt(1, 2);
    }

    //factory Method - select room for simple reservation
    public RoomFactory selectRoomFactory() {
        System.out.println("Select room type:");
        System.out.println("1. Standard Room - €80 / night");
        System.out.println("2. Deluxe Room - €120 / night");
        System.out.println("3. Suite - €200 / night");
        System.out.print("Your choice (1-3): ");

        int choice = readInt(1, 3);
        switch(choice) {
            case 1: return new StandardRoomFactory();
            case 2: return new DeluxeRoomFactory();
            case 3: return new SuiteRoomFactory();
            default: return new StandardRoomFactory();
        }
    }

    //abstract Factory - select package for full reservation
    public ReservationPackageFactory selectPackage() {
        System.out.println("============================================================");
        System.out.println("                   NEW RESERVATION PROCESS ");
        System.out.println("============================================================");
        System.out.println("Select room type:");
        System.out.println("1. Standard Room - €80 / night");
        System.out.println("2. Deluxe Room - €120 / night");
        System.out.println("3. Suite - €200 / night");
        System.out.print("Your choice (1-3): ");

        int choice = readInt(1, 3);
        switch (choice) {
            case 1: return new StandardPackageFactory();
            case 2: return new DeluxePackageFactory();
            case 3: return new SuitePackageFactory();
            default: return new StandardPackageFactory();
        }
    }

    public int askNumberOfNights() {
        System.out.print("Enter number of nights: ");
        return readInt(1, 365);
    }

    public List<ExtraService> askExtraServices(ExtraService[] options, Room room) {
        List<ExtraService> selected = new ArrayList<>();

        if (room instanceof SuiteRoom) {
            System.out.println("All services included for Suite:");
            for (ExtraService s : options) {
                selected.add(s);
                System.out.println("✓ " + s.getDescription());
            }
            return selected;
        }

        System.out.println("------------------------------------------------------------");
        System.out.println("Would you like to add extra services?");
        System.out.println("------------------------------------------------------------");
        for (ExtraService s : options) {
            System.out.print("Add " + s.getDescription() + "? (+€" + (int)s.getPrice() + ") (yes/no): ");
            String ans = scanner.nextLine().trim().toLowerCase();
            if (ans.equals("yes") || ans.equals("y")) {
                selected.add(s);
                System.out.println("✓ " + s.getDescription() + " added");
            }
        }
        System.out.println("------------------------------------------------------------");
        System.out.println("✓ Services added successfully");
        return selected;
    }

    //display for simple reservation (factory method)
    public void displaySimpleSummary(Room room, int nights, boolean loyalty) {
        System.out.println("============================================================");
        System.out.println("                     RESERVATION SUMMARY ");
        System.out.println("============================================================");

        String reservationID = "RES-" + (int)(Math.random() * 1000000);
        System.out.println("Reservation ID: " + reservationID);
        System.out.println("============================================================");

        double basePrice = room.getPricePerNight() * nights;
        System.out.println("ROOM:");
        System.out.println("• " + room.getDescription() + " (" + nights + " nights)");
        System.out.println("Base price: €" + String.format("%.2f", basePrice));

        double discount = 0;
        if(loyalty) {
            discount = basePrice * 0.15;
            System.out.println("Loyalty Discount (15%): -€" + String.format("%.2f", discount));
        }

        double discountedTotal = basePrice - discount;
        double vat = discountedTotal * manager.getTaxRate();
        double total = discountedTotal + vat;

        System.out.println("------------------------------------------------------------");
        System.out.println("SUBTOTAL: €" + String.format("%.2f", discountedTotal));
        System.out.println("VAT (9%): €" + String.format("%.2f", vat));
        System.out.println("------------------------------------------------------------");
        System.out.println("TOTAL TO PAY: €" + String.format("%.2f", total));
        System.out.println("============================================================");

        System.out.println("PAYMENT");
        System.out.println("Amount to pay: €" + String.format("%.2f", total));
        System.out.println("Select payment method: 1. Card  2. Cash  3. Online Banking");
        System.out.print("Your choice (1-3): ");
        int method = readInt(1,3);
        System.out.println("============================================================");
        System.out.println("PROCESSING PAYMENT...");
        System.out.println("Payment authorized... ✔ Payment successful!");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("============================================================");
        System.out.println("PAYMENT RECEIPT");
        System.out.println("Payment Method: " + (method==1?"Card":method==2?"Cash":"Online Banking"));
        System.out.println("Date: " + dtf.format(LocalDateTime.now()));
        System.out.println("--------------------------------------------");
        System.out.println("Amount Paid: €" + String.format("%.2f", total));
        System.out.println("Status: PAID");
        System.out.println("--------------------------------------------");
        System.out.println("Thank you for choosing Aurora Hotel!");
        System.out.println("============================================================");
    }

    //display for package reservation (abstract factory)
    public void displaySummary(Room room, int nights, List<ExtraService> services, boolean loyalty) {
        System.out.println("============================================================");
        System.out.println("                     RESERVATION SUMMARY ");
        System.out.println("============================================================");

        String reservationID = "RES-" + (int)(Math.random() * 1000000);
        System.out.println("Reservation ID: " + reservationID);
        System.out.println("============================================================");

        double basePrice = room.getPricePerNight() * nights;
        System.out.println("ROOM:");
        System.out.println("• " + room.getDescription() + " (" + nights + " nights)");
        System.out.println("Base price: €" + String.format("%.2f", basePrice));

        double servicesTotal = 0;
        if (!services.isEmpty()) {
            System.out.println("EXTRA SERVICES:");
            for (ExtraService s : services) {
                System.out.println("• " + s.getDescription());
                servicesTotal += s.getPrice();
            }
        }

        double subtotal = basePrice + servicesTotal;
        double discount = 0;
        if (loyalty) {
            discount = subtotal * 0.15;
            System.out.println("Loyalty Discount (15%): -€" + String.format("%.2f", discount));
        }

        double discountedTotal = subtotal - discount;
        double vat = discountedTotal * manager.getTaxRate();
        double total = discountedTotal + vat;

        System.out.println("------------------------------------------------------------");
        System.out.println("SUBTOTAL: €" + String.format("%.2f", discountedTotal));
        System.out.println("VAT (9%): €" + String.format("%.2f", vat));
        System.out.println("------------------------------------------------------------");
        System.out.println("TOTAL TO PAY: €" + String.format("%.2f", total));
        System.out.println("============================================================");

        // Simulate validation
        System.out.println("============================================================");
        System.out.println("                 RESERVATION VALIDATION ");
        System.out.println("============================================================");
        System.out.println("[Room Availability Check] Validating... ✓ Room available");
        System.out.println("[Date Validation Check] Validating... ✓ Dates are valid");
        System.out.println("[Minimum Stay Check] Validating... ✓ Minimum stay satisfied");
        System.out.println("All validations PASSED!");
        System.out.println("============================================================");

        System.out.println("PAYMENT");
        System.out.println("Amount to pay: €" + String.format("%.2f", total));
        System.out.println("Select payment method: 1. Card  2. Cash  3. Online Banking");
        System.out.print("Your choice (1-3): ");
        int method = readInt(1,3);
        System.out.println("============================================================");
        System.out.println("PROCESSING PAYMENT...");
        System.out.println("Payment authorized... ✔ Payment successful!");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("============================================================");
        System.out.println("PAYMENT RECEIPT");
        System.out.println("Payment Method: " + (method==1?"Card":method==2?"Cash":"Online Banking"));
        System.out.println("Date: " + dtf.format(LocalDateTime.now()));
        System.out.println("--------------------------------------------");
        System.out.println("Amount Paid: €" + String.format("%.2f", total));
        System.out.println("Status: PAID");
        System.out.println("--------------------------------------------");
        System.out.println("Thank you for choosing Aurora Hotel!");
        System.out.println("============================================================");

    }

    private int readInt(int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(scanner.nextLine().trim());
                if (n < min || n > max) throw new NumberFormatException();
                return n;
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Enter number (" + min + "-" + max + "): ");
            }
        }
    }
}