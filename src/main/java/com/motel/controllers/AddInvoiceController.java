package com.motel.controllers;

import com.motel.exception.DataNotFoundException;
import com.motel.interfaces.service.InvoiceService;
import com.motel.model.Invoice;
import com.motel.model.Reservation;
import com.motel.model.enums.InvoiceStatus;
import com.motel.utils.AlertPopper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

@Component
@RequiredArgsConstructor
@Slf4j
public class AddInvoiceController {

    private final InvoiceService invoiceService;
    private Reservation currentReservation;
    private ModifyReservationController modifyReservationController;

    @FXML
    private TextField amountField;
    @FXML
    private Button createButton;

    @FXML
    private void initialize() {
        createButton.setOnAction(event -> createInvoice());
    }

    public void setReservation(Reservation reservation, ModifyReservationController controller) {
        this.currentReservation = reservation;
        this.modifyReservationController = controller;
    }

    private void createInvoice() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                AlertPopper.showErrorAlert("Amount must be greater than zero!");
                return;
            }

            Invoice primaryInvoice = currentReservation.getInvoices().stream()
                    .filter(invoice -> invoice.getStatus() == InvoiceStatus.OPEN)
                    .min(Comparator.comparing(Invoice::getId))
                    .orElseThrow(() -> new DataNotFoundException("Primary invoice not found!"));

            if (primaryInvoice.getSum().compareTo(amount) <= 0) {
                AlertPopper.showErrorAlert("Amount exceeds or equals primary invoice sum!");
                return;
            }

            primaryInvoice.setSum(primaryInvoice.getSum().subtract(amount));
            invoiceService.saveInvoice(primaryInvoice);

            Invoice newInvoice = new Invoice();
            newInvoice.setReservation(currentReservation);
            newInvoice.setDateOfIssue(LocalDate.now());
            newInvoice.setSum(amount);
            newInvoice.setStatus(InvoiceStatus.OPEN);
            newInvoice.setVat(23);

            invoiceService.saveInvoice(newInvoice);

            showConfirmationDialog();
            closeWindow();
        } catch (Exception e) {
            log.error("Error while creating invoice", e);
            AlertPopper.showErrorAlert("Error while creating invoice: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    private void showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice Created!");
        alert.setHeaderText(null);
        alert.setContentText("Invoice has been created successfully.");
        alert.showAndWait();

        modifyReservationController.refreshReservationDetails();
        modifyReservationController.setInvoiceGeneratedCheckBox(true);
        closeWindow();
    }
}