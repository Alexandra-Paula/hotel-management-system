package client;

import factory.*;
import models.Room;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserInterface ui = new UserInterface(scanner);

        ui.displayWelcome();
        boolean loyalty = ui.handleLoyaltyProgram();

        int reservationType = ui.selectReservationType();

        if(reservationType == 1) {
            // simple reservation
            RoomFactory roomFactory = ui.selectRoomFactory();
            Room room = roomFactory.createRoom();
            int nights = ui.askNumberOfNights();
            ui.displaySimpleSummary(room, nights, loyalty);
        } else {
            // full reservation
            ui.handleFullReservation(loyalty);
        }

        scanner.close();
    }
}