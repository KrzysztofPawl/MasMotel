package com.motel.controllers;

import com.motel.exception.ControllerException;
import com.motel.interfaces.service.GuestService;
import com.motel.interfaces.service.PersonService;
import com.motel.model.Person;
import com.motel.model.Guest;
import com.motel.utils.AlertPopper;
import com.motel.utils.InfoPopper;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EditGuestController {

    private final PersonService personService;
    private final GuestService guestService;
    private static final String HELP_MESSAGE = """
            To edit guest data, fill in the fields with the new data and click the "Edit" button.
            The fields are validated, so make sure to provide correct data.
            
            The progress bar shows how many fields are filled.
            """;

    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField peselField;
    @FXML
    private TextField plateNumberField;
    @FXML
    private TextField personIdField;
    @FXML
    private TextField guestIdField;
    @FXML
    private Button editPersonButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private CheckBox changesSavedCheckBox;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem helpMenuItem;

    private final StringProperty nameProperty = new SimpleStringProperty("");
    private final StringProperty surnameProperty = new SimpleStringProperty("");
    private final StringProperty phoneNumberProperty = new SimpleStringProperty("");
    private final StringProperty emailProperty = new SimpleStringProperty("");
    private final StringProperty peselProperty = new SimpleStringProperty("");

    @FXML
    private void initialize() {
        bindProperties();
        personIdField.setEditable(false);
        guestIdField.setEditable(false);

        editPersonButton.setOnAction(event -> editPerson());
        closeMenuItem.setOnAction(event -> closeWindow());
        helpMenuItem.setOnAction(event -> InfoPopper.showInfo("Help", HELP_MESSAGE));

        progressBar.progressProperty().bind(
                Bindings.createDoubleBinding(this::calculateProgress, nameProperty, surnameProperty, phoneNumberProperty, emailProperty, peselProperty, personIdField.textProperty(), guestIdField.textProperty())
        );

        addFieldListeners();
    }

    private void bindProperties() {
        nameField.textProperty().bindBidirectional(nameProperty);
        surnameField.textProperty().bindBidirectional(surnameProperty);
        phoneNumberField.textProperty().bindBidirectional(phoneNumberProperty);
        emailField.textProperty().bindBidirectional(emailProperty);
        peselField.textProperty().bindBidirectional(peselProperty);
    }

    public void loadGuestData(String pesel){
        if (!guestService.existsByPesel(pesel)) {
            AlertPopper.showErrorAlert("Guest with PESEL " + pesel + " does not exist");
            return;
        }
        if (guestService.isGuestDeleted(guestService.getGuestByPesel(pesel).getId())) {
            AlertPopper.showErrorAlert("Guest with PESEL " + pesel + " has been deleted");
            return;
        }

        Guest guest = guestService.getGuestByPesel(pesel);
        Person person = guest.getPerson();

        nameProperty.set(person.getName());
        surnameProperty.set(person.getSurname());
        phoneNumberProperty.set(person.getPhoneNumber());
        emailProperty.set(person.getEmail());
        peselProperty.set(person.getPesel());
        personIdField.setText(person.getId().toString());
        guestIdField.setText(guest.getId().toString());
        if (guest.getRegistrationPlateNumber() != null) {
            plateNumberField.setText(guest.getRegistrationPlateNumber());
        }
    }

    private void editPerson() {
        if (!isPhoneNumberValid()) {
            AlertPopper.showErrorAlert("Phone number must contain only digits!");
            phoneNumberField.clear();
            return;
        }
        if (!isPeselValid()) {
            AlertPopper.showErrorAlert("PESEL must contain only digits!");
            peselField.clear();
            return;
        }
        if (!isPeselUnique()) {
            AlertPopper.showErrorAlert("PESEL already exists in the database!");
            peselField.clear();
            return;
        }

        Person person = new Person();
        person.setId(Integer.parseInt(personIdField.getText()));
        person.setName(nameProperty.get());
        person.setSurname(surnameProperty.get());
        person.setPhoneNumber(phoneNumberProperty.get());
        person.setEmail(emailProperty.get());
        person.setPesel(peselProperty.get());

        Task<Void> editPersonTask = new Task<>() {
            @Override
            protected Void call() {
                personService.savePerson(person);
                return null;
            }
        };

        editPersonTask.setOnSucceeded(event -> {
            changesSavedCheckBox.setSelected(true);
            validateAndSetRegistrationPlateNumber(peselProperty.get());
            var guest = guestService.getGuestByPesel(peselProperty.get());
            InfoPopper.showInfo("Guest updated", "Guest data has been updated with ID: " + guest.getId());
            log.info("Guest updated: {}", guest);
        });

        editPersonTask.setOnFailed(event -> {
            Throwable e = editPersonTask.getException();
            log.error("Error while updating guest", e);
            AlertPopper.showErrorAlert("Error while updating guest: " + e.getMessage());
        });

        new Thread(editPersonTask).start();
    }

    private boolean isPhoneNumberValid() {
        return phoneNumberProperty.get().matches("\\d*");
    }

    private boolean isPeselValid() {
        return peselProperty.get().matches("\\d*");
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private double calculateProgress() {
        int filledFields = 0;
        if (!nameProperty.get().isEmpty()) filledFields++;
        if (!surnameProperty.get().isEmpty()) filledFields++;
        if (!phoneNumberProperty.get().isEmpty()) filledFields++;
        if (!emailProperty.get().isEmpty()) filledFields++;
        if (!peselProperty.get().isEmpty()) filledFields++;
        if (!personIdField.getText().isEmpty()) filledFields++;
        if (!guestIdField.getText().isEmpty()) filledFields++;
        return filledFields / 7.0;
    }

    private void addFieldListeners() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
        surnameField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
        phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
        emailField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
        peselField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
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

    private boolean isPeselUnique() {
        String newPesel = peselField.getText();
        int currentPersonId = Integer.parseInt(personIdField.getText());
        return !personService.existsByPesel(newPesel) || personService.getPersonByPeselNoException(newPesel).map(Person::getId).orElse(-1) == currentPersonId;
    }
}