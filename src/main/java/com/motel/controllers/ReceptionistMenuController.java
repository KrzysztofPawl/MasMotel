package com.motel.controllers;

import com.motel.DTO.ReservationView;
import com.motel.MotelSystemApplication;
import com.motel.interfaces.service.GuestService;
import com.motel.interfaces.service.ReservationService;
import com.motel.interfaces.service.RoomService;
import com.motel.model.enums.AdditionalServiceStatus;
import com.motel.utils.AlertPopper;
import com.motel.utils.InfoPopper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceptionistMenuController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final GuestService guestService;

    //Manage Reservations TextFields
    @FXML
    private TextField peselFieldSearchGuestReservation;
    @FXML
    private TextField peselFieldCreateReservation;
    @FXML
    private TextField modifyReservationIdField;
    @FXML
    private TextField viewReservationDetailsIdField;
    @FXML
    private TextField deleteReservationIdField;

    //Manage Reservations Buttons
    @FXML
    private Button searchGuestReservationButton;
    @FXML
    private Button createReservationButton;
    @FXML
    private Button modifyReservationSearchButton;
    @FXML
    private Button viewReservationDetailsSearchButton;
    @FXML
    private Button deleteReservationDeleteButton;

    //------------------------------------------------------------------

    //Manage Guests TextFields
    @FXML
    private TextField peselFieldSearchGuest;
    @FXML
    private TextField peselFieldEditGuest;
    @FXML
    private TextField peselFieldRemoveGuest;

    //Manage Guests Buttons
    @FXML
    private Button createNewGuestButton;
    @FXML
    private Button searchGuestButton;
    @FXML
    private Button editGuestButton;
    @FXML
    private Button removeGuestButton;

    //------------------------------------------------------------------

    //Manage Additional Services TextFields
    @FXML
    private TextField editAdditionalServiceIdField;
    @FXML
    private TextField addAdditionalServiceIdField;
    @FXML
    private TextField changeAdditionalServiceStatusIdField;

    //Manage Additional Services Buttons
    @FXML
    private Button editAdditionalServiceButton;
    @FXML
    private Button addAdditionalServiceButton;
    @FXML
    private Button changeAdditionalServiceStatusButton;

    //Manage Additional Services CheckBoxes
    @FXML
    private ChoiceBox<AdditionalServiceStatus> additionalServiceStatusChoiceBox;

    //------------------------------------------------------------------

    @FXML
    private MenuItem menuHelpItem;
    @FXML
    private MenuItem menuClearItem;


    @FXML
    private void initialize() {
        additionalServiceStatusChoiceBox.getItems().setAll(AdditionalServiceStatus.values());
        additionalServiceStatusChoiceBox.setOnAction(this::setAdditionalServiceStatusChoiceBox);

        searchGuestReservationButton.setOnAction(event -> {
            openGuestReservationsView();
            clearFieldsAfter2Seconds();
        });

        createReservationButton.setOnAction(actionEvent -> {
            createReservation();
            clearFieldsAfter2Seconds();
        });

        viewReservationDetailsSearchButton.setOnAction(actionEvent -> {
            viewReservationDetails();
            clearFieldsAfter2Seconds();
        });

        modifyReservationSearchButton.setOnAction(actionEvent -> {
            openModifyReservationView();
            clearFieldsAfter2Seconds();
        });

        deleteReservationDeleteButton.setOnAction(actionEvent -> {
            deleteReservation();
            clearFieldsAfter2Seconds();
        });

        menuHelpItem.setOnAction(event -> InfoPopper.showInfo("Help", "Help"));
        menuClearItem.setOnAction(event -> clearFields());
    }

    @FXML
    private void createReservation() {
        try {
            String pesel = peselFieldCreateReservation.getText();
            if (validatePesel(pesel)) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateReservationView.fxml"));
            loader.setControllerFactory(MotelSystemApplication.getContext()::getBean);
            Parent root = loader.load();

            CreateReservationController controller = loader.getController();
            if (!controller.checkAvailableRooms()) {
                return;
            }
            controller.setGuestPesel(pesel);

            Stage stage = new Stage();
            stage.setTitle("Create New Reservation");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log.error("Error while opening Create Reservation View");
            AlertPopper.showErrorAlert("Error while opening Create Reservation View: " + e.getMessage());
        }
    }

    private void openGuestReservationsView() {
        try {
            String pesel = peselFieldSearchGuestReservation.getText();
            if (validatePesel(pesel)) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReservationsView.fxml"));
            loader.setControllerFactory(MotelSystemApplication.getContext()::getBean);
            Parent root = loader.load();

            ReservationsViewController controller = loader.getController();
            List<ReservationView> reservationViews = controller.getReservationViews(pesel);
            controller.setReservations(reservationViews);

            Stage stage = new Stage();
            stage.setTitle("Guest Reservations");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log.error("Error while opening Guest Reservations View", e);
            AlertPopper.showErrorAlert("Error while opening Guest Reservations View: " + e.getMessage());
        }
    }

    @FXML
    private void viewReservationDetails() {
        try {
            String reservationId = viewReservationDetailsIdField.getText();
            if (validateReservationId(reservationId)) {
                return;
            }

            int id = parseId(reservationId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReservationDetailsView.fxml"));
            loader.setControllerFactory(MotelSystemApplication.getContext()::getBean);
            Parent root = loader.load();

            ReservationDetailsController controller = loader.getController();
            controller.loadReservationDetails(id);

            Stage stage = new Stage();
            stage.setTitle("Reservation Details");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log.error("Error while opening Reservation Details View", e);
            AlertPopper.showErrorAlert("Error while opening Reservation Details View: " + e.getMessage());
        }
    }

    @FXML
    private void openModifyReservationView() {
        try {
            String reservationId = modifyReservationIdField.getText();
            if (validateReservationId(reservationId)) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifyReservationView.fxml"));
            loader.setControllerFactory(MotelSystemApplication.getContext()::getBean);
            Parent root = loader.load();

            ModifyReservationController controller = loader.getController();
            controller.loadReservationDetails(parseId(reservationId));

            Stage stage = new Stage();
            stage.setTitle("Modify Reservation");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log.error("Error while opening Modify Reservation view", e);
            AlertPopper.showErrorAlert("Error while opening Modify Reservation view: " + e.getMessage());
        }
    }

    private void setAdditionalServiceStatusChoiceBox(ActionEvent actionEvent) {
        AdditionalServiceStatus additionalServiceStatus = additionalServiceStatusChoiceBox.getValue();
    }

    private void deleteReservation() {
        try {
            String reservationId = deleteReservationIdField.getText();
            if (validateReservationId(reservationId)) {
                return;
            }

            int id = parseId(reservationId);
            if (id == -1) {
                return;
            }

            reservationService.markReservationAsDeletedAndDeleteInvoices(id);
            InfoPopper.showInfo("Reservation deleted!","Reservation with ID " + id +
                    " has been deleted, and associated invoices have been removed.");
        } catch (Exception e) {
            log.error("Error while deleting reservation", e);
            AlertPopper.showErrorAlert("Error while deleting reservation: " + e.getMessage());
        }
    }

    private boolean validateReservationId(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.error("Reservation ID is empty");
            AlertPopper.showErrorAlert("Please enter reservation ID");
            return true;
        }
        if (!reservationService.existsById(Integer.parseInt(id))) {
            log.error("Reservation with ID " + id + " does not exist");
            AlertPopper.showErrorAlert("Reservation with ID " + id + " does not exist");
            return true;
        }
        return false;
    }

    private int parseId(String id) {
        try {
            log.info("Parsing ID: " + id);
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            AlertPopper.showErrorAlert("Please enter valid ID");
            return -1;
        }
    }

    private boolean validatePesel(String pesel) {
        if (pesel == null || pesel.trim().isEmpty()) {
            log.error("Pesel is empty");
            AlertPopper.showErrorAlert("Please enter guest PESEL");
            return true;
        }
        if (!guestService.existsByPesel(pesel)) {
            log.error("Guest with PESEL " + pesel + " does not exist");
            AlertPopper.showErrorAlert("Guest with PESEL " + pesel + " does not exist");
            return true;
        }
        return false;
    }

    private void clearFieldsAfter2Seconds() {
        log.info("Clearing fields after 2 seconds");
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                clearFields();
                log.info("Fields cleared");
            } catch (InterruptedException e) {
                log.error("Error while clearing fields", e);
            }
        }).start();
    }

    private void clearFields() {
        peselFieldSearchGuestReservation.clear();
        peselFieldCreateReservation.clear();
        modifyReservationIdField.clear();
        viewReservationDetailsIdField.clear();
        deleteReservationIdField.clear();
        peselFieldSearchGuest.clear();
        peselFieldEditGuest.clear();
        peselFieldRemoveGuest.clear();
        editAdditionalServiceIdField.clear();
        addAdditionalServiceIdField.clear();
        changeAdditionalServiceStatusIdField.clear();
    }
}