package com.motel.controllers;

import com.motel.MotelSystemApplication;
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
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModifyReservationController {

    private final ReservationService reservationService;
    private final InvoiceService invoiceService;
    private final GuestService guestService;

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

    private Reservation currentReservation;
    private Invoice primaryInvoice;
    private BigDecimal invoiceSum;

    @FXML
    private void initialize() {
        modifyReservationButton.setOnAction(event -> modifyReservation());
        payInvoiceButton.setOnAction(event -> payInvoice());
        addInvoiceButton.setOnAction(event -> openAddInvoiceDialog());

        reservationStatusChoiceBox.getItems().setAll(ReservationStatus.values());
    }

    public void loadReservationDetails(int reservationId) {
        try {
            currentReservation = reservationService.getReservationById(reservationId);
            Set<Invoice> invoices = currentReservation.getInvoices();
            if (!invoices.isEmpty()) {
                invoices.forEach(invoice -> invoiceSum = invoice.getSum());
            } else {
                invoiceSum = BigDecimal.ZERO;
                log.warn("No invoices found for reservation with id: {}", reservationId);
                AlertPopper.showErrorAlert("No invoices found for reservation with id: " + reservationId);
            }
            sumField.setText(invoiceSum.toString());

            reservationDatePicker.setValue(currentReservation.getReservationDate());
            reservationDatePicker.setEditable(false);
            arrivalDatePicker.setValue(currentReservation.getArrivalDate());
            leaveDatePicker.setValue(currentReservation.getLeaveDate());
            reservationStatusChoiceBox.setValue(currentReservation.getStatus());
            numberOfNightsField.setText(currentReservation.getNights().toString());

            Optional<String> registrationPlateNumber = guestService.findRegistrationPlateNumberByPesel(currentReservation.getGuest().getPerson().getPesel());
            plateNumberField.setText(registrationPlateNumber.orElse(""));

            String details = formatReservationDetails(currentReservation);
            reservationDetailsTextArea.setText(details);
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

            String details = formatReservationDetails(currentReservation);
            reservationDetailsTextArea.setText(details);

            savedInSystemCheckBox.setSelected(true);

        } catch (Exception e) {
            log.error("Error while modifying reservation", e);
            AlertPopper.showErrorAlert("Error while modifying reservation: " + e.getMessage());
        }
    }

    private void payInvoice() {
        try {
            int invoiceId = parseId(invoiceIdField.getText());
            BigDecimal amount = new BigDecimal(amountField.getText());
            String paymentMethod = paymentMethodField.getText();

            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            if (invoice.getSum().compareTo(amount) == 0) {
                invoice.markAsPaid(paymentMethod);
                invoiceService.saveInvoice(invoice);

                if (currentReservation.getInvoices().stream().allMatch(inv -> inv.getStatus() == InvoiceStatus.PAID)) {
                    paidCheckBox.setSelected(true);
                    currentReservation.setStatus(ReservationStatus.FINISHED);
                    reservationService.saveReservation(currentReservation);
                }

                refreshReservationDetails();
            } else {
                AlertPopper.showErrorAlert("Amount does not match invoice sum!");
            }
        } catch (Exception e) {
            log.error("Error while paying invoice", e);
            AlertPopper.showErrorAlert("Error while paying invoice: " + e.getMessage());
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
        sb.append("Arrival Date: ").append(reservation.getArrivalDate()).append("\n");
        sb.append("Leave Date: ").append(reservation.getLeaveDate()).append("\n");
        sb.append("Nights: ").append(reservation.getNights()).append("\n");
        sb.append("Room Numbers: ").append(reservation.getRooms().stream().map(room -> room.getNumber().toString()).collect(Collectors.joining(", "))).append("\n");
        sb.append("Room Details: ").append(reservation.getRooms().stream().map(this::formatRoomDetails).collect(Collectors.joining(", "))).append("\n");

        reservation.getInvoices().forEach(invoice -> {
            sb.append("Invoice Id: ").append(invoice.getId()).append("\n");
            sb.append("Invoice Status: ").append(invoice.getStatus()).append("\n");
            sb.append("Invoice Sum: ").append(invoice.getSum()).append("\n");
            sb.append("Invoice Date of Issue: ").append(invoice.getDateOfIssue()).append("\n");
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

    private int parseId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            AlertPopper.showErrorAlert("Please enter valid ID");
            return -1;
        }
    }

    private void refreshReservationDetails() {
        refreshReservationDetails(false);
    }

    public void refreshReservationDetails(boolean invoiceGenerated) {
        String details = formatReservationDetails(currentReservation);
        reservationDetailsTextArea.setText(details);

        invoiceGeneratedCheckBox.setSelected(invoiceGenerated || !currentReservation.getInvoices().isEmpty());
    }

}


