package client;

import abstractFactory.ReservationPackageFactory;
import factory.*;
import models.ExtraService;
import models.Room;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserInterface ui = new UserInterface(scanner);

        ui.displayWelcome();
        boolean loyalty = ui.handleLoyaltyProgram();

        int reservationType = ui.selectReservationType();

        if(reservationType == 1) {
            //simple reservation (room only) - factory method
            RoomFactory roomFactory = ui.selectRoomFactory();
            Room room = roomFactory.createRoom();
            int nights = ui.askNumberOfNights();
            ui.displaySimpleSummary(room, nights, loyalty);
        } else {
            //booking a complete package - abstract Factory
            ReservationPackageFactory factory = ui.selectPackage();
            Room room = factory.createRoom();
            ExtraService[] extraOptions = factory.createExtraServices();
            int nights = ui.askNumberOfNights();
            List<ExtraService> selectedServices = ui.askExtraServices(extraOptions, room);
            ui.displaySummary(room, nights, selectedServices, loyalty);
        }

        scanner.close();
    }
}