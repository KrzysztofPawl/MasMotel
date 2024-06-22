package com.motel.controllers;

import com.motel.interfaces.service.RoomService;
import com.motel.model.Room;
import com.motel.model.RoomTraits;
import com.motel.model.enums.RoomStatus;
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
public class RoomInfoController {

    private final RoomService roomService;

    @FXML
    private TextArea roomInfoTextArea;

    @FXML
    private void initialize() {
        loadRoomInfo();
    }

    private void loadRoomInfo() {
        try {
            List<Room> availableRooms = roomService.findRoomByStatus(RoomStatus.AVAILABLE);
            String roomInfo = availableRooms.stream()
                    .map(this::formatRoomInfo)
                    .collect(Collectors.joining("\n\n"));
            roomInfoTextArea.setText(roomInfo);
        } catch (Exception e) {
            log.error("Error while loading room info", e);
            AlertPopper.showErrorAlert("Error while loading room info: " + e.getMessage());
        }
    }

    private String formatRoomInfo(Room room) {
        RoomTraits traits = room.getRoomTraits();
        StringBuilder sb = new StringBuilder();
        sb.append("Room Number: ").append(room.getNumber()).append("\n");
        sb.append("Floor: ").append(room.getFloor()).append("\n");
        sb.append("Price per Night: ").append(room.getPriceForNight()).append("\n");
        sb.append("Traits: ").append(resolveRoomTraits(traits)).append("\n");
        sb.append("Beds: ").append(resolveBedCount(traits));
        return sb.toString();
    }

    private String resolveRoomTraits(RoomTraits traits) {
        StringBuilder sb = new StringBuilder();
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

    private int resolveBedCount(RoomTraits traits) {
        if (traits.getSingleRoom() != null) {
            return 1;
        }
        if (traits.getMultiPersonRoom() != null) {
            return traits.getMultiPersonRoom().getNumberOfBeds();
        }
        return 0;
    }
}