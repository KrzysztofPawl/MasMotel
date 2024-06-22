package com.motel.controllers;

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
public class CreateGuestController {

    private final PersonService personService;
    private final GuestService guestService;

    private static final String HELP_MESSAGE = """
            To create a guest, you need to create a person first.
            Fill in the form with the person's data and click the "Create Person" button.
            After the person is created, the fields will be locked and the ID of the person will be displayed.
            If needed fill in the registration plate number and click the "Create Guest" button.
            After the guest is created, the ID of the guest will be displayed.
            
            If you want to clear the form, click the "Clear" button.
            If you want to close the application, click the "Close" button.
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
    private Button createPersonButton;
    @FXML
    private Button createGuestButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private CheckBox personSavedCheckBox;
    @FXML
    private CheckBox guestCreatedCheckBox;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem helpMenuItem;
    @FXML
    private MenuItem clearMenuItem;

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

        createPersonButton.disableProperty().bind(
                Bindings.createBooleanBinding(this::isPersonFormIncomplete, nameProperty, surnameProperty, phoneNumberProperty, emailProperty, peselProperty)
        );

        createGuestButton.disableProperty().bind(
                personIdField.textProperty().isEmpty()
        );

        progressBar.progressProperty().bind(
                Bindings.createDoubleBinding(this::calculateProgress, nameProperty, surnameProperty, phoneNumberProperty, emailProperty, peselProperty, personIdField.textProperty(), guestIdField.textProperty())
        );

        createPersonButton.setOnAction(event -> createPerson());
        createGuestButton.setOnAction(event -> createGuest());
        helpMenuItem.setOnAction(event -> InfoPopper.showInfo("Help", HELP_MESSAGE));
        clearMenuItem.setOnAction(event -> validateIfPersonNotCreatedBeforeClearing());
        closeMenuItem.setOnAction(event -> closeWindow());

        addFieldListeners();
    }

    private void bindProperties() {
        nameField.textProperty().bindBidirectional(nameProperty);
        surnameField.textProperty().bindBidirectional(surnameProperty);
        phoneNumberField.textProperty().bindBidirectional(phoneNumberProperty);
        emailField.textProperty().bindBidirectional(emailProperty);
        peselField.textProperty().bindBidirectional(peselProperty);
    }

    private boolean isPersonFormIncomplete() {
        return nameProperty.get().isEmpty() || surnameProperty.get().isEmpty() || phoneNumberProperty.get().isEmpty() || emailProperty.get().isEmpty() || peselProperty.get().isEmpty();
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

    private void createPerson() {
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
        if (validateIfPersonExists()) {
            return;
        }


        Person person = new Person();
        person.setName(nameProperty.get());
        person.setSurname(surnameProperty.get());
        person.setPhoneNumber(phoneNumberProperty.get());
        person.setEmail(emailProperty.get());
        person.setPesel(peselProperty.get());

        Task<Person> createPersonTask = new Task<>() {
            @Override
            protected Person call() {
                return personService.savePerson(person);
            }
        };

        createPersonTask.setOnSucceeded(event -> {
            Person savedPerson = createPersonTask.getValue();
            personIdField.setText(savedPerson.getId().toString());
            personSavedCheckBox.setSelected(true);
            setPersonFieldsEditable(false);
            log.info("Person created: {}", savedPerson.getId());
            InfoPopper.showInfo("Person created", "Person created successfully with ID: " + savedPerson.getId());
        });

        createPersonTask.setOnFailed(event -> {
            Throwable e = createPersonTask.getException();
            log.error("Error while creating person", e);
            AlertPopper.showErrorAlert("Error while creating person: " + e.getMessage());
        });

        new Thread(createPersonTask).start();
    }

    private void createGuest() {
        if (personIdField.getText().isEmpty()) {
            AlertPopper.showErrorAlert("Create person first!");
            return;
        }

        Guest guest = new Guest();
        guest.setPerson(personService.getPersonById(Integer.parseInt(personIdField.getText())));
        if (!plateNumberField.getText().isEmpty()) {
            guest.setRegistrationPlateNumber(plateNumberField.getText());
        }

        Task<Guest> createGuestTask = new Task<>() {
            @Override
            protected Guest call() {
                return guestService.saveGuest(guest);
            }
        };

        createGuestTask.setOnSucceeded(event -> {
            Guest savedGuest = createGuestTask.getValue();
            guestIdField.setText(savedGuest.getId().toString());
            guestCreatedCheckBox.setSelected(true);
            log.info("Guest created: {}", savedGuest.getId());
            InfoPopper.showInfo("Guest created", "Guest created successfully with ID: " + savedGuest.getId());
        });

        createGuestTask.setOnFailed(event -> {
            Throwable e = createGuestTask.getException();
            log.error("Error while creating guest", e);
            AlertPopper.showErrorAlert("Error while creating guest: " + e.getMessage());
        });

        new Thread(createGuestTask).start();
    }

    private void addFieldListeners() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
        surnameField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
        phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
        emailField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
        peselField.textProperty().addListener((observable, oldValue, newValue) -> calculateProgress());
    }

    private boolean isPhoneNumberValid() {
        return phoneNumberProperty.get().matches("\\d*");
    }

    private boolean isPeselValid() {
        return peselProperty.get().matches("\\d*");
    }

    private void setPersonFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        surnameField.setEditable(editable);
        phoneNumberField.setEditable(editable);
        emailField.setEditable(editable);
        peselField.setEditable(editable);
    }

    private void validateIfPersonNotCreatedBeforeClearing() {
        if (personSavedCheckBox.isSelected() && guestCreatedCheckBox.isSelected()) {
            clearFields();
            InfoPopper.showInfo("Fields cleared", "Fields cleared successfully!");
        }

        if (personSavedCheckBox.isSelected()) {
            AlertPopper.showErrorAlert("Person already created! Clearing the is not allowed!");
        } else {
            clearFields();
        }
    }

    private boolean validateIfPersonExists() {
        if (personService.existsByPesel(peselProperty.get())) {
            AlertPopper.showErrorAlert("Person with PESEL " + peselProperty.get() + " already exists!");
            clearFields();
            return true;
        }
        return false;
    }
    private void clearFields() {
        nameField.clear();
        surnameField.clear();
        phoneNumberField.clear();
        emailField.clear();
        peselField.clear();
        plateNumberField.clear();
        personIdField.clear();
        guestIdField.clear();
        personSavedCheckBox.setSelected(false);
        guestCreatedCheckBox.setSelected(false);
        setPersonFieldsEditable(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}