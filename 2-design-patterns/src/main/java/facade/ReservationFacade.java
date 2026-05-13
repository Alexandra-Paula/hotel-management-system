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

import strategy.*;
import command.*;

import decorator.*;
import models.SpaAccess;
import models.AirportTransfer;
import models.RoomService;

public class ReservationFacade {

    private final HotelManager manager;
    private final Scanner scanner;

    private final ReservationInvoker invoker = new ReservationInvoker();

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

    public void makeFullReservation(boolean loyalty, String phoneNumber) {
        System.out.println("============================================================");
        System.out.println("           Processing Full Reservation...");
        System.out.println("============================================================");

        ReservationPackageFactory factory = selectPackage();
        Room room = factory.createRoom();
        ExtraService[] extraOptions = factory.createExtraServices();

        RoomComponent decoratedRoom = new BasicRoom(
                room.getDescription(),
                room.getPricePerNight()
        );

        List<ExtraService> selectedServices = new ArrayList<>();

        if (room instanceof SuiteRoom) {
            decoratedRoom = new SpaDecorator(decoratedRoom);
            decoratedRoom = new RoomServiceDecorator(decoratedRoom);
            decoratedRoom = new AirportTransferDecorator(decoratedRoom);

            for (ExtraService s : extraOptions) {
                selectedServices.add(s);
            }
            System.out.println("All services included:");
        } else {
            for (ExtraService s : extraOptions) {
                System.out.print("Add " + s.getDescription() +
                        "? (+€" + (int) s.getPrice() + ") (yes/no): ");
                String ans = scanner.nextLine().trim().toLowerCase();
                if (ans.equals("yes") || ans.equals("y")) {
                    selectedServices.add(s);

                    if (s instanceof SpaAccess) {
                        decoratedRoom = new SpaDecorator(decoratedRoom);
                    } else if (s instanceof AirportTransfer) {
                        decoratedRoom = new AirportTransferDecorator(decoratedRoom);
                    } else if (s instanceof RoomService) {
                        decoratedRoom = new RoomServiceDecorator(decoratedRoom);
                    }
                }
            }
        }

        System.out.println("------------------------------------------------------------");
        System.out.println("Room with services: " + decoratedRoom.getDescription());
        System.out.println("Total room price/night: €" + decoratedRoom.getPrice());
        System.out.println("------------------------------------------------------------");

        System.out.print("Enter guest name: ");
        String guestName = scanner.nextLine().trim();

        Reservation reservation = new ReservationBuilder()
                .withGuestName(guestName)
                .withPhoneNumber(phoneNumber)
                .withRoom(room.clone())
                .withNights(askNumberOfNights())
                .withServices(selectedServices)
                .withLoyalty(loyalty)
                .withPaymentType(PaymentType.CARD)
                .build();

        System.out.println("Initial Status: " + reservation.getStatusName());

        double total = calculateTotal(
                decoratedRoom.getPrice() * reservation.getNights(),
                0,
                loyalty
        );
        processPayment(total);

        command.Command placeOrder = new command.PlaceReservationCommand(manager, reservation);
        invoker.executeCommand(placeOrder);

        System.out.println("Final Status: " + reservation.getStatusName());
        System.out.println("Reservation completed successfully!");


    System.out.print("Do you want to UNDO this reservation? (yes/no): ");
    if(scanner.nextLine().trim().equalsIgnoreCase("yes")) {
        invoker.undoLastCommand();
    }

    }

    private double calculateTotal(double roomPrice, double servicesPrice, boolean loyalty) {
        double subtotal = roomPrice + servicesPrice;
        if (loyalty) subtotal *= 0.85;
        double vat = subtotal * manager.getTaxRate();
        return subtotal + vat;
    }

    private void processPayment(double total) {
        System.out.println("Amount to pay: €" + String.format("%.2f", total));
        System.out.println("Select payment method: 1. Card   2. Cash  3. PayPal");
        System.out.print("Your choice (1-3): ");
        int method = readInt(1, 3);

        PaymentStrategy strategy;
        switch (method) {
            case 1: strategy = new StripePaymentStrategy(); break;
            case 3: strategy = new PayPalPaymentStrategy(); break;
            default: strategy = new CashPaymentStrategy(); break;
        }

        System.out.println("============================================================");
        System.out.println("PROCESSING PAYMENT...");
        strategy.pay(total); 

        System.out.println("============================================================");
        System.out.println("PAYMENT RECEIPT");
        System.out.println("Payment Method: " + strategy.getMethodName());
        System.out.println("Amount Paid: €" + String.format("%.2f", total));
        System.out.println("Status: PAID ✔");
        System.out.println("============================================================");
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