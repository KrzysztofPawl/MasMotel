package com.motel.controllers;

import com.motel.interfaces.service.ReservationService;
import com.motel.model.Reservation;
import com.motel.model.Room;
import com.motel.model.RoomTraits;
import com.motel.utils.AlertPopper;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationDetailsController {

    private final ReservationService reservationService;

    @FXML
    private TextArea reservationDetailsTextArea;

    public void loadReservationDetails(int reservationId) {
        try {
            Reservation reservation = reservationService.getReservationByIdNotDeleted(reservationId);
            String details = formatReservationDetails(reservation);
            reservationDetailsTextArea.setText(details);
        } catch (Exception e) {
            log.error("Error while loading reservation details", e);
            AlertPopper.showErrorAlert("Error while loading reservation details: " + e.getMessage());
        }
    }

    private String formatReservationDetails(Reservation reservation) {
        StringBuilder sb = new StringBuilder();
        sb.append("Reservation Date: ").append(reservation.getReservationDate()).append("\n");
        sb.append("Arrival Date: ").append(reservation.getArrivalDate()).append("\n");
        sb.append("Leave Date: ").append(reservation.getLeaveDate()).append("\n");
        sb.append("Nights: ").append(reservation.getNights()).append("\n");
        sb.append("Room Numbers: ").append(reservation.getRooms().stream().map(room -> room.getNumber().toString()).collect(Collectors.joining(", "))).append("\n");
        sb.append("Room Details: ").append(reservation.getRooms().stream().map(this::formatRoomDetails).collect(Collectors.joining(", "))).append("\n");
        sb.append("Status: ").append(reservation.getStatus()).append("\n");
        sb.append("------\n");
        sb.append("Guest: ").append(reservation.getGuest().getPerson().getName()).append(" ").append(reservation.getGuest().getPerson().getSurname()).append("\n");
        sb.append("------\n");
        sb.append("Invoices: \n");
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
}