package com.motel.service;

import com.motel.exception.DataNotFoundException;
import com.motel.repositories.ReservationRepository;
import com.motel.interfaces.service.ReservationService;
import com.motel.model.Reservation;
import com.motel.model.enums.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public Reservation saveReservation(Reservation reservation) {
        var result = reservationRepository.save(reservation);
        log.info("Reservation saved: {}", result);
        return result;
    }

    @Override
    public Reservation getReservationById(int id) {
        var result = reservationRepository.findById(id).orElse(null);
        if (result == null) {
            log.warn("Reservation not found: {}", id);
            throw new DataNotFoundException("Reservation not found: " + id);
        }
        log.info("Reservation found: {}", result);
        return result;
    }

    @Override
    public List<Reservation> getGuestReservations(String pesel) {
        var result = reservationRepository.findReservationsByPesel(pesel);
        if (result.isEmpty()) {
            log.warn("Reservations not found for guest: {}", pesel);
            throw new DataNotFoundException("Reservations not found for guest: " + pesel);
        }

        log.info("Reservations found: {}", result);
        return result;
    }

    @Override
    @Transactional
    public Reservation changeReservationStatus(int id, ReservationStatus status) {
        var reservation = getReservationById(id);
        reservation.setStatus(status);
        var result = reservationRepository.save(reservation);
        log.info("Reservation status changed: {}", result);
        return result;
    }

    @Override
    public void deleteReservation(int id) {
        var reservation = getReservationById(id);
        reservation.markAsDeleted();
        reservationRepository.save(reservation);
        log.info("Reservation deleted: {}", id);
    }

    @Override
    public boolean existsById(int id) {
        var result = reservationRepository.existsById(id);
        log.info("Reservation exists: {}", result);
        return result;
    }
}
