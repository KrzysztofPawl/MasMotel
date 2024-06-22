package com.motel.controllers;

import com.motel.MotelSystemApplication;
import com.motel.exception.ControllerException;
import com.motel.exception.DataNotFoundException;
import com.motel.interfaces.service.GuestService;
import com.motel.interfaces.service.ReservationService;
import com.motel.interfaces.service.InvoiceService;
import com.motel.model.Invoice;
import com.motel.model.Reservation;
import com.motel.model.Room;
import com.motel.model.RoomTraits;
import com.motel.model.enums.InvoiceStatus;
import com.motel.model.enums.ReservationStatus;
import com.motel.utils.AlertPopper;
import com.motel.utils.InfoPopper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModifyReservationController {

    private final ReservationService reservationService;
    private final InvoiceService invoiceService;
    private final GuestService guestService;

    private static final String HELP_MESSAGE = """
            To modify reservation:
            1. Change arrival and leave dates.
            2. Choose reservation status.
            3. Click 'Modify Reservation' button.
            
            To pay invoice:
            1. Enter invoice ID.
            2. Enter payment method.
            3. Enter amount (must match invoice sum).
            4. Click 'Pay Invoice' button.
            
            To add new invoice:
            1. Click 'Add Invoice' button.
            2. Enter invoice sum.
            3. Click 'Add Invoice' button.
            """;

    @FXML
    private TextField numberOfNightsField;
    @FXML
    private TextField plateNumberField;
    @FXML
    private DatePicker reservationDatePicker;
    @FXML
    private DatePicker arrivalDatePicker;
    @FXML
    private DatePicker leaveDatePicker;
    @FXML
    private ChoiceBox<ReservationStatus> reservationStatusChoiceBox;
    @FXML
    private TextArea reservationDetailsTextArea;
    @FXML
    private Button modifyReservationButton;
    @FXML
    private Button addInvoiceButton;
    @FXML
    private Button payInvoiceButton;
    @FXML
    private TextField invoiceIdField;
    @FXML
    private TextField paymentMethodField;
    @FXML
    private TextField sumField;
    @FXML
    private TextField amountField;
    @FXML
    private CheckBox invoiceGeneratedCheckBox;
    @FXML
    private CheckBox savedInSystemCheckBox;
    @FXML
    private CheckBox paidCheckBox;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem helpMenuItem;
    @FXML
    private MenuItem refreshMenuItem;

    private Reservation currentReservation;

    @FXML
    private void initialize() {
        modifyReservationButton.setOnAction(event -> modifyReservation());
        payInvoiceButton.setOnAction(event -> {
            payInvoice();
            clearFields();
        });
        addInvoiceButton.setOnAction(event -> openAddInvoiceDialog());
        closeMenuItem.setOnAction(event -> closeWindow());
        refreshMenuItem.setOnAction(event -> refreshReservationDetails());
        helpMenuItem.setOnAction(event -> InfoPopper.showInfo("Help", HELP_MESSAGE));

        reservationStatusChoiceBox.getItems().setAll(ReservationStatus.values());
    }

    public void loadReservationDetails(int reservationId) {
        try {
            currentReservation = reservationService.getReservationByIdNotDeleted(reservationId);
            isReservationPaid(currentReservation);
            blockModifyReservationButtonWhenReservationIsPaid(currentReservation);
            blockInvoiceButtonsWhenReservationIsPaid(currentReservation);
            refreshInvoiceSum();

            reservationDatePicker.setValue(currentReservation.getReservationDate());
            reservationDatePicker.setEditable(false);
            arrivalDatePicker.setValue(currentReservation.getArrivalDate());
            leaveDatePicker.setValue(currentReservation.getLeaveDate());
            reservationStatusChoiceBox.setValue(currentReservation.getStatus());
            numberOfNightsField.setText(currentReservation.getNights().toString());

            Optional<String> registrationPlateNumber = guestService
                    .findRegistrationPlateNumberByPesel(currentReservation.getGuest().getPerson().getPesel());
            plateNumberField.setText(registrationPlateNumber.orElse(""));

            refreshReservationDetails();
        } catch (Exception e) {
            log.error("Error while loading reservation details", e);
            AlertPopper.showErrorAlert("Error while loading reservation details: " + e.getMessage());
        }
    }

    private void modifyReservation() {
        try {
            LocalDate arrivalDate = arrivalDatePicker.getValue();
            LocalDate leaveDate = leaveDatePicker.getValue();
            int numberOfNights = calculateNumberOfNights(arrivalDate, leaveDate);

            currentReservation.setArrivalDate(arrivalDate);
            currentReservation.setLeaveDate(leaveDate);
            currentReservation.setNights(numberOfNights);
            currentReservation.setStatus(reservationStatusChoiceBox.getValue());
            numberOfNightsField.setText(String.valueOf(numberOfNights));

            if (currentReservation.getStatus() == ReservationStatus.CANCELED) {
                currentReservation.getInvoices().forEach(invoice -> invoice.setStatus(InvoiceStatus.CANCELED));
            }

            reservationService.saveReservation(currentReservation);

            if (currentReservation.getStatus() == ReservationStatus.DELETED) {
                reservationService.markReservationAsDeletedAndDeleteInvoices(currentReservation.getId());
            }

            validateAndSetRegistrationPlateNumber(currentReservation.getGuest().getPerson().getPesel());

           refreshReservationDetails();
           savedInSystemCheckBox.setSelected(true);
        } catch (Exception e) {
            log.error("Error while modifying reservation", e);
            AlertPopper.showErrorAlert("Error while modifying reservation: " + e.getMessage());
        }
    }

    private void payInvoice() {
        try {
            int invoiceId = parseId(invoiceIdField.getText());
            if (invoiceId == -1) {
                return;
            }

            validateIfInvoiceIdExists(invoiceId);
            checkIfInvoiceIsPaid(invoiceId);

            var amountFromField = amountField.getText();
            validateAmountFromString(amountFromField);
            BigDecimal amount = new BigDecimal(amountFromField);
            String paymentMethod = paymentMethodField.getText();

            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            if (invoice.getSum().compareTo(amount) == 0) {
                invoice.markAsPaid(paymentMethod);
                invoiceService.saveInvoice(invoice);
                refreshReservationDetails();

                if (currentReservation.getInvoices().stream().allMatch(inv -> inv.getStatus() == InvoiceStatus.PAID)) {
                    paidCheckBox.setSelected(true);
                    currentReservation.setStatus(ReservationStatus.FINISHED);
                    reservationService.saveReservation(currentReservation);
                    reservationService.clearRoomAssignmentForReservation(currentReservation.getId());
                    reservationStatusChoiceBox.setValue(ReservationStatus.FINISHED);
                }

                refreshReservationDetails();
                InfoPopper.showInfo("Success", "Invoice paid successfully!");
            } else {
                AlertPopper.showErrorAlert("Amount does not match invoice sum!");
            }
        } catch (Exception e) {
            log.error("Error while paying invoice", e);
            AlertPopper.showErrorAlert("Error while paying invoice: " + e.getMessage());
        }
    }

    private void checkIfInvoiceIsPaid(int invoiceId) {
        if (currentReservation.getInvoices().stream().anyMatch(invoice -> invoice.getId() == invoiceId && invoice.getStatus() == InvoiceStatus.PAID)) {
            AlertPopper.showErrorAlert("Invoice with ID " + invoiceId + " is already paid!");
            throw new IllegalStateException("Invoice with ID " + invoiceId + " is already paid!");
        }
    }

    private void validateIfInvoiceIdExists(int invoiceId) {
        if (currentReservation.getInvoices().stream().noneMatch(invoice -> invoice.getId() == invoiceId)) {
            AlertPopper.showErrorAlert("Invoice with ID " + invoiceId + " does not exist!");
            throw new DataNotFoundException("Invoice with ID " + invoiceId + " does not exist!");
        }
    }

    private void openAddInvoiceDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddInvoiceView.fxml"));
            loader.setControllerFactory(MotelSystemApplication.getContext()::getBean);
            Parent root = loader.load();

            AddInvoiceController controller = loader.getController();
            controller.setReservation(currentReservation, this);

            Stage stage = new Stage();
            stage.setTitle("Add New Invoice");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log.error("Error while opening Add Invoice dialog", e);
            AlertPopper.showErrorAlert("Error while opening Add Invoice dialog: " + e.getMessage());
        }
    }

    private int calculateNumberOfNights(LocalDate arrivalDate, LocalDate leaveDate) {
        return (int) (leaveDate.toEpochDay() - arrivalDate.toEpochDay());
    }

    private String formatReservationDetails(Reservation reservation) {
        StringBuilder sb = new StringBuilder();
        sb.append("Reservation Date: ").append(reservation.getReservationDate()).append("\n");
        sb.append("Reservation Status: ").append(reservation.getStatus()).append("\n");
        sb.append("Arrival Date: ").append(reservation.getArrivalDate()).append("\n");
        sb.append("Leave Date: ").append(reservation.getLeaveDate()).append("\n");
        sb.append("Nights: ").append(reservation.getNights()).append("\n");
        sb.append("Room Numbers: ").append(reservation.getRooms().stream().map(room -> room.getNumber().toString()).collect(Collectors.joining(", "))).append("\n");
        sb.append("Room Details: ").append(reservation.getRooms().stream().map(this::formatRoomDetails).collect(Collectors.joining(", "))).append("\n");
        sb.append("------\n");
        sb.append("Guest: ").append(reservation.getGuest().getPerson().getName()).append(" ").append(reservation.getGuest().getPerson().getSurname()).append("\n");
        sb.append("------\n");
        sb.append("Invoices:\n");
        reservation.getInvoices().forEach(invoice -> {
            sb.append("Invoice Id: ").append(invoice.getId()).append("\n");
            sb.append("Invoice Status: ").append(invoice.getStatus()).append("\n");
            sb.append("Invoice Sum: ").append(invoice.getSum()).append("\n");
            sb.append("Invoice Date of Issue: ").append(invoice.getDateOfIssue()).append("\n");
            sb.append("------\n");
        });

        return sb.toString();
    }

    private String formatRoomDetails(Room room) {
        StringBuilder sb = new StringBuilder();
        RoomTraits traits = room.getRoomTraits();
        if (traits.getStandardRoom() != null) {
            sb.append("Standard, Room Size: ").append(traits.getStandardRoom().getRoomSize()).append(" ");
        }
        if (traits.getPremiumRoom() != null) {
            sb.append("Premium, Room Size: ").append(traits.getPremiumRoom().getRoomSize())
                    .append(", Free Standing Bathtub Size: ").append(traits.getPremiumRoom().getFreeStandingBathtubSize()).append(" ");
        }
        if (traits.getSingleRoom() != null) {
            sb.append("Single, Bed Size: ").append(traits.getSingleRoom().getBedSize()).append(" ");
        }
        if (traits.getMultiPersonRoom() != null) {
            sb.append("Multi-person, Number of Beds: ").append(traits.getMultiPersonRoom().getNumberOfBeds()).append(" ");
        }
        return sb.toString();
    }

    private void isReservationPaid(Reservation currentReservation){
        if (currentReservation.getInvoices().stream().allMatch(invoice -> invoice.getStatus() == InvoiceStatus.PAID)) {
            paidCheckBox.setSelected(true);
        }
    }

    private int parseId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            AlertPopper.showErrorAlert("Please enter valid ID");
            return -1;
        }
    }

    private void blockModifyReservationButtonWhenReservationIsPaid(Reservation currentReservation) {
        if (currentReservation.getInvoices().stream()
                .allMatch(invoice -> invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELED)) {
            modifyReservationButton.setDisable(true);
        }
    }

    private void blockInvoiceButtonsWhenReservationIsPaid(Reservation currentReservation) {
        if (currentReservation.getInvoices().stream()
                .allMatch(invoice -> invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELED)) {
            addInvoiceButton.setDisable(true);
            payInvoiceButton.setDisable(true);
        }
    }

    public void refreshReservationDetails() {
        currentReservation = reservationService.getReservationById(currentReservation.getId());
        String details = formatReservationDetails(currentReservation);
        reservationDetailsTextArea.setText(details);

        refreshInvoiceSum();
    }

    private void validateAndSetRegistrationPlateNumber(String guestPesel) {
        if (plateNumberField != null) {
            var registrationPlateNumber = plateNumberField.getText().trim();
            if (!registrationPlateNumber.isEmpty()) {
                if (registrationPlateNumber.length() < 5 || registrationPlateNumber.length() > 7) {
                    throw new ControllerException("Registration plate number must be between 5 and 7 characters long");
                }
                guestService.updateGuestRegistrationPlateNumber(guestPesel, registrationPlateNumber);
            }
        }
    }

    private void validateAmountFromString(String amountStr) {
        try {
            new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            AlertPopper.showErrorAlert("Amount must be a number!");
        }
    }

    private void refreshInvoiceSum() {
        BigDecimal totalSum = currentReservation.getInvoices().stream()
                .filter(invoice -> invoice.getStatus() != InvoiceStatus.CANCELED)
                .filter(invoice -> invoice.getStatus() != InvoiceStatus.PAID)
                .map(Invoice::getSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total sum: {}", totalSum);
        sumField.setText(totalSum.toString());
    }

    private void clearFields() {
        invoiceIdField.clear();
        paymentMethodField.clear();
        amountField.clear();
    }

    private void closeWindow() {
        Stage stage = (Stage) closeMenuItem.getParentPopup().getOwnerWindow();
        stage.close();
    }

    public void setInvoiceGeneratedCheckBox(boolean isSelected) {
        invoiceGeneratedCheckBox.setSelected(isSelected);
    }
}