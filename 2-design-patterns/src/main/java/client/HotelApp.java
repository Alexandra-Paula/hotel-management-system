package client;

import domain.HotelManager;
import domain.Reservation;
import models.*;
import enums.PaymentType;
import observer.EmailNotificationService;
import observer.LoyaltyPointService;
import builder.ReservationBuilder;
 
import abstractFactory.ReservationPackageFactory;
import abstractFactory.StandardPackageFactory;
import abstractFactory.DeluxePackageFactory;
import abstractFactory.SuitePackageFactory;

import factory.RoomFactory;
import factory.StandardRoomFactory;
import factory.DeluxeRoomFactory;
import factory.SuiteRoomFactory;

import strategy.PaymentStrategy;
import strategy.StripePaymentStrategy;
import strategy.CashPaymentStrategy;
import strategy.PayPalPaymentStrategy;

import command.Command;
import command.PlaceReservationCommand;
import command.ReservationInvoker;

import composite.ServiceComponent;
import composite.SingleService;
import composite.ServicePackage;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class HotelApp extends Application {

    // State 
    private boolean isLoyalty = false;
    private String phoneNumber = "";
    private String guestName = "";
    private String guestEmail = "";
    private Room selectedRoom = null;
    private int nights = 1;
    private LocalDate checkInDate = null;
    private LocalDate checkOutDate = null;
    private int adults = 1;
    private int children = 0;
    private static final int MAX_NIGHTS = 30;
    private List<ExtraService> selectedServices = new ArrayList<>();
    private PaymentType paymentType = PaymentType.CARD;
    private int reservationType = 1; // 1=Simple, 2=Package

    private StackPane root;
    private Label totalLabel;
    private boolean isDarkMode = true;
    private Runnable currentStepRefresh = null;

    // Command pattern
    private final ReservationInvoker invoker = new ReservationInvoker();

    // Dark Mode Colors
    private static final String DARK_BG     = "#0B1F3A";
    private static final String DARK_CARD   = "#122040";
    private static final String DARK_FIELD  = "#1A3057";
    private static final String DARK_BORDER = "#2A4A77";
    private static final String DARK_TEXT   = "#F5F0E8";

    // Light Mode Colors
    private static final String LIGHT_BG     = "#DCE8F5";
    private static final String LIGHT_CARD   = "#EEF4FB";
    private static final String LIGHT_FIELD  = "#FFFFFF";
    private static final String LIGHT_BORDER = "#A8C4E0";
    private static final String LIGHT_TEXT   = "#0B1F3A";

    // Shared Colors
    private static final String GOLD  = "#C9A84C";
    private static final String WHITE = "#FFFFFF";
    private static final String GRAY  = "#8A8A8A";
    private static final String NAVY  = "#0B1F3A";

    private String BG()     { return isDarkMode ? DARK_BG     : LIGHT_BG;     }
    private String CARD()   { return isDarkMode ? DARK_CARD   : LIGHT_CARD;   }
    private String FIELD()  { return isDarkMode ? DARK_FIELD  : LIGHT_FIELD;  }
    private String BORDER() { return isDarkMode ? DARK_BORDER : LIGHT_BORDER; }
    private String TEXT()   { return isDarkMode ? DARK_TEXT   : LIGHT_TEXT;   }
  
    private String GOLD_FG() { return isDarkMode ? "#C9A84C" : "#9B7E2E"; }
    private String MUTED()   { return isDarkMode ? "#8A8A8A" : "#5A6B82"; }

    @Override
    public void start(Stage stage) {
        HotelManager manager = HotelManager.getInstance();
        manager.subscribe(new EmailNotificationService());
        manager.subscribe(new LoyaltyPointService());

        root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        // Theme toggle button
        Button themeBtn = new Button("Day Mode");
        themeBtn.setFont(Font.font("Georgia", 12));
        themeBtn.setTextFill(Color.web(GOLD));
        themeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + GOLD + ";" +
            "-fx-border-radius: 6;" +
            "-fx-padding: 6 14;" +
            "-fx-cursor: hand;"
        );
        themeBtn.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            themeBtn.setText(isDarkMode ? "Day Mode" : "Night Mode");
            if (root.getStyleClass().contains("landing-root")) {
                root.setStyle("-fx-background-color: transparent;");
            } else {
                root.setStyle("-fx-background-color: " + BG() + ";");
            }
            // Toggle CSS class 
            if (isDarkMode) {
                root.getStyleClass().remove("light");
            } else {
                if (!root.getStyleClass().contains("light")) {
                    root.getStyleClass().add("light");
                }
            }
            if (currentStepRefresh != null) currentStepRefresh.run();
        });

        StackPane.setAlignment(themeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(themeBtn, new Insets(16));

        currentStepRefresh = () -> showLandingPage();
        showLandingPage();

        root.getChildren().add(themeBtn);

        Scene scene = new Scene(root, 900, 750);

        // global stylesheet forDatePicker, Spinner, ScrollBar
        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("[WARN] styles.css not found in resources/!");
        }

        stage.setTitle("Aurora Smart Hotel Management System");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.setOnCloseRequest(ev -> {
            if (hasUnsavedReservation()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit Aurora Hotel");
                alert.setHeaderText("You have an unfinished reservation.");
                alert.setContentText("Are you sure you want to exit? Any entered data will be lost.");
                ButtonType yes = new ButtonType("Yes, exit");
                ButtonType no  = new ButtonType("No, continue", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(yes, no);
                alert.showAndWait().ifPresent(response -> {
                    if (response != yes) ev.consume();
                });
            }
        });

        stage.show();
    }


    private boolean hasUnsavedReservation() {
        return !guestName.isEmpty()
            || !guestEmail.isEmpty()
            || !phoneNumber.isEmpty()
            || selectedRoom != null
            || !selectedServices.isEmpty()
            || checkInDate != null
            || checkOutDate != null;
    }

    // STEP 1 Welcome + Loyalty
    private void showStep1() {
        currentStepRefresh = () -> showStep1();
        root.getStyleClass().remove("landing-root");
        root.setStyle("-fx-background-color: " + BG() + ";");
        VBox card = makeCard();

        Label title = makeTitle("Aurora Hotel");
        Label subtitle = makeSubtitle("RESERVATION SYSTEM");
        Region sep = makeSeparator();

        Label q = makeQuestion("Are you a loyalty member?");
        Label hint = makeHint("Loyalty members receive 15% discount & priority check-in");

        ToggleGroup tg = new ToggleGroup();
        RadioButton yes = makeRadio("Yes, I'm a member", tg);
        RadioButton no  = makeRadio("No, continue without discount", tg);
        no.setSelected(true);

        TextField phoneField = makeTextField("Enter your phone number");
        phoneField.setVisible(false);
        phoneField.setManaged(false);
        phoneField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.matches("\\d*")) {
                phoneField.setText(newV.replaceAll("[^\\d]", ""));
            }
        });

        Label error = makeError("");

        yes.setOnAction(e -> {
            phoneField.setVisible(true);
            phoneField.setManaged(true);
            error.setText("");
        });
        no.setOnAction(e  -> {
            phoneField.setVisible(false);
            phoneField.setManaged(false);
            error.setText("");
        });

        Button next = makeButton("Continue");

        next.setOnAction(e -> {
            if (yes.isSelected()) {
                String ph = phoneField.getText().trim();

                if (ph.isEmpty()) {
                    error.setText("Please enter your phone number.");
                    return;
                }

                if (!ph.matches("\\d{7,15}")) {
                    error.setText("Invalid phone number. Please enter 7-15 digits.");
                    return;
                }

                HotelManager manager = HotelManager.getInstance();
                if (!manager.isLoyaltyMember(ph)) {
                    error.setText("Phone number not found in our loyalty program. Please check and try again, or select 'No' to continue.");
                    return;
                }

                isLoyalty = true;
                phoneNumber = ph;
            } else {
                isLoyalty = false;
                phoneNumber = "";
            }
            error.setText("");
            fadeTransition(() -> showStep2());
        });

        card.getChildren().addAll(title, subtitle, sep, q, hint,
                new HBox(20, yes, no), phoneField, error, next);
        setCard(card);
    }

    // STEP 2 Reservation Type
    private void showLandingPage() {
        currentStepRefresh = () -> showLandingPage();
        if (!root.getStyleClass().contains("landing-root")) {
            root.getStyleClass().add("landing-root");
        }
        root.setStyle("-fx-background-color: transparent;");

        Label title = new Label("Experience the Northern Lights Comfort in Rovaniemi, Finland");
        title.getStyleClass().add("landing-title");
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("Book your unforgettable stay at Aurora Hotel with our easy-to-use reservation system.");
        subtitle.getStyleClass().add("landing-subtitle");
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);

        Button reserve = new Button("Make a Reservation");
        reserve.getStyleClass().add("landing-button");
        reserve.setOnAction(e -> fadeTransition(() -> showStep1()));

        VBox overlay = new VBox(20, title, subtitle, reserve);
        overlay.setAlignment(Pos.CENTER);
        overlay.getStyleClass().add("landing-overlay");
        overlay.setMaxWidth(760);

        setLandingScene(overlay);
    }

    private void setLandingScene(VBox content) {
        javafx.scene.Node themeBtn = root.getChildren().stream()
            .filter(n -> n instanceof Button)
            .findFirst()
            .orElse(null);
        root.getChildren().clear();

        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        VBox wrapper = new VBox(topSpacer, content, bottomSpacer);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(30, 20, 30, 20));
        wrapper.setStyle("-fx-background-color: transparent;");
        wrapper.minHeightProperty().bind(root.heightProperty());

        ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-control-inner-background: transparent;"
        );
        scrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            javafx.scene.Node viewport = scrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: transparent;");
            }
        });
        javafx.application.Platform.runLater(() -> {
            javafx.scene.Node viewport = scrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: transparent;");
            }
        });

        root.getChildren().add(scrollPane);
        if (themeBtn != null) root.getChildren().add(themeBtn);
    }

    private void showStep2() {
        currentStepRefresh = () -> showStep2();
        VBox card = makeCard();
        card.getChildren().addAll(
            makeStepIndicator("Step 1 of 4"),
            makeTitle("Reservation Type"),
            makeSeparator()
        );

        ToggleGroup tg = new ToggleGroup();
        VBox opt1 = makeOptionBox("Simple Room Reservation",
                "Choose your room, number of nights & payment method.", tg, true);
        VBox opt2 = makeOptionBox("Complete Package Reservation",
                "Room + curated extras (Spa, Airport Transfer, etc.)", tg, false);

        Button next = makeButton("Continue");
        next.setOnAction(e -> {
            reservationType = ((RadioButton) tg.getSelectedToggle()).getText().startsWith("Simple") ? 1 : 2;
            fadeTransition(() -> showStep3());
        });

        card.getChildren().addAll(opt1, opt2, next);
        setCard(card);
    }

    // STEP 3 Room + Services + Guest Info
    private void showStep3() {
        currentStepRefresh = () -> showStep3();
        VBox card = makeCard();
        card.getChildren().addAll(
            makeStepIndicator("Step 2 of 4"),
            makeTitle("Room & Details"),
            makeSeparator()
        );

        // Room selection
        Label roomLabel = makeQuestion("Select room type:");
        ToggleGroup rg = new ToggleGroup();
        RadioButton r1 = makeRadio("Standard Room - €80/night", rg);
        RadioButton r2 = makeRadio("Deluxe Room - €120/night", rg);
        RadioButton r3 = makeRadio("Suite - €200/night", rg);
        r1.setSelected(true);
        boolean packageReservation = reservationType == 2;

        // Services
        Label svcLabel = makeQuestion("Extra services:");
        Label svcHint  = makeHint("Default services are pre-selected based on the room type (provided by the package factory). If you chose a simple reservation, no extra services apply.");
        CheckBox cbSpa      = makeCheckBox("Spa Access (+€40)");
        CheckBox cbAirport  = makeCheckBox("Airport Transfer (+€25)");
        CheckBox cbRoomSvc  = makeCheckBox("Room Service Package (+€30)");

        if (!packageReservation) {
            svcLabel.setVisible(false);
            svcLabel.setManaged(false);
            svcHint.setVisible(false);
            svcHint.setManaged(false);
            cbSpa.setVisible(false);
            cbSpa.setManaged(false);
            cbAirport.setVisible(false);
            cbAirport.setManaged(false);
            cbRoomSvc.setVisible(false);
            cbRoomSvc.setManaged(false);
        }

        Runnable applyDefaultsForRoom = () -> {
            if (!packageReservation) {
                cbSpa.setSelected(false);
                cbAirport.setSelected(false);
                cbRoomSvc.setSelected(false);
                cbSpa.setDisable(true);
                cbAirport.setDisable(true);
                cbRoomSvc.setDisable(true);
                return;
            }

            ReservationPackageFactory factory;
            if (r3.isSelected())      factory = new SuitePackageFactory();
            else if (r2.isSelected()) factory = new DeluxePackageFactory();
            else                      factory = new StandardPackageFactory();

            cbSpa.setSelected(false);
            cbAirport.setSelected(false);
            cbRoomSvc.setSelected(false);
            cbSpa.setDisable(false);
            cbAirport.setDisable(false);
            cbRoomSvc.setDisable(false);

            for (ExtraService s : factory.createExtraServices()) {
                if (s instanceof SpaAccess)        cbSpa.setSelected(true);
                else if (s instanceof AirportTransfer) cbAirport.setSelected(true);
                else if (s instanceof RoomService) cbRoomSvc.setSelected(true);
            }

            if (r3.isSelected()) {
                cbSpa.setDisable(true);
                cbAirport.setDisable(true);
                cbRoomSvc.setDisable(true);
            }
        };

        Label guestLabel = makeQuestion("Guest details:");
        Label guestHint  = makeHint("Please enter your full name (first + last name) and a valid email address.");

        TextField nameField   = makeTextField("Full name (e.g. John Smith)");
        TextField emailField  = makeTextField("Email address");

        Label datesLabel = makeQuestion("Check-in / Check-out:");
        Label datesHint  = makeHint("Check-in must be today or later. Maximum stay is " + MAX_NIGHTS + " nights.");

        DatePicker checkInPicker  = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        checkInPicker.setPromptText("Check-in date");
        checkOutPicker.setPromptText("Check-out date");
        styleDatePicker(checkInPicker);
        styleDatePicker(checkOutPicker);

        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null) setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        checkOutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate ci = checkInPicker.getValue();
                if (date != null && ci != null) setDisable(empty || !date.isAfter(ci));
            }
        });

        Label nightsLabel = new Label();
        nightsLabel.setFont(Font.font("Georgia", FontPosture.ITALIC, 12));
        nightsLabel.setStyle("-fx-text-fill: " + GOLD_FG() + ";");

        Label guestsLabel = makeQuestion("Number of guests:");
        Label guestsHint  = makeHint("Capacity depends on room type - Standard: 2, Deluxe: 3, Suite: 4 guests.");

        Spinner<Integer> adultsSpinner   = new Spinner<>(1, 10, 1);
        Spinner<Integer> childrenSpinner = new Spinner<>(0, 10, 0);
        adultsSpinner.setPrefWidth(100);
        childrenSpinner.setPrefWidth(100);

        Label adultsCap   = new Label("Adults:");
        Label childrenCap = new Label("Children:");
        adultsCap.setFont(Font.font("Georgia", 13));
        childrenCap.setFont(Font.font("Georgia", 13));
        adultsCap.setStyle("-fx-text-fill: " + TEXT() + ";");
        childrenCap.setStyle("-fx-text-fill: " + TEXT() + ";");

        totalLabel = new Label("Total: €0.00");
        totalLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        totalLabel.setStyle("-fx-text-fill: " + GOLD_FG() + ";");

        java.util.function.IntSupplier computeNights = () -> {
            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            if (ci == null || co == null || !co.isAfter(ci)) return 0;
            return (int) ChronoUnit.DAYS.between(ci, co);
        };

        Runnable updatePrice = () -> {
            double base = r1.isSelected() ? 80 : r2.isSelected() ? 120 : 200;
            double extras = packageReservation
                          ? (cbSpa.isSelected() ? 40 : 0)
                          + (cbAirport.isSelected() ? 25 : 0)
                          + (cbRoomSvc.isSelected() ? 30 : 0)
                          : 0;
            int n = computeNights.getAsInt();
            if (n < 1) {
                nightsLabel.setText("Please select valid dates.");
                totalLabel.setText("Total: —");
                return;
            }
            if (n > MAX_NIGHTS) {
                nightsLabel.setText("Stay too long (" + n + " nights). Max " + MAX_NIGHTS + ".");
                totalLabel.setText("Total: —");
                return;
            }
            nightsLabel.setText(n + " night" + (n == 1 ? "" : "s"));
            double subtotal = (base + extras) * n;
            if (isLoyalty) subtotal *= 0.85;
            double total = subtotal * 1.09;
            totalLabel.setText(String.format("Total: €%.2f  (VAT 9%s%s)",
                total, "%", isLoyalty ? " · Loyalty -15%" : ""));
        };

        r1.setOnAction(e -> { applyDefaultsForRoom.run(); updatePrice.run(); });
        r2.setOnAction(e -> { applyDefaultsForRoom.run(); updatePrice.run(); });
        r3.setOnAction(e -> { applyDefaultsForRoom.run(); updatePrice.run(); });
        cbSpa.setOnAction(e -> updatePrice.run());
        cbAirport.setOnAction(e -> updatePrice.run());
        cbRoomSvc.setOnAction(e -> updatePrice.run());
        checkInPicker.valueProperty().addListener((obs, o, n) -> {
            if (n != null && checkOutPicker.getValue() != null && !checkOutPicker.getValue().isAfter(n)) {
                checkOutPicker.setValue(n.plusDays(1));
            }
            updatePrice.run();
        });
        checkOutPicker.valueProperty().addListener((obs, o, n) -> updatePrice.run());

        applyDefaultsForRoom.run();
        updatePrice.run();

        Label error = makeError("");
        Button next = makeButton("Continue");
        next.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty()) {
                error.setText("Please enter guest name.");
                return;
            }

            String[] parts = name.split("\\s+");
            if (parts.length < 2) {
                error.setText("Please enter your FULL name - both first name and last name (e.g. 'John Smith').");
                return;
            }
            boolean allValid = true;
            for (String p : parts) {
                if (p.length() < 2 || !p.matches("[\\p{L}'-]+")) {
                    allValid = false;
                    break;
                }
            }
            if (!allValid) {
                error.setText("Name parts must contain only letters and be at least 2 characters long.");
                return;
            }

            if (email.isEmpty()) {
                error.setText("Please enter your email address.");
                return;
            }
            if (!email.matches("^[A-Za-z0-9._%+-]+@(?:gmail|yahoo|outlook|hotmail|live|icloud|aol|protonmail|zoho|yandex|mail)\\.com$") ||
                email.contains("@.") || email.startsWith(".") ||
                email.contains("..")) {
                    error.setText("Please enter a valid .com email address using a known provider (e.g. name@gmail.com).");
                    return;
            }

            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            if (ci == null || co == null) {
                error.setText("Please select both check-in and check-out dates.");
                return;
            }
            if (ci.isBefore(LocalDate.now())) {
                error.setText("Check-in date cannot be in the past.");
                return;
            }
            if (!co.isAfter(ci)) {
                error.setText("Check-out must be after check-in.");
                return;
            }
            int nVal = (int) ChronoUnit.DAYS.between(ci, co);
            if (nVal > MAX_NIGHTS) {
                error.setText("Maximum stay is " + MAX_NIGHTS + " nights. For longer stays, please contact reception.");
                return;
            }

            int adultsVal   = adultsSpinner.getValue();
            int childrenVal = childrenSpinner.getValue();
            int totalGuests = adultsVal + childrenVal;
            if (adultsVal < 1) {
                error.setText("At least one adult is required.");
                return;
            }
            int roomCapacity = r3.isSelected() ? 4 : r2.isSelected() ? 3 : 2;
            if (totalGuests > roomCapacity) {
                String roomName = r3.isSelected() ? "Suite" : r2.isSelected() ? "Deluxe" : "Standard";
                error.setText(roomName + " room accommodates max " + roomCapacity
                        + " guests. You selected " + totalGuests
                        + ". Please choose a larger room or fewer guests.");
                return;
            }

            guestName = name;
            guestEmail = email;
            nights = nVal;
            checkInDate = ci;
            checkOutDate = co;
            adults = adultsVal;
            children = childrenVal;

            RoomFactory roomFactory;
            if (r3.isSelected())      roomFactory = new SuiteRoomFactory();
            else if (r2.isSelected()) roomFactory = new DeluxeRoomFactory();
            else                      roomFactory = new StandardRoomFactory();
            selectedRoom = roomFactory.createRoom();

            selectedServices.clear();
            if (packageReservation) {
                if (cbSpa.isSelected())     selectedServices.add(new SpaAccess());
                if (cbAirport.isSelected()) selectedServices.add(new AirportTransfer());
                if (cbRoomSvc.isSelected()) selectedServices.add(new RoomService());
            }

            fadeTransition(() -> showStep4());
        });

        HBox svcRow = new HBox(20, cbSpa, cbAirport, cbRoomSvc);

        HBox datesRow = new HBox(20,
            new VBox(4, new Label("Check-in"), checkInPicker),
            new VBox(4, new Label("Check-out"), checkOutPicker),
            new VBox(4, new Label(" "), nightsLabel)
        );
        for (javafx.scene.Node n : datesRow.getChildren()) {
            if (n instanceof VBox) {
                Label l = (Label) ((VBox) n).getChildren().get(0);
                l.setFont(Font.font("Georgia", FontPosture.ITALIC, 11));
                l.setStyle("-fx-text-fill: " + MUTED() + ";");
            }
        }

        HBox guestsRow = new HBox(20,
            new VBox(4, adultsCap, adultsSpinner),
            new VBox(4, childrenCap, childrenSpinner)
        );

        card.getChildren().addAll(
            roomLabel, new HBox(20, r1, r2, r3),
            svcLabel, svcHint, svcRow,
            guestLabel, guestHint,
            new HBox(20, nameField, emailField),
            datesLabel, datesHint, datesRow,
            guestsLabel, guestsHint, guestsRow,
            totalLabel, error, next
        );
        setCard(card);
    }

    // STEP 4 Payment
    private void showStep4() {
        currentStepRefresh = () -> showStep4();
        VBox card = makeCard();
        card.getChildren().addAll(
            makeStepIndicator("Step 3 of 4"),
            makeTitle("Payment"),
            makeSeparator()
        );

        ToggleGroup pg = new ToggleGroup();
        RadioButton pCard    = makeRadio("Card", pg);
        RadioButton pCash    = makeRadio("Cash on Arrival", pg);
        RadioButton pPayPal  = makeRadio("PayPal", pg);
        pCard.setSelected(true);

        Label error = makeError("");
        Button confirm = makeButton("Confirm Reservation");
        confirm.setStyle(confirm.getStyle() + "-fx-background-color: #2E7D32;");

        confirm.setOnAction(e -> {
            // Map UI → PaymentType + Strategy (Adapter inside)
            PaymentStrategy strategy;
            if (pCard.isSelected()) {
                paymentType = PaymentType.CARD;
                strategy = new StripePaymentStrategy();
            } else if (pCash.isSelected()) {
                paymentType = PaymentType.CASH;
                strategy = new CashPaymentStrategy();
            } else {
                paymentType = PaymentType.ONLINE_BANKING;
                strategy = new PayPalPaymentStrategy();
            }

            double base = selectedRoom.getPricePerNight();
            double extras = selectedServices.stream().mapToDouble(ExtraService::getPrice).sum();
            double subtotal = (base + extras) * nights;
            if (isLoyalty) subtotal *= 0.85;
            double total = subtotal * 1.09;

            // Builder
            Reservation reservation = new ReservationBuilder()
                .withGuestName(guestName)
                .withPhoneNumber(phoneNumber)
                .withRoom(selectedRoom.clone())
                .withNights(nights)
                .withServices(selectedServices)
                .withLoyalty(isLoyalty)
                .withPaymentType(paymentType)
                .withCheckIn(checkInDate)
                .withCheckOut(checkOutDate)
                .build();

            System.out.println("============================================================");
            System.out.println("PROCESSING PAYMENT via " + strategy.getMethodName());
            strategy.pay(total);
            System.out.println("============================================================");


            Command placeOrder = new PlaceReservationCommand(
                HotelManager.getInstance(), reservation
            );
            invoker.executeCommand(placeOrder);

            final double finalTotal = total;
            final PaymentStrategy finalStrategy = strategy;
            fadeTransition(() -> showConfirmation(reservation, finalTotal, finalStrategy));
        });

        card.getChildren().addAll(
            makeQuestion("Select payment method:"),
            new VBox(12, pCard, pCash, pPayPal),
            error, confirm
        );
        setCard(card);
    }

    // STEP 5 Confirmation
    private void showConfirmation(Reservation res, double total, PaymentStrategy strategy) {
        VBox card = makeCard();

        Label icon = new Label("CONFIRMED");
        icon.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        icon.setStyle("-fx-text-fill: #2E7D32;");

        Label title = makeTitle("Reservation Confirmed!");
        title.setStyle("-fx-text-fill: #2E7D32; -fx-font-family: Georgia; -fx-font-size: 28; -fx-font-weight: bold;");

        // COMPOSITE: pentru Suite afisam pachetul ca arbore composite
        // Pentru Standard / Deluxe afisam lista simpla
        boolean isSuite = res.getRoom() instanceof SuiteRoom;
        VBox servicesBlock = new VBox(4);

        if (isSuite && !res.getExtraServices().isEmpty()) {
            ServicePackage suitePackage = new ServicePackage("Suite Premium Package");
            for (ExtraService s : res.getExtraServices()) {
                suitePackage.add(new SingleService(s.getDescription(), s.getPrice()));
            }
            System.out.println("[COMPOSITE] Displaying Suite package structure:");
            suitePackage.display();

            Label header = new Label("◆ " + suitePackage.getDescription()
                    + "  (€" + String.format("%.2f", suitePackage.getPrice()) + ")");
            header.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
            header.setStyle("-fx-text-fill: " + GOLD_FG() + "; -fx-font-family: Georgia; -fx-font-size: 13; -fx-font-weight: bold;");
            servicesBlock.getChildren().add(header);

            for (ExtraService s : res.getExtraServices()) {
                Label item = new Label("    • " + s.getDescription()
                        + " — €" + String.format("%.0f", s.getPrice()));
                item.setFont(Font.font("Georgia", 12));
                item.setStyle("-fx-text-fill: " + TEXT() + ";");
                servicesBlock.getChildren().add(item);
            }
        } else {
            String svcList = res.getExtraServices().isEmpty() ? "None"
                : res.getExtraServices().stream().map(ExtraService::getDescription)
                    .reduce((a, b) -> a + ", " + b).orElse("None");
            Label item = new Label(svcList);
            item.setFont(Font.font("Georgia", 13));
            item.setStyle("-fx-text-fill: " + TEXT() + ";");
            servicesBlock.getChildren().add(item);
        }

        Label statusValue = new Label(res.getStatusName());
        statusValue.setFont(Font.font("Georgia", 13));
        statusValue.setStyle("-fx-text-fill: " + TEXT() + ";");

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
        String checkInStr  = res.getCheckInDate()  != null ? res.getCheckInDate().format(dateFmt)  : "—";
        String checkOutStr = res.getCheckOutDate() != null ? res.getCheckOutDate().format(dateFmt) : "—";
        String guestsStr   = adults + " adult" + (adults == 1 ? "" : "s")
                + (children > 0 ? ", " + children + " child" + (children == 1 ? "" : "ren") : "");

        VBox details = new VBox(8,
            makeDetail("Guest",     res.getGuestName()),
            makeDetail("Email",     guestEmail.isEmpty() ? "—" : guestEmail),
            makeDetail("Room",      res.getRoom().getDescription()),
            makeDetail("Check-in",  checkInStr),
            makeDetail("Check-out", checkOutStr),
            makeDetail("Nights",    res.getNights() + " nights"),
            makeDetail("Guests",    guestsStr),
            makeDetailNode("Services", servicesBlock),
            makeDetail("Loyalty",   res.isLoyalty() ? "Yes (-15%)" : "No"),
            makeDetail("Payment",   strategy.getMethodName()),
            makeDetail("Total",     String.format("€%.2f (incl. 9%% VAT) · PAID", total)),
            makeDetailNode("Status", statusValue)
        );
        details.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-padding: 16; -fx-background-radius: 8;");

        Button undoBtn = new Button("Undo Reservation");
        undoBtn.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        undoBtn.setTextFill(Color.web("#FF6B6B"));
        undoBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #FF6B6B;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 10 24;" +
            "-fx-cursor: hand;" +
            "-fx-text-fill: #FF6B6B;"
        );

        Label undoStatus = makeHint("");

        undoBtn.setOnAction(e -> {
            invoker.undoLastCommand();
            undoBtn.setDisable(true);
            undoBtn.setText("Reservation Cancelled");
            statusValue.setText(res.getStatusName());
            statusValue.setStyle("-fx-text-fill: #FF6B6B;");
            undoStatus.setText("Command undo executed. Reservation rolled back.");
            title.setText("Reservation Cancelled");
            title.setStyle("-fx-text-fill: #FF6B6B; -fx-font-family: Georgia; -fx-font-size: 28; -fx-font-weight: bold;");
            icon.setText("CANCELLED");
            icon.setStyle("-fx-text-fill: #FF6B6B;");
        });

        Button newRes = makeButton("Make Another Reservation");
        newRes.setOnAction(e -> {
            // reset state
            isLoyalty = false; phoneNumber = ""; guestName = ""; guestEmail = "";
            selectedRoom = null; nights = 1;
            checkInDate = null; checkOutDate = null;
            adults = 1; children = 0;
            selectedServices.clear(); paymentType = PaymentType.CARD;
            fadeTransition(() -> showStep1());
        });

        HBox actions = new HBox(12, newRes, undoBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(icon, title, makeSeparator(), details, undoStatus, actions);
        setCard(card);
    }

    // UI HELPERS
    private VBox makeCard() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(48));
        card.setMaxWidth(680);
        card.setStyle(
            "-fx-background-color: " + CARD() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 30, 0, 0, 8);"
        );
        return card;
    }

    private void setCard(VBox card) {
        javafx.scene.Node themeBtn = root.getChildren().size() > 0
            ? root.getChildren().stream().filter(n -> n instanceof Button).findFirst().orElse(null)
            : null;
        root.getChildren().clear();

       
        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(30, 20, 30, 20));

        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
        wrapper.getChildren().addAll(topSpacer, card, bottomSpacer);

        ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-control-inner-background: transparent;"
        );
        wrapper.minHeightProperty().bind(scrollPane.heightProperty());
        wrapper.setStyle("-fx-background-color: transparent;");

        scrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            javafx.scene.Node viewport = scrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: transparent;");
            }
        });

        javafx.application.Platform.runLater(() -> {
            javafx.scene.Node viewport = scrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: transparent;");
            }
        });

        root.getChildren().add(scrollPane);
        if (themeBtn != null) root.getChildren().add(themeBtn);
        FadeTransition ft = new FadeTransition(Duration.millis(300), card);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void fadeTransition(Runnable next) {
        if (!root.getChildren().isEmpty()) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), root.getChildren().get(0));
            ft.setToValue(0);
            ft.setOnFinished(e -> next.run());
            ft.play();
        } else {
            next.run();
        }
    }

    private Label makeTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        l.setStyle("-fx-text-fill: " + GOLD_FG() + ";");
        return l;
    }

    private Label makeSubtitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", 13));
        l.setStyle("-fx-text-fill: " + MUTED() + "; -fx-letter-spacing: 3;");
        return l;
    }

    private Label makeQuestion(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", FontWeight.SEMI_BOLD, 15));
        l.setStyle("-fx-text-fill: " + TEXT() + ";");
        return l;
    }

    private Label makeHint(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", FontPosture.ITALIC, 12));
        l.setStyle("-fx-text-fill: " + MUTED() + ";");
        return l;
    }

    private Label makeError(String text) {
        Label l = new Label(text);
        String errorColor = isDarkMode ? "#FF6B6B" : "#2B2B2B";
        l.setStyle("-fx-text-fill: " + errorColor + ";");
        l.setFont(Font.font(12));
        return l;
    }

    private Label makeStepIndicator(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", 11));
        l.setStyle("-fx-text-fill: " + GOLD_FG() + "; -fx-letter-spacing: 2;");
        return l;
    }

    private Region makeSeparator() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: " + GOLD + "; -fx-opacity: 0.4;");
        return r;
    }

    private RadioButton makeRadio(String text, ToggleGroup tg) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(tg);
        rb.setFont(Font.font("Georgia", 13));
        rb.setStyle(
            "-fx-text-fill: " + TEXT() + ";" +
            "-fx-mark-color: " + GOLD + ";" +
            "-fx-focus-color: " + GOLD + ";"
        );
        return rb;
    }

    private CheckBox makeCheckBox(String text) {
        CheckBox cb = new CheckBox(text);
        cb.setFont(Font.font("Georgia", 13));
        cb.setStyle("-fx-text-fill: " + TEXT() + ";");
        return cb;
    }

    private TextField makeTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Georgia", 13));
        tf.setStyle(
            "-fx-background-color: " + FIELD() + ";" +
            "-fx-text-fill: " + TEXT() + ";" +
            "-fx-prompt-text-fill: " + GRAY + ";" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 10 14;" +
            "-fx-border-color: " + BORDER() + ";" +
            "-fx-border-radius: 6;"
        );
        tf.setPrefWidth(240);
        return tf;
    }

    private Button makeButton(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        b.setTextFill(Color.web(NAVY));
        b.setStyle(
            "-fx-background-color: " + GOLD + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12 32;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle() + "-fx-opacity: 0.85;"));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace("-fx-opacity: 0.85;", "")));
        return b;
    }

    private VBox makeOptionBox(String title, String desc, ToggleGroup tg, boolean selected) {
        RadioButton rb = new RadioButton(title);
        rb.setToggleGroup(tg);
        rb.setSelected(selected);
        rb.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        rb.setStyle(
            "-fx-text-fill: " + TEXT() + ";" +
            "-fx-mark-color: " + GOLD + ";" +
            "-fx-focus-color: " + GOLD + ";"
        );

        Label d = new Label(desc);
        d.setFont(Font.font("Georgia", FontPosture.ITALIC, 12));
        d.setStyle("-fx-text-fill: " + MUTED() + ";");

        VBox box = new VBox(6, rb, d);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: " + FIELD() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + BORDER() + ";" +
            "-fx-border-radius: 10;"
        );
        return box;
    }

    private HBox makeDetail(String label, String value) {
        Label k = new Label(label + ":");
        k.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        k.setStyle("-fx-text-fill: " + GOLD_FG() + ";");
        k.setMinWidth(90);

        Label v = new Label(value);
        v.setFont(Font.font("Georgia", 13));
        v.setStyle("-fx-text-fill: " + TEXT() + ";");
        v.setWrapText(true);

        return new HBox(12, k, v);
    }

    private HBox makeDetailNode(String label, javafx.scene.Node node) {
        Label k = new Label(label + ":");
        k.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        k.setStyle("-fx-text-fill: " + GOLD_FG() + ";");
        k.setMinWidth(90);

        HBox row = new HBox(12, k, node);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private void styleDatePicker(DatePicker dp) {
        String fieldStyle =
            "-fx-background-color: " + FIELD() + ";" +
            "-fx-text-fill: " + TEXT() + ";" +
            "-fx-border-color: " + BORDER() + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;";

        dp.setStyle(fieldStyle);
        dp.getEditor().setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT() + ";"
        );
        dp.setPrefWidth(180);
    }

    public static void main(String[] args) {
        launch(args);
    }
}