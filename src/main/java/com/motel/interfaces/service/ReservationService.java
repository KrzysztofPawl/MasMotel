package com.motel.interfaces.service;

import com.motel.model.Reservation;
import com.motel.model.enums.ReservationStatus;

import java.util.List;

public interface ReservationService {
    Reservation saveReservation(Reservation reservation);
    Reservation getReservationById(int id);
    Reservation getReservationByIdNotDeleted(int id);
    List<Reservation> getGuestReservationsNotDeleted(String pesel);
    Reservation changeReservationStatus(int id, ReservationStatus status);
    boolean existsById(int id);
    void markReservationAsDeletedAndDeleteInvoices(int id);
    void clearRoomAssignmentForReservation(int id);
}
