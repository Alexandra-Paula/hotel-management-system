package client;

import domain.HotelManager;
import observer.*;
import facade.ReservationFacade;
import factory.*;
import models.Room;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        HotelManager manager = HotelManager.getInstance();

        manager.subscribe(new EmailNotificationService());
        manager.subscribe(new LoyaltyPointService());

        UserInterface ui = new UserInterface(scanner);
        ReservationFacade facade = new ReservationFacade(scanner);

        ui.displayWelcome();
        boolean loyalty = ui.handleLoyaltyProgram();
        String phone = ui.getLastPhoneNumber();

        int reservationType = ui.selectReservationType();

        if (reservationType == 1) {
            RoomFactory roomFactory = ui.selectRoomFactory();
            Room room = roomFactory.createRoom();
            int nights = ui.askNumberOfNights();
            facade.makeSimpleReservation(room, nights, loyalty);
        } else {
            facade.makeFullReservation(loyalty, phone);
        }
        scanner.close();
    }
}


