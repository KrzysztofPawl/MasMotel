package com.motel.controllers;

import com.motel.interfaces.service.GuestService;
import com.motel.model.Guest;
import com.motel.utils.AlertPopper;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GuestDetailsController {

    private final GuestService guestService;

    @FXML
    private TextArea guestDetailsTextArea;

    public void loadGuestDetails(String pesel) {
        try {
            Guest guest = guestService.getGuestByPesel(pesel);
            String details = formatGuestDetails(guest);
            guestDetailsTextArea.setText(details);
        } catch (Exception e) {
            log.error("Error while loading guest details", e);
            AlertPopper.showErrorAlert("Error while loading guest details: " + e.getMessage());
        }
    }

    private String formatGuestDetails(Guest guest) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(guest.getPerson().getName()).append("\n");
        sb.append("Surname: ").append(guest.getPerson().getSurname()).append("\n");
        sb.append("Phone Number: ").append(guest.getPerson().getPhoneNumber()).append("\n");
        sb.append("Email: ").append(guest.getPerson().getEmail()).append("\n");
        sb.append("PESEL: ").append(guest.getPerson().getPesel()).append("\n");
        sb.append("Registration Plate Number: ").append(guest.getRegistrationPlateNumber()).append("\n");
        sb.append("Guest ID: ").append(guest.getId()).append("\n");
        return sb.toString();
    }
}