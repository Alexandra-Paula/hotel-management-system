package client;

import facade.ReservationFacade;
import factory.*;
import models.Room;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserInterface ui = new UserInterface(scanner);
        ReservationFacade facade = new ReservationFacade(scanner);

        ui.displayWelcome();
        boolean loyalty = ui.handleLoyaltyProgram();

        int reservationType = ui.selectReservationType();

        if (reservationType == 1) {
            // simple reservation - prin UI ca inainte
            RoomFactory roomFactory = ui.selectRoomFactory();
            Room room = roomFactory.createRoom();
            int nights = ui.askNumberOfNights();
            facade.makeSimpleReservation(room, nights, loyalty);
        } else {
            // full reservation - prin Facade
            facade.makeFullReservation(loyalty);
        }
        scanner.close();
    }
}