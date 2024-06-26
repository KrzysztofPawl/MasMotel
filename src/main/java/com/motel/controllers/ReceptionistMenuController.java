package com.motel.controllers;

import com.motel.DTO.ReservationView;
import com.motel.MotelSystemApplication;
import com.motel.interfaces.service.GuestService;
import com.motel.interfaces.service.ReservationService;
import com.motel.model.Guest;
import com.motel.model.enums.AdditionalServiceStatus;
import com.motel.utils.AlertPopper;
import com.motel.utils.InfoPopper;
import javafx.application.Platform;
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
    private final GuestService guestService;

    private static final String HELP_MESSAGE = """
            Welcome to the Receptionist Menu!
            Here you can manage reservations, guests, and additional services.
                        
            Manage Reservations:
            - To create a new reservation, enter the guest's PESEL in the field and click the button.
            - To view reservation details, enter the reservation ID in the field and click the button.
            - To modify a reservation, enter the reservation ID in the field and click the button.
            - To delete a reservation, enter the reservation ID in the field and click the button.
            
            Manage Guests:
            - To create a new guest, click the button.
            - To search for a guest, enter the guest's PESEL in the field and click the button.
            - To edit a guest, enter the guest's PESEL in the field and click the button.
            - To remove a guest, enter the guest's PESEL in the field and click the button.
            
            Manage Additional Services:
            - Not implemented yet.
            """;

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
            clearFieldsAfter1Second();
        });

        createReservationButton.setOnAction(actionEvent -> {
            createReservation();
            clearFieldsAfter1Second();
        });

        viewReservationDetailsSearchButton.setOnAction(actionEvent -> {
            viewReservationDetails();
            clearFieldsAfter1Second();
        });

        modifyReservationSearchButton.setOnAction(actionEvent -> {
            openModifyReservationView();
            clearFieldsAfter1Second();
        });

        deleteReservationDeleteButton.setOnAction(actionEvent -> {
            deleteReservation();
            clearFieldsAfter1Second();
        });

        createNewGuestButton.setOnAction(actionEvent -> {
            createNewGuest();
            clearFieldsAfter1Second();
        });

        editGuestButton.setOnAction(actionEvent -> {
            editGuest();
            clearFieldsAfter1Second();
        });

        removeGuestButton.setOnAction(actionEvent -> {
            removeGuest();
            clearFieldsAfter1Second();
        });

        searchGuestButton.setOnAction(actionEvent -> {
            searchForGuest();
            clearFieldsAfter1Second();
        });


        //Manage Additional Services - Not implemented yet
        editAdditionalServiceButton.setOnAction(actionEvent -> notImplemented());
        addAdditionalServiceButton.setOnAction(actionEvent -> notImplemented());
        changeAdditionalServiceStatusButton.setOnAction(actionEvent -> notImplemented());

        menuHelpItem.setOnAction(event -> InfoPopper.showInfo("Help", HELP_MESSAGE));
        menuClearItem.setOnAction(event -> clearFields());
    }

    @FXML
    private void searchForGuest() {
        try {
            String pesel = peselFieldSearchGuest.getText();
            if (validatePesel(pesel)) {
                return;
            }
            if (guestService.getGuestByPesel(pesel).isDeleted()) {
                log.error("Guest with PESEL " + pesel + " is deleted");
                AlertPopper.showErrorAlert("Guest with PESEL " + pesel + " is deleted");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GuestDetailsView.fxml"));
            loader.setControllerFactory(MotelSystemApplication.getContext()::getBean);
            Parent root = loader.load();

            GuestDetailsController controller = loader.getController();
            controller.loadGuestDetails(pesel);

            Stage stage = new Stage();
            stage.setTitle("Guest Details");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log.error("Error while opening Guest Details View" + e.getMessage());
            AlertPopper.showErrorAlert("Error while opening Guest Details View: " + e.getMessage());
        }
    }

    @FXML
    private void removeGuest() {
        try {
            String pesel = peselFieldRemoveGuest.getText();
            if (validatePesel(pesel)) {
                return;
            }

            Guest guest = guestService.getGuestByPesel(pesel);
            guestService.deleteGuest(guest.getId());

            InfoPopper.showInfo("Guest removed!", "Guest with PESEL " + pesel + " has been removed.");
        } catch (Exception e) {
            log.error("Error while removing guest" + e.getMessage());
            AlertPopper.showErrorAlert("Error while removing guest: " + e.getMessage());
        }
    }

    @FXML
    private void editGuest() {
        try {
            String pesel = peselFieldEditGuest.getText();
            if (validatePesel(pesel)) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditGuestView.fxml"));
            loader.setControllerFactory(MotelSystemApplication.getContext()::getBean);
            Parent root = loader.load();

            EditGuestController controller = loader.getController();
            controller.loadGuestData(pesel);

            Stage stage = new Stage();
            stage.setTitle("Edit Guest");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log.error("Error while opening Edit Guest View" + e.getMessage());
            AlertPopper.showErrorAlert("Error while opening Edit Guest View: " + e.getMessage());
        }
    }

    @FXML
    private void createNewGuest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateGuestView.fxml"));
            loader.setControllerFactory(MotelSystemApplication.getContext()::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create New Guest");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log.error("Error while opening Create Guest View" + e.getMessage());
            AlertPopper.showErrorAlert("Error while opening Create Guest View: " + e.getMessage());
        }
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
            log.error("Error while opening Create Reservation View" + e.getMessage());
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
            log.error("Error while opening Guest Reservations View" + e.getMessage());
            AlertPopper.showErrorAlert("Error while opening Guest Reservations View: " + e.getMessage());
        }
    }

    @FXML
    private void viewReservationDetails() {
        try {
            String reservationId = viewReservationDetailsIdField.getText();

            int id = parseId(reservationId);
            if (id == -1) {
                return;
            }
            if (validateReservationId(reservationId)) {
                return;
            }
            if (reservationService.getReservationById(id).isDeleted()) {
                log.error("Reservation with ID " + id + " is deleted");
                AlertPopper.showErrorAlert("Reservation with ID " + id + " is deleted");
                return;
            }

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
            log.error("Error while opening Reservation Details View" + e.getMessage());
            AlertPopper.showErrorAlert("Error while opening Reservation Details View: " + e.getMessage());
        }
    }

    @FXML
    private void openModifyReservationView() {
        try {
            String reservationId = modifyReservationIdField.getText();
            int id = parseId(reservationId);
            if (id == -1) {
                return;
            }
            if (validateReservationId(reservationId)) {
                return;
            }
            if (reservationService.getReservationById(id).isDeleted()) {
                log.error("Reservation with ID " + id + " is deleted");
                AlertPopper.showErrorAlert("Reservation with ID " + id + " is deleted");
                return;
            }
            if (reservationService.getReservationById(id) == null) {
                log.error("Reservation with ID " + reservationId + " does not exist");
                AlertPopper.showErrorAlert("Reservation with ID " + reservationId + " does not exist");
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
            log.error("Error while opening Modify Reservation view" + e.getMessage());
            AlertPopper.showErrorAlert("Error while opening Modify Reservation view: " + e.getMessage());
        }
    }

    private void setAdditionalServiceStatusChoiceBox(ActionEvent actionEvent) {
        AdditionalServiceStatus additionalServiceStatus = additionalServiceStatusChoiceBox.getValue();
    }

    private void deleteReservation() {
        try {
            String reservationId = deleteReservationIdField.getText();
            int id = parseId(reservationId);
            if (id == -1) {
                return;
            }
            if (validateReservationId(reservationId)) {
                return;
            }

            reservationService.markReservationAsDeletedAndDeleteInvoices(id);
            reservationService.clearRoomAssignmentForReservation(id);
            InfoPopper.showInfo("Reservation deleted!","Reservation with ID " + id +
                    " has been deleted, and associated invoices have been removed.");
        } catch (Exception e) {
            log.error("Error while deleting reservation" + e.getMessage());
            AlertPopper.showErrorAlert("Error while deleting reservation: " + e.getMessage());
        }
    }

    private boolean validateReservationId(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.error("Reservation ID is empty");
            AlertPopper.showErrorAlert("Please enter reservation ID");
            return true;
        }
        if (!reservationService.existsById(parseId(id))) {
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

    private void notImplemented() {
        AlertPopper.showErrorAlert("This feature is not implemented yet :(");
    }

    private void clearFieldsAfter1Second() {
        log.info("Clearing fields after 2 seconds");
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(this::clearFields);
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