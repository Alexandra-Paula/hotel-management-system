package facade;

import abstractFactory.*;
import builder.ReservationBuilder;
import composite.*;
import domain.HotelManager;
import domain.Reservation;
import enums.PaymentType;
import models.*;
import payment.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ReservationFacade {

    private final HotelManager manager;
    private final Scanner scanner;

    public ReservationFacade(Scanner scanner) {
        this.scanner = scanner;
        this.manager = HotelManager.getInstance();
    }

    public void makeSimpleReservation(Room room, int nights, boolean loyalty) {
        System.out.println("============================================================");
        System.out.println("           Processing Simple Reservation...");
        System.out.println("============================================================");

        double total = calculateTotal(room.getPricePerNight() * nights, 0, loyalty);
        processPayment(total);
    }

    public void makeFullReservation(boolean loyalty) {
        System.out.println("============================================================");
        System.out.println("           Processing Full Reservation...");
        System.out.println("============================================================");

        // 1. Abstract Factory - selecteaza pachetul
        ReservationPackageFactory factory = selectPackage();
        Room room = factory.createRoom();
        ExtraService[] extraOptions = factory.createExtraServices();

        // 2. Composite - grupeaza serviciile
        ServicePackage servicePackage = new ServicePackage("Selected Services");
        List<ExtraService> selectedServices = new ArrayList<>();

        if (room instanceof SuiteRoom) {
            for (ExtraService s : extraOptions) {
                servicePackage.add(new SingleService(s.getDescription(), s.getPrice()));
                selectedServices.add(s);
            }
            System.out.println("All services included:");
            servicePackage.display();
        } else {
            for (ExtraService s : extraOptions) {
                System.out.print("Add " + s.getDescription() + "? (+€" + (int) s.getPrice() + ") (yes/no): ");
                String ans = scanner.nextLine().trim().toLowerCase();
                if (ans.equals("yes") || ans.equals("y")) {
                    servicePackage.add(new SingleService(s.getDescription(), s.getPrice()));
                    selectedServices.add(s);
                }
            }
            servicePackage.display();
        }

        // 3. Builder - construieste rezervarea
        System.out.print("Enter guest name: ");
        String guestName = scanner.nextLine().trim();

        Reservation reservation = new ReservationBuilder()
                .withGuestName(guestName)
                .withRoom(room.clone())
                .withNights(askNumberOfNights())
                .withServices(selectedServices)
                .withLoyalty(loyalty)
                .withPaymentType(PaymentType.CARD)
                .build();

        manager.addReservation(reservation);

        // 4. Adapter - proceseaza plata
        double total = calculateTotal(
                room.getPricePerNight() * reservation.getNights(),
                servicePackage.getPrice(),
                loyalty
        );
        processPayment(total);

        System.out.println("Reservation completed successfully!");
    }

    // metode private helper ---
    private double calculateTotal(double roomPrice, double servicesPrice, boolean loyalty) {
        double subtotal = roomPrice + servicesPrice;
        if (loyalty) subtotal *= 0.85;
        double vat = subtotal * manager.getTaxRate();
        return subtotal + vat;
    }

    private void processPayment(double total) {
        System.out.println("Amount to pay: €" + String.format("%.2f", total));
        System.out.println("Select payment method: 1. Card (Stripe)  2. Cash  3. PayPal");
        System.out.print("Your choice (1-3): ");
        int method = readInt(1, 3);

        PaymentProcessor processor = null;
        if (method == 1) {
            processor = new StripeAdapter(new StripePaymentService());
        } else if (method == 3) {
            processor = new PayPalAdapter(new PayPalPaymentService());
        }

        if (processor != null) {
            processor.processPayment(total);
        } else {
            System.out.println("Cash: Payment received.");
        }
    }

    private ReservationPackageFactory selectPackage() {
        System.out.println("Select room type:");
        System.out.println("1. Standard Room - €80/night");
        System.out.println("2. Deluxe Room - €120/night");
        System.out.println("3. Suite - €200/night");
        System.out.print("Your choice (1-3): ");
        int choice = readInt(1, 3);
        switch (choice) {
            case 1: return new StandardPackageFactory();
            case 2: return new DeluxePackageFactory();
            case 3: return new SuitePackageFactory();
            default: return new StandardPackageFactory();
        }
    }

    private int askNumberOfNights() {
        System.out.print("Enter number of nights: ");
        return readInt(1, 365);
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