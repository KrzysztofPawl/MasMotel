package com.motel.interfaces.service;

import com.motel.model.Reservation;
import com.motel.model.enums.ReservationStatus;

import java.util.List;

public interface ReservationService {
    Reservation saveReservation(Reservation reservation);
    Reservation getReservationById(int id);
    List<Reservation> getGuestReservations(String pesel);
    Reservation changeReservationStatus(int id, ReservationStatus status);
    void deleteReservation(int id);
    boolean existsById(int id);
}
