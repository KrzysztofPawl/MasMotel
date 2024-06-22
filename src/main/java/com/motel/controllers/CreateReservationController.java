package com.motel.controllers;

import com.motel.DTO.RoomView;
import com.motel.MotelSystemApplication;
import com.motel.exception.ControllerException;
import com.motel.exception.DataNotFoundException;
import com.motel.interfaces.service.GuestService;
import com.motel.interfaces.service.InvoiceService;
import com.motel.interfaces.service.ReservationService;
import com.motel.interfaces.service.RoomService;
import com.motel.model.Invoice;
import com.motel.model.Reservation;
import com.motel.model.Room;
import com.motel.model.RoomTraits;
import com.motel.model.enums.InvoiceStatus;
import com.motel.model.enums.ReservationStatus;
import com.motel.model.enums.RoomStatus;
import com.motel.utils.AlertPopper;
import com.motel.utils.InfoPopper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
@Slf4j
@RequiredArgsConstructor
public class CreateReservationController {
    private final RoomService roomService;
    private final GuestService guestService;
    private final ReservationService reservationService;
    private final InvoiceService invoiceService;

    private String guestPesel;
    private LocalDate reservationDate;
    private LocalDate arrivalDate;
    private LocalDate leaveDate;
    private ReservationStatus status;
    private List<Integer> selectedRoomsIds;

    private ObservableList<RoomView> selectedRooms = FXCollections.observableArrayList();

    private static final String HElP_MESSAGE = """
            This window allows you to create a new reservation for a guest.
            To create a reservation, you need to provide the following information:
            - Arrival date
            - Leave date
            - Reservation status (must be OPEN)
            - Select available rooms
            - Registration plate number (optional)
            After providing the necessary information, click the 'Create Reservation' button.
            """;

    @FXML
    private TextField registrationPlateNumberField;
    @FXML
    private TextField numberOfNightsField;
    @FXML
    private DatePicker reservationDatePicker;
    @FXML
    private DatePicker arrivalDatePicker;
    @FXML
    private DatePicker leaveDatePicker;
    @FXML
    private ChoiceBox<ReservationStatus> reservationStatusChoiceBox;
    @FXML
    private CheckBox emailSentCheckBox;
    @FXML
    private CheckBox invoiceGeneratedCheckBox;
    @FXML
    private CheckBox savedInSystemCheckBox;
    @FXML
    private Button createReservationButton;
    @FXML
    private MenuItem menuBackItem;
    @FXML
    private MenuItem menuHelpItem;
    @FXML
    private MenuItem menuRoomsInfoItem;

    @FXML
    private ChoiceBox<ReservationStatus> reservationStatus;
    @FXML
    private TableView<RoomView> reservationTableView;
    @FXML
    private TableColumn<RoomView, Integer> roomNumberColumn;
    @FXML
    private TableColumn<RoomView, Integer> floorColumn;
    @FXML
    private TableColumn<RoomView, BigDecimal> priceForNightColumn;
    @FXML
    private TableColumn<RoomView, String> standardColumn;
    @FXML
    private TableColumn<RoomView, Integer> bedsColumn;
    @FXML
    private TableColumn<RoomView, Boolean> addColumn;

    @FXML
    private void initialize() {
        reservationDate = LocalDate.now();
        log.debug("Setting reservation date to: {}", reservationDate);
        reservationDatePicker.setValue(reservationDate);

        reservationStatusChoiceBox.getItems().addAll(ReservationStatus.values());
        reservationStatusChoiceBox.setOnAction(this::setReservationStatus);

        ChangeListener<LocalDate> dateChangeListener = (observable, oldValue, newValue) -> areDatesPicked();

        arrivalDatePicker.valueProperty().addListener(dateChangeListener);
        leaveDatePicker.valueProperty().addListener(dateChangeListener);

        createReservationButton.setOnAction(event -> {
            try {
                log.debug("Creating reservation...");
                validateFields();
                fetchDates();
                validateDates();
                addRoomToSelection();
                validateAndSetRegistrationPlateNumber(guestPesel);
                saveReservation(arrivalDate, leaveDate);
                createReservationButton.setDisable(true);
            } catch (ControllerException e) {
                log.error("Error while creating reservation");
                AlertPopper.showErrorAlert("Error while creating reservation: " + e.getMessage());
                createReservationButton.setDisable(false);
            } catch (Exception e) {
                log.error("Unexpected error while creating reservation", e);
                AlertPopper.showErrorAlert("Unexpected error while creating reservation" + e.getMessage());
                createReservationButton.setDisable(false);
            }
        });

        menuBackItem.setOnAction(event -> closeWindow());
        menuHelpItem.setOnAction(event -> InfoPopper.showInfo(
                "Create Reservation",HElP_MESSAGE));
        menuRoomsInfoItem.setOnAction(event -> openRoomInfoView());

        setupColumns();
        loadRooms();
        areDatesPicked();
    }

    private void areDatesPicked() {
        boolean datesSelected = arrivalDatePicker.getValue() != null && leaveDatePicker.getValue() != null;
        createReservationButton.setDisable(!datesSelected);
    }

    @FXML
    private void addRoomToSelection() {
        selectedRooms.clear();
        for (RoomView room : reservationTableView.getItems()) {
            if (room.getSelected().get()) {
                selectedRooms.add(room);
            }
        }
        if (selectedRooms.isEmpty()) {
            throw new ControllerException("No rooms selected");
        }
    }

    private void addRoomsToReservation(Reservation reservation, List<RoomView> selectedRooms) {
        Set<Room> rooms = selectedRooms.stream()
                .map(roomView -> {
                    Room room = roomService.getRoomByNumber(roomView.getRoomNumber())
                            .orElseThrow(() -> new ControllerException("Room not found: " + roomView.getRoomNumber()));
                    room.setReservation(reservation);
                    room.setStatus(RoomStatus.UNAVAILABLE);
                    return room;
                })
                .collect(Collectors.toSet());
        reservation.setRooms(rooms);
        reservationService.saveReservation(reservation);
        rooms.forEach(roomService::saveRoom);
    }

    private void saveReservation(LocalDate arrivalDate, LocalDate leaveDate) {
        var numberOfNights = calculateNumberOfNights(arrivalDate, leaveDate);
        numberOfNightsField.setText(String.valueOf(numberOfNights));

        var createdReservation = buildReservation(numberOfNights);
        addRoomsToReservation(createdReservation, selectedRooms);

        log.info("Reservation created: {}", createdReservation.getId());
        savedInSystemCheckBox.setSelected(true);
        generateInvoice(createdReservation);
    }

    private String resolveRoomTraits(RoomTraits traits) {
        StringBuilder sb = new StringBuilder();
        sb.append("Room: ");
        if (traits.getStandardRoom() != null) {
            sb.append("Standard").append(", ");
        }
        if (traits.getPremiumRoom() != null) {
            sb.append("Premium").append(", ");
        }
        if (traits.getSingleRoom() != null) {
            sb.append("Single").append(", ");
        }
        if (traits.getMultiPersonRoom() != null) {
            sb.append("Multi-person").append(", ");
        }
        return sb.toString();
    }

    private int resolveBedCount(RoomTraits traits) {
        if (traits.getSingleRoom() != null) {
            return 1;
        }
        if (traits.getMultiPersonRoom() != null) {
            return traits.getMultiPersonRoom().getNumberOfBeds();
        }
        return 0;
    }

    private Reservation buildReservation(int numberOfNights) {
        Reservation reservation = new Reservation();
        reservation.setGuest(guestService.getGuestByPesel(guestPesel));
        reservation.setNights(numberOfNights);
        reservation.setPesel(guestPesel);
        reservation.setReservationDate(reservationDate);
        reservation.setArrivalDate(arrivalDate);
        reservation.setLeaveDate(leaveDate);
        reservation.setStatus(status);
        reservation.setDeleted(false);
        return reservation;
    }

    private void validateAndSetRegistrationPlateNumber(String guestPesel) {
        if (registrationPlateNumberField != null) {
            var registrationPlateNumber = registrationPlateNumberField.getText().trim();
            if (!registrationPlateNumber.isEmpty()) {
                if (registrationPlateNumber.length() < 5 || registrationPlateNumber.length() > 7) {
                    throw new ControllerException("Registration plate number must be between 5 and 7 characters long");
                }
                guestService.updateGuestRegistrationPlateNumber(guestPesel, registrationPlateNumber);
            }
        }
    }

    private void setReservationStatus(ActionEvent actionEvent) {
        status = reservationStatusChoiceBox.getValue();
        if (!status.equals(ReservationStatus.OPEN)) {
            throw new ControllerException("Reservation status must be OPEN");
        }
    }

    private int calculateNumberOfNights(LocalDate arrivalDate, LocalDate leaveDate) {
        return arrivalDate.until(leaveDate).getDays();
    }

    private void fetchDates() {
        arrivalDate = arrivalDatePicker.getValue();
        log.debug("Arrival date picked: {}", arrivalDate);
        leaveDate = leaveDatePicker.getValue();
        log.debug("Leave date picked: {}", leaveDate);
    }

    private void validateDates() {
        if (arrivalDate.isBefore(reservationDate) || leaveDate.isBefore(arrivalDate)) {
            throw new ControllerException("Invalid dates");
        }
        if (arrivalDate.isEqual(leaveDate)) {
            throw new ControllerException("Arrival date cannot be the same as leave date");
        }
    }

    private void validateFields() {
        if (reservationDatePicker.getValue() == null) {
            throw new ControllerException("Reservation date cannot be null");
        }
        if (arrivalDatePicker.getValue() == null) {
            throw new ControllerException("Arrival date cannot be null");
        }
        if (leaveDatePicker.getValue() == null) {
            throw new ControllerException("Leave date cannot be null");
        }
        if (reservationStatusChoiceBox.getValue() == null) {
            throw new ControllerException("Reservation status cannot be null");
        }
    }

    private void setupColumns() {
        log.debug("Setting up columns");

        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        log.debug("roomNumberColumn set");

        floorColumn.setCellValueFactory(new PropertyValueFactory<>("floor"));
        log.debug("floorColumn set");

        priceForNightColumn.setCellValueFactory(new PropertyValueFactory<>("priceForNight"));
        log.debug("priceForNightColumn set");

        standardColumn.setCellValueFactory(new PropertyValueFactory<>("standard"));
        log.debug("standardColumn set");

        bedsColumn.setCellValueFactory(new PropertyValueFactory<>("beds"));
        log.debug("bedsColumn set");

        addColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        addColumn.setCellFactory(CheckBoxTableCell.forTableColumn(addColumn));
        log.debug("addColumn set");

        reservationTableView.setEditable(true);
        log.debug("TableView set to editable");

        reservationTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        log.debug("Selection mode set to multiple");
    }

    private void loadRooms() {
        try {
            List<Room> rooms = roomService.findRoomByStatus(RoomStatus.AVAILABLE);
            List<RoomView> roomViews = rooms.stream()
                    .map(room -> {
                        RoomTraits traits = room.getRoomTraits();
                        return new RoomView(
                                room.getNumber(),
                                room.getFloor(),
                                room.getPriceForNight(),
                                resolveRoomTraits(traits),
                                resolveBedCount(traits)
                        );
                    }).toList();
            reservationTableView.setItems(FXCollections.observableArrayList(roomViews));
        } catch (DataNotFoundException e) {
            log.error("Error while loading rooms");
        }
    }

    public boolean checkAvailableRooms() {
        try {
            List<Room> rooms = roomService.findRoomByStatus(RoomStatus.AVAILABLE);
            return !rooms.isEmpty();
        } catch (DataNotFoundException e) {
            log.error("No free rooms available");
            AlertPopper.showErrorAlert("No Free Rooms: " + e.getMessage());
            return false;
        }
    }

    private void generateInvoice(Reservation reservation) {
        BigDecimal totalCost = selectedRooms.stream()
                .map(RoomView::getPriceForNight)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(reservation.getNights()));

        Invoice invoice = new Invoice();
        invoice.setReservation(reservation);
        invoice.setDateOfIssue(reservation.getReservationDate());
        invoice.setSum(totalCost);
        invoice.setStatus(InvoiceStatus.OPEN);
        invoice.setVat(23);

        invoiceService.saveInvoice(invoice);
        invoiceGeneratedCheckBox.setSelected(true);
        sendEmail();
        log.info("Invoice generated: {}", invoice.getId());
    }

    private void sendEmail() {
        emailSentCheckBox.setSelected(true);
        log.info("Email sent");
    }

    private void closeWindow() {
        Stage stage = (Stage) reservationTableView.getScene().getWindow();
        stage.close();
    }

    private void openRoomInfoView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RoomInfoView.fxml"));
            loader.setControllerFactory(MotelSystemApplication.getContext()::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("More Room Info");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log.error("Error while opening More Room Info View", e);
            AlertPopper.showErrorAlert("Error while opening More Room Info View: " + e.getMessage());
        }
    }
}