package com.motel.controllers;

import com.motel.DTO.ReservationView;
import com.motel.interfaces.service.ReservationService;
import com.motel.model.Reservation;
import com.motel.model.Room;
import com.motel.model.enums.ReservationStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationsViewController {

    private final ReservationService reservationService;

    @FXML
    private TableView<ReservationView> reservationsTableView;
    @FXML
    private TableColumn<ReservationView, Integer> idColumn;
    @FXML
    private TableColumn<ReservationView, String> roomNumberColumn;
    @FXML
    private TableColumn<ReservationView, LocalDate> reservationDateColumn;
    @FXML
    private TableColumn<ReservationView, LocalDate> arrivalDateColumn;
    @FXML
    private TableColumn<ReservationView, LocalDate> leaveDateColumn;
    @FXML
    private TableColumn<ReservationView, Integer> nightsColumn;
    @FXML
    private TableColumn<ReservationView, ReservationStatus> statusColumn;
    @FXML
    private MenuItem menuBackItem;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("Reservation_id"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("Room_Numbers"));
        reservationDateColumn.setCellValueFactory(new PropertyValueFactory<>("Reservation_Date"));
        arrivalDateColumn.setCellValueFactory(new PropertyValueFactory<>("Arrival_Date"));
        leaveDateColumn.setCellValueFactory(new PropertyValueFactory<>("Leave_Date"));
        nightsColumn.setCellValueFactory(new PropertyValueFactory<>("Nights"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("Status"));

        menuBackItem.setOnAction(event -> closeWindow());
    }

    public void setReservations(List<ReservationView> reservations) {
        ObservableList<ReservationView> data = FXCollections.observableArrayList(reservations);
        reservationsTableView.setItems(data);
    }

    private void closeWindow() {
        Stage stage = (Stage) reservationsTableView.getScene().getWindow();
        stage.close();
    }

    public List<ReservationView> getReservationViews(String pesel) {
        List<Reservation> reservations = reservationService.getGuestReservationsNotDeleted(pesel);
        return reservations.stream()
                .map(reservation -> new ReservationView(
                        reservation.getId(),
                        transformRoomNumbers(reservation.getRooms()),
                        reservation.getReservationDate(),
                        reservation.getArrivalDate(),
                        reservation.getLeaveDate(),
                        reservation.getNights(),
                        reservation.getStatus())
                ).toList();
    }

    public String transformRoomNumbers(Set<Room> roomNumbers) {
        return roomNumbers.stream()
                .map(room -> Integer.toString(room.getNumber()))
                .collect(Collectors.joining(", "));
    }
}